package com.fullmetalsonic.brightnessoffset.domain

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object AdjustmentScale {
    const val MIN = -1f
    const val MAX = 1f
    const val NEUTRAL = 0f
    const val STEP = 0.05f
    val PRESETS = listOf(-0.75f, -0.5f, -0.25f, 0f, 0.25f, 0.5f, 0.75f)
    private const val EQUALITY_TOLERANCE = 0.006f

    fun normalize(value: Float): Float {
        val clamped = value.coerceIn(MIN, MAX)
        return (clamped / STEP).roundToInt() * STEP
    }

    fun isSame(first: Float, second: Float): Boolean =
        abs(first - second) < EQUALITY_TOLERANCE

    fun points(value: Float): Int = (normalize(value) * 100).roundToInt()

    fun signedPoints(value: Float): String {
        val points = points(value)
        return when {
            points > 0 -> "+$points"
            else -> points.toString()
        }
    }

    fun rawValue(value: Float): String = String.format(Locale.US, "%.2f", value)
}

data class BrightnessSnapshot(
    val privilegeStatus: PrivilegeStatus,
    val isAutomaticMode: Boolean,
    val currentAdjustment: Float,
    val isManaged: Boolean,
    val originalAdjustment: Float?,
    val lastAppliedAdjustment: Float?,
    val restoreOnBoot: Boolean,
    val externalChangeDetected: Boolean,
    val readError: String? = null,
)

sealed interface OperationResult {
    data class Success(
        val verifiedValue: Float,
        val message: String,
    ) : OperationResult

    data class Failure(
        val reason: FailureReason,
        val message: String,
    ) : OperationResult
}

enum class FailureReason {
    SHIZUKU_NOT_INSTALLED,
    SHIZUKU_NOT_RUNNING,
    PERMISSION_REQUIRED,
    PRIVILEGE_NOT_READY,
    AUTOMATIC_MODE_REQUIRED,
    WRITE_REJECTED,
    VERIFICATION_FAILED,
    NO_ORIGINAL_VALUE,
    UNKNOWN,
}

enum class PrivilegeStatus {
    NOT_INSTALLED,
    NOT_RUNNING,
    PERMISSION_REQUIRED,
    PERMISSION_DENIED,
    CONNECTING,
    READY,
    ERROR,
}
