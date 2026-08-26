package com.fullmetalsonic.brightnessoffset.shizuku

internal object AutomaticBrightnessCurveCommand {
    private val STATE_PATTERN = Regex(
        """getAmbientBrightnessInfo\s*:\s*(\d+)\([^)]*\)\s+(-?\d+)\s*<\s*[\d.]+\s*<\s*(\d+)""",
    )

    fun args(ambientLux: Float): List<String> {
        require(ambientLux.isFinite() && ambientLux >= 0f) {
            "Ambient lux must be a finite non-negative value."
        }
        return listOf(
            "/system/bin/cmd",
            "display",
            "get-ambient-brightness-info",
            ambientLux.toString(),
        )
    }

    fun parse(output: String): Float = parsePoint(output).brightness

    fun parsePoint(output: String): AutomaticBrightnessCurvePoint {
        val groups = STATE_PATTERN.find(output)?.groupValues
            ?: error("Samsung automatic-brightness curve output was not recognized.")
        val level = groups[1].toIntOrNull()
            ?: error("Automatic-brightness level was not numeric.")
        val lowerLux = groups[2].toFloatOrNull()
            ?: error("Lower ambient-light threshold was not numeric.")
        val upperLux = groups[3].toFloatOrNull()
            ?: error("Upper ambient-light threshold was not numeric.")
        require(level in MIN_LEVEL..MAX_LEVEL) {
            "Automatic-brightness level is out of range: $level"
        }
        require(lowerLux < upperLux) { "Ambient-light thresholds are invalid." }
        return AutomaticBrightnessCurvePoint(
            brightness = level / MAX_LEVEL.toFloat(),
            lowerLux = lowerLux,
            upperLux = upperLux,
        )
    }

    private const val MIN_LEVEL = 0
    private const val MAX_LEVEL = 255
}

internal data class AutomaticBrightnessCurvePoint(
    val brightness: Float,
    val lowerLux: Float,
    val upperLux: Float,
) {
    fun toFloatArray(): FloatArray = floatArrayOf(brightness, lowerLux, upperLux)
}
