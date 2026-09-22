<#
.SYNOPSIS
    Generate the Android upload keystore for رَبْوَة.

.DESCRIPTION
    Creates my-upload-key.jks at the repository root with the alias and
    filename that app/build.gradle.kts expects. Prompts for the store and
    key passwords without echoing them, so they never reach the console
    history or any transcript.

    The keystore is git-ignored. Back it up along with its passwords:
    losing either means you can never publish an update to this app.

.EXAMPLE
    .\make-keystore.ps1
#>
[CmdletBinding()]
param(
    [string]$DistinguishedName = "CN=Rabwa Upload, OU=Royaraqamia, O=Royaraqamia, C=SA"
)

$ErrorActionPreference = 'Stop'

$Root       = $PSScriptRoot
$Keystore   = Join-Path $Root 'my-upload-key.jks'
$Alias      = 'upload'
# Prefer the JDK that Gradle uses (org.gradle.java.home pins Temurin 21),
# since JAVA_HOME on this machine points at JDK 25.
$Keytool    = $null
$gradleProps = Join-Path $env:USERPROFILE '.gradle\gradle.properties'
if (Test-Path -LiteralPath $gradleProps) {
    $pinned = (Select-String -Path $gradleProps -Pattern '^\s*org\.gradle\.java\.home\s*=\s*(.+)$').Matches
    if ($pinned) {
        $candidate = Join-Path ($pinned[0].Groups[1].Value.Trim() -replace '/', '\') 'bin\keytool.exe'
        if (Test-Path -LiteralPath $candidate) { $Keytool = $candidate }
    }
}
if (-not $Keytool) { $Keytool = Join-Path $env:JAVA_HOME 'bin\keytool.exe' }
if (-not (Test-Path -LiteralPath $Keytool)) {
    $Keytool = (Get-Command keytool -ErrorAction SilentlyContinue).Source
}
if (-not $Keytool -or -not (Test-Path -LiteralPath $Keytool)) {
    throw 'keytool.exe not found. Set JAVA_HOME to a JDK (21 recommended) or add its bin to PATH.'
}

if (Test-Path -LiteralPath $Keystore) {
    throw "Keystore already exists: $Keystore. Delete it first if you really want to regenerate it."
}

Write-Host "Keystore : $Keystore" -ForegroundColor Cyan
Write-Host "Alias    : $Alias" -ForegroundColor Cyan
Write-Host "Keytool  : $Keytool" -ForegroundColor DarkGray
Write-Host ''

$storePass = Read-Host -Prompt 'Keystore password' -AsSecureString
$storePass2 = Read-Host -Prompt 'Confirm keystore password' -AsSecureString
$p1 = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePass))
$p2 = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePass2))
if ($p1 -ne $p2) {
    throw 'Passwords do not match.'
}
if ($p1.Length -lt 6) {
    throw 'Keystore passwords must be at least 6 characters.'
}
$keyPass = $p1
Write-Host 'Using the same password for the key and the keystore.' -ForegroundColor DarkGray

& $Keytool -genkeypair -v `
    -keystore $Keystore `
    -alias $Alias `
    -keyalg RSA -keysize 2048 -validity 10000 `
    -storetype JKS `
    -dname $DistinguishedName `
    -storepass $p1 -keypass $keyPass

if ($LASTEXITCODE -ne 0) {
    throw "keytool failed with exit code $LASTEXITCODE."
}

Write-Host ''
Write-Host 'Keystore created.' -ForegroundColor Green
Write-Host ''
Write-Host 'Certificate fingerprints (register the SHA-1 with Google Sign-In):' -ForegroundColor Cyan
& $Keytool -list -v -keystore $Keystore -alias $Alias -storepass $p1 |
    Select-String -Pattern 'SHA1:|SHA256:|Alias name:|Valid from:'

Write-Host ''
Write-Host 'Next steps:' -ForegroundColor Yellow
Write-Host '  1. Back up my-upload-key.jks and its password somewhere safe.'
Write-Host '  2. Build the release:'
Write-Host ('       $env:KEYSTORE_PATH  = "{0}"' -f $Keystore)
Write-Host '       $env:STORE_PASSWORD = "<your password>"'
Write-Host '       $env:KEY_PASSWORD   = "<your password>"'
Write-Host '       .\gradlew.bat assembleRelease'
Write-Host '  3. Register the SHA-1 above for package com.royaraqamia.rabwa in Google Cloud Console.'
