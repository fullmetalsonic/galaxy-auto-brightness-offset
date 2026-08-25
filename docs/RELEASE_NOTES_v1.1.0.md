# 자동 밝기 보정 v1.1.0 | Auto Brightness Offset v1.1.0

## 다운로드 | Download

[AutoBrightnessOffset-v1.1.0-debug.apk](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.1.0/AutoBrightnessOffset-v1.1.0-debug.apk)

- 크기 | Size: 18,643,383 bytes
- SHA-256: `CBA7064305F51C58054DC52FE5D2BB11F64201A4A6E1C73F22070DC513A1A992`
- 서명 | Signature: Android Debug, APK Signature Scheme v2 verified

## 변경사항 | What changed

- 한국어 시스템 언어에서는 한국어 UI 사용
- 영어 및 그 외 시스템 언어에서는 영어 UI 사용
- 앱 이름, 화면, 버튼, 상태, 오류, 진단 보고서에 Android 표준 언어 리소스 적용
- GitHub README와 검색 설명에 한국어·영어 제품 정보 병기

- Uses Korean UI when the Android system language is Korean.
- Uses English UI for English and all other system locales.
- Localizes the app name, screens, buttons, status messages, errors, and diagnostics with standard Android resources.
- Adds bilingual Korean and English product information and search terms to GitHub.

## 검증 | Verification

- Debug build: PASS
- Unsigned minified Release build: PASS
- JVM unit tests: 5/5 PASS
- Android Lint: 0 issues
- R8 and resource shrinking: PASS
- Default app label: `Auto Brightness Offset`
- Korean `ko` app label: `자동 밝기 보정`
- APK v2 signature, permissions, version, and SHA-256 audit: PASS
- Unresolved CRITICAL/HIGH issues: 0

## 현장 검증 필요 | Physical-device validation required

갤럭시 Z Fold8에서 한국어·영어 전환 화면, One UI의 실제 밝기 반영, 커버·메인 화면, 재부팅 재적용, 원래 값 복원 E2E를 확인해야 합니다.

Validate Korean/English switching, actual One UI brightness behavior, cover/main layouts, reboot reapply, and original-value restore on a Galaxy Z Fold8.

이 APK는 실기기 시험용 Debug 인증서로 서명되었습니다. This APK is debug-signed for physical-device testing.
