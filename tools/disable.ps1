# Versi Windows dari disable.sh: nonaktifkan semua paket di file debloat di TV
# (satu paket per baris, komentar # boleh). Paket dinonaktifkan untuk user 0, tidak pernah di-uninstall.
# Balikin pakai tools\restore.ps1 <file yang sama>.
#
# Usage: tools\disable.ps1 -TvHost 192.168.1.50 examples\xiaomi-mi-tv-4a\debloat.txt
param(
    [Parameter(Mandatory = $true, Position = 0)][string]$List,
    [string]$TvHost = $env:TV_HOST
)
$ErrorActionPreference = 'Stop'
# Pipe plain UTF-8 to native commands; the default here prepends a BOM that corrupts the first command.
$OutputEncoding = New-Object System.Text.UTF8Encoding $false
$repo = Split-Path -Parent $PSScriptRoot
$List = (Resolve-Path $List).Path
$py = Join-Path $repo 'tools\venv\Scripts\python.exe'
if (-not (Test-Path $py)) { throw 'jalankan dulu: python -m venv tools\venv; tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt' }
if (-not $TvHost) { throw 'kasih -TvHost <tv ip> atau set $env:TV_HOST' }
$env:TV_HOST = $TvHost

$pkgs = Get-Content $List | ForEach-Object { ($_ -replace '#.*$', '').Trim() } | Where-Object { $_ } | ForEach-Object { ($_ -split '\s+')[0] }
# Lists come from contributors and each name is pasted into a shell command on the TV (#165).
$bad = @($pkgs | Where-Object { $_ -notmatch '^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$' })
if ($bad.Count -gt 0) {
    throw "Menolak $List: not a package name: $($bad -join ', ')"
}
# Nothing to do: a list whose entries are all commented out is what CONTRIBUTING asks for while one
# is untested. Say it here, before a query goes to the TV, rather than letting recovery.py stop the run
# after them with a message about the record.
if (-not $pkgs -or $pkgs.Count -eq 0) {
    throw "Menolak ${List}: no package names di dalamnya (semua baris komentar atau kosong), jadi tidak ada yang dikirim ke TV dan catatan pemulihan tidak ditulis."
}
# Core Android packages, the same block as disable.sh: with one of these disabled Android itself can stop
# booting (#884, an original Mi TV Stick). No switch overrides it. Keep both patterns in step with
# disable.sh and recovery.py (tests/test_core_packages.py).
$coreRe = '^(android|com\.android\.systemui|com\.android\.shell|com\.android\.phone|com\.android\.settings|com\.android\.tv\.settings|com\.(google\.)?android\.(permissioncontroller|packageinstaller|ext\.services|ext\.shared|modulemetadata)|com\.google\.android\.overlay\.modules.*|com\.android\.providers\..*)$'
$coreOk = '^com\.android\.providers\.(calendar|contacts|userdictionary|blockednumber)$'
$core = @($pkgs | Where-Object { $_ -cmatch $coreRe -and $_ -cnotmatch $coreOk })
if ($core.Count -gt 0) {
    throw "Menolak ${List}: daftar ini menonaktifkan paket Android inti, dan tanpa paket itu TV bisa gagal nyala lagi (#884): $($core -join ', '). Hapus baris ini dari daftar (atau jadikan komentar) lalu jalankan lagi. Tidak ada yang diubah di TV."
}
# A Wi-Fi network saved during the first-run wizard belongs to setupwraith, and Android deletes it when
# setupwraith is disabled. Skip it while it owns the saved network; `$env:FORCE_SETUPWRAITH=1` overrides.
$setup = 'com.google.android.tungsten.setupwraith'
if ($pkgs -contains $setup -and -not $env:FORCE_SETUPWRAITH) {
    $wifi = (& $py -I (Join-Path $repo 'tools\tv.py') 'dumpsys wifi') -join "`n"
    # Be safe: if we cannot read the owners (tv.py failed, timed out, or showed no network at all),
    # do not disable setupwraith, so the TV cannot lose the network it is on.
    if ($LASTEXITCODE -ne 0 -or $wifi -notmatch 'cname=') {
        Write-Warning "Skipping ${setup}: could not check aplikasi mana yang punya jaringan Wi-Fi tersimpan (dumpsys wifi gagal atau tidak menampilkan apa pun). Coba lagi, atau set `$env:FORCE_SETUPWRAITH=1 kalau TV pakai Ethernet."
        $pkgs = @($pkgs | Where-Object { $_ -ne $setup })
    } elseif ($wifi -match 'cname=[^ ]*setupwraith') {
        Write-Warning "Skipping ${setup}: jaringan Wi-Fi tersimpan milik paket ini, dan menonaktifkannya akan menghapus jaringan itu lalu TV lepas dari Wi-Fi. Di TV, lupakan jaringan itu lalu sambungkan lagi lewat Settings > Network & Internet, kemudian jalankan lagi (atau set `$env:FORCE_SETUPWRAITH=1 kalau pakai Ethernet)."
        $pkgs = @($pkgs | Where-Object { $_ -ne $setup })
    }
}
# Same as disable.sh: on firmware with ro.adb.secure=0, port 5555 gives a shell to any device on the
# network without the "Allow" prompt. Read it now and say so once the list is done; a failed read only
# skips the note. Windows PowerShell turns redirected stderr into an error that 'Stop' would throw.
$ErrorActionPreference = 'Continue'
$adbSecure = ((& $py -I (Join-Path $repo 'tools\tv.py') 'getprop ro.adb.secure' 2>$null) -join '').Trim()
if ($LASTEXITCODE -ne 0) { $adbSecure = '' }
$ErrorActionPreference = 'Stop'

