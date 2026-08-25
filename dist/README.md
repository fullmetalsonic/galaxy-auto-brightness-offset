# 설치 산출물

## 자동밝기보정-v1.0.1-debug.apk

- 용도: 갤럭시 Z Fold8 실기기 기능·UI 시험
- 상태: Android Debug 인증서로 서명되어 바로 설치 가능
- 크기: 18,608,031 bytes
- SHA-256: `BC5E976E617FFCD6BAE5F453E51607171F0202B06D83492D59B40100751845D3`
- 패키지: `com.fullmetalsonic.brightnessoffset`
- minSdk: 26
- targetSdk: 36

이 파일은 배포용 서명이 아닌 Debug 서명입니다. 폴드8 검증이 끝난 뒤 사용자 소유 키로 Release APK 또는 AAB를 서명해야 합니다.

외부 기기에서는 [GitHub Release APK](https://github.com/fullmetalsonic/galaxy-auto-brightness-offset/releases/download/v1.0.1/AutoBrightnessOffset-v1.0.1-debug.apk)를 내려받습니다. Release의 영문 파일명 APK는 이 파일과 바이트 및 SHA-256이 같습니다.

상세 시험은 [폴드8 실기기 시험계획](../docs/DEVICE_TEST_PLAN.md)을 따릅니다.

## Release 빌드

R8·리소스 축소가 적용된 서명 전 Release APK는 다음 위치에 있습니다.

`app/build/outputs/apk/release/app-release-unsigned.apk`

- 크기: 1,104,444 bytes
- SHA-256: `C3339748C49E3B954607CB81E9A1814EF0175195967E05BCD30A5BF06F04A6CC`

서명 전 파일이므로 기기 설치용으로 사용하지 않습니다.
