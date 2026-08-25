package com.fullmetalsonic.brightnessoffset.data

import android.content.Context
import android.provider.Settings
import com.fullmetalsonic.brightnessoffset.R
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.BrightnessSnapshot
import com.fullmetalsonic.brightnessoffset.domain.FailureReason
import com.fullmetalsonic.brightnessoffset.domain.OperationResult

interface BrightnessRepositoryContract {
    fun snapshot(): BrightnessSnapshot
    fun applyAdjustment(value: Float): OperationResult
    fun restoreOriginal(): OperationResult
    fun setRestoreOnBoot(enabled: Boolean)
}

class BrightnessRepository(
    private val context: Context,
    private val managementPreferences: ManagementPreferences = ManagementPreferences(context),
) : BrightnessRepositoryContract {
    private val resolver = context.contentResolver

    override fun snapshot(): BrightnessSnapshot {
        return runCatching {
            val current = readAdjustment()
            val lastApplied = managementPreferences.lastAppliedAdjustment
            val managed = managementPreferences.isManaged

            BrightnessSnapshot(
                canWriteSettings = Settings.System.canWrite(context),
                isAutomaticMode = isAutomaticMode(),
                currentAdjustment = current,
                isManaged = managed,
                originalAdjustment = managementPreferences.originalAdjustment,
                lastAppliedAdjustment = lastApplied,
                restoreOnBoot = managementPreferences.restoreOnBoot,
                externalChangeDetected = managed && lastApplied != null &&
                    !AdjustmentScale.isSame(current, lastApplied),
            )
        }.getOrElse { error ->
            BrightnessSnapshot(
                canWriteSettings = Settings.System.canWrite(context),
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
        if (!Settings.System.canWrite(context)) {
            return OperationResult.Failure(
                FailureReason.PERMISSION_REQUIRED,
                context.getString(R.string.error_permission_apply),
            )
        }
        if (!isAutomaticMode()) {
            return OperationResult.Failure(
                FailureReason.AUTOMATIC_MODE_REQUIRED,
                context.getString(R.string.error_adaptive_required),
            )
        }

        return runCatching {
            val normalized = AdjustmentScale.normalize(value)
            val before = readAdjustment()
            val wasManaged = managementPreferences.isManaged
            val accepted = Settings.System.putFloat(resolver, ADJUSTMENT_KEY, normalized)

            if (!accepted) {
                return OperationResult.Failure(
                    FailureReason.WRITE_REJECTED,
                    context.getString(R.string.error_write_rejected),
                )
            }

            val verified = readAdjustment()
            if (!AdjustmentScale.isSame(verified, normalized)) {
                return OperationResult.Failure(
                    FailureReason.VERIFICATION_FAILED,
                    context.getString(R.string.error_apply_verification),
                )
            }

            managementPreferences.startOrUpdateSession(
                original = managementPreferences.originalAdjustment ?: before,
                applied = verified,
                wasManaged = wasManaged,
            )
            OperationResult.Success(
                verified,
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
        if (!Settings.System.canWrite(context)) {
            return OperationResult.Failure(
                FailureReason.PERMISSION_REQUIRED,
                context.getString(R.string.error_permission_restore),
            )
        }
        val original = managementPreferences.originalAdjustment
            ?: return OperationResult.Failure(
                FailureReason.NO_ORIGINAL_VALUE,
                context.getString(R.string.error_no_original),
            )

        return runCatching {
            val accepted = Settings.System.putFloat(resolver, ADJUSTMENT_KEY, original)
            if (!accepted) {
                return OperationResult.Failure(
                    FailureReason.WRITE_REJECTED,
                    context.getString(R.string.error_restore_rejected),
                )
            }
            val verified = readAdjustment()
            if (!AdjustmentScale.isSame(verified, original)) {
                return OperationResult.Failure(
                    FailureReason.VERIFICATION_FAILED,
                    context.getString(R.string.error_restore_verification),
                )
            }
            managementPreferences.clearSession()
            OperationResult.Success(
                verified,
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
    }

    fun reapplyAfterBoot(): Boolean {
        if (!managementPreferences.isManaged || !managementPreferences.restoreOnBoot) return false
        if (!Settings.System.canWrite(context) || !isAutomaticMode()) return false
        val target = managementPreferences.lastAppliedAdjustment ?: return false
        return runCatching {
            Settings.System.putFloat(resolver, ADJUSTMENT_KEY, target) &&
                AdjustmentScale.isSame(readAdjustment(), target)
        }.getOrDefault(false)
    }

    private fun readAdjustment(): Float {
        val value = Settings.System.getFloat(resolver, ADJUSTMENT_KEY, AdjustmentScale.NEUTRAL)
        return if (value.isFinite()) value.coerceIn(-1f, 1f) else AdjustmentScale.NEUTRAL
    }

    private fun isAutomaticMode(): Boolean =
        Settings.System.getInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
        ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC

    companion object {
        // AOSP uses this hidden Settings.System key for the automatic-brightness curve adjustment.
        // Using the literal avoids reflection and hidden-API invocation.
        const val ADJUSTMENT_KEY = "screen_auto_brightness_adj"
    }
}
