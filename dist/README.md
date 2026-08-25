# 설치 산출물

## 자동밝기보정-v1.1.0-debug.apk

- 용도: 갤럭시 Z Fold8 실기기 기능·UI 시험
- 상태: Android Debug 인증서로 서명되어 바로 설치 가능
- 크기: 18,643,383 bytes
- SHA-256: `CBA7064305F51C58054DC52FE5D2BB11F64201A4A6E1C73F22070DC513A1A992`
- 패키지: `com.fullmetalsonic.brightnessoffset`
- minSdk: 26
- targetSdk: 36

이 파일은 배포용 서명이 아닌 Debug 서명입니다. 폴드8 검증이 끝난 뒤 사용자 소유 키로 Release APK 또는 AAB를 서명해야 합니다.

외부 기기에서는 [GitHub Release APK](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.1.0/AutoBrightnessOffset-v1.1.0-debug.apk)를 내려받습니다. Release의 영문 파일명 APK는 이 파일과 바이트 및 SHA-256이 같습니다.

상세 시험은 [폴드8 실기기 시험계획](../docs/DEVICE_TEST_PLAN.md)을 따릅니다.

## Release 빌드

R8·리소스 축소가 적용된 서명 전 Release APK는 다음 위치에 있습니다.

`app/build/outputs/apk/release/app-release-unsigned.apk`

- 크기: 1,140,512 bytes
- SHA-256: `CE0E238D178C080B7302B4A5C1270649B5700BA07BF8B76B137821F06B53DE51`

서명 전 파일이므로 기기 설치용으로 사용하지 않습니다.
