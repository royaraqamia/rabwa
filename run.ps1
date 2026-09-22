<#
.SYNOPSIS
    Build, install, launch and stream logs for the رَبْوَة Android app.

.DESCRIPTION
    CLI replacement for Android Studio's Run button. Builds a debug APK with
    Gradle, installs it on the connected device (BlueStacks, AVD or phone),
    launches the app and streams its logcat output until you press Ctrl+C.

.PARAMETER Device
    adb serial to target (e.g. 127.0.0.1:5555). Auto-detected when omitted.

.PARAMETER NoBuild
    Skip the Gradle build/install step and only launch + stream logs.

.PARAMETER Clean
    Run `gradlew clean` before building.

.PARAMETER ErrorsOnly
    Show only error-level log lines (*:E).

.PARAMETER Snapshot
    Dump the recent log buffer and exit instead of streaming.

.PARAMETER Tail
    Number of lines to show with -Snapshot. Default 200.

.PARAMETER NoClear
    Keep the existing logcat buffer instead of clearing before launch.

.EXAMPLE
    .\run.ps1
    Build, install, launch and follow logs.

.EXAMPLE
    .\run.ps1 -NoBuild -ErrorsOnly
    Relaunch the installed app and show only errors.
#>
[CmdletBinding()]
param(
    [string]$Device,
    [switch]$NoBuild,
    [switch]$Clean,
    [switch]$ErrorsOnly,
    [switch]$Snapshot,
    [int]$Tail = 200,
    [switch]$NoClear
)

$Package  = 'com.royaraqamia.rabwa'
$Activity = 'com.royaraqamia.rabwa.MainActivity'
$Root     = $PSScriptRoot

function Resolve-Adb {
    $candidates = @()
    if ($env:ANDROID_HOME)     { $candidates += Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe' }
    if ($env:ANDROID_SDK_ROOT) { $candidates += Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe' }
    if ($env:LOCALAPPDATA)     { $candidates += Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe' }
    $candidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
}

$Adb = Resolve-Adb
if (-not $Adb) { throw 'adb.exe not found. Install the Android SDK or set ANDROID_HOME.' }

function Get-AdbDevices {
    & $Adb devices 2>$null | Select-Object -Skip 1 | ForEach-Object {
        if ($_ -match '^(\S+)\s+device$') { $Matches[1] }
    }
}

if (-not $Device) {
    $devices = @(Get-AdbDevices)
    if ($devices.Count -eq 0) {
        Write-Host 'No device connected. Trying BlueStacks on 127.0.0.1:5555...'
        & $Adb connect 127.0.0.1:5555 2>$null | Out-Null
        Start-Sleep -Seconds 2
        $devices = @(Get-AdbDevices)
    }
    if ($devices.Count -eq 0) {
        throw 'No device/emulator available. Start BlueStacks or an AVD, then retry.'
    }
    $Device = $devices[0]
    if ($devices.Count -gt 1) {
        Write-Warning "Multiple devices detected; using '$Device'. Pass -Device to pick another."
    }
}

Write-Host "Device : $Device" -ForegroundColor Cyan
Write-Host "Package: $Package" -ForegroundColor Cyan

if ($Clean) {
    Write-Host "`n[1/4] gradlew clean" -ForegroundColor Cyan
    Push-Location $Root
    try { & .\gradlew.bat clean --console=plain } finally { Pop-Location }
}

if (-not $NoBuild) {
    Write-Host "`n[2/4] gradlew installDebug" -ForegroundColor Cyan
    Push-Location $Root
    try { & .\gradlew.bat installDebug --console=plain } finally { Pop-Location }
    if ($LASTEXITCODE -ne 0) { throw 'Build/install failed (gradlew installDebug).' }
} else {
    Write-Host "`n[2/4] skipped (-NoBuild)" -ForegroundColor DarkGray
}

if (-not $NoClear) { & $Adb -s $Device logcat -c 2>$null }

Write-Host "`n[3/4] launching $Activity" -ForegroundColor Cyan
& $Adb -s $Device shell am start -n "$Package/$Activity" | Out-Host
Start-Sleep -Seconds 2

$appPid = (& $Adb -s $Device shell pidof -s $Package 2>$null).Trim()
if (-not $appPid) {
    Write-Host "App process not found - it may have crashed. Recent crash log:" -ForegroundColor Red
    & $Adb -s $Device logcat -d -b crash -t 40
    exit 1
}

$levelFilters = @()
if ($ErrorsOnly) { $levelFilters = @('*:E') }

if ($Snapshot) {
    Write-Host "`n[4/4] logcat snapshot (pid $appPid, last $Tail lines)" -ForegroundColor Cyan
    & $Adb -s $Device logcat -d "--pid=$appPid" -t $Tail @levelFilters
} else {
    Write-Host "`n[4/4] streaming logcat for pid $appPid - Ctrl+C to stop`n" -ForegroundColor Cyan
    & $Adb -s $Device logcat "--pid=$appPid" @levelFilters
}
