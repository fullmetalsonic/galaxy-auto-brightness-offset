# 디버그·재발방지 대장

## DBG-001 한글 프로젝트 경로 빌드 차단

- 증상: Android Gradle Plugin 적용 단계에서 빌드 중단
- 재현 방법: 한글이 포함된 Windows 프로젝트 경로에서 Gradle 빌드 실행
- 직접 원인: Android Gradle Plugin의 Windows 비 ASCII 경로 안전검사
- 확인 증거: `Your project path contains non-ASCII characters` 오류
- 영향 범위: 모든 Android 빌드 작업
- 잘못된 기존 접근: 별도 우회 설정 없이 일반 Gradle 실행
- 수정 내용: `android.overridePathCheck=true` 추가
- 자동 재발방지 장치: `scripts/verify.ps1`에서 임시 ASCII 경로 `Q:` 사용
- 재시험 결과: 임시 경로에서 Debug·Release 빌드 성공

## DBG-002 JVM 테스트 클래스 미발견

- 증상: 테스트가 컴파일되지만 `ClassNotFoundException: AdjustmentScaleTest`
- 재현 방법: 한글 원본 경로에서 `testDebugUnitTest` 실행
- 직접 원인: Windows Gradle 테스트 워커 클래스패스가 비 ASCII 프로젝트 경로를 정상 처리하지 못함
- 확인 증거: 테스트 클래스는 `app/build/intermediates`에 존재하지만 런타임에서 찾지 못함. 같은 소스를 `Q:` 임시 경로에서 실행하면 즉시 통과
- 영향 범위: 로컬 JVM 단위시험
- 잘못된 기존 접근: AGP 9 내장 Kotlin 문제로 보고 AGP 8.12.2로 변경했으나 동일 증상 재현
- 수정 내용: 최신 AGP 9.1.1로 복귀하고 검증 스크립트에서 임시 ASCII 경로 사용
- 자동 재발방지 장치: 검증 스크립트가 `Q:` 사용 여부를 확인하고 종료 시 항상 해제
- 재시험 결과: 단위시험 5건, 실패 0건

## DBG-003 Lint 캐시 파일 잠금

- 증상: `lintAnalyzeDebug`가 migrated JAR 접근 거부로 실패하고 `clean`도 같은 파일을 삭제하지 못함
- 재현 방법: AGP 8.12.2 시험 직후 AGP 9.1.1 Lint 실행
- 직접 원인: 이전 Gradle 8.13 데몬과 Kotlin 2.0.21 데몬이 생성된 Lint JAR 핸들을 유지
- 확인 증거: PID 4704 명령행에 `GradleDaemon 8.13`, PID 29404에 `KotlinCompileDaemon 2.0.21` 확인
- 영향 범위: Lint와 생성 산출물 정리
- 잘못된 기존 접근: 현재 Gradle 9 데몬만 중지하여 이전 8.13 데몬이 남음
- 수정 내용: 확인된 이전 데몬 두 개만 종료 후 clean과 Lint 재실행
- 자동 재발방지 장치: 빌드 체인을 AGP 9.1.1·Gradle 9.3.1로 단일화하고 검증 스크립트에서 clean부터 순차 실행
- 재시험 결과: Lint와 전체 검증 성공

## DBG-004 GitHub Actions Android 37 SDK 미발견

- 증상: 첫 Public 푸시의 Android CI가 SDK 설치 단계에서 실패
- 재현 방법: GitHub Ubuntu runner에서 기본 채널로 `platforms;android-37` 설치
- 직접 원인: GitHub runner의 공개 SDK 저장소에 Android 37 플랫폼이 노출되지 않음
- 확인 증거: `Warning: Failed to find package 'platforms;android-37'`
- 영향 범위: GitHub Actions 빌드만 실패하며 게시된 APK와 로컬 검증 결과에는 영향 없음
- 잘못된 기존 접근: 기본 채널 실패 후 `--channel=3` 미리보기 채널만 추가하면 설치될 것으로 판단했으나 같은 오류 재현
- 수정 내용: AAR 메타데이터의 `minCompileSdk`를 확인하여 API 36 호환 최신 안정 조합으로 고정하고 CI도 `platforms;android-36`을 설치하도록 변경
- 자동 재발방지 장치: 공개 안정 채널에서 설치 가능한 SDK만 CI에 사용하고 `checkDebugAarMetadata`를 빌드 과정에서 실행
- 재시험 결과: 로컬 API 36 전체 검증 PASS. GitHub Actions 실행 `32866305890`에서 SDK 설치·단위시험·Lint·Debug 빌드·APK 업로드 모두 PASS

