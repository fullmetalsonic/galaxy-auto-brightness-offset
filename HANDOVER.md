# 인수인계 · 자동 밝기 보정

## 현재 상태

v1.4.0은 Galaxy Z Fold8에서 실제 밝기 보정이 동작합니다. 기존 `screen_auto_brightness_adj` 쓰기 방식은 폐기하고, 공식 Shizuku UserService를 통해 삼성 자동 밝기 곡선을 조회한 뒤 임시 패널 밝기를 적용하는 추적형 엔진으로 전환했습니다. 숫자·슬라이더·프리셋·주요 버튼의 OLED 발광 UI를 보강했고, 래스터 앱 아이콘은 둥근 런처 마스크에서 여유가 생기도록 중심 그래픽을 약 14% 축소했습니다.

현재 Fold8는 사용자가 시험 중이던 `+50` 관리 상태로 되돌려 놓았습니다. v1.4.0 최종 APK가 설치되어 있고 재부팅 후 재적용도 다시 켰습니다.

- 자동 밝기 모드: `1`
- `screen_auto_brightness_adj`: `0.00`
- 임시 자동 보정: `NaN`
- 임시 패널 밝기: 약 `0.5267` (`161 lux` 측정 시)
- 재부팅 후 재적용: `true`
- 관리 서비스: RUNNING

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
- 복원: 서비스 STOPPED, 임시값 `NaN`, 시스템 자동 밝기 약 `0.3229` — PASS

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
- JVM UNIT: PASS, 13/13
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
- Shizuku 자체 강제 중지: 현장 검증 필요
- 영어 UI: PASS, 영어 관리 알림: 현장 검증 필요
- 극저조도·창가·야외 고휘도: 현장 검증 필요

미해결 CRITICAL: 0건. 현장 검증 전 HIGH 위험은 커버 화면 display 전환과 Shizuku 자체 중지 시 임시 밝기 복구입니다.

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
2. 관리 중 Shizuku 앱 자체 중지에서 임시값이 남는지 시험하고 안전 복구를 보강합니다.
3. 한국어·영어 알림 권한과 `보정 해제` 액션을 시험합니다.
4. 어두운 방, 사무실, 창가, 야외에서 보정 체감과 고휘도·발열 제한을 확인합니다.
5. v1.4.0 UI를 펼친 메인 화면에서 추가 시각 QA하고 영어 관리 알림을 확인합니다.

## 빌드 주의사항

한글 프로젝트 경로에서 AIDL 의존성 파일과 JVM 테스트 클래스패스가 깨집니다. 반드시 다음 스크립트를 사용합니다.

```powershell
.\scripts\verify.ps1 -IncludeRelease
```

스크립트는 임시 `Q:` 드라이브를 만들고 종료 시 해제합니다.

## 게시와 발송

- GitHub: `https://github.com/fullmetalsonic/galaxy-auto-brightness-offset`
- 가시성: Public
- 현재 Release: [v1.4.0](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/tag/v1.4.0)
- 기능·문서 태그 커밋: `e68092347e2fc3d4f577913492634efc26d65cc2`
- GitHub Actions: [32923140342](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/runs/32923140342), PASS
- 공개 저장소·한영 설명서·README 이미지·Release: 익명 HTTP 200 확인
- 공개 APK 재다운로드 바이트·SHA-256: 로컬 원본과 일치
- 상세 설명서: `docs/USER_GUIDE_KO.md`, `docs/USER_GUIDE_EN.md`
- 회사 메일: 발송하지 않음

## 로컬 설치 산출물

- 파일: `dist/AutoBrightnessOffset-v1.4.0-release.apk`
- 버전: `1.4.0` (`versionCode=11`, `targetSdk=36`)
- 크기: `3,466,647 bytes`
- SHA-256: `7052CB8EC87544481D6CF9824F8B79D2243FBF901CB45246087814617D8931C9`
- Fold8 설치: PASS
- 설치된 `base.apk`와 로컬 설치파일 SHA-256 일치: PASS
- Release 빌드: R8 최적화 후 Android Debug 인증서 v2·v3 서명, Debug 전용 ADB 시험 Receiver 미포함 확인
- 최적화 Release APK의 Shizuku 사용자 서비스·`+50` 관리 서비스 기동: PASS
