# 누적 작업이력

## 2026-08-26 · GitHub 검색 키워드 보강

- 자동 밝기를 유지하고 싶지만 화면이 항상 어둡거나 밝게 느껴지는 사용자를 README의 한·영 대상 사용자로 명시
- 사생활 보호필름·프라이버시 필름·저반사 필름·강화유리로 화면이 어둡게 느껴지는 검색 의도를 한·영 자연어로 추가
- 보호필름의 투과율이나 시야각을 바꾸는 기능이 아니라 자동 밝기 결과를 보정한다는 한계 명시
- GitHub 저장소 설명을 `too dark or bright`, `privacy screen protectors` 중심으로 보강
- 사용자 검색어 중심의 GitHub Topics를 추가하고 기존 Android·Samsung·Shizuku 기술 Topics를 유지

## 2026-08-26 · v1.4.0 공개 배포 준비와 아이콘 여백 조정

- 사용자 승인에 따라 기존 Private 저장소를 Public으로 전환하고 v1.4.0 Release를 게시하는 범위 확정
- 래스터 앱 아이콘의 중심 그래픽을 약 14% 축소해 둥근 런처 마스크 안의 안전 여백 확보
- 축소 아이콘이 포함된 APK를 Fold8에 덮어 설치하고 삼성 앱 정보 화면에서 실제 표시 확인
- 재설치 뒤 Shizuku 연결·자동 밝기·기존 `+50` 관리 상태 유지 확인
- 축소 아이콘 Debug APK와 당시 기기 `base.apk` SHA-256 일치 확인
- 통신사·시간·알림·배터리·내비게이션 영역을 제거한 실제 Fold8 한국어·영어 화면을 공개용으로 분리
- README 첫 화면에 한·영 실제 화면과 양언어 제품 설명·다운로드·검증·한계를 구성
- 이미지가 포함된 한국어·영어 15단계 상세 설명서와 Shizuku 공식 링크·무선 디버깅·ADB 절차 작성
- 공개 이미지 대장과 공개 배포 감사 문서 추가
- Debug 전용 ADB 시험 Receiver를 제외한 R8 최적화 Release APK를 Android Debug 인증서로 v2·v3 서명
- 최적화 APK를 Fold8에 설치하고 Shizuku 사용자 서비스·`+50` 보정 관리 서비스 기동, Debug Receiver 미포함, 기기 `base.apk` SHA-256 일치 확인
- 현재 공개 대상과 전체 Git 이력의 기기 식별자·내부 경로·회사 메일·토큰 문자열 검사 PASS, 커밋 메일은 GitHub noreply만 확인
- GitHub Actions `32923140342`의 Verify and build·APK 업로드 포함 전 단계 PASS
- 저장소를 Public으로 전환하고 `e680923`에 `v1.4.0` 태그·Release 생성
- 저장소, 한·영 설명서, 한국어·영어 README 이미지, Release 페이지의 익명 HTTP 200 확인
- 공개 APK 재다운로드 `3,466,647 bytes`, SHA-256 `7052CB8EC87544481D6CF9824F8B79D2243FBF901CB45246087814617D8931C9`로 로컬 원본과 일치

## 2026-08-26 · v1.4.0 프리미엄 UI 디테일 보강

- 사용자 확정 시안을 기준으로 메인 숫자에 파랑·시안 이중 후광과 흰색 선명 코어 적용
- Material Slider 기본 트랙을 자체 Canvas 트랙으로 교체하여 손잡이 좌우의 잘린 간격을 제거
- 슬라이더 손잡이를 28dp 코어와 64dp 방사형 후광 영역으로 구성하고 배경 이미지만 별도로 클리핑하여 후광 잘림 방지
- 프리셋을 `-75/-50/-25/0/+25/+50/+75`의 대칭 7단계로 세분화
- Material 기본 OutlinedButton을 자체 Surface 방식으로 교체하여 숫자 뒤의 희미한 중첩 사각형 제거
- 펼친 화면은 7개 한 줄, 420dp 미만 화면은 4개+3개 두 줄의 반응형 배치 적용
- 적용 버튼을 68dp 높이의 다층 그라데이션·상단 하이라이트·외곽 후광으로 보강하고 비활성 상태도 문구와 형태를 유지
- 적용 중인 값과 선택한 초안 값이 같으면 현재 활성값을 버튼 문구로 표시
- 앱 버전을 `1.4.0` (`versionCode=11`)으로 갱신
- 프리셋 계약 시험을 추가해 Unit 13/13, Lint, Debug, R8·리소스 축소 Release 빌드 PASS
- 새 Debug APK를 Fold8에 덮어 설치하고 `versionName=1.4.0`, Shizuku 및 밝기 관리 서비스 실행 확인
- Fold8 커버 화면 `1248 x 1972`, `420 dpi`에서 기준 시안과 실제 화면을 한 이미지로 비교하여 디자인 QA PASS
- `0`에서 시스템 자동 밝기 `0.33397412`, `+75=0.6143789`, 마지막 `+50=0.5267039` 실동작 PASS
- 재부팅 후 재적용을 `true`로 복귀하고 `+50` 관리 서비스 실행 상태 확인
- 설치 APK와 폰의 `base.apk` SHA-256 일치, Debug v2 서명 PASS
- 로컬 기능 검증용 `AutoBrightnessOffset-v1.4.0-premium-ui-debug.apk` 생성 후, 공개본은 Debug 전용 Receiver를 제외한 R8 최적화 Release APK로 교체

