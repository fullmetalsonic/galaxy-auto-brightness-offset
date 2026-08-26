package com.fullmetalsonic.brightnessoffset.shizuku

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import java.lang.reflect.InvocationTargetException

/**
 * Calls the platform display service from the Shizuku shell process.
 *
 * The public SDK intentionally hides this API behind the signature-only
 * CONTROL_DISPLAY_BRIGHTNESS permission. The Shizuku shell process owns that
 * permission on supported Android builds, while the ordinary app process does not.
 */
// Shizuku UserService runs as shell and, unlike the normal app process, is not
// subject to Android's non-SDK API restriction. Keep this call inside that process.
@SuppressLint("BlockedPrivateApi", "PrivateApi", "SoonBlockedPrivateApi")
internal class TemporaryBrightnessController(context: Context) {
    private val displayManager = context.getSystemService(DisplayManager::class.java)
        ?: error("DisplayManager is unavailable.")

    private val adjustmentMethod by lazy {
        DisplayManager::class.java.getDeclaredMethod(
            METHOD_NAME,
            Float::class.javaPrimitiveType,
        ).apply { isAccessible = true }
    }

    private val temporaryBrightnessMethod by lazy {
        DisplayManager::class.java.getDeclaredMethod(
            TEMPORARY_BRIGHTNESS_METHOD_NAME,
            Int::class.javaPrimitiveType,
            Float::class.javaPrimitiveType,
        ).apply { isAccessible = true }
    }

    fun apply(value: Float) {
        val normalized = AdjustmentScale.normalize(value)
        invoke(normalized)
    }

    fun clear() {
        invoke(Float.NaN)
    }

    fun setTemporaryBrightness(displayId: Int, value: Float) {
        require(value.isFinite() && value in 0f..1f) { "Brightness must be between 0 and 1." }
        invokeTemporaryBrightness(displayId, value)
    }

    fun clearTemporaryBrightness(displayId: Int) {
        invokeTemporaryBrightness(displayId, Float.NaN)
    }

    private fun invoke(value: Float) {
        try {
            adjustmentMethod.invoke(displayManager, value)
        } catch (error: InvocationTargetException) {
            throw IllegalStateException(
                error.targetException?.message ?: "Display service rejected the adjustment.",
                error.targetException ?: error,
            )
        } catch (error: ReflectiveOperationException) {
            throw IllegalStateException(
                "Temporary auto-brightness API is unavailable on this device.",
                error,
            )
        }
    }

    private fun invokeTemporaryBrightness(displayId: Int, value: Float) {
        try {
            temporaryBrightnessMethod.invoke(displayManager, displayId, value)
        } catch (error: InvocationTargetException) {
            throw IllegalStateException(
                error.targetException?.message ?: "Display service rejected temporary brightness.",
                error.targetException ?: error,
            )
        } catch (error: ReflectiveOperationException) {
            throw IllegalStateException(
                "Temporary display brightness API is unavailable on this device.",
                error,
            )
        }
    }

    private companion object {
        const val METHOD_NAME = "setTemporaryAutoBrightnessAdjustment"
        const val TEMPORARY_BRIGHTNESS_METHOD_NAME = "setTemporaryBrightness"
    }
}
