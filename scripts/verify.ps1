[CmdletBinding()]
param(
    [switch]$IncludeRelease
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$verificationDrive = "Q:"

if (Test-Path "$verificationDrive\") {
    throw "$verificationDrive drive is already in use. Free it or change verificationDrive in this script."
}

& subst.exe $verificationDrive $projectRoot
if ($LASTEXITCODE -ne 0) {
    throw "Failed to create the temporary ASCII-path drive."
}

try {
    Push-Location "$verificationDrive\"
    try {
        $tasks = @("clean", "testDebugUnitTest", "lintDebug", "assembleDebug")
        if ($IncludeRelease) {
            $tasks += "assembleRelease"
        }
        foreach ($task in $tasks) {
            & .\gradlew.bat $task --no-parallel
            if ($LASTEXITCODE -ne 0) {
                throw "Gradle task $task failed with exit code $LASTEXITCODE."
            }
        }
    }
    finally {
        Pop-Location
    }
}
finally {
    & subst.exe $verificationDrive /D
}
