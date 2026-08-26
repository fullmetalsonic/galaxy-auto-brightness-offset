# v1.4.1 공개 배포 감사

## 범위

- v1.4.1 앱 소스와 한국어·영어 리소스
- README, 한국어·영어 상세 설명서, 릴리스 노트
- 실제 Fold8 공개용 스크린샷
- R8 최적화·디버그 인증서 서명 APK
- Git 이력, GitHub Actions, Release 다운로드

## 개인정보·민감정보 검사

공개 스크린샷은 실제 Fold8 캡처에서 통신사, 시간, 알림, 배터리와 내비게이션 시스템 영역을 결정론적으로 잘랐습니다. 앱 픽셀은 생성형 편집하지 않았습니다. 원본 ADB 캡처, 기기 식별자와 디버그 로그는 `.gitignore`의 `build-evidence/`에만 보관합니다.

공개 대상에서 다음 항목을 검사합니다.

- 기기 일련번호, ADB 식별자, 로컬 사용자명과 내부 작업경로
- 통신사, 시간, 알림 아이콘과 배터리 상태가 포함된 원본 화면
- 이메일 주소, 회사 정보, 사설 IP
- API 키, 토큰, 비밀번호, 키스토어
- 실제 개인정보와 디버그 원본

공개 이미지별 출처와 처리 내역은 [이미지 대장](images/README.md)에 기록했습니다.

## 빌드·시험·실기기 결과

| 항목 | 결과 |
|---|---|
| Clean | PASS |
| JVM Unit Test | PASS, 17/17 |
| Android Lint | PASS, 이슈 0건 |
| Debug APK | PASS |
| R8·리소스 축소 Release APK | PASS |
| Release 디버그 인증서 서명 | PASS, v2·v3 |
| Debug 전용 ADB Receiver 제거 | PASS, Manifest 일치 0건 |
| Fold8 Shizuku 중지·일시 중지 UI | PASS |
| 중지 상태 반복 `NOT_RUNNING` 오류 | PASS, 6초간 0건 |
| 연결 끊김 중 복원 예약 | PASS |
| Shizuku USB 재시작·앱 열기·자동 복원 | PASS |
| 복원 뒤 임시 밝기 | PASS, `NaN` |
| `+75` 재적용 | PASS, 실제 약 `0.6270` |
| Android 알림 권한 요청 | PASS |
| 관리 알림과 `보정 해제` 액션 생성 | PASS |
| 두 앱 절전 예외 후 Wi-Fi·무선 디버깅 OFF | PASS, 백그라운드 3분과 화면 잠금·재점등 뒤 Shizuku·서비스·`+75` 유지 |
| Wi-Fi OFF 중 Binder 수동 복구 | PASS, Shizuku 열기 뒤 앱 열기로 대기 작업 완료 |
| 최종 Release Fold8 설치 | PASS |
| 설치 `base.apk`와 로컬 APK SHA-256 | PASS, 일치 |

## 공개 APK

- 파일: `AutoBrightnessOffset-v1.4.1-release.apk`
- 크기: `3,474,839 bytes`
- SHA-256: `91B5700052DECF3BCCBC67B17684CF90DF4B7C2955FCBD9A1AD1799DA472BBD0`
- 패키지: `com.fullmetalsonic.brightnessoffset`
- 버전: `1.4.1`, `versionCode=12`, `targetSdk=36`
- 인터넷 권한: 없음
- 서명: Android Debug 인증서, v2·v3

## 공개 후 확인 결과

이 절은 실제 GitHub 푸시, Actions 완료, Release 업로드와 익명 재다운로드 검증 후 최종 기록합니다.
