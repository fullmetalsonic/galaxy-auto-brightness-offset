package com.fullmetalsonic.brightnessoffset.shizuku

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.fullmetalsonic.brightnessoffset.BuildConfig
import com.fullmetalsonic.brightnessoffset.domain.PrivilegeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

interface PrivilegedSettingsContract {
    val status: StateFlow<PrivilegeStatus>
    fun refreshStatus(): PrivilegeStatus
    fun requestPermission()
    fun readAdjustment(): Float
    fun writeAdjustment(value: Float): Boolean
    fun applyTemporaryAdjustment(value: Float): Boolean
    fun clearTemporaryAdjustment(): Boolean
    fun setTemporaryBrightness(displayId: Int, value: Float): Boolean
    fun clearTemporaryBrightness(displayId: Int): Boolean
    fun readAutomaticBrightnessTarget(ambientLux: Float): Float
    fun readAutomaticBrightnessState(ambientLux: Float): FloatArray
}

class ShizukuPrivilegeClient private constructor(private val context: Context) :
    PrivilegedSettingsContract {
    private val _status = MutableStateFlow(PrivilegeStatus.CONNECTING)
    override val status: StateFlow<PrivilegeStatus> = _status.asStateFlow()

    @Volatile
    private var service: IPrivilegedBrightnessService? = null
    @Volatile
    private var binding = false
    @Volatile
    private var connectionLatch = CountDownLatch(1)

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(context.packageName, PrivilegedBrightnessService::class.java.name),
        )
            .daemon(false)
            .processNameSuffix("brightness")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = IPrivilegedBrightnessService.Stub.asInterface(binder)
            binding = false
            _status.value = PrivilegeStatus.READY
            connectionLatch.countDown()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            binding = false
            _status.value = if (Shizuku.pingBinder()) {
                PrivilegeStatus.CONNECTING
            } else {
                PrivilegeStatus.NOT_RUNNING
            }
            connectionLatch.countDown()
        }
    }

    private val binderReceived = Shizuku.OnBinderReceivedListener { refreshStatus() }
    private val binderDead = Shizuku.OnBinderDeadListener {
        service = null
        binding = false
        _status.value = PrivilegeStatus.NOT_RUNNING
        connectionLatch.countDown()
    }
    private val permissionResult = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == PERMISSION_REQUEST_CODE) {
            _status.value = if (grantResult == PackageManager.PERMISSION_GRANTED) {
                bindIfNeeded()
                PrivilegeStatus.CONNECTING
            } else {
                PrivilegeStatus.PERMISSION_DENIED
            }
        }
    }

    init {
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        Shizuku.addRequestPermissionResultListener(permissionResult)
        refreshStatus()
    }

    override fun refreshStatus(): PrivilegeStatus {
        val next = when {
            !isManagerInstalled() -> PrivilegeStatus.NOT_INSTALLED
            !Shizuku.pingBinder() -> PrivilegeStatus.NOT_RUNNING
            Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED -> {
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    PrivilegeStatus.PERMISSION_DENIED
                } else {
                    PrivilegeStatus.PERMISSION_REQUIRED
                }
            }
            service?.asBinder()?.pingBinder() == true -> PrivilegeStatus.READY
            else -> {
                bindIfNeeded()
                PrivilegeStatus.CONNECTING
            }
        }
        _status.value = next
        return next
    }

    override fun requestPermission() {
        if (!Shizuku.pingBinder()) {
            _status.value = PrivilegeStatus.NOT_RUNNING
            return
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            bindIfNeeded()
        } else {
            Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
        }
    }

    override fun readAdjustment(): Float = requireService().readAdjustment()

    override fun writeAdjustment(value: Float): Boolean =
        requireService().writeAdjustment(value)

    override fun applyTemporaryAdjustment(value: Float): Boolean =
        requireService().applyTemporaryAdjustment(value)

    override fun clearTemporaryAdjustment(): Boolean =
        requireService().clearTemporaryAdjustment()

    override fun setTemporaryBrightness(displayId: Int, value: Float): Boolean =
        requireService().setTemporaryBrightness(displayId, value)

    override fun clearTemporaryBrightness(displayId: Int): Boolean =
        requireService().clearTemporaryBrightness(displayId)

    override fun readAutomaticBrightnessTarget(ambientLux: Float): Float =
        requireService().readAutomaticBrightnessTarget(ambientLux)

    override fun readAutomaticBrightnessState(ambientLux: Float): FloatArray =
        requireService().readAutomaticBrightnessState(ambientLux)

    @Synchronized
    private fun bindIfNeeded() {
        if (service?.asBinder()?.pingBinder() == true || binding) return
        binding = true
        connectionLatch = CountDownLatch(1)
        runCatching { Shizuku.bindUserService(userServiceArgs, connection) }
            .onFailure {
                binding = false
                _status.value = PrivilegeStatus.ERROR
                connectionLatch.countDown()
            }
    }

    private fun requireService(): IPrivilegedBrightnessService {
        if (refreshStatus() !in setOf(PrivilegeStatus.READY, PrivilegeStatus.CONNECTING)) {
            throw IllegalStateException("Shizuku is not ready: ${_status.value}")
        }
        service?.takeIf { it.asBinder().pingBinder() }?.let { return it }
        connectionLatch.await(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        return service?.takeIf { it.asBinder().pingBinder() }
            ?: throw IllegalStateException("Shizuku user service connection timed out.")
    }

    @Suppress("DEPRECATION")
    private fun isManagerInstalled(): Boolean = runCatching {
        context.packageManager.getApplicationInfo(SHIZUKU_PACKAGE, 0)
    }.isSuccess

    companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        private const val PERMISSION_REQUEST_CODE = 48731
        private const val CONNECTION_TIMEOUT_SECONDS = 5L

        // The singleton stores only context.applicationContext; no Activity can be retained.
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ShizukuPrivilegeClient? = null

        fun get(context: Context): ShizukuPrivilegeClient = instance ?: synchronized(this) {
            instance ?: ShizukuPrivilegeClient(context.applicationContext).also { instance = it }
        }
    }
}
