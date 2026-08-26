package com.fullmetalsonic.brightnessoffset.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AutomaticBrightnessCurveCommandTest {
    @Test
    fun parse_convertsSamsungLevelToNormalizedBrightness() {
        val output = "getAmbientBrightnessInfo :     85(0.33)     47 <    174.0 <    582 (adj:+0.0)"
        assertEquals(85f / 255f, AutomaticBrightnessCurveCommand.parse(output), 0.0001f)
        val point = AutomaticBrightnessCurveCommand.parsePoint(output)
        assertEquals(47f, point.lowerLux, 0.0001f)
        assertEquals(582f, point.upperLux, 0.0001f)
    }

    @Test
    fun parse_rejectsUnknownOrOutOfRangeOutput() {
        assertThrows(IllegalStateException::class.java) {
            AutomaticBrightnessCurveCommand.parse("Unknown command")
        }
        assertThrows(IllegalArgumentException::class.java) {
            AutomaticBrightnessCurveCommand.parse(
                "getAmbientBrightnessInfo : 300(1.18) 47 < 174.0 < 582",
            )
        }
    }
}
