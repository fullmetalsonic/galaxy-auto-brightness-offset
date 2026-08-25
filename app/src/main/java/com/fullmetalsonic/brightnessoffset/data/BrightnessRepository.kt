package com.fullmetalsonic.brightnessoffset.data

import android.content.Context
import android.provider.Settings
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
                "시스템 설정 변경 권한이 필요합니다.",
            )
        }
        if (!isAutomaticMode()) {
            return OperationResult.Failure(
                FailureReason.AUTOMATIC_MODE_REQUIRED,
                "자동 밝기를 먼저 켜 주세요.",
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
                    "기기가 보정값 변경을 거부했습니다.",
                )
            }

            val verified = readAdjustment()
            if (!AdjustmentScale.isSame(verified, normalized)) {
                return OperationResult.Failure(
                    FailureReason.VERIFICATION_FAILED,
                    "적용 후 읽은 값이 요청값과 다릅니다.",
                )
            }

            managementPreferences.startOrUpdateSession(
                original = managementPreferences.originalAdjustment ?: before,
                applied = verified,
                wasManaged = wasManaged,
            )
            OperationResult.Success(verified, "보정값을 적용하고 다시 확인했습니다.")
        }.getOrElse { error ->
            OperationResult.Failure(
                FailureReason.UNKNOWN,
                error.message ?: "알 수 없는 오류가 발생했습니다.",
            )
        }
    }

    override fun restoreOriginal(): OperationResult {
        if (!Settings.System.canWrite(context)) {
            return OperationResult.Failure(
                FailureReason.PERMISSION_REQUIRED,
                "복원하려면 시스템 설정 변경 권한이 필요합니다.",
            )
        }
        val original = managementPreferences.originalAdjustment
            ?: return OperationResult.Failure(
                FailureReason.NO_ORIGINAL_VALUE,
                "저장된 원래 값이 없습니다.",
            )

        return runCatching {
            val accepted = Settings.System.putFloat(resolver, ADJUSTMENT_KEY, original)
            if (!accepted) {
                return OperationResult.Failure(
                    FailureReason.WRITE_REJECTED,
                    "기기가 원래 값 복원을 거부했습니다.",
                )
            }
            val verified = readAdjustment()
            if (!AdjustmentScale.isSame(verified, original)) {
                return OperationResult.Failure(
                    FailureReason.VERIFICATION_FAILED,
                    "복원 후 읽은 값이 원래 값과 다릅니다.",
                )
            }
            managementPreferences.clearSession()
            OperationResult.Success(verified, "앱 사용 전 원래 값으로 복원했습니다.")
        }.getOrElse { error ->
            OperationResult.Failure(
                FailureReason.UNKNOWN,
                error.message ?: "복원 중 알 수 없는 오류가 발생했습니다.",
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
