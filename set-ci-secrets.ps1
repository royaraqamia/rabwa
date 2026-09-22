<#
.SYNOPSIS
    Upload the رَبْوَة release-signing passwords as GitHub Actions secrets.

.DESCRIPTION
    Prompts for STORE_PASSWORD and KEY_PASSWORD without echoing them, then
    uploads them as the repository secrets consumed by
    .github/workflows/release.yml.

    Values are encrypted client-side with the repository's public key
    (libsodium sealed box via Python + PyNaCl) and passed on stdin, so they
    never appear in a command line, an environment variable, a log, or a file
    on disk. The temporary helper script contains no secrets and is removed
    afterwards.

    Requires Python 3 with PyNaCl:  python -m pip install pynacl
    and a token in GH_TOKEN (or GITHUB_TOKEN) that may write repository secrets.

.PARAMETER Repo
    Target repository as owner/name. Defaults to royaraqamia/rabwa.

.EXAMPLE
    .\set-ci-secrets.ps1
    Prompt for both passwords and upload them.

.EXAMPLE
    .\set-ci-secrets.ps1 -Repo other-owner/other-repo
    Upload to a different repository.
#>
[CmdletBinding()]
param(
    [string]$Repo = 'royaraqamia/rabwa'
)

$ErrorActionPreference = 'Stop'

function ConvertFrom-SecureStringPlain {
    param([Security.SecureString]$Secure)
    if (-not $Secure) { return '' }
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Secure)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
}

$py = Get-Command python -ErrorAction SilentlyContinue
if (-not $py) {
    throw 'Python not found. Install Python 3, then run: python -m pip install pynacl'
}
& $py.Source -c 'import nacl.public' 2>$null
if ($LASTEXITCODE -ne 0) {
    throw 'PyNaCl is not installed. Run: python -m pip install pynacl'
}
if (-not ($env:GH_TOKEN -or $env:GITHUB_TOKEN)) {
    throw 'Set GH_TOKEN (or GITHUB_TOKEN) to a token that can write repository secrets.'
}

$pyHelper = Join-Path ([IO.Path]::GetTempPath()) 'rabwa-set-gh-secret.py'
$helper = @'
import base64
import json
import os
import sys
import urllib.request

from nacl.public import PublicKey, SealedBox


def request(method, url, token, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Authorization", "Bearer " + token)
    req.add_header("Accept", "application/vnd.github+json")
    req.add_header("X-GitHub-Api-Version", "2022-11-28")
    req.add_header("User-Agent", "rabwa-set-ci-secrets")
    if data is not None:
        req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req) as response:
        raw = response.read().decode()
    return json.loads(raw) if raw else {}


def main():
    repo, name = sys.argv[1], sys.argv[2]
    value = sys.stdin.buffer.read().decode("utf-8").rstrip("\r\n")
    token = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN")
    if not token:
        print("GH_TOKEN is not set", file=sys.stderr)
        return 1
    if not value:
        print("empty value; refusing to upload", file=sys.stderr)
        return 1

    base = "https://api.github.com/repos/" + repo + "/actions/secrets"
    public_key = request("GET", base + "/public-key", token)
    sealed = SealedBox(PublicKey(base64.b64decode(public_key["key"]))).encrypt(value.encode())
    request(
        "PUT",
        base + "/" + name,
        token,
        {"encrypted_value": base64.b64encode(sealed).decode(), "key_id": public_key["key_id"]},
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
'@

[IO.File]::WriteAllText($pyHelper, $helper, (New-Object System.Text.UTF8Encoding($false)))

$prevOutputEncoding = $OutputEncoding
try {
    Write-Host "Repository : $Repo" -ForegroundColor Cyan
    Write-Host 'Uploader   : python + PyNaCl (sealed box)' -ForegroundColor DarkGray
    Write-Host ''

    $storeSecure = Read-Host -Prompt 'STORE_PASSWORD (keystore password)' -AsSecureString
    $keySecure   = Read-Host -Prompt 'KEY_PASSWORD (blank = same as STORE_PASSWORD)' -AsSecureString

    $store = ConvertFrom-SecureStringPlain $storeSecure
    $key   = ConvertFrom-SecureStringPlain $keySecure
    if (-not $store) { throw 'STORE_PASSWORD cannot be empty.' }
    if (-not $key) {
        $key = $store
        Write-Host 'Reusing STORE_PASSWORD for KEY_PASSWORD.' -ForegroundColor DarkGray
    }
    Write-Host ''

    $OutputEncoding = New-Object System.Text.UTF8Encoding($false)

    $store | & $py.Source $pyHelper $Repo 'STORE_PASSWORD'
    if ($LASTEXITCODE -ne 0) { throw "Uploading STORE_PASSWORD failed (exit $LASTEXITCODE)." }
    Write-Host 'STORE_PASSWORD set.' -ForegroundColor Green

    $key | & $py.Source $pyHelper $Repo 'KEY_PASSWORD'
    if ($LASTEXITCODE -ne 0) { throw "Uploading KEY_PASSWORD failed (exit $LASTEXITCODE)." }
    Write-Host 'KEY_PASSWORD set.' -ForegroundColor Green

    Write-Host ''
    Write-Host "Both secrets uploaded to $Repo." -ForegroundColor Green
} finally {
    $store = $null
    $key   = $null
    $OutputEncoding = $prevOutputEncoding
    if (Test-Path -LiteralPath $pyHelper) {
        Remove-Item -LiteralPath $pyHelper -Force -ErrorAction SilentlyContinue
    }
}
