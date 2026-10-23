# Versi Windows dari restore.sh: kembalikan TV seperti sebelum disable.ps1, dari keadaan
# yang dicatat waktu itu. Paket yang memang sudah nonaktif tetap nonaktif, Home kembali ke
# komponen yang memegangnya, dan skala animasi kembali ke nilai yang tercatat. Pakai daftar
# yang sama dengan disable.ps1.
#
# Usage: tools\restore.ps1 -TvHost 192.168.1.50 examples\xiaomi-mi-tv-4a\debloat.txt [-Stock stock.launcher.package]
param(
    [Parameter(Mandatory = $true, Position = 0)][string]$List,
    [string]$Stock = 'com.google.android.tvlauncher',
    [string]$TvHost = $env:TV_HOST
)
$ErrorActionPreference = 'Stop'
# Pipe plain UTF-8 to native commands; the default here prepends a BOM that corrupts the first command.
$OutputEncoding = New-Object System.Text.UTF8Encoding $false
$repo = Split-Path -Parent $PSScriptRoot
$List = (Resolve-Path $List).Path
$py = Join-Path $repo 'tools\venv\Scripts\python.exe'
if (-not $TvHost) { throw 'kasih -TvHost <tv ip> atau set $env:TV_HOST' }
$env:TV_HOST = $TvHost

# Same as restore.sh: replay the recorded states instead of enabling the whole list, so packages that
# were already disabled stay disabled (#239). recovery.py reads and validates the list itself, warns
# when no record exists and then falls back to the old behaviour.
$cmds = & $py -I (Join-Path $repo 'tools\recovery.py') --commands $List $Stock
if ($LASTEXITCODE -ne 0) { throw 'rencana restore tidak jadi, TV tidak diubah' }
$cmds | & $py -I (Join-Path $repo 'tools\tv.py') --batch
