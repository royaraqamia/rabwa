<#
.SYNOPSIS
    Build a signed release APK for the رَبْوَة Android app.

.DESCRIPTION
    Prompts for the upload keystore password (without echoing it), exports
    the signing environment variables that app/build.gradle.kts reads, and
    runs `gradlew assembleRelease`. Reports the resulting APK and verifies
    that it is signed and carries the expected package.

    The keystore itself is git-ignored; its password is never written to
    disk or to the console.

.PARAMETER Keystore
    Path to the upload keystore. Defaults to my-upload-key.jks at the repo root.

.PARAMETER Clean
    Run `gradlew clean` before building.

.PARAMETER Bundle
    Build an Android App Bundle (.aab) instead of an APK. Use this for
    Google Play uploads.

.PARAMETER NoVerify
    Skip the post-build signature and package verification.

.EXAMPLE
    .\release.ps1
    Prompt for the password, then build a signed release APK.

.EXAMPLE
    .\release.ps1 -Bundle
    Build a signed release App Bundle for Play.

.EXAMPLE
    .\release.ps1 -Clean
    Clean first, then build.
#>
[CmdletBinding()]
param(
    [string]$Keystore,
    [switch]$Clean,
    [switch]$Bundle,
    [switch]$NoVerify
)

$ErrorActionPreference = 'Stop'

$Root     = $PSScriptRoot
$Package  = 'com.royaraqamia.rabwa'
$Alias    = 'upload'

if (-not $Keystore) { $Keystore = Join-Path $Root 'my-upload-key.jks' }
if (-not (Test-Path -LiteralPath $Keystore)) {
    throw "Keystore not found: $Keystore. Run .\make-keystore.ps1 first, or pass -Keystore."
}

Write-Host "Keystore : $Keystore" -ForegroundColor Cyan
Write-Host "Package  : $Package" -ForegroundColor Cyan
Write-Host "Output   : $(if ($Bundle) { 'App Bundle (.aab)' } else { 'APK (.apk)' })" -ForegroundColor Cyan
Write-Host ''

$secure = Read-Host -Prompt "Keystore password (alias '$Alias')" -AsSecureString
$plain  = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
              [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))
if (-not $plain) { throw 'No password supplied.' }

$env:KEYSTORE_PATH  = (Resolve-Path -LiteralPath $Keystore).Path
$env:STORE_PASSWORD = $plain
$env:KEY_PASSWORD   = $plain

$task = if ($Bundle) { 'bundleRelease' } else { 'assembleRelease' }

Push-Location $Root
try {
    if ($Clean) {
        Write-Host "[1/3] gradlew clean" -ForegroundColor Cyan
        & .\gradlew.bat clean --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'gradlew clean failed.' }
    } else {
        Write-Host '[1/3] clean skipped' -ForegroundColor DarkGray
    }

    Write-Host "[2/3] gradlew $task" -ForegroundColor Cyan
    & .\gradlew.bat $task --console=plain
    if ($LASTEXITCODE -ne 0) { throw "gradlew $task failed (exit $LASTEXITCODE)." }
} finally {
    Pop-Location
    Remove-Item Env:STORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:KEY_PASSWORD   -ErrorAction SilentlyContinue
    Remove-Item Env:KEYSTORE_PATH  -ErrorAction SilentlyContinue
}

$artifact = if ($Bundle) {
    Join-Path $Root 'app\build\outputs\bundle\release\app-release.aab'
} else {
    Join-Path $Root 'app\build\outputs\apk\release\app-release.apk'
}

if (-not (Test-Path -LiteralPath $artifact)) {
    throw "Expected artifact not found: $artifact"
}

$file = Get-Item -LiteralPath $artifact
Write-Host ''
Write-Host 'Build succeeded.' -ForegroundColor Green
Write-Host ("Artifact : {0}" -f $file.FullName)
Write-Host ("Size     : {0:N2} MB" -f ($file.Length / 1MB))

if ($NoVerify) { return }

Write-Host ''
Write-Host '[3/3] verifying signature and package' -ForegroundColor Cyan

$apksigner = $null
if ($env:ANDROID_HOME)     { $apksigner = Join-Path $env:ANDROID_HOME 'build-tools\36.1.0\apksigner.bat' }
if (-not $apksigner -or -not (Test-Path -LiteralPath $apksigner)) {
    if ($env:LOCALAPPDATA) {
        $cand = Get-ChildItem -Path (Join-Path $env:LOCALAPPDATA 'Android\Sdk\build-tools') -Filter 'apksigner.bat' -Recurse -ErrorAction SilentlyContinue |
                Sort-Object FullName -Descending | Select-Object -First 1
        if ($cand) { $apksigner = $cand.FullName }
    }
}

if ($apksigner) {
    if ($Bundle) {
        Write-Host '  (App Bundles are signed with jarsigner; apksigner does not apply)' -ForegroundColor DarkGray
        Write-Host '  Verify the upload signature with:' -ForegroundColor DarkGray
        Write-Host '    jarsigner -verify -verbose app\build\outputs\bundle\release\app-release.aab' -ForegroundColor DarkGray
    } else {
        & $apksigner verify --print-certs --verbose $artifact
        if ($LASTEXITCODE -ne 0) { throw 'apksigner verify failed.' }
    }
} else {
    Write-Host '  apksigner not found; skipping signature check.' -ForegroundColor Yellow
}

if (-not $Bundle) {
    $aapt = $null
    if ($env:LOCALAPPDATA) {
        $aapt = Get-ChildItem -Path (Join-Path $env:LOCALAPPDATA 'Android\Sdk\build-tools') -Filter 'aapt2.exe' -Recurse -ErrorAction SilentlyContinue |
                Sort-Object FullName -Descending | Select-Object -First 1
    }
    if ($aapt) {
        $pkg = (& $aapt.FullName dump packagename $artifact 2>$null)
        Write-Host ("Package  : {0}" -f $pkg)
        if ($pkg -ne $Package) {
            Write-Host "  WARNING: package is '$pkg', expected '$Package'." -ForegroundColor Yellow
        } else {
            Write-Host '  Package matches.' -ForegroundColor Green
        }
    }
}

Write-Host ''
Write-Host 'Install on a connected device with:' -ForegroundColor Cyan
Write-Host ("  adb install -r `"{0}`"" -f $file.FullName)
