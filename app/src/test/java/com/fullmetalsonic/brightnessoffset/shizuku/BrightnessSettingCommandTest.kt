package com.fullmetalsonic.brightnessoffset.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BrightnessSettingCommandTest {
    @Test
    fun writeArgs_acceptOnlyNormalizedSafeValues() {
        assertEquals(
            listOf(
                "/system/bin/settings",
                "put",
                "system",
                "screen_auto_brightness_adj",
                "0.10",
            ),
            BrightnessSettingCommand.writeArgs(0.1f),
        )
        assertThrows(IllegalArgumentException::class.java) {
            BrightnessSettingCommand.writeArgs(0.12f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BrightnessSettingCommand.writeArgs(Float.NaN)
        }
    }

    @Test
    fun parse_rejectsInvalidAndClampsUnexpectedOutput() {
        assertEquals(0.1f, BrightnessSettingCommand.parse("0.10\n"), 0.0001f)
        assertEquals(0f, BrightnessSettingCommand.parse("null"), 0.0001f)
        assertEquals(1f, BrightnessSettingCommand.parse("3.0"), 0.0001f)
    }
}
