# 인수인계 · 자동 밝기 보정

## 현재 상태

갤럭시 Z Fold8용 자동 밝기 보정 앱 v1.1.0의 코드·UI·아이콘·권한·복원·진단·문서가 구현되었습니다. 한국어 시스템에서는 한국어, 그 외 시스템 언어에서는 영어 UI를 사용합니다. PC 검증은 통과했고 실제 폴드8의 One UI 반영 여부는 미검증입니다.

## 핵심 계약

- 시스템 키: `screen_auto_brightness_adj`
- Android 내부 허용 범위: `-1.0~+1.0`
- 앱 노출 안전 범위: `-0.50~+0.50`
- 앱 표시: 내부값 × 100을 `보정 강도`로 표시하며 퍼센트로 표현하지 않음
- 적용 성공: `Settings.System.putFloat` 성공 후 읽은 값이 허용 오차 안에서 요청값과 일치
- 원본 보존: 첫 성공 적용 직전 값을 세션 원래 값으로 저장
- 복원: 원래 값 쓰기와 읽기 재확인 성공 후에만 세션 삭제
- 재부팅: 사용자가 선택하고 관리 세션이 활성화된 경우에만 한 번 재적용
- 언어: Android 표준 리소스 선택 사용. `values-ko`는 한국어, 기본 `values`는 영어

## 코드 구조

- `domain`: 값 범위, 표시, 결과 모델
- `data`: 시스템 설정과 앱 전용 상태 저장
- `diagnostics`: 복사용 진단 보고서
- `system`: 재부팅 수신기
- `ui`: 상태 관리와 반응형 Compose 화면
- `ui/theme`: 제품 색상·타이포그래피·시스템 바

## 현재 검증 결과

- BUILD Debug: PASS
- BUILD Release unsigned: PASS
- UNIT TEST: PASS, 5/5
- LINT: PASS
- R8/RESOURCE SHRINK: PASS
- REQUIREMENT CHECK: PASS, 실기기 항목 제외
- REGRESSION CHECK: PASS, 최초 버전의 자동시험 범위
- REVIEW: PASS, 소스 정적 검토 범위
- GITHUB ACTIONS: PASS, 실행 `32868790614`
- RELEASE DOWNLOAD: v1.1.0 게시 후 확인 예정
- 미해결 CRITICAL/HIGH: 0건
- 폴드8 실제 화면 밝기: 현장 검증 필요
- 실제 UI 화면 캡처: 검증 불가, 에뮬레이터 이미지 및 기기 미연결

설치용 산출물은 `dist/자동밝기보정-v1.1.0-debug.apk`이며 SHA-256은 `CBA7064305F51C58054DC52FE5D2BB11F64201A4A6E1C73F22070DC513A1A992`입니다.

## 다음 작업

1. 폴드8을 ADB로 연결합니다.
2. [실기기 시험계획](docs/DEVICE_TEST_PLAN.md)의 T1~T7을 순서대로 실행합니다.
3. 각 시험의 앱 표시값, ADB 값, 커버·메인 화면 결과를 기록합니다.
4. 실패 시 [디버그 대장](docs/DEBUG_LEDGER.md)에 증상부터 추가하고 원인 확인 전 추측 수정하지 않습니다.
5. 실기기 PASS 후 사용자 소유 서명키로 Release APK/AAB를 생성합니다.

## 빌드 주의사항

원본 폴더에 한글이 포함되어 JVM 테스트가 직접 실행되지 않을 수 있습니다. 반드시 다음 스크립트를 사용합니다.

```powershell
.\scripts\verify.ps1 -IncludeRelease
```

스크립트가 사용하는 `Q:`가 이미 사용 중이면 다른 빈 드라이브 문자로 스크립트의 `$verificationDrive`를 변경해야 합니다.

## 게시와 발송

- GitHub: `https://github.com/fullmetalsonic/galaxy-auto-brightness-offset`
- 저장소 가시성: Public
- Release: `https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/tag/v1.1.0`
- 직접 다운로드: `https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.1.0/AutoBrightnessOffset-v1.1.0-debug.apk`
- 회사 메일: 발송하지 않음
