package com.fullmetalsonic.brightnessoffset.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrightnessCompensationTest {
    @Test
    fun neutral_keepsAutomaticTarget() {
        assertEquals(0.33f, BrightnessCompensation.apply(0.33f, 0f), 0.0001f)
    }

    @Test
    fun positiveIsBrighterAndNegativeIsDarker() {
        val base = 85f / 255f
        val brighter = BrightnessCompensation.apply(base, 0.2f)
        val darker = BrightnessCompensation.apply(base, -0.2f)

        assertTrue(brighter > base)
        assertTrue(darker < base)
        assertEquals(0.414f, brighter, 0.003f)
        assertEquals(0.255f, darker, 0.003f)
    }

    @Test
    fun fullRangeProducesStrongButBoundedDifference() {
        val base = 0.32f

        assertEquals(0.684f, BrightnessCompensation.apply(base, 1f), 0.003f)
        assertEquals(0.033f, BrightnessCompensation.apply(base, -1f), 0.003f)
    }
}
