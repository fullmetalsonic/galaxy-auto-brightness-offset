package com.fullmetalsonic.brightnessoffset.shizuku

import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import java.util.Locale

internal object BrightnessSettingCommand {
    const val KEY = "screen_auto_brightness_adj"

    fun readArgs(): List<String> =
        listOf("/system/bin/settings", "get", "system", KEY)

    fun writeArgs(value: Float): List<String> {
        require(value.isFinite()) { "Adjustment must be finite." }
        val normalized = AdjustmentScale.normalize(value)
        require(AdjustmentScale.isSame(value, normalized)) { "Adjustment must use 0.05 steps." }
        return listOf(
            "/system/bin/settings",
            "put",
            "system",
            KEY,
            String.format(Locale.US, "%.2f", normalized),
        )
    }

    fun parse(output: String): Float {
        val value = output.trim().toFloatOrNull() ?: AdjustmentScale.NEUTRAL
        return if (value.isFinite()) {
            value.coerceIn(AdjustmentScale.MIN, AdjustmentScale.MAX)
        } else {
            AdjustmentScale.NEUTRAL
        }
    }
}
