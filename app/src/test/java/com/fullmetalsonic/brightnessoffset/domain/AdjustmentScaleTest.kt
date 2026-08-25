package com.fullmetalsonic.brightnessoffset.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjustmentScaleTest {
    @Test
    fun normalize_clampsValuesToSafeRange() {
        assertEquals(-0.5f, AdjustmentScale.normalize(-2f), 0.0001f)
        assertEquals(0.5f, AdjustmentScale.normalize(2f), 0.0001f)
    }

    @Test
    fun normalize_roundsToFivePointSteps() {
        assertEquals(0.15f, AdjustmentScale.normalize(0.13f), 0.0001f)
        assertEquals(-0.15f, AdjustmentScale.normalize(-0.13f), 0.0001f)
    }

    @Test
    fun signedPoints_formatsDirectionClearly() {
        assertEquals("+20", AdjustmentScale.signedPoints(0.2f))
        assertEquals("-20", AdjustmentScale.signedPoints(-0.2f))
        assertEquals("0", AdjustmentScale.signedPoints(0f))
    }

    @Test
    fun directionLabel_doesNotDescribePointsAsPercent() {
        assertEquals("더 밝게", AdjustmentScale.directionLabel(0.1f))
        assertEquals("더 어둡게", AdjustmentScale.directionLabel(-0.1f))
        assertEquals("보정 없음", AdjustmentScale.directionLabel(0f))
    }

    @Test
    fun isSame_allowsSettingsRoundTripToleranceOnly() {
        assertTrue(AdjustmentScale.isSame(0.2f, 0.204f))
        assertFalse(AdjustmentScale.isSame(0.2f, 0.21f))
    }
}
