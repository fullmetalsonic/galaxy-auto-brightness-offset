package com.fullmetalsonic.brightnessoffset.shizuku

import android.content.Context
import androidx.annotation.Keep
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import java.util.concurrent.TimeUnit

@Keep
class PrivilegedBrightnessService private constructor(
    private val controller: TemporaryBrightnessController?,
) : IPrivilegedBrightnessService.Stub() {
    @Keep
    constructor() : this(null)

    @Keep
    constructor(context: Context) : this(TemporaryBrightnessController(context))

    override fun readAdjustment(): Float =
        BrightnessSettingCommand.parse(run(BrightnessSettingCommand.readArgs(), requireSuccess = true))

    override fun writeAdjustment(value: Float): Boolean {
        val args = runCatching { BrightnessSettingCommand.writeArgs(value) }.getOrNull()
            ?: return false
        run(args, requireSuccess = true)
        return AdjustmentScale.isSame(readAdjustment(), AdjustmentScale.normalize(value))
    }

    override fun applyTemporaryAdjustment(value: Float): Boolean {
        if (!value.isFinite()) return false
        controller().apply(value)
        return true
    }

    override fun clearTemporaryAdjustment(): Boolean {
        controller().clear()
        return true
    }

    override fun setTemporaryBrightness(displayId: Int, value: Float): Boolean {
        controller().setTemporaryBrightness(displayId, value)
        return true
    }

    override fun clearTemporaryBrightness(displayId: Int): Boolean {
        controller().clearTemporaryBrightness(displayId)
        return true
    }

    override fun readAutomaticBrightnessTarget(ambientLux: Float): Float =
        AutomaticBrightnessCurveCommand.parse(
            run(AutomaticBrightnessCurveCommand.args(ambientLux), requireSuccess = true),
        )

    override fun readAutomaticBrightnessState(ambientLux: Float): FloatArray =
        AutomaticBrightnessCurveCommand.parsePoint(
            run(AutomaticBrightnessCurveCommand.args(ambientLux), requireSuccess = true),
        ).toFloatArray()

    override fun destroy() {
        controller?.let { brightnessController ->
            runCatching { brightnessController.clearTemporaryBrightness(DEFAULT_DISPLAY) }
            runCatching { brightnessController.clear() }
        }
        System.exit(0)
    }

    private fun run(args: List<String>, requireSuccess: Boolean = false): String {
        val process = ProcessBuilder(args)
            .redirectErrorStream(true)
            .start()
        if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            throw IllegalStateException("Settings command timed out.")
        }
        val output = process.inputStream.bufferedReader().use { it.readText() }
        if (requireSuccess && process.exitValue() != 0) {
            throw IllegalStateException(output.ifBlank { "Settings command failed." })
        }
        return output
    }

    private fun controller(): TemporaryBrightnessController =
        controller ?: error("Shizuku did not provide a service context.")

    private companion object {
        const val COMMAND_TIMEOUT_SECONDS = 3L
        const val DEFAULT_DISPLAY = 0
    }
}
