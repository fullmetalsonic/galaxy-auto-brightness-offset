# 인수인계 · 자동 밝기 보정

## 현재 상태

### 2026-08-27 UI 공개 자료 후속 점검

- USB를 분리한 무선 ADB 단독 연결로 설치 APK를 읽었으며 공개 v1.4.1과 SHA-256 일치
- UI 소스·APK는 이미 공개되어 있었고, v1.4.0에 머문 디자인 QA와 적용 전/중 화면 설명을 보완
- 한·영 적용/선택/설정과 기존 복원 대기 화면 7개의 자르기 좌표·원본/출력 해시·픽셀 동일성 검사 추가
- 현재 사용자 상태: `+75` 적용 중, 재부팅 선택 OFF, 시스템 언어 추종. 확인·촬영 후 이 상태를 유지
- 무선 디버깅 ON, Wi-Fi ON, USB 케이블 분리. 사설 주소·포트·페어링 코드는 공개 기록에 남기지 않음
- APK·버전·기능 태그는 그대로 유지하며 README·한영 설명서·디자인 QA·Release 설명만 갱신
- 공개 반영 완료: `d86959b`, [Android CI 33037733219](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/runs/33037733219) PASS. 공개 이미지 7개 해시·현재 문서·Release 본문 일치와 페이지 접근 확인. 상세는 [배포 감사](docs/PUBLICATION_AUDIT_v1.4.1.md) 참조

### 2026-08-26 배포 직후 상태 (이력 보존)

v1.4.1은 Galaxy Z Fold8에서 실제 밝기 보정과 Shizuku 연결 중단·복원 흐름이 동작합니다. 공식 Shizuku UserService로 삼성 자동 밝기 곡선을 조회해 임시 패널 밝기를 적용하고, Shizuku가 중지되면 센서 추적을 멈추고 `보정 일시 중지`로 전환합니다. 연결이 끊긴 상태의 복원 요청은 예약되며 Shizuku를 시작하고 앱을 열면 자동 완료됩니다.

현재 Fold8는 사용자가 선택해 둔 `+50` 관리 상태입니다. Shizuku v13.6.0을 USB ADB로 시작했고, v1.4.1 최종 Release APK를 설치했습니다. Android 알림 권한을 허용했으며 재부팅 후 재적용도 다시 켰습니다. Shizuku와 자동 밝기 보정은 삼성 배터리 제한 예외 및 active standby bucket 상태입니다.

- 자동 밝기 모드: `1`
- `screen_auto_brightness_adj`: `0.00`
- 임시 자동 보정: `NaN`
- 임시 패널 밝기: 약 `0.5446` (삼성 기본 약 `0.3484`, `+50`)
- 재부팅 후 재적용: `true`
- 관리 서비스: RUNNING
- Shizuku: v13.6.0, USB ADB 시작, `shizuku_server` PID `18637`
- 시험 종료 상태: Wi-Fi ON, 무선 디버깅 OFF, USB 디버깅 ON
- 알림 권한: 허용, 관리 알림 표시 확인
- 배터리 제한: Shizuku·자동 밝기 보정 모두 device-idle whitelist, standby bucket `5(active)`

## 실제 동작 구조

1. 앱 포그라운드 서비스가 `TYPE_LIGHT` 센서의 lux를 읽습니다.
2. Shizuku 셸 프로세스가 `cmd display get-ambient-brightness-info <lux>`를 실행합니다.
3. 삼성 기본 밝기와 어두워짐·밝아짐 lux 경계를 파싱합니다.
4. `BrightnessCompensation`이 Android 방식의 최대 감마 `3.0`으로 `-1.00~+1.00` 보정을 계산합니다.
5. 숨김 `DisplayManager.setTemporaryBrightness(0, value)`를 Shizuku 셸 프로세스에서 호출합니다.
6. lux가 현재 경계를 벗어나면 다시 계산합니다.
7. 화면 OFF, 사용자 복원, 관리 알림 해제 시 임시 밝기를 `NaN`으로 지웁니다.

설정 DB와 자동 밝기 단기 학습값은 변경하지 않습니다.

## 핵심 실기기 증거