## DBG-005 폴드8 자동 밝기 보정값 쓰기 차단

- 증상: 설정 권한과 자동 밝기가 켜진 상태에서 보정 적용 시 `You cannot change private secure settings.` 오류 표시
- 재현 방법: 폴드8에서 v1.1.0 앱을 실행하고 보정값을 선택한 뒤 적용 버튼 누름
- 직접 원인: `screen_auto_brightness_adj`가 일반 앱이 변경할 수 있는 공개 `Settings.System` 키가 아니며, Fold8 SettingsProvider가 일반 앱의 쓰기 요청을 비공개 시스템 설정 변경으로 차단
- 확인 증거: Fold8 로그의 `warnOrThrowForUndesiredSecureSettingsMutationForTargetSdk` 및 `enforceRestrictedSystemSettingsMutationForCallingPackage` 호출. shell 직접 쓰기는 성공했으나 앱의 `WRITE_SECURE_SETTINGS` 선언·ADB 부여 후에도 같은 차단이 재현됨
- 영향 범위: 핵심 기능인 자동 밝기 보정값 적용이 동작하지 않음. 읽기·복원·재부팅 재적용에 미치는 영향도 실기기 확인 필요
- 잘못된 기존 접근: 일반 `WRITE_SETTINGS` 승인과 PC 빌드·단위시험만으로 폴드8 쓰기 동작이 가능하다고 간주
- 수정 내용: 단순 `WRITE_SECURE_SETTINGS` 추가는 효과가 없어 즉시 제거. 일반 앱 단독 구현 대신 shell 권한 중개 방식 또는 PC 도구 방식의 제품 구조 결정 필요
- 자동 재발방지 장치: 설정 테이블·키·쓰기 결과·재읽기 값과 SettingsProvider 예외를 ADB 시험 절차에 포함하고, 실기기 쓰기 실패를 릴리스 차단 조건으로 유지
- 재시험 결과: FAIL. 앱 직접 쓰기와 ADB 부여 후 쓰기 모두 0.0 유지. `adb shell settings put system screen_auto_brightness_adj 0.1`은 성공했고 시험 후 0.0으로 복원

## DBG-006 Fold8가 저장된 자동 밝기 보정값을 실제 밝기에 반영하지 않음

- 증상: Shizuku 셸 권한으로 `screen_auto_brightness_adj`를 정상 기록해도 화면 밝기가 변하지 않음
- 재현 방법: 같은 조도에서 `+1.0`, `-1.0`, `0.0`을 기록하고 `cmd display get-brightness`와 `dumpsys display` 비교
- 직접 원인: Fold8 One UI의 자동 밝기 컨트롤러 내부 `mAutoBrightnessAdjustment`가 계속 `0.0`이며 SettingsProvider 값과 연결되지 않음
- 확인 증거: 설정 DB에는 요청값이 기록되지만 실제 밝기는 약 `0.293~0.303` 범위의 조도 변화만 보임. 숨김 `setTemporaryAutoBrightnessAdjustment` 호출도 `mAppliedTemporaryAutoBrightnessAdjustment=true`까지 기록되지만 실제 밝기 `0.3337651`은 `+0.2/-0.2`에서 동일
- 영향 범위: 설정 키 쓰기와 AOSP 임시 자동 보정 인자를 사용하는 모든 기존 구현
- 잘못된 기존 접근: 쓰기 성공 또는 내부 적용 플래그를 실제 패널 변화로 간주
- 수정 내용: 해당 경로를 제품 적용에서 제외하고, 삼성 자동 밝기 목표 조회와 임시 패널 밝기 적용을 분리한 추적형 보정 엔진으로 전환
- 자동 재발방지 장치: 실기기 시험에서 설정값뿐 아니라 `mScreenBrightness`, `mTemporaryScreenBrightness`, 밝기 이벤트를 함께 확인
- 재시험 결과: 기존 두 경로는 FAIL로 확정. 시험 후 설정값 `0.00`, 임시 자동 보정 `NaN`, 단기 학습 모델 초기화 상태로 복원

