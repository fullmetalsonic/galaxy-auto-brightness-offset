package com.fullmetalsonic.brightnessoffset.domain

import kotlin.math.pow

/** Applies the same gamma-style curve shift used by Android adaptive brightness. */
object BrightnessCompensation {
    fun apply(baseBrightness: Float, adjustment: Float): Float {
        require(baseBrightness.isFinite()) { "Base brightness must be finite." }
        val base = baseBrightness.coerceIn(0f, 1f)
        val normalizedAdjustment = AdjustmentScale.normalize(adjustment)
        if (base == 0f || normalizedAdjustment == AdjustmentScale.NEUTRAL) return base

        val gamma = MAX_GAMMA.toDouble().pow(-normalizedAdjustment.toDouble())
        return base.toDouble().pow(gamma).toFloat().coerceIn(0f, 1f)
    }

    private const val MAX_GAMMA = 3f
}
