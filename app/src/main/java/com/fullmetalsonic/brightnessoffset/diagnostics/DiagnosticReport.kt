package com.fullmetalsonic.brightnessoffset.diagnostics

import android.os.Build
import com.fullmetalsonic.brightnessoffset.BuildConfig
import com.fullmetalsonic.brightnessoffset.data.BrightnessRepository
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.BrightnessSnapshot

object DiagnosticReport {
    fun create(snapshot: BrightnessSnapshot): String = buildString {
        appendLine("자동 밝기 보정 진단")
        appendLine("앱 버전: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("기기: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("빌드: ${Build.DISPLAY}")
        appendLine("설정 키: ${BrightnessRepository.ADJUSTMENT_KEY}")
        appendLine("설정 변경 권한: ${snapshot.canWriteSettings.toKoreanStatus()}")
        appendLine("자동 밝기: ${snapshot.isAutomaticMode.toKoreanStatus()}")
        appendLine("현재 보정값: ${AdjustmentScale.rawValue(snapshot.currentAdjustment)}")
        appendLine("앱 관리 중: ${snapshot.isManaged.toKoreanStatus()}")
        appendLine(
            "원래 값: ${snapshot.originalAdjustment?.let(AdjustmentScale::rawValue) ?: "기록 없음"}",
        )
        appendLine(
            "마지막 적용값: ${snapshot.lastAppliedAdjustment?.let(AdjustmentScale::rawValue) ?: "기록 없음"}",
        )
        appendLine("재부팅 후 복원: ${snapshot.restoreOnBoot.toKoreanStatus()}")
        appendLine("외부 변경 감지: ${snapshot.externalChangeDetected.toKoreanStatus()}")
        append("읽기 오류: ${snapshot.readError ?: "없음"}")
    }

    private fun Boolean.toKoreanStatus(): String = if (this) "예" else "아니오"
}