## DBG-007 조도 추적형 임시 밝기 보정 구현

- 증상: 삼성 기본 자동 보정 인자는 무시되지만 사용자는 자동 밝기를 유지한 채 일정 방향의 보정이 필요함
- 재현 방법: 앱의 Shizuku UserService에서 삼성 `cmd display get-ambient-brightness-info <lux>`와 숨김 `DisplayManager.setTemporaryBrightness`를 각각 호출
- 직접 원인: 삼성 자동 밝기 곡선 조회와 임시 패널 밝기 API는 Fold8에서 실제 동작하지만 하나의 공개 API로 결합되어 있지 않음
- 확인 증거: `130 lux → 기본 0.32156864 → +20 보정 0.40222412`가 실제 `mScreenBrightness`에 반영. 약 `135 lux`에서 `+20=0.40222412`, `-20=0.24332996`. 화면 OFF 시 임시값 `NaN`, ON 시 재측정·재적용, 복원 후 시스템 자동값 약 `0.3229`
- 영향 범위: 핵심 적용·복원, 화면 수명주기, 포그라운드 서비스, Shizuku 연결, 알림, 재부팅 재적용
- 잘못된 기존 접근: 상시 추적 없이 보정값을 한 번만 기록하면 One UI가 자동으로 적용할 것으로 가정
- 수정 내용: 조도 센서 → 삼성 기본 곡선 → 감마 보정 → 임시 밝기 적용 파이프라인과 화면 OFF/ON 복구, 명시적 복원, ADB 전용 Debug 시험 Receiver 구현
- 자동 재발방지 장치: 곡선 출력 파서와 감마 보정 JVM 시험, Debug Receiver 실기기 E2E, `scripts/verify.ps1 -IncludeRelease`
- 재시험 결과: Unit 11/11 PASS, Lint PASS, Debug PASS, R8 Release PASS. Fold8 펼친 화면 적용·조도 변화·화면 재점등·UI 적용·복원 PASS. 강제 종료 시 Shizuku 종료 훅이 임시값을 `NaN`으로 복원하고, 앱 재실행 및 APK 업데이트 후 마지막 관리 상태 재개 PASS. 커버 화면·재부팅·Shizuku 자체 강제 중지·영어 UI는 현장 검증 필요

## DBG-009 앱 강제 종료·업데이트 후 임시 밝기 잔류

- 증상: 보정 관리 중 APK를 덮어 설치하면 포그라운드 서비스는 종료되지만 마지막 `mTemporaryScreenBrightness`가 남음
- 재현 방법: `+10` 관리 상태에서 `adb install -r` 또는 `adb shell am force-stop` 실행 후 서비스와 임시 밝기 비교
- 직접 원인: Shizuku UserService의 `destroy()`가 임시 밝기를 지우지 않고 바로 프로세스를 종료
- 확인 증거: 서비스 STOPPED인데 `mTemporaryScreenBrightness=0.3894148`이 남고 자동 밝기 변화가 중단됨
- 영향 범위: 앱 업데이트, 강제 종료, 비정상 앱 프로세스 종료
- 잘못된 기존 접근: Android Service의 `onDestroy()`만으로 모든 종료 경로를 복구할 수 있다고 가정
- 수정 내용: Shizuku UserService `destroy()`에서 임시 패널 밝기와 임시 자동 보정을 먼저 해제. 앱 열기와 `MY_PACKAGE_REPLACED`에서 관리 세션을 재개하도록 Repository·BootReceiver 보강
- 자동 재발방지 장치: 실기기 `force-stop → 임시값 NaN → 앱 재실행 재개`와 `install -r → 관리 서비스 재개` 시험을 릴리스 전 항목에 추가
- 재시험 결과: 강제 종료 후 실제 밝기 `0.32288462`, 임시값 `NaN`; 재실행 후 마지막 `+20` 재개; 동일 APK 덮어 설치 후 `+10` 서비스·임시값 재개 PASS. 마지막 시험은 보정 0, 서비스 STOPPED로 종료

