<#
.SYNOPSIS
    Build, install, launch, and tail logs for Snitch on a connected physical device.

.DESCRIPTION
    One-command version of BUILD.md's day-to-day loop:
      gradlew.bat assembleDebug -> adb install -r ... -> adb shell am start -> adb logcat

    Requires the one-time setup in BUILD.md (JDK 17+, Android cmdline-tools on PATH,
    ANDROID_HOME set, a phone with USB debugging authorized) to already be done.

.PARAMETER NoLogcat
    Skip tailing logcat after install/launch (useful in CI-ish contexts).
#>

param(
    [switch]$NoLogcat
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Fail($message) {
    Write-Host "ERROR: $message" -ForegroundColor Red
    exit 1
}

Push-Location $scriptDir
try {
    if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
        Fail "adb not found on PATH. Follow BUILD.md's setup steps first."
    }

    $devices = & adb devices | Select-String "\tdevice$"
    if (-not $devices) {
        Fail "No authorized device found. Run 'adb devices' and check USB debugging is enabled/authorized on your phone."
    }

    Write-Host "==> Building debug APK..." -ForegroundColor Cyan
    & "$scriptDir\gradlew.bat" assembleDebug
    if ($LASTEXITCODE -ne 0) {
        Fail "Gradle build failed (exit code $LASTEXITCODE)."
    }

    $apk = Join-Path $scriptDir "app\build\outputs\apk\debug\app-debug.apk"
    if (-not (Test-Path $apk)) {
        Fail "Build succeeded but APK not found at $apk"
    }

    Write-Host "==> Installing on device..." -ForegroundColor Cyan
    & adb install -r $apk
    if ($LASTEXITCODE -ne 0) {
        Fail "adb install failed (exit code $LASTEXITCODE)."
    }

    Write-Host "==> Launching Snitch..." -ForegroundColor Cyan
    & adb shell am start -n com.pwd5018.snitch/.MainActivity | Out-Null

    if ($NoLogcat) {
        Write-Host "==> Done (skipping logcat)." -ForegroundColor Green
        return
    }

    Write-Host "==> Tailing SnitchVpnService logs (Ctrl+C to stop)..." -ForegroundColor Cyan
    & adb logcat -c
    & adb logcat -s SnitchVpnService
}
finally {
    Pop-Location
}
