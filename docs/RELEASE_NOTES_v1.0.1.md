# 자동 밝기 보정 v1.0.1

## 다운로드

[AutoBrightnessOffset-v1.0.1-debug.apk](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.0.1/AutoBrightnessOffset-v1.0.1-debug.apk)

- 크기: 18,608,031 bytes
- SHA-256: `BC5E976E617FFCD6BAE5F453E51607171F0202B06D83492D59B40100751845D3`
- 서명: Android Debug, APK Signature Scheme v2 검증 완료

## v1.0.1 변경사항

- 공개 Android SDK 저장소에서 재현 가능한 API 36 빌드로 전환
- API 36 호환 최신 안정 AndroidX 조합으로 의존성 고정
- GitHub Actions가 안정 채널의 `platforms;android-36`을 사용하도록 수정
- 앱 기능과 UI 동작은 v1.0.0과 동일

## 검증 결과

- Debug·Release unsigned 빌드: PASS
- JVM 단위시험: 5/5 PASS
- Android Lint: 이슈 0건
- R8·리소스 축소: PASS
- 앱 버전 1.0.1·versionCode 2 확인
- APK v2 서명·권한·SHA-256 감사: PASS
- 미해결 CRITICAL/HIGH: 0건

## 현장 검증 필요

갤럭시 Z Fold8 실기기 연결 전 릴리스입니다. One UI의 실제 밝기 반영, 메인·커버 화면, 재부팅, 원래 값 복원 E2E는 [실기기 시험계획](DEVICE_TEST_PLAN.md)에 따라 확인해야 합니다.

이 APK는 실기기 시험을 위해 Debug 인증서로 서명되었습니다. 폴드8 검증 후 일반 배포용 키로 다시 서명해야 합니다.
