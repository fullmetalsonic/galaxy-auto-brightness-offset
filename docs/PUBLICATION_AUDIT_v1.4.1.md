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

## 2026-08-27 UI 공개 자료 후속 검증

- 범위: README, 한·영 상세 설명서, 디자인 QA, 이미지, Release 설명 및 문서 회귀검사. 앱 소스·APK·태그는 변경하지 않음
- USB 분리 후 무선 ADB 단독으로 설치 APK를 읽어 아래 공개 APK 해시와 동일함을 재확인
- 한·영 적용/선택/설정 및 기존 복원 대기 화면 총 7개: 원본 지정 영역과 RGBA 픽셀 차이 0, PNG 해시·규격 검사 PASS
- 비교기 음성시험: 합성 RGBA 입력의 1픽셀 변경을 차이 1로 탐지 PASS
- 공개 CI 검사: 비공개 원본 없이 PNG 해시·규격, 앱 버전과 현행 문서 제목을 비교하도록 추가. 로컬 실행 PASS
- 변경 문서의 로컬 링크·이미지 73개 존재 확인 PASS. 변경 공개 텍스트의 사설 주소·로컬 사용자 경로·토큰·개인 키·페어링 정보 패턴 검사 검출 0건
- 시각·사용성 검토: 한·영 선택 화면의 초안/적용값과 버튼 상태, 설정 화면의 OFF 상태·복원·진단 안내 확인. 앱 코드·버전 변경 0건
- 사용 중인 +75 보정, 재부팅 재적용 OFF, 시스템 언어 추종을 보존. 확인 중 서버 중지·보정 재적용·앱 재설치 없음
- 새로운 장시간 야외·HDR·접힘/펼침 시험은 수행하지 않음. 기존 시험 결과와 한계를 그대로 유지
- UI 자료 커밋: `d86959b3fa1e488d07d73356ae7857ed26bde0b3`, Public 저장소 main 반영 완료
- 신규 [Android CI 33037733219](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/runs/33037733219): 공개 UI 자료 검사, `testDebugUnitTest`, `lintDebug`, `assembleDebug`, APK 업로드 모두 PASS
- 인증 없는 공개 다운로드: 이미지 7개의 SHA-256 일치, main README·디자인 QA·한영 상세 설명서 내용 일치 PASS
- 저장소·Release·한영 상세 설명서 페이지 HTTP 200. Release 본문과 로컬 한영 릴리스 노트 일치 PASS
- 기능 태그는 기존 `e4de65679da8241427833c9c02ef8e4cec824254` 유지, Release APK 크기·digest 변경 없음
- 후속 검증 결과만 기록하는 문서 커밋은 `[skip ci]`로 게시. 이미 통과한 앱·CI 스크립트·이미지에는 추가 변경 없음

## 공개 APK

- 파일: `AutoBrightnessOffset-v1.4.1-release.apk`
- 크기: `3,474,839 bytes`
- SHA-256: `91B5700052DECF3BCCBC67B17684CF90DF4B7C2955FCBD9A1AD1799DA472BBD0`
- 패키지: `com.fullmetalsonic.brightnessoffset`
- 버전: `1.4.1`, `versionCode=12`, `targetSdk=36`
- 인터넷 권한: 없음
- 서명: Android Debug 인증서, v2·v3

## 공개 후 확인 결과

- 저장소 가시성: **Public**
- 기능·문서 태그 커밋: `e4de65679da8241427833c9c02ef8e4cec824254`
- GitHub Actions: [Android CI 32945280941](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/runs/32945280941), `verify` PASS
- 태그·Release: [v1.4.1](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/tag/v1.4.1), Draft 아님, Prerelease 아님
- Release 자산: `AutoBrightnessOffset-v1.4.1-release.apk`, `3,474,839 bytes`
- GitHub 자산 digest: `sha256:91b5700052decf3bccbc67b17684cf90df4b7c2955fcbd9a1ad1799da472bbd0`
- 인증 없이 재다운로드한 APK와 로컬 원본 SHA-256: PASS, 일치
- 저장소, Release, 한국어·영어 설명서, README 한국어·영어 이미지: HTTP 200
- GitHub 검색 Topics: 자동 밝기, 화면 밝음·어두움, Samsung Galaxy, One UI, Shizuku, privacy screen protector 관련 20개 유지 확인
- 공개 후 민감정보 검사: 기기 일련번호·로컬 경로·사설 IP·자격증명 검출 0건