# Home guard (#239), the same check as disable.sh: a list that disables the app owning Home leaves the
# TV with nothing to start Home with. Skip the owner while nothing else on the TV can answer HOME;
# `$env:FORCE_HOME=1` disables it anyway, for a TV reachable another way.
if (-not $env:FORCE_HOME) {
    $homeQuery = 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
    $homeLine = @(& $py -I (Join-Path $repo 'tools\tv.py') $homeQuery) |
        Where-Object { $_ -match '\S' } | Select-Object -Last 1
    $homePkg = "$homeLine".Trim() -split '/' | Select-Object -First 1
    # `cmd` arrived with Android 7 (Nougat): on an older set this query answers nothing, or the shell's
    # own "cmd: not found", and the guard was skipped without a word (disable.sh, #795). `dumpsys
    # activity` prints the package holding Home on those sets: mHomeProcess: ProcessRecord{... pkg/u0a56}
    $named = $homePkg -and ($homePkg -match '^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$')
    if (-not $named) {
        $fallback = @(& $py -I (Join-Path $repo 'tools\tv.py') 'dumpsys activity' | ForEach-Object {
            $line = "$_".Trim()
            if ($line -match 'mHomeProcess:.*[: ]([A-Za-z0-9_.]+)/[A-Za-z0-9_.]+') { $Matches[1] }
        } | Select-Object -First 1)
        $homePkg = if ($fallback.Count -gt 0) { "$($fallback[0])" } else { '' }
    }
    if ($homePkg -and ($pkgs -contains $homePkg)) {
        # Every package with a HOME activity the TV can start now; a disabled one does not answer.
        $claimQuery = 'cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
        $claims = @(& $py -I (Join-Path $repo 'tools\tv.py') $claimQuery | ForEach-Object {
            # Keep the class: a vendor fallback is named FallbackHome and is not a usable launcher
            # (TCL Android TV sets register com.tcl.keycustomfunctionservice/.FallbackHome).
            # Trim first: a trailing CR from the TV would stop the `$` anchor from matching any line.
            if ("$_".Trim() -match '^([A-Za-z0-9_.]+)/([A-Za-z0-9_.$]+)$') {
                if ($Matches[2] -notmatch 'FallbackHome$') { $Matches[1] }
            }
        } | Sort-Object -Unique)
        # The setup wizard and the settings apps answer HOME too, but they are not launchers (disable.sh).
        $notLaunchers = @('com.google.android.tungsten.setupwraith', 'com.android.tv.settings', 'com.android.settings', 'com.android.provision')
        # Nor is a factory-test app (com.duokan.factorytest/.FactoryLauncher answers HOME on a Mi TV 4A).
        $others = @($claims | Where-Object { $_ -ne $homePkg -and $pkgs -notcontains $_ -and $notLaunchers -notcontains $_ -and $_ -notmatch 'factorytest|factory_test|factorymode' })
        if ($others.Count -eq 0) {
            Write-Warning "Skipping ${homePkg}: paket ini yang pegang Home, dan tidak ada aplikasi lain yang bisa menjawab Home, terpasang, dan tetap aktif. Jadikan launcher baru sebagai Home dulu (launcher\build-and-install.ps1), lalu jalankan daftar ini lagi untuk menonaktifkan yang lama; atau set `$env:FORCE_HOME=1 kalau kamu masih bisa menjangkau TV."
            $pkgs = @($pkgs | Where-Object { $_ -ne $homePkg })
        }
    }
    # Neither query named Home, so the guard could not run. Say so (disable.sh, #795).
    if (-not $homePkg -and $pkgs.Count -gt 0) {
        Write-Warning 'Could not tell which app owns Home di TV ini, jadi penjaga Home tidak jalan: baik "cmd package resolve-activity" (ada sejak Android 7) maupun "dumpsys activity" tidak menyebutkannya. Cek sendiri bahwa daftar tidak memuat launcher kamu sebelum dijalankan. Launcher pengganti butuh Android 8.0 (minSdk 26), jadi di set yang lebih lama stock launcher adalah satu-satunya Home yang ada: kalau dinonaktifkan, tidak ada yang bisa dibuka, dan set tanpa pemulihan tidak ada jalan balik (#795).'
    }
}
# Same as disable.sh (#321): a package that asks for android.permission.TV_INPUT_HARDWARE is the software
# layer under the HDMI, AV and tuner inputs. Disable one and the port still notices the cable but shows
# "No Signal" for good; re-enabling the pieces in batches does not bring the picture back, a full restore
# and a cold boot do. Keep them; `$env:TVINPUT_ALLOW=1` disables them anyway.
if (-not $env:TVINPUT_ALLOW -and $pkgs.Count -gt 0) {
    # Trailing `; true`: the loop's status is the last package's `grep -q`, which is nonzero whenever the
    # last listed package is not an input driver, and would look like a failed check.
    $loop = 'for p in ' + ($pkgs -join ' ') + '; do dumpsys package $p | sed -n -E ''/requested permissions:/,/(install|runtime) permissions:|User [0-9]+:/p'' | grep -q TV_INPUT_HARDWARE && echo $p; done; true'
    # Windows PowerShell turns redirected stderr into an error that 'Stop' would throw (see getprop).
    $ErrorActionPreference = 'Continue'
    $tvhw = @(& $py -I (Join-Path $repo 'tools\tv.py') $loop 2>$null)
    $checked = $LASTEXITCODE -eq 0
    $ErrorActionPreference = 'Stop'
    if (-not $checked) {
        Write-Warning 'Could not check paket mana di daftar yang mengurus input HDMI/AV (tv.py gagal). Cek setiap input setelah proses ini: port yang mendeteksi kabel tapi tetap hitam berarti salah satunya dinonaktifkan, dan hanya restore penuh plus cold boot yang mengembalikannya (#321).'
    } else {
        foreach ($p in ($tvhw | ForEach-Object { $_.Trim() } | Where-Object { $_ })) {
            if ($pkgs -contains $p) {
                Write-Warning "Skipping ${p}: paket ini minta android.permission.TV_INPUT_HARDWARE, jadi ini lapisan perangkat lunak di bawah input HDMI/AV/tuner. Kalau dinonaktifkan, input itu tetap mendeteksi kabel tapi layar tetap hitam (No Signal), dan mengaktifkan ulang sepotong-sepotong tidak mengembalikan gambar: yang mengembalikan adalah restore penuh plus cold boot (#321). Minta izin itu bukan bukti: aplikasi bawaan bisa memintanya tanpa mengurus input sama sekali. Yang memastikan adalah paket mana yang benar-benar melayani layanan input: pm query-services --components -a android.media.tv.TvInputService. Kalau ${p} tidak ada di daftar itu, ini false positive di set kamu, seperti pemutar media USB MediaTek yang ternyata begitu (examples/cecotec-shandao). Cek setiap input setelah reboot sebelum percaya, lalu set `$env:TVINPUT_ALLOW=1 untuk menonaktifkannya."
                $pkgs = @($pkgs | Where-Object { $_ -ne $p })
            }
        }
    }
}
# Same as disable.sh: the state undo needs is recorded before the first command reaches the TV, and
# a failed save leaves the TV untouched (#239).
& $py -I (Join-Path $repo 'tools\recovery.py') --save $List
if ($LASTEXITCODE -ne 0) { throw 'catatan pemulihan tidak tertulis, jadi tidak ada yang dinonaktifkan' }

$cmds = @($pkgs | ForEach-Object { "pm disable-user --user 0 $_" }) + 'pm list packages -d'
$cmds | & $py -I (Join-Path $repo 'tools\tv.py') --batch

if ($adbSecure -eq '0') {
    Write-Warning "Peringatan: TV ini melaporkan ro.adb.secure=0, jadi ADB jaringan di port 5555 menerima perangkat mana pun di jaringan kamu tanpa prompt `"Allow`". Setelah selesai, matikan USB debugging dan Network/ADB debugging di TV lalu reboot (README: `"Isn't ADB access a security hole?`")."
}