- `setTemporaryAutoBrightnessAdjustment(+0.2/-0.2)`: 내부 적용 플래그는 true지만 실제 밝기 `0.3337651`로 동일 — FAIL
- 약 `135 lux`, 삼성 기본 약 `0.3216`
  - `+20` → `mScreenBrightness=0.40222412`
  - `-20` → `mScreenBrightness=0.24332996`
- 조도 `136 → 42 lux`: 기본값 `0.3216 → 0.2745`, `+20` 결과 `0.4022 → 0.3542`로 재계산 — PASS
- 화면 OFF: 임시값 해제 — PASS
- 화면 ON: 새 lux 읽기 및 재적용 — PASS
- 실제 UI `+20` 선택·적용 → `원래 값 복원` — PASS
- v1.4.0 커버 화면 `0 → +75 → +50` 실제 적용 — PASS
- 약 `161 lux`, 삼성 기본 `0.3294`에서 `+75=0.6144`, `+50=0.5267` — PASS
- USB ADB로 Shizuku 시작 후 무선 디버깅 OFF·Wi-Fi OFF: `193 lux`, 기본 `0.3373`, `+75=0.6208` — PASS
- USB·무선 디버깅 ON 상태에서 Wi-Fi만 OFF: Shizuku PID 유지, 앱 재실행 후 `188 lux`, `+75=0.6208` — PASS
- 복원: 서비스 STOPPED, 임시값 `NaN`, 시스템 자동 밝기 약 `0.3229` — PASS
- Shizuku 중지: 센서 추적 중단, `보정 일시 중지`, 6초간 반복 `NOT_RUNNING` 오류 0건 — PASS
- Shizuku 없는 상태에서 복원: `pending_restore=true`, `원래 값 복원 대기` — PASS
- USB로 Shizuku 재시작 후 앱 열기: 예약 복원 완료, 서비스 STOPPED, 임시값 `NaN` — PASS
- v1.4.1 `+75` 재적용: `239 lux`, 기본 `0.3451`, 실제 `0.6270` — PASS
- Wi-Fi OFF 뒤 약 2~3분 후 보정 해제 재현: Shizuku PID는 유지됐지만 Samsung Freecess로 Binder 전달 중단 — 원인 확인
- 두 앱 절전 예외 후 Wi-Fi OFF 화면 켠 백그라운드 0~180초: PID `18637`, 임시 밝기 `0.63016194` 유지 — PASS
- 화면 잠금에서 임시값 `NaN`, 재점등 후 `0.63016194` 재적용 및 조도 변화에 따른 `0.61115956` 재계산, 180초 유지 — PASS
- 최종 Release 재설치 뒤 로컬·기기 APK SHA-256 `91B570...2BBD0` 일치, `+50` 실제 `0.5445832` — PASS

## 코드 구조

- `domain/AdjustmentScale.kt` 계열: 보정 범위와 표시
- `domain/BrightnessCompensation.kt`: 감마 보정 계산
- `shizuku/AutomaticBrightnessCurveCommand.kt`: 삼성 곡선 명령과 출력 파서
- `shizuku/TemporaryBrightnessController.kt`: 숨김 DisplayManager 호출
- `shizuku/PrivilegedBrightnessService.kt`: Shizuku UserService AIDL 구현
- `system/BrightnessManagementService.kt`: 조도 추적 포그라운드 서비스
- `data/BrightnessRepository.kt`: 적용·복원·재적용 상태 관리
- `src/debug/.../DeviceTestReceiver.kt`: DUMP 권한을 요구하는 ADB 전용 실기기 시험 진입점. Release에는 미포함

## 현재 검증 결과

- BUILD Debug: PASS
- JVM UNIT: PASS, 17/17
- Android Lint: PASS
- BUILD Release unsigned + R8/resource shrink: PASS
- Fold8 펼친 화면 기능 E2E: PASS
- Fold8 커버 화면 v1.4.0 UI·프리셋 E2E: PASS
- 기준 시안 대 커버 화면 디자인 QA: PASS
- 앱 전용 영어 로케일 메인·하단 화면 시각 확인: PASS
- 축소 래스터 아이콘 Fold8 설치·삼성 앱 정보 표시: PASS
- 화면 OFF/ON E2E: PASS
- 실제 UI 적용·복원 E2E: PASS
- 앱 강제 종료 안전 복원·재실행 재개: PASS
- APK 덮어 설치 후 관리 서비스 재개: PASS
- 접기·펼치기 연속 전환: 현장 검증 필요
- 재부팅 후 Shizuku 수동 재시작·앱 열기·마지막 값 재적용: PASS
- Shizuku 중지 감지·일시 중지 UI·반복 오류 차단: PASS
- Shizuku 중지 중 복원 예약·재연결 자동 복원: PASS
- 두 앱 절전 예외 적용 후 USB ADB 시작·Wi-Fi 없는 야외 운용: PASS(백그라운드 3분 및 화면 잠금·재점등 범위)
- Android 알림 권한 요청·한국어 관리 알림: PASS, 영어 관리 알림: 현장 검증 필요
- 극저조도·창가·야외 고휘도: 현장 검증 필요

