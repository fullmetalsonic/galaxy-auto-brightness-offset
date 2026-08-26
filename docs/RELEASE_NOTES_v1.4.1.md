# Auto Brightness Offset v1.4.1

Shizuku 연결이 끊겼을 때의 상태·복원 흐름과 Android 관리 알림 권한을 보강한 안정화 릴리스입니다.

## 한국어

### 주요 변경

- Shizuku 서버가 중지되면 조도 센서 추적을 멈추고 `보정 일시 중지`로 표시
- 연결이 끊긴 동안 반복되던 `NOT_RUNNING` 오류와 불필요한 센서 재시도 제거
- 마지막 보정값과 현재 적용값을 구분해 중지 상태를 오해하지 않도록 개선
- Shizuku 재시작 후 앱을 열면 저장한 보정값을 자동 재적용
- 연결이 끊긴 상태의 `원래 값 복원`을 예약하고 다음 연결 때 자동 완료
- 진단 정보에 `복원 대기` 상태 추가
- Android 13 이상에서 관리 알림 권한을 첫 실행에 한 번 요청
- USB ADB 시작 후 Wi-Fi 없이 사용하는 야외 운용 절차, 두 앱의 배터리 제한 해제 조건과 수동 연결 복구를 한영 설명서에 추가

### Galaxy Z Fold8 실제 검증

| 시험 | 결과 |
|---|---|
| JVM Unit Test | PASS, 17/17 |
| Android Lint | PASS, 이슈 0건 |
| Debug APK | PASS |
| R8·리소스 축소 Release APK | PASS |
| Shizuku 중지 감지와 센서 추적 중지 | PASS |
| 중지 뒤 6초간 반복 `NOT_RUNNING` 오류 | PASS, 0건 |
| 끊김 상태에서 복원 예약 | PASS, `pending_restore=true` |
| Shizuku USB 재시작 후 앱 열기 | PASS, 예약 복원 완료 |
| 복원 후 임시 밝기 | PASS, `NaN` |
| 저장한 `+75` 재적용 | PASS, 기본 약 `0.3451` → 실제 약 `0.6270` |
| Android 알림 권한 창과 관리 알림 | PASS |
| 두 앱 절전 예외 후 Wi-Fi·무선 디버깅 OFF | PASS, 백그라운드 3분과 화면 잠금·재점등 뒤 Shizuku·서비스·`+75` 유지 |
| 최종 Release 설치와 기기 `base.apk` 해시 | PASS, 일치 |

Samsung Freecess가 Shizuku 또는 앱을 동결하면 서버 PID가 살아 있어도 Binder 접근이 일시적으로 풀릴 수 있습니다. 두 앱을 배터리 `제한 없음`과 `절전 상태로 전환하지 않을 앱`에 추가하십시오. 접근이 풀리면 Wi-Fi를 켤 필요 없이 **Shizuku 열기 → Auto Brightness Offset 열기**로 복구할 수 있습니다. 재부팅하거나 서버가 실제 종료되면 PC, USB ADB 브리지 또는 루트 방식으로 Shizuku를 다시 시작해야 합니다.

### 설치 파일

- 파일: `AutoBrightnessOffset-v1.4.1-release.apk`
- 크기: `3,474,839 bytes`
- SHA-256: `91B5700052DECF3BCCBC67B17684CF90DF4B7C2955FCBD9A1AD1799DA472BBD0`
- 버전: `1.4.1` (`versionCode=12`, `targetSdk=36`)
- 서명: Android Debug 인증서, APK Signature Scheme v2·v3 PASS
- Debug 전용 ADB 시험 Receiver: Release에서 미포함 확인

이 APK는 개인 테스트·사이드로드용 디버그 서명 릴리스입니다. Google Play 배포용 서명 패키지가 아닙니다.

## English

### Changes

- Pauses ambient-light tracking and shows **Offset paused** when the Shizuku server stops
- Removes repeated `NOT_RUNNING` failures and unnecessary sensor retries while disconnected
- Distinguishes the saved offset from a currently applied value
- Reapplies the saved offset after Shizuku restarts and the app is opened
- Queues **Restore original** while disconnected and completes it on the next connection
- Adds the pending-restore state to Diagnostics
- Requests Android management-notification permission once on Android 13 and later
- Adds bilingual USB-start and Wi-Fi-free outdoor instructions, battery-exemption requirements, and manual Binder recovery

### Physical Fold8 verification

- Unit tests: PASS, 17/17
- Android Lint: PASS, zero issues
- Debug and R8/resource-shrunk Release builds: PASS
- Shizuku stop detection, paused UI, and sensor stop: PASS
- Repeated `NOT_RUNNING` log lines during a six-second paused window: 0
- Disconnected restore queue: PASS
- USB Shizuku restart, app reopen, and automatic restore: PASS
- Temporary brightness after restore: `NaN`
- Saved `+75` reapplied: Samsung base about `0.3451` to actual about `0.6270`
- One-time notification permission prompt and active management notification: PASS
- With both apps battery-exempt, Wi-Fi and Wireless debugging off: Shizuku, service, and `+75` survived a three-minute background run plus screen lock and wake
- Installed final Release and local APK SHA-256: identical

Samsung Freecess can temporarily interrupt Binder access even while the Shizuku server PID remains alive. Set both apps to **Unrestricted** and add both to **Never auto sleeping apps**. If access drops, keep Wi-Fi off and use **open Shizuku → open Auto Brightness Offset**. A reboot or terminated server still needs a computer, USB ADB bridge, or root method to restart Shizuku.

### APK

- File: `AutoBrightnessOffset-v1.4.1-release.apk`
- Size: `3,474,839 bytes`
- SHA-256: `91B5700052DECF3BCCBC67B17684CF90DF4B7C2955FCBD9A1AD1799DA472BBD0`
- Version: `1.4.1` (`versionCode=12`, `targetSdk=36`)
- Signature: Android Debug certificate, APK Signature Scheme v2 and v3 verified

This is a debug-signed sideload package for personal testing, not a Google Play production-signed build.
