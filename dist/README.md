# 설치 산출물

## 자동밝기보정-v1.0.0-debug.apk

- 용도: 갤럭시 Z Fold8 실기기 기능·UI 시험
- 상태: Android Debug 인증서로 서명되어 바로 설치 가능
- 크기: 18,703,896 bytes
- SHA-256: `6AE4583E1A610FDB8169B9F746E2F6ED4AB2362D2F884F51C2508E00EFA7FD7A`
- 패키지: `com.fullmetalsonic.brightnessoffset`
- minSdk: 26
- targetSdk: 36

이 파일은 배포용 서명이 아닌 Debug 서명입니다. 폴드8 검증이 끝난 뒤 사용자 소유 키로 Release APK 또는 AAB를 서명해야 합니다.

외부 기기에서는 [GitHub Release APK](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.0.0/AutoBrightnessOffset-v1.0.0-debug.apk)를 내려받습니다. Release의 영문 파일명 APK는 이 파일과 바이트 및 SHA-256이 같습니다.

상세 시험은 [폴드8 실기기 시험계획](../docs/DEVICE_TEST_PLAN.md)을 따릅니다.

## Release 빌드

R8·리소스 축소가 적용된 서명 전 Release APK는 다음 위치에 있습니다.

`app/build/outputs/apk/release/app-release-unsigned.apk`

- 크기: 1,118,453 bytes
- SHA-256: `405799293D6C346DA44116389E90840E98B9A5748B413011992DBD99DB75E215`

서명 전 파일이므로 기기 설치용으로 사용하지 않습니다.