미해결 CRITICAL/HIGH: 0건. 커버 화면 display 전환, 영어 관리 알림과 극저조도·야외 고휘도는 추가 현장 검증 대상입니다.

## 적용된 디자인 기준

- UI 기준: `design/ui-concepts/2026-08-26/06-hybrid-oled-connected-controls.png`
- 아이콘 원본: `app/src/main/res/drawable-nodpi/launcher_art.png`
- 기준 UI 시안과 비교용 원본 이미지는 PC 로컬 전용이며 Git 제외 상태
- 앱에 적용된 아이콘은 래스터 PNG이고, 중심 그래픽을 약 14% 축소한 최종본
- 숫자 이중 후광, 64dp 후광 영역의 슬라이더 손잡이, 끊김 없는 자체 트랙, 입체형 적용 버튼을 v1.4.0에 적용
- 프리셋은 `-75/-50/-25/0/+25/+50/+75`로 세분화하고 Material 기본 버튼에서 생기던 숫자 뒤 중첩 사각형을 제거
- 420dp 미만에서는 4개+3개 2줄, 펼친 화면에서는 7개 한 줄로 배치

## 다음 작업

1. Fold8를 접고 펼치며 커버·메인 물리 패널의 logical display ID와 실제 밝기를 기록합니다.
2. 영어 관리 알림과 알림의 `Clear offset` 액션을 실기기에서 확인합니다.
3. 어두운 방, 사무실, 창가, 야외에서 보정 체감과 고휘도·발열 제한을 확인합니다.
4. 더 긴 야외 운용에서 Samsung 절전 예외가 유지되는지 장시간 관찰합니다.

## 빌드 주의사항

한글 프로젝트 경로에서 AIDL 의존성 파일과 JVM 테스트 클래스패스가 깨집니다. 반드시 다음 스크립트를 사용합니다.

```powershell
.\scripts\verify.ps1 -IncludeRelease
```

스크립트는 임시 `Q:` 드라이브를 만들고 종료 시 해제합니다.

## 게시와 발송

- GitHub: `https://github.com/fullmetalsonic/galaxy-auto-brightness-offset`
- 가시성: Public
- 현재 Release: [v1.4.1](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/tag/v1.4.1) 공개 완료
- v1.4.1 기능·문서 태그 커밋: `e4de65679da8241427833c9c02ef8e4cec824254`
- GitHub Actions: [32945280941](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/runs/32945280941), PASS
- 공개 저장소·한영 설명서·README 이미지·Release: 인증 없이 HTTP 200 확인
- 공개 APK 재다운로드: `3,474,839 bytes`, SHA-256 `91B570...2BBD0`, 로컬 원본과 일치
- 상세 설명서: `docs/USER_GUIDE_KO.md`, `docs/USER_GUIDE_EN.md`
- 회사 메일: 발송하지 않음

## 로컬 설치 산출물

- 파일: `dist/AutoBrightnessOffset-v1.4.1-release.apk`
- 버전: `1.4.1` (`versionCode=12`, `targetSdk=36`)
- 크기: `3,474,839 bytes`
- SHA-256: `91B5700052DECF3BCCBC67B17684CF90DF4B7C2955FCBD9A1AD1799DA472BBD0`
- Fold8 설치: PASS
- 설치된 `base.apk`와 로컬 설치파일 SHA-256 일치: PASS
- Release 빌드: R8 최적화 후 Android Debug 인증서 v2·v3 서명, Debug 전용 ADB 시험 Receiver 미포함 확인
- 최적화 Release APK의 Shizuku 사용자 서비스·`+50` 관리 서비스 기동: PASS
