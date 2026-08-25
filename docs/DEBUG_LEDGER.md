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
- 직접 원인: GitHub runner의 기본 안정 채널 목록에 Android 37 플랫폼이 노출되지 않음
- 확인 증거: `Warning: Failed to find package 'platforms;android-37'`
- 영향 범위: GitHub Actions 빌드만 실패하며 게시된 APK와 로컬 검증 결과에는 영향 없음
- 잘못된 기존 접근: 기본 SDK 채널만 조회
- 수정 내용: `sdkmanager --channel=3`으로 미리보기 채널까지 명시
- 자동 재발방지 장치: CI 워크플로에 SDK 채널 인자를 고정
- 재시험 결과: 다음 GitHub Actions 실행에서 확인
