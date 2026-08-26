# v1.4.0 공개 배포 감사

## 범위

- 앱 소스와 리소스
- 한국어·영어 README 및 상세 설명서
- 공개용 실제 기기 스크린샷
- 디버그 서명 APK
- Git 전체 이력
- GitHub Actions와 Release 다운로드

## 개인정보·민감정보 기준

공개 대상에서 다음 항목을 검사합니다.

- 통신사, 시간, 알림 아이콘, 배터리 상태
- 기기 일련번호와 ADB 식별자
- 로컬 사용자명과 작업경로
- 이메일 주소와 회사 정보
- API 키, 토큰, 비밀번호, 키스토어
- 사설 IP와 디버그 원본

README와 설명서 이미지는 실제 Fold8 캡처에서 시스템 상태 표시줄과 내비게이션 표시줄을 잘라냈습니다. 공개 이미지별 처리 내역은 [이미지 대장](images/README.md)에 기록합니다. 원본 캡처와 기기 증거는 `.gitignore`로 제외된 `build-evidence/`에만 보관합니다.

## 빌드와 시험

| 항목 | 결과 |
|---|---|
| Clean | PASS |
| JVM Unit Test | PASS, 13/13 |
| Android Lint | PASS |
| Debug APK | PASS |
| R8·리소스 축소 Release APK | PASS, unsigned |
| 최적화 Release APK 디버그 인증서 서명 | PASS, v2·v3 |
| Fold8 설치 | PASS |
| Shizuku 준비 상태 | PASS |
| 설치 뒤 마지막 `+50` 관리 상태 유지 | PASS |
| 설치 APK와 기기 `base.apk` SHA-256 | PASS, 일치 |
| Release APK의 Debug 전용 Receiver 제거 | PASS |
| 축소 아이콘 실제 삼성 앱 정보 화면 | PASS |

## 공개 APK

- 파일: `AutoBrightnessOffset-v1.4.0-release.apk`
- 크기: `3,466,647 bytes`
- SHA-256: `7052CB8EC87544481D6CF9824F8B79D2243FBF901CB45246087814617D8931C9`
- 빌드·서명: R8 최적화 Release, Android Debug 인증서, APK Signature Scheme v2·v3
- Debug 전용 ADB 시험 Receiver: 미포함

## 공개 후 확인 결과

- GitHub 저장소 가시성: Public, PASS
- 공개 저장소: <https://github.com/fullmetalsonic/galaxy-auto-brightness-offset>
- `v1.4.0` 태그: 기능·문서 커밋 `e68092347e2fc3d4f577913492634efc26d65cc2`, PASS
- GitHub Actions: [run 32923140342](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/actions/runs/32923140342), PASS
- README 한국어·영어 실제 화면: 익명 HTTP 200, PASS
- 한국어·영어 상세 설명서: 익명 HTTP 200, PASS
- Release: <https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/tag/v1.4.0>, 익명 HTTP 200, PASS
- Release APK: 익명 HTTP 200, `3,466,647 bytes`, PASS
- 공개 재다운로드 SHA-256: `7052CB8EC87544481D6CF9824F8B79D2243FBF901CB45246087814617D8931C9`, 로컬 원본과 일치

민감정보 문자열 검사는 현재 공개 대상과 기존 Git 이력에서 모두 일치 항목 0건이었습니다. 커밋 메일은 GitHub `noreply` 주소만 사용합니다.
