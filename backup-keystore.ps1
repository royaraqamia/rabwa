<#
.SYNOPSIS
    Back up the رَبْوَة upload keystore to a location outside the repo.

.DESCRIPTION
    Copies my-upload-key.jks to a destination you choose and verifies the copy
    byte-for-byte via SHA-256. The password is never read, written, or echoed
    here - keep it in a password manager, because losing either the file or the
    password means the app can never be updated again.

.PARAMETER To
    Destination directory or file path. If it points at an existing directory
    the original filename is kept; otherwise it is treated as the destination
    file path (missing parent directories are created).

.PARAMETER Keystore
    Path to the upload keystore. Defaults to my-upload-key.jks at the repo root.

.EXAMPLE
    .\backup-keystore.ps1 -To E:\backups\rabwa
    Copy the keystore into a directory.

.EXAMPLE
    .\backup-keystore.ps1 -To D:\keys\rabwa-upload-2026-09-22.jks
    Copy the keystore to an explicit filename.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$To,
    [string]$Keystore
)

$ErrorActionPreference = 'Stop'

$Root = $PSScriptRoot
if (-not $Keystore) { $Keystore = Join-Path $Root 'my-upload-key.jks' }
if (-not (Test-Path -LiteralPath $Keystore -PathType Leaf)) {
    throw "Keystore not found: $Keystore. Pass -Keystore, or run .\make-keystore.ps1 first."
}

if ((Resolve-Path -LiteralPath $Keystore).Path -eq (Join-Path $Root 'my-upload-key.jks')) {
    Write-Host 'Tip: keep the backup off this machine - the whole point is surviving disk loss.' -ForegroundColor DarkGray
}

$dest = $To
if (Test-Path -LiteralPath $To -PathType Container) {
    $dest = Join-Path $To (Split-Path -Leaf $Keystore)
}

$destDir = Split-Path -Parent $dest
if ($destDir -and -not (Test-Path -LiteralPath $destDir)) {
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
}

Copy-Item -LiteralPath $Keystore -Destination $dest -Force

$srcHash = (Get-FileHash -LiteralPath $Keystore -Algorithm SHA256).Hash
$dstHash = (Get-FileHash -LiteralPath $dest -Algorithm SHA256).Hash
if ($srcHash -ne $dstHash) {
    throw "Copy verification failed: hashes differ ($srcHash vs $dstHash)."
}

Write-Host ''
Write-Host 'Keystore backed up and verified.' -ForegroundColor Green
Write-Host ("From    : {0}" -f (Resolve-Path -LiteralPath $Keystore).Path)
Write-Host ("To      : {0}" -f (Resolve-Path -LiteralPath $dest).Path)
Write-Host ("SHA-256 : {0}" -f $srcHash)
Write-Host ''
Write-Host 'The password is NOT in the backup file. Store it in a password manager' -ForegroundColor Yellow
Write-Host 'and confirm you can restore this file before you need to.' -ForegroundColor Yellow
