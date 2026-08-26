package com.fullmetalsonic.brightnessoffset.data

import android.content.Context
import android.provider.Settings
import com.fullmetalsonic.brightnessoffset.R
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.BrightnessSnapshot
import com.fullmetalsonic.brightnessoffset.domain.FailureReason
import com.fullmetalsonic.brightnessoffset.domain.OperationResult
import com.fullmetalsonic.brightnessoffset.domain.PrivilegeStatus
import com.fullmetalsonic.brightnessoffset.shizuku.PrivilegedSettingsContract
import com.fullmetalsonic.brightnessoffset.shizuku.ShizukuPrivilegeClient
import com.fullmetalsonic.brightnessoffset.system.BrightnessManagementService

interface BrightnessRepositoryContract {
    fun snapshot(): BrightnessSnapshot
    fun applyAdjustment(value: Float): OperationResult
    fun restoreOriginal(): OperationResult
    fun setRestoreOnBoot(enabled: Boolean)
    fun reapplyPendingIfReady(): Boolean
}

class BrightnessRepository(
    private val context: Context,
    private val managementPreferences: ManagementPreferences = ManagementPreferences(context),
    private val privilegedSettings: PrivilegedSettingsContract = ShizukuPrivilegeClient.get(context),
) : BrightnessRepositoryContract {
    private val resolver = context.contentResolver

    override fun snapshot(): BrightnessSnapshot {
        return runCatching {
            val lastApplied = managementPreferences.lastAppliedAdjustment
            val managed = managementPreferences.isManaged
            val current = if (managed) {
                lastApplied ?: AdjustmentScale.NEUTRAL
            } else {
                AdjustmentScale.NEUTRAL
            }

            BrightnessSnapshot(
                privilegeStatus = privilegedSettings.refreshStatus(),
                isAutomaticMode = isAutomaticMode(),
                currentAdjustment = current,
                isManaged = managed,
                originalAdjustment = managementPreferences.originalAdjustment,
                lastAppliedAdjustment = lastApplied,
                restoreOnBoot = managementPreferences.restoreOnBoot,
                // Android exposes no public getter for a temporary auto-brightness
                // adjustment. The live value is verified during device tests instead.
                externalChangeDetected = false,
            )
        }.getOrElse { error ->
            BrightnessSnapshot(
                privilegeStatus = privilegedSettings.refreshStatus(),
                isAutomaticMode = false,
                currentAdjustment = 0f,
                isManaged = managementPreferences.isManaged,
                originalAdjustment = managementPreferences.originalAdjustment,
                lastAppliedAdjustment = managementPreferences.lastAppliedAdjustment,
                restoreOnBoot = managementPreferences.restoreOnBoot,
                externalChangeDetected = false,
                readError = error.message ?: error.javaClass.simpleName,
            )
        }
    }

    override fun applyAdjustment(value: Float): OperationResult {
        privilegeFailure(privilegedSettings.refreshStatus())?.let { return it }
        if (!isAutomaticMode()) {
            return OperationResult.Failure(
                FailureReason.AUTOMATIC_MODE_REQUIRED,
                context.getString(R.string.error_adaptive_required),
            )
        }

        return runCatching {
            val normalized = AdjustmentScale.normalize(value)
            if (AdjustmentScale.isSame(normalized, AdjustmentScale.NEUTRAL)) {
                BrightnessManagementService.stop(context)
                val cleared = privilegedSettings.clearTemporaryBrightness(DEFAULT_DISPLAY) &&
                    privilegedSettings.clearTemporaryAdjustment()
                if (!cleared) {
                    return OperationResult.Failure(
                        FailureReason.WRITE_REJECTED,
                        context.getString(R.string.error_write_rejected),
                    )
                }
                managementPreferences.clearSession()
            } else {
                managementPreferences.startOrUpdateSession(
                    original = AdjustmentScale.NEUTRAL,
                    applied = normalized,
                    wasManaged = managementPreferences.isManaged,
                )
                runCatching { BrightnessManagementService.start(context, normalized) }
                    .onFailure { managementPreferences.clearSession() }
                    .getOrThrow()
            }
            OperationResult.Success(
                normalized,
                context.getString(R.string.success_applied),
            )
        }.getOrElse { error ->
            OperationResult.Failure(
                FailureReason.UNKNOWN,
                error.message ?: context.getString(R.string.error_unknown),
            )
        }
    }

    override fun restoreOriginal(): OperationResult {
        privilegeFailure(privilegedSettings.refreshStatus())?.let { return it }
        if (!managementPreferences.isManaged) {
            return OperationResult.Failure(
                FailureReason.NO_ORIGINAL_VALUE,
                context.getString(R.string.error_no_original),
            )
        }

        return runCatching {
            BrightnessManagementService.stop(context)
            val accepted = privilegedSettings.clearTemporaryBrightness(DEFAULT_DISPLAY) &&
                privilegedSettings.clearTemporaryAdjustment()
            if (!accepted) {
                return OperationResult.Failure(
                    FailureReason.WRITE_REJECTED,
                    context.getString(R.string.error_restore_rejected),
                )
            }
            managementPreferences.clearSession()
            OperationResult.Success(
                AdjustmentScale.NEUTRAL,
                context.getString(R.string.success_restored),
            )
        }.getOrElse { error ->
            OperationResult.Failure(
                FailureReason.UNKNOWN,
                error.message ?: context.getString(R.string.error_unknown_restore),
            )
        }
    }

    override fun setRestoreOnBoot(enabled: Boolean) {
        managementPreferences.restoreOnBoot = enabled
        if (!enabled) managementPreferences.reapplyPending = false
    }

    fun markReapplyPending() {
        if (managementPreferences.isManaged && managementPreferences.restoreOnBoot) {
            managementPreferences.reapplyPending = true
        }
    }

    override fun reapplyPendingIfReady(): Boolean {
        if (!managementPreferences.isManaged) return false
        val applied = resumeManagedIfReady()
        if (applied) managementPreferences.reapplyPending = false
        return applied
    }

    fun reapplyAfterBoot(): Boolean {
        if (!managementPreferences.isManaged || !managementPreferences.restoreOnBoot) return false
        return resumeManagedIfReady()
    }

    fun resumeManagedIfReady(): Boolean {
        if (!managementPreferences.isManaged) return false
        if (privilegedSettings.refreshStatus() !in READY_OR_CONNECTING || !isAutomaticMode()) {
            return false
        }
        val target = managementPreferences.lastAppliedAdjustment ?: return false
        return runCatching {
            BrightnessManagementService.start(context, target)
            true
        }.getOrDefault(false)
    }

    private fun privilegeFailure(status: PrivilegeStatus): OperationResult.Failure? = when (status) {
        PrivilegeStatus.NOT_INSTALLED -> OperationResult.Failure(
            FailureReason.SHIZUKU_NOT_INSTALLED,
            context.getString(R.string.error_shizuku_not_installed),
        )
        PrivilegeStatus.NOT_RUNNING -> OperationResult.Failure(
            FailureReason.SHIZUKU_NOT_RUNNING,
            context.getString(R.string.error_shizuku_not_running),
        )
        PrivilegeStatus.PERMISSION_REQUIRED,
        PrivilegeStatus.PERMISSION_DENIED,
        -> OperationResult.Failure(
            FailureReason.PERMISSION_REQUIRED,
            context.getString(R.string.error_shizuku_permission),
        )
        PrivilegeStatus.ERROR -> OperationResult.Failure(
            FailureReason.PRIVILEGE_NOT_READY,
            context.getString(R.string.error_shizuku_connection),
        )
        PrivilegeStatus.CONNECTING,
        PrivilegeStatus.READY,
        -> null
    }

    private fun isAutomaticMode(): Boolean =
        Settings.System.getInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
        ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC

    companion object {
        private const val DEFAULT_DISPLAY = 0
        private val READY_OR_CONNECTING = setOf(
            PrivilegeStatus.READY,
            PrivilegeStatus.CONNECTING,
        )
    }
}
