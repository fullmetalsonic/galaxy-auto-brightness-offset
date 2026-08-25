# 누적 작업이력

## 2026-08-26 · v1.0.1 재현 가능한 CI 보강

- 공개 SDK 저장소에서 Android 37 플랫폼을 찾지 못해 GitHub Actions가 설치 단계에서 실패한 원인 확인
- AAR 메타데이터를 기준으로 API 36 호환 안정 의존성 조합으로 고정
- 앱 버전을 1.0.1로 올리고 로컬 Debug·Release·단위시험·Lint 재검증
- 새 APK의 버전·권한·v2 서명·SHA-256 재감사
- GitHub Actions 실행 `32866305890`: SDK 설치·단위시험·Lint·Debug 빌드·APK 업로드 PASS
- GitHub 액션 런타임을 현재 안정 주버전으로 갱신하여 Node 20 사용 경고 제거
- v1.0.1 Release 공개 HTTP 200 확인
- Release APK 재다운로드 결과 18,608,031 bytes 및 SHA-256 `BC5E976E617FFCD6BAE5F453E51607171F0202B06D83492D59B40100751845D3`로 로컬 원본과 일치

## 2026-08-26 · GitHub Public 게시

- Public 저장소: `https://github.com/fullmetalsonic/galaxy-auto-brightness-offset`
- 태그·Release: `v1.0.0`
- 설치 APK는 Git 이력에 넣지 않고 GitHub Release 첨부파일로 게시
- GitHub Actions에서 단위시험·Lint·Debug 빌드 재검증
- 공개 전 민감정보 검사와 APK SHA-256 일치 확인

## 2026-08-26 · 최종 PC 검증 및 설치 산출물

- 최신 소스 기준 JVM 단위시험 5건 통과
- Lint 이슈 0건, 새 경고를 오류로 처리하도록 설정
- Debug APK와 R8·리소스 축소 Release APK 빌드 성공
- Debug APK v2 서명 검증 성공
- APK 권한 감사: 시스템 설정 변경·부팅 수신만 선언, 인터넷 권한 없음
- 설치용 Debug APK를 `dist`에 복사하고 SHA-256 기록
- Android Studio 커버 화면·펼친 화면 Preview 추가

## 2026-08-25 · v1.0.0 최초 구현

### 요구사항과 결정

- 자동 밝기를 유지하면서 시스템 계산값을 일정 방향으로 보정
- 진단용 임시 UI가 아닌 처음부터 배포 수준으로 제작
- 숨김 SDK 호출·리플렉션·접근성·오버레이를 사용하지 않음
- 공개 AOSP 내부 설정 키 문자열과 사용자 승인 `WRITE_SETTINGS` 사용
- 정확한 퍼센트가 아니므로 UI에서 `보정 강도`로 표현
- 앱 표시 안전 범위를 `-50~+50`으로 제한
- 적용 전 원래 값 보존과 명시적 복원 제공
- 커버 화면과 펼친 화면을 각각 고려한 1열·2열 UI
- 상시 서비스 없이 선택형 부팅 수신만 사용
- 개인정보와 네트워크 권한 미사용

### 구현

- Kotlin, Jetpack Compose, Material 3
- 기능별 `data`, `domain`, `diagnostics`, `system`, `ui`, `ui/theme` 모듈화
- 설정 권한과 자동 밝기 전제조건 표시
- 보정 적용 후 읽기 재확인
- 외부 변경 감지와 진단 복사
- 원래 값 복원과 재부팅 재적용
- 밝은/어두운 테마 및 Adaptive Icon·단색 아이콘
- Debug 및 축소 Release 빌드 구성

### 검증

- JVM 단위시험 5건 통과
- Android Lint 실행 성공
- Debug APK 빌드 성공
- R8·리소스 축소 Release APK 빌드 성공
- Android 에뮬레이터 이미지와 실기기 미연결로 화면 실행·실제 보정효과는 검증 불가

### 남은 항목

- 폴드8 실기기에서 One UI 설정값 반영 확인
- 메인·커버 화면 기능 및 시각 검증
- 재부팅·외부 변경·원래 값 복원 E2E
- 실기기 화면 확인 후 필요 시 UI 미세조정
- 배포 시 사용자 소유 키로 Release 서명