## 2026-08-26 · v1.3.1 보정 범위 확장

- 사용자 실기기 체감 의견에 따라 슬라이더 범위를 `-50~+50`에서 `-100~+100`으로 확장
- Android 감마 보정 입력도 `-1.0~+1.0`으로 넓혀 끝값이 실제로 더 강하게 동작하도록 변경
- 5단계 간격을 유지하고 Compose 슬라이더 내부 눈금을 39개로 조정
- `+100/-100` 표시, 범위 정규화, 최대 강도 보정 결과에 대한 JVM 회귀시험 추가
- `-100`은 저조도에서 화면이 매우 어두워질 수 있으므로 기존 알림의 보정 해제와 앱의 원래 값 복원 경로 유지
- Fold8 약 `186~187 lux`, 삼성 기본값 `0.3372549`에서 `+100=0.6960698`, `-100=0.038359668` 실동작 PASS
- 시험 후 사용자 기존값 `+50=0.5339082`로 복귀하고 관리 서비스 실행 상태 확인
- Unit 12/12, Lint, Debug, R8·리소스 축소 Release 빌드 PASS
- Debug APK v2 서명 검증 및 Fold8 덮어 설치 PASS

## 2026-08-26 · v1.3.0 Fold8 실동작 보정 엔진

- 기존 설정값 쓰기가 성공해도 Fold8 실제 밝기에 반영되지 않는 현상을 ADB로 확정
- Shizuku 셸 권한의 `setTemporaryAutoBrightnessAdjustment`도 내부 적용 플래그만 바뀌고 패널 밝기는 동일함을 확인
- 실제 동작하는 `setTemporaryBrightness`와 삼성 자동 밝기 곡선 조회 명령을 분리 검증
- 조도 센서 → 삼성 기본값 → 감마 보정 → 임시 패널 밝기 적용 파이프라인 구현
- 보정 관리 중 포그라운드 서비스, 화면 OFF 해제, 화면 ON 재측정·재적용, 알림 해제 동작 추가
- 설정 DB와 자동 밝기 학습값을 변경하지 않는 적용·복원 흐름으로 Repository 변경
- Fold8 약 `135 lux`에서 `+20=0.4022`, `-20=0.2433`, 복원 후 시스템 자동값 약 `0.3229` 확인
- 실제 앱 화면의 `+20` 적용 버튼과 `원래 값 복원` E2E PASS
- 관리 중 앱 강제 종료 시 Shizuku 종료 훅으로 임시값 복원, 앱 재실행·APK 덮어 설치 후 마지막 관리 상태 재개 PASS
- 삼성 곡선 파서·감마 보정 시험을 추가하여 JVM 시험 11건 PASS
- clean, Unit, Lint, Debug, R8·리소스 축소 Release PASS
- v1.3.0은 로컬과 연결된 Fold8에만 적용했으며 Private GitHub 저장소에는 게시하지 않음
- 남은 현장시험: 커버 화면 전환, 재부팅, Shizuku 강제 중지, 영어 UI·알림, 극저조도·야외 고휘도

## 2026-08-26 · 실기기 오류 확인과 차기 디자인 확정

- 폴드8 실제 실행에서 `You cannot change private secure settings.` 오류를 확인하고 PC–ADB 실기기 디버깅 필요 상태로 판정
- GitHub 저장소를 사용자 요청에 따라 Private로 전환하고 비인증 공개 직접 다운로드가 차단된 상태를 확인
- 기존 화면의 반복 카드·원형 배지·다중 강조색·가려지는 적용 버튼을 중심으로 UX/UI 감사 수행
- 새 래스터 앱 아이콘 후보 `design/icon-concepts/auto-brightness-icon-premium-v1.png`를 PC 로컬 전용으로 보관
- 최종 UI 기준을 `design/ui-concepts/2026-08-26/06-hybrid-oled-connected-controls.png`로 확정
- 최종 방향은 직관적인 설정 구조에 OLED 곡면 광원, 발광 슬라이더, 연결된 적용 버튼과 활성 토글 효과를 결합
- 아이콘·UI 시안·실기기 화면 자료는 로컬 Git 제외 처리했으며 현재 앱 코드·APK·GitHub에는 미적용
- 다음 PC–Fold8 디버깅에서 기능 원인을 먼저 확정한 뒤 아이콘과 최종 UI를 같은 업데이트에 반영하기로 결정

## 2026-08-26 · v1.1.0 시스템 언어 지원과 GitHub 영문 병기

- 앱 내부 동시 병기 시안을 게시 전에 폐기하고 Android 표준 시스템 언어 선택 방식으로 변경
- 한국어 시스템은 `values-ko`, 영어 및 그 외 시스템 언어는 기본 `values` 리소스 사용
- 앱 이름·화면·버튼·상태·오류·진단 보고서를 한국어와 영어 리소스로 분리
- GitHub README에 영어 제품 설명·검색어·사용 절차·한계를 추가
- Debug·Release 빌드, JVM 단위시험 5건, Lint, R8·리소스 축소 PASS
- APK 리소스 감사에서 기본 라벨 `Auto Brightness Offset`, `ko` 라벨 `자동 밝기 보정` 확인
- GitHub Actions 실행 `32868790614`: SDK 설치·단위시험·Lint·Debug 빌드·APK 업로드 PASS
- v1.1.0 Release 공개 HTTP 200 확인, 재다운로드한 APK의 바이트·SHA-256이 로컬 원본과 일치
- 실제 폴드8 언어 전환 화면과 밝기 반영은 현장 검증 필요

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
