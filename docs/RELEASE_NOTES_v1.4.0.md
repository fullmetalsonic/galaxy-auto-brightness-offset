# Auto Brightness Offset v1.4.0

Galaxy Z Fold8 실기기에서 기능과 커버 화면 UI를 검증한 공개 배포 버전입니다.

## 주요 변경

- Shizuku 사용자 서비스로 삼성 자동 밝기 목표를 조회하고 임시 밝기 오버라이드 적용
- 조도 구간 변경, 화면 OFF/ON, 서비스 재시작에 대응하는 보정 관리 서비스
- 보정 범위를 `-100~+100`, 5단계 간격으로 확장
- 프리셋을 `-75`, `-50`, `-25`, `0`, `+25`, `+50`, `+75`로 세분화
- 발광 숫자, 연속 슬라이더, 클리핑 없는 손잡이 후광, 다층 적용 버튼 반영
- 프리셋 숫자 뒤에 보이던 중첩 사각형 제거
- 래스터 앱 아이콘의 중심 그래픽을 약 14% 축소해 런처 안전 여백 확보
- 시스템 언어 기반 한국어·영어 UI
- 한국어·영어 상세 설명서와 개인정보 제거 실제 화면 이미지 추가

## 실제 검증

- JVM Unit Test: PASS, 13/13
- Android Lint: PASS
- Debug APK: PASS
- R8·리소스 축소 Release APK: PASS, unsigned
- Fold8 커버 화면 `1248 × 1972`, `420 dpi`: 시각 QA PASS
- `0`: 임시 오버라이드 해제, 실제 자동 밝기 약 `0.3340`
- `+75`: 약 `161 lux`, 삼성 기본 `0.3294`에서 실제 `0.6144`
- `+50` 복귀: 실제 `0.5267`
- 재부팅 후 Shizuku 재시작·앱 재실행: 마지막 설정 복귀 확인
- 축소 아이콘 APK 설치 후 삼성 앱 정보 화면 표시: PASS
- 최적화 Release APK Fold8 설치·Shizuku 사용자 서비스·보정 관리 서비스 기동: PASS
- 설치 Release APK와 기기 `base.apk` SHA-256: 일치

## 설치 파일

- 파일: `AutoBrightnessOffset-v1.4.0-release.apk`
- 크기: `3,466,647 bytes`
- SHA-256: `7052CB8EC87544481D6CF9824F8B79D2243FBF901CB45246087814617D8931C9`
- 빌드·서명: R8 최적화 Release, Android Debug 인증서, APK Signature Scheme v2·v3 PASS
- Debug 전용 ADB 시험 Receiver: 미포함 확인

이 APK는 개인 테스트·사이드로드용 디버그 서명 빌드입니다. Google Play 배포용 서명 빌드는 아닙니다.

## 설치 전 필수 사항

1. 공식 [Shizuku](https://shizuku.rikka.app/download/) 설치
2. 공식 [Shizuku 시작 안내](https://shizuku.rikka.app/guide/setup/)에 따라 서비스 시작
3. 삼성 자동 밝기 활성화
4. 앱 첫 실행에서 Shizuku 권한 허용

자세한 절차는 [한국어 설명서](USER_GUIDE_KO.md) 또는 [English guide](USER_GUIDE_EN.md)를 참고하십시오.

## 알려진 한계와 남은 현장 검증

- 삼성 전용 명령과 Android 비공개 임시 밝기 API에 의존
- 다른 제조사와 향후 One UI 업데이트에서 호환되지 않을 수 있음
- 비루트 Shizuku는 재부팅 후 수동으로 다시 시작해야 함
- 펼친 메인 화면 v1.4.0 시각 QA, 영어 관리 알림, 연속 접기·펼치기, Shizuku 강제 중지, 극저조도·야외 고휘도는 추가 실기기 회귀시험 대상