## DBG-008 AIDL 컴파일의 한글 경로 디코딩 실패

- 증상: AIDL 인터페이스 변경 후 `compileDebugAidl`이 `MalformedInputException: Input length = 1`로 실패
- 재현 방법: 한글 프로젝트 경로에서 AIDL 전체 컴파일 실행
- 직접 원인: Android 빌드 도구가 AIDL 의존성 파일의 Windows 비 ASCII 경로를 기본 문자셋으로 잘못 해석
- 확인 증거: 동일 소스를 임시 ASCII 드라이브 `R:` 또는 검증 스크립트의 `Q:`에서 실행하면 즉시 성공
- 영향 범위: AIDL 소스가 변경되거나 clean 후 다시 생성될 때의 Debug·Release 빌드
- 잘못된 기존 접근: AIDL 파일 인코딩 문제로 보고 소스 바이트만 검사
- 수정 내용: 기존 `scripts/verify.ps1`의 ASCII 드라이브 우회를 AIDL 빌드에도 사용
- 자동 재발방지 장치: 전체 검증은 원본 한글 경로에서 Gradle을 직접 호출하지 않고 검증 스크립트로 실행
- 재시험 결과: clean 후 Debug AIDL, Release AIDL, Unit, Lint, Debug, R8 Release 모두 PASS

## DBG-010 슬라이더 끊김과 프리셋 숫자 뒤 중첩 사각형

- 증상: 슬라이더 손잡이 좌우에서 막대가 잘려 보이고, 선택한 프리셋 숫자 뒤에 희미한 사각형 판이 중첩되어 보임. 메인 숫자와 손잡이에는 확정 시안 수준의 후광이 없음
- 재현 방법: v1.3.1을 Fold8 펼친 화면에서 실행하고 보정값 `0`을 선택한 화면을 확정 시안과 나란히 비교
- 직접 원인: Material Slider 기본 트랙이 손잡이 영역을 비우는 구조였고, Material OutlinedButton의 기본 컨테이너·상태 레이어와 별도 배경이 겹침. 발광 요소의 부모도 함께 클리핑되어 넓은 후광을 표시할 여유가 부족함
- 확인 증거: `build-evidence/ui-audit-2026-08-26/02-current-draft-zero-valid.png`에서 막대 공백과 숫자 뒤 중첩 판을 확인하고 `AUDIT.md`에 픽셀 치수 기록
- 영향 범위: 핵심 조절 영역의 시각 완성도, 선택 상태 인지, 슬라이더 연속성
- 잘못된 기존 접근: Material 기본 컴포넌트에 테두리와 배경만 덧씌우면 확정 시안과 같은 결과가 난다고 판단
- 수정 내용: 자체 Canvas 연속 트랙, 64dp 방사형 후광 손잡이, 자체 프리셋 Surface, 메인 숫자 이중 후광으로 교체. 배경 이미지만 별도 클리핑하고 후광 오버레이는 클리핑하지 않음
- 자동 재발방지 장치: 확정 시안과 최신 실기기 캡처를 같은 비교 이미지에 넣는 `design-qa.md` 절차와 7개 프리셋 계약 단위시험 추가
- 재시험 결과: Unit 13/13, Lint, Debug, R8 Release PASS. Fold8 커버 화면 실화면에서 숫자·손잡이 후광, 연속 트랙, 프리셋 중첩 판 제거, 주요 버튼과 하단 영역을 확인하고 `design-qa.md` 최종 판정 PASS
