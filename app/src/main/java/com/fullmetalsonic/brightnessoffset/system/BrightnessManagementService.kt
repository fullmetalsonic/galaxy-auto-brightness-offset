package com.fullmetalsonic.brightnessoffset.system

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.fullmetalsonic.brightnessoffset.MainActivity
import com.fullmetalsonic.brightnessoffset.R
import com.fullmetalsonic.brightnessoffset.data.ManagementPreferences
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.BrightnessCompensation
import com.fullmetalsonic.brightnessoffset.domain.PrivilegeStatus
import com.fullmetalsonic.brightnessoffset.shizuku.PrivilegedSettingsContract
import com.fullmetalsonic.brightnessoffset.shizuku.ShizukuPrivilegeClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Keeps Samsung's ambient-light curve active while applying a temporary,
 * non-persistent brightness correction to the active logical display.
 */
class BrightnessManagementService : Service(), SensorEventListener {
    private val worker = Executors.newSingleThreadExecutor()
    private val updateInFlight = AtomicBoolean(false)
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var sensorRegistered = false
    private var receiverRegistered = false
    private var adjustment = AdjustmentScale.NEUTRAL
    private var curveState: CurveState? = null
    private var lastQueryAt = 0L

    private val privilegeClient: PrivilegedSettingsContract by lazy {
        ShizukuPrivilegeClient.get(applicationContext)
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    stopSensorTracking()
                    curveState = null
                    worker.execute(::clearTemporaryBrightness)
                }
                Intent.ACTION_SCREEN_ON -> startSensorTracking()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SensorManager::class.java)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(AdjustmentScale.NEUTRAL))
        ContextCompat.registerReceiver(
            this,
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        receiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopManagement(clearSession = true)
            return START_NOT_STICKY
        }

        val stored = ManagementPreferences(this).lastAppliedAdjustment
        val requested = intent?.takeIf { it.hasExtra(EXTRA_ADJUSTMENT) }
            ?.getFloatExtra(EXTRA_ADJUSTMENT, AdjustmentScale.NEUTRAL)
            ?: stored
        if (requested == null || !requested.isFinite()) {
            stopManagement(clearSession = false)
            return START_NOT_STICKY
        }

        adjustment = AdjustmentScale.normalize(requested)
        if (AdjustmentScale.isSame(adjustment, AdjustmentScale.NEUTRAL)) {
            stopManagement(clearSession = true)
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification(adjustment))
        curveState = null
        if (getSystemService(PowerManager::class.java).isInteractive) {
            startSensorTracking()
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        val lux = event.values.firstOrNull()?.takeIf { it.isFinite() && it >= 0f } ?: return
        val currentState = curveState
        if (currentState != null && lux > currentState.lowerLux && lux < currentState.upperLux) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (now - lastQueryAt < MIN_QUERY_INTERVAL_MILLIS) return
        if (!updateInFlight.compareAndSet(false, true)) return
        lastQueryAt = now
        worker.execute {
            try {
                updateForAmbientLux(lux)
            } catch (error: Throwable) {
                Log.e(TAG, "Brightness tracking update failed.", error)
            } finally {
                updateInFlight.set(false)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        stopSensorTracking()
        if (receiverRegistered) {
            unregisterReceiver(screenReceiver)
            receiverRegistered = false
        }
        val cleanup = worker.submit(::clearTemporaryBrightness)
        runCatching { cleanup.get(CLEANUP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS) }
        worker.shutdownNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateForAmbientLux(lux: Float) {
        val status = privilegeClient.refreshStatus()
        check(status == PrivilegeStatus.READY || status == PrivilegeStatus.CONNECTING) {
            "Shizuku is not ready: $status"
        }
        val values = privilegeClient.readAutomaticBrightnessState(lux)
        check(values.size >= CURVE_STATE_SIZE) { "Automatic-brightness state is incomplete." }
        val state = CurveState(
            baseBrightness = values[0],
            lowerLux = values[1],
            upperLux = values[2],
        )
        check(state.isValid()) { "Automatic-brightness state is invalid." }
        val target = BrightnessCompensation.apply(state.baseBrightness, adjustment)
        check(privilegeClient.setTemporaryBrightness(DEFAULT_DISPLAY, target)) {
            "Temporary brightness request was rejected."
        }
        curveState = state
        Log.i(
            TAG,
            "Applied lux=$lux base=${state.baseBrightness} adjustment=$adjustment target=$target " +
                "range=${state.lowerLux}..${state.upperLux}",
        )
    }

    private fun startSensorTracking() {
        if (sensorRegistered) return
        val sensor = lightSensor
        if (sensor == null) {
            Log.e(TAG, "Ambient light sensor is unavailable.")
            stopManagement(clearSession = false)
            return
        }
        sensorRegistered = sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
        )
        if (!sensorRegistered) {
            Log.e(TAG, "Ambient light sensor registration failed.")
            stopManagement(clearSession = false)
        }
    }

    private fun stopSensorTracking() {
        if (!sensorRegistered) return
        sensorManager.unregisterListener(this)
        sensorRegistered = false
    }

    private fun stopManagement(clearSession: Boolean) {
        stopSensorTracking()
        if (clearSession) ManagementPreferences(this).clearSession()
        worker.execute(::clearTemporaryBrightness)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun clearTemporaryBrightness() {
        runCatching {
            val status = privilegeClient.refreshStatus()
            if (status == PrivilegeStatus.READY || status == PrivilegeStatus.CONNECTING) {
                privilegeClient.clearTemporaryBrightness(DEFAULT_DISPLAY)
                privilegeClient.clearTemporaryAdjustment()
            }
        }.onFailure { Log.w(TAG, "Could not clear temporary brightness.", it) }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.management_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    private fun buildNotification(value: Float): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, BrightnessManagementService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.management_notification_title))
            .setContentText(
                getString(
                    R.string.management_notification_text,
                    AdjustmentScale.signedPoints(value),
                ),
            )
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                0,
                getString(R.string.management_notification_stop),
                stopIntent,
            )
            .build()
    }

    private data class CurveState(
        val baseBrightness: Float,
        val lowerLux: Float,
        val upperLux: Float,
    ) {
        fun isValid(): Boolean =
            baseBrightness.isFinite() && baseBrightness in 0f..1f &&
                lowerLux.isFinite() && upperLux.isFinite() && lowerLux < upperLux
    }

    companion object {
        private const val TAG = "BrightnessManagement"
        private const val ACTION_START =
            "com.fullmetalsonic.brightnessoffset.action.START_MANAGEMENT"
        private const val ACTION_STOP =
            "com.fullmetalsonic.brightnessoffset.action.STOP_MANAGEMENT"
        private const val EXTRA_ADJUSTMENT = "adjustment"
        private const val NOTIFICATION_CHANNEL_ID = "brightness_management"
        private const val NOTIFICATION_ID = 37021
        private const val DEFAULT_DISPLAY = 0
        private const val CURVE_STATE_SIZE = 3
        private const val MIN_QUERY_INTERVAL_MILLIS = 750L
        private const val CLEANUP_TIMEOUT_MILLIS = 1_500L

        fun start(context: Context, adjustment: Float) {
            val intent = Intent(context, BrightnessManagementService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_ADJUSTMENT, AdjustmentScale.normalize(adjustment))
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BrightnessManagementService::class.java))
        }
    }
}
