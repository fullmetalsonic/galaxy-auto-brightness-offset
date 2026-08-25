# 자동 밝기 보정 v1.0.0

## 다운로드

[AutoBrightnessOffset-v1.0.0-debug.apk](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.0.0/AutoBrightnessOffset-v1.0.0-debug.apk)

- 크기: 18,703,896 bytes
- SHA-256: `6AE4583E1A610FDB8169B9F746E2F6ED4AB2362D2F884F51C2508E00EFA7FD7A`
- 서명: Android Debug, APK Signature Scheme v2 검증 완료

## 주요 기능

- 자동 밝기를 유지한 상태에서 밝기 곡선 보정
- `-50~+50` 보정과 권장 프리셋
- 적용 전 원래 값 보존과 명시적 복원
- 적용값 재확인과 외부 변경 감지
- 선택형 재부팅 후 재적용
- 커버 화면 1열·펼친 화면 2열 반응형 UI
- 밝은/어두운 테마와 Adaptive Icon
- 기기·설정 진단정보 복사
- 네트워크·개인정보 권한 미사용

## 검증 결과

- Debug·Release unsigned 빌드: PASS
- JVM 단위시험: 5/5 PASS
- Android Lint: 이슈 0건
- R8·리소스 축소: PASS
- APK 서명·권한·SHA-256 감사: PASS
- 미해결 CRITICAL/HIGH: 0건

## 현장 검증 필요

갤럭시 Z Fold8 실기기 연결 전 릴리스입니다. One UI의 실제 밝기 반영, 메인·커버 화면, 재부팅, 원래 값 복원 E2E는 [실기기 시험계획](DEVICE_TEST_PLAN.md)에 따라 확인해야 합니다.

이 APK는 실기기 시험을 위해 Debug 인증서로 서명되었습니다. 폴드8 검증 후 일반 배포용 키로 다시 서명할 예정입니다.
