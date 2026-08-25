package com.fullmetalsonic.brightnessoffset.diagnostics

import android.content.Context
import android.os.Build
import com.fullmetalsonic.brightnessoffset.BuildConfig
import com.fullmetalsonic.brightnessoffset.R
import com.fullmetalsonic.brightnessoffset.data.BrightnessRepository
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.BrightnessSnapshot

object DiagnosticReport {
    fun create(context: Context, snapshot: BrightnessSnapshot): String = buildString {
        appendLine(context.getString(R.string.diag_title))
        appendLine("${context.getString(R.string.diag_app_version)}: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("${context.getString(R.string.diag_device)}: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("${context.getString(R.string.diag_build)}: ${Build.DISPLAY}")
        appendLine("${context.getString(R.string.diag_settings_key)}: ${BrightnessRepository.ADJUSTMENT_KEY}")
        appendLine("${context.getString(R.string.diag_write_settings)}: ${snapshot.canWriteSettings.toStatus(context)}")
        appendLine("${context.getString(R.string.diag_adaptive_brightness)}: ${snapshot.isAutomaticMode.toStatus(context)}")
        appendLine("${context.getString(R.string.diag_current_offset)}: ${AdjustmentScale.rawValue(snapshot.currentAdjustment)}")
        appendLine("${context.getString(R.string.diag_managed)}: ${snapshot.isManaged.toStatus(context)}")
        appendLine(
            "${context.getString(R.string.diag_original)}: " +
                (snapshot.originalAdjustment?.let(AdjustmentScale::rawValue)
                    ?: context.getString(R.string.not_recorded)),
        )
        appendLine(
            "${context.getString(R.string.diag_last_applied)}: " +
                (snapshot.lastAppliedAdjustment?.let(AdjustmentScale::rawValue)
                    ?: context.getString(R.string.not_recorded)),
        )
        appendLine("${context.getString(R.string.diag_reapply_after_reboot)}: ${snapshot.restoreOnBoot.toStatus(context)}")
        appendLine("${context.getString(R.string.diag_external_change)}: ${snapshot.externalChangeDetected.toStatus(context)}")
        append("${context.getString(R.string.diag_read_error)}: ${snapshot.readError ?: context.getString(R.string.none)}")
    }

    private fun Boolean.toStatus(context: Context): String =
        context.getString(if (this) R.string.yes else R.string.no)
}
