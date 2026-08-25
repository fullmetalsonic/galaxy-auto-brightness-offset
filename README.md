# 자동 밝기 보정

[![Android CI](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/workflows/android.yml/badge.svg)](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/workflows/android.yml)

**[갤럭시 설치용 APK 바로 다운로드](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.0.0/AutoBrightnessOffset-v1.0.0-debug.apk)**

갤럭시의 자동 밝기를 끄지 않고, Android가 계산한 자동 밝기 곡선을 전반적으로 더 밝거나 어둡게 보정하는 로컬 Android 앱입니다.

현재 버전은 **1.0.0**입니다. PC에서 빌드·단위시험·Lint·Release 축소 빌드까지 통과했지만, 갤럭시 Z Fold8의 One UI가 AOSP 보정 키를 실제 화면에 반영하는지는 실기기 검증이 필요합니다.

## 주요 기능

- 보정 강도 `-50~+50`, 5단계 간격
- 권장 프리셋 `-20`, `0`, `+10`, `+20`
- 시스템 설정 변경 권한과 자동 밝기 상태 확인
- 적용 전 원래 값 보존
- 적용 직후 값 재확인
- 앱 사용 전 값으로 복원
- 외부 변경 감지
- 선택형 재부팅 후 재적용
- 기기·설정 진단 정보 복사
- 커버 화면용 1열과 펼친 화면용 2열 반응형 UI
- 네트워크 및 개인정보 권한 미사용

## 보정값의 의미

앱의 `+10`은 화면 밝기 10%를 뜻하지 않습니다. 내부 보정값 `+0.10`을 의미하며 자동 밝기 곡선을 전반적으로 더 밝게 이동시킵니다.

예시:

1. 자동 밝기를 켭니다.
2. 앱에서 `+10`을 선택합니다.
3. `선택한 보정 적용`을 누릅니다.
4. 앱이 `0.10`을 기록하고 다시 읽어 같은 값인지 확인합니다.
5. 실내에서 너무 어두우면 `+15` 또는 `+20`으로 조금씩 올립니다.

야외 고휘도, 발열 감광, 절전 모드, HDR, 앱별 밝기 제한은 시스템이 우선하므로 항상 정확한 고정 비율로 증가하지는 않습니다.

## 설치 및 사용

1. [GitHub Release 설치용 APK](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.0.0/AutoBrightnessOffset-v1.0.0-debug.apk)를 폴드8에 내려받아 설치합니다.
2. 앱을 열고 `시스템 설정 변경`의 `권한 열기`를 누릅니다.
3. Android 설정에서 `자동 밝기 보정`을 허용합니다.
4. `자동 밝기`가 꺼져 있으면 `화면 설정`을 눌러 켭니다.
5. 처음에는 `+10`을 선택하고 적용합니다.
6. 메인 화면과 커버 화면에서 각각 밝기 변화를 확인합니다.
7. 문제가 있으면 `앱 사용 전 값으로 복원`을 누릅니다.

## 빌드와 검증

Windows 한글 경로에서 Gradle 테스트 클래스패스 문제가 발생하지 않도록 검증 스크립트가 임시 `Q:` 드라이브를 사용하고 완료 후 자동 해제합니다.

```powershell
.\scripts\verify.ps1 -IncludeRelease
```

이 명령은 다음을 순서대로 수행합니다.

1. 생성된 빌드 산출물 정리
2. JVM 단위시험
3. Android Lint
4. Debug APK 빌드
5. R8·리소스 축소 Release APK 빌드

Release APK는 서명되지 않았습니다. 실제 배포 전에는 사용자 소유의 안전한 키로 서명해야 합니다.

## 문서

- [문서 색인](docs/INDEX.md)
- [폴드8 실기기 시험 절차](docs/DEVICE_TEST_PLAN.md)
- [개인정보 및 권한](docs/PRIVACY.md)
- [디버그·재발방지 대장](docs/DEBUG_LEDGER.md)
- [누적 작업이력](docs/PROJECT_HISTORY.md)
- [v1.0.0 릴리스 노트](docs/RELEASE_NOTES_v1.0.0.md)
- [인수인계](HANDOVER.md)

## 기술적 한계

앱은 AOSP 내부 설정 키 `screen_auto_brightness_adj`를 `Settings.System`을 통해 사용합니다. 리플렉션이나 접근성 서비스는 사용하지 않습니다. 이 키는 공개 SDK 상수가 아니므로 One UI 업데이트에 따라 무시되거나 다른 값으로 다시 기록될 가능성이 있습니다. 앱은 그 상황을 확인할 수 있도록 적용값 재확인과 외부 변경 감지를 제공합니다.
