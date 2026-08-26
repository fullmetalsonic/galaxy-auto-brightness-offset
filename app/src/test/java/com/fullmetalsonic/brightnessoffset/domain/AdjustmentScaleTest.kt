package com.fullmetalsonic.brightnessoffset.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjustmentScaleTest {
    @Test
    fun normalize_clampsValuesToSafeRange() {
        assertEquals(-1f, AdjustmentScale.normalize(-2f), 0.0001f)
        assertEquals(1f, AdjustmentScale.normalize(2f), 0.0001f)
    }

    @Test
    fun normalize_roundsToFivePointSteps() {
        assertEquals(0.15f, AdjustmentScale.normalize(0.13f), 0.0001f)
        assertEquals(-0.15f, AdjustmentScale.normalize(-0.13f), 0.0001f)
    }

    @Test
    fun signedPoints_formatsDirectionClearly() {
        assertEquals("+100", AdjustmentScale.signedPoints(1f))
        assertEquals("-100", AdjustmentScale.signedPoints(-1f))
        assertEquals("+20", AdjustmentScale.signedPoints(0.2f))
        assertEquals("-20", AdjustmentScale.signedPoints(-0.2f))
        assertEquals("0", AdjustmentScale.signedPoints(0f))
    }

    @Test
    fun points_expressesStrengthWithoutLocaleSpecificText() {
        assertEquals(10, AdjustmentScale.points(0.1f))
        assertEquals(-10, AdjustmentScale.points(-0.1f))
        assertEquals(0, AdjustmentScale.points(0f))
    }

    @Test
    fun isSame_allowsSettingsRoundTripToleranceOnly() {
        assertTrue(AdjustmentScale.isSame(0.2f, 0.204f))
        assertFalse(AdjustmentScale.isSame(0.2f, 0.21f))
    }

    @Test
    fun presets_coverSymmetricQuarterStrengths() {
        assertEquals(
            listOf(-0.75f, -0.5f, -0.25f, 0f, 0.25f, 0.5f, 0.75f),
            AdjustmentScale.PRESETS,
        )
    }
}
