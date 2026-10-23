#!/usr/bin/env bash
# Nonaktifkan semua paket di file debloat di TV (satu paket per baris, komentar # boleh).
# Paket dinonaktifkan untuk user 0, tidak pernah di-uninstall. Balikin pakai tools/restore.sh <file yang sama>.
#
# Usage: TV_HOST=192.168.1.50 tools/disable.sh examples/xiaomi-mi-tv-4a/debloat.txt
set -euo pipefail
LIST="${1:?cara pakai: disable.sh <debloat list>}"
# Resolve the list against the caller's directory before moving into the repo. Git Bash's pwd says
# /c/Users/..., which the Windows Python cannot open with MSYS_NO_PATHCONV=1 set; pwd -W gives
# C:/Users/..., the same path (and so the same record) as the PowerShell scripts.
LIST_DIR="$(cd "$(dirname "$LIST")" && { pwd -W 2>/dev/null || pwd; })"
LIST="$LIST_DIR/$(basename "$LIST")"
cd "$(dirname "$0")/.."
PY=tools/venv/bin/python
[ -x "$PY" ] || PY=tools/venv/Scripts/python.exe
export MSYS_NO_PATHCONV=1
[ -x "$PY" ] || { echo "jalankan dulu: python3 -m venv tools/venv && tools/venv/bin/pip install -r tools/requirements.txt"; exit 1; }
# Lists come from contributors, and the package name is pasted into a shell command on the TV.
# Refuse the whole list unless every entry is a bare package name (#165).
# A list read from a directory that is not there dies in the `cd` above; one whose file is not
# readable only made awk complain, so say it in the script's own words, next to the empty-list
# message below rather than as a stray awk line.
[ -r "$LIST" ] || { echo "File daftarnya tidak kebaca — cannot read the list: $LIST" >&2; exit 1; }
# Normalise first: Notepad on Windows saves a BOM and CRLF, and without this the first "# TV" line
# arrives as a package name and every entry keeps a trailing \r (#208).
# `|| true`: an empty result is not a failure of this pipeline. Under pipefail the grep exits 1 when
# the list holds no package line at all, and `set -e` then ended the run with no output and status 1,
# so a list whose entries are all commented out (what CONTRIBUTING asks for while one is untested)
# read exactly like a run that died halfway.
PKGS=$(LC_ALL=C awk 'NR==1{sub(/^\357\273\277/,"")} {sub(/\r$/,""); print}' "$LIST" \
  | grep -vE '^\s*(#|$)' | sed -E 's/\s+#.*$//' | awk '{print $1}' || true)
BAD=$(printf '%s\n' "$PKGS" | grep -vE '^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$' || true)
if [ -n "$BAD" ]; then
  { echo "Menolak $LIST: not a package name:"; printf '%s\n' "$BAD" | sed 's/^/  /'; } >&2
  exit 1
fi
# Nothing to do, and nothing to lose by saying so before the TV is asked anything: no record is
# written and no query is sent, so this cannot be mistaken for a list that ran. It is the first
# refusal after the shape check because it is the one a contributor is most likely to hit: a list
# whose entries are all commented out is what CONTRIBUTING asks for while an entry is untested.
if [ -z "$PKGS" ]; then
  echo "Menolak $LIST: no package names di dalamnya (semua baris komentar atau kosong), jadi tidak ada" >&2
  echo "yang dikirim ke TV dan catatan pemulihan tidak ditulis." >&2
  exit 1
fi
# Core Android packages: with one of these disabled, Android itself can stop booting. An original Mi TV
# Stick never came back after a list took the permission controller, package installer, ext services,
# module metadata and the mainline overlays (#884). No switch overrides this. The content providers
# below CORE_OK are the exception: tested lists turn them off and no TV feature uses them.
# Keep CORE_RE and CORE_OK the same in disable.ps1 and recovery.py (tests/test_core_packages.py).
CORE_RE='^(android|com\.android\.systemui|com\.android\.shell|com\.android\.phone|com\.android\.settings|com\.android\.tv\.settings|com\.(google\.)?android\.(permissioncontroller|packageinstaller|ext\.services|ext\.shared|modulemetadata)|com\.google\.android\.overlay\.modules.*|com\.android\.providers\..*)$'
CORE_OK='^com\.android\.providers\.(calendar|contacts|userdictionary|blockednumber)$'
CORE=$(printf '%s\n' "$PKGS" | grep -E "$CORE_RE" | grep -vE "$CORE_OK" || true)
if [ -n "$CORE" ]; then
  { echo "Menolak $LIST: daftar ini menonaktifkan paket Android inti, dan tanpa paket itu TV bisa gagal nyala lagi (#884):"
    printf '%s\n' "$CORE" | sed 's/^/  /'
    echo "Hapus baris ini dari daftar (atau jadikan komentar) lalu jalankan lagi. Tidak ada yang diubah di TV."; } >&2
  exit 1
fi
# A Wi-Fi network saved during the first-run wizard belongs to setupwraith, and Android deletes it the
# moment setupwraith is disabled: the TV drops off Wi-Fi, and ADB and the phone remote go with it.
# Skip it until the network has been forgotten and re-joined in Settings (it then belongs to
# android.uid.system). FORCE_SETUPWRAITH=1 disables it anyway, e.g. on Ethernet.
SETUP=com.google.android.tungsten.setupwraith
if printf '%s\n' "$PKGS" | grep -x "$SETUP" >/dev/null && [ -z "${FORCE_SETUPWRAITH:-}" ]; then
  # Be safe: if we cannot read the owners (tv.py failed, timed out, or showed no network at all),
  # do not disable setupwraith, so the TV cannot lose the network it is on.
  WIFI="$("$PY" -I tools/tv.py "dumpsys wifi")" || WIFI=""
  OWNERS="$(printf '%s\n' "$WIFI" | grep -o 'cname=[^ ]*' || true)"
  if [ -z "$OWNERS" ]; then
    echo "Skipping $SETUP: could not check aplikasi mana yang punya jaringan Wi-Fi tersimpan (dumpsys wifi gagal" >&2
    echo "atau tidak menampilkan apa pun). Coba lagi, atau set FORCE_SETUPWRAITH=1 kalau TV pakai Ethernet." >&2
    PKGS="$(printf '%s\n' "$PKGS" | grep -vx "$SETUP" || true)"
  # no grep -q here: with pipefail its early exit would SIGPIPE the grep before it and fail open
  elif printf '%s\n' "$OWNERS" | grep setupwraith >/dev/null; then
    echo "Skipping $SETUP: jaringan Wi-Fi tersimpan milik paket ini, dan menonaktifkannya akan menghapus jaringan" >&2
    echo "itu lalu TV lepas dari Wi-Fi. Di TV, lupakan jaringan itu lalu sambungkan lagi lewat" >&2
    echo "Settings > Network & Internet, kemudian jalankan lagi (atau set FORCE_SETUPWRAITH=1 kalau pakai Ethernet)." >&2
    PKGS="$(printf '%s\n' "$PKGS" | grep -vx "$SETUP" || true)"
  fi
fi

# On firmware with ro.adb.secure=0, port 5555 gives a shell to any device on the network without the
# "Allow" prompt. Read it now and say so once the list is done; a failed read only skips the note.
ADB_SECURE="$("$PY" -I tools/tv.py "getprop ro.adb.secure" 2>/dev/null | tr -d '[:space:]')" || ADB_SECURE=""

# Home guard (#239): a list that disables the app owning Home leaves the TV with nothing to start the
# next time Home is pressed, and on Google TV the setup wizard can take the category instead and
# crash-loop system_server (docs/TROUBLESHOOTING.md). Skip the owner while nothing else on the TV can
# answer HOME; FORCE_HOME=1 disables it anyway, for a TV reachable another way.
# The extra query is worth it only when the list holds Home's own package, which is how a list that
# leaves the TV without a launcher looks before anything is disabled.
HOME_QUERY='cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
HOME_PKG=$("$PY" -I tools/tv.py "$HOME_QUERY" 2>/dev/null | tail -n 1 | tr -d '\r' | cut -d/ -f1 || true)
# `cmd` arrived with Android 7 (Nougat): on an older set the query answers nothing, or the shell's own
# "cmd: not found", and the guard below was skipped without a word - which is how a list disabled the
# only launcher on a KitKat TV that had no replacement to hand Home over to (#795). Ask the framework
# instead, which has printed the package holding Home since 4.x:
#   mHomeProcess: ProcessRecord{52f4a5c8 2f4b:com.example.launcher/u0a56}
# Only a package-shaped answer counts; on a set where Home has not started yet the line says null.
# grep -c, not -q: under pipefail an early exit could SIGPIPE printf and fail open (see above).
HOME_SHAPE=$(printf '%s\n' "$HOME_PKG" | grep -cE '^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$' || true)
if [ -n "$PKGS" ] && [ "${HOME_SHAPE:-0}" -eq 0 ]; then
  HOME_PKG=$("$PY" -I tools/tv.py 'dumpsys activity' 2>/dev/null | tr -d '\r' \
    | sed -nE 's/.*mHomeProcess:.*[: ]([A-Za-z0-9_.]+)\/[A-Za-z0-9_.]+.*/\1/p' | head -n 1 || true)
fi
# grep without -q: an early exit could SIGPIPE printf and fail open under pipefail (see above).
if [ -n "$HOME_PKG" ] && printf '%s\n' "$PKGS" | grep -xF "$HOME_PKG" >/dev/null && [ -z "${FORCE_HOME:-}" ]; then
  # Every package with a HOME activity the TV can start now: disabled packages do not answer, so this
  # is "what could take Home if the list went through".
  CLAIMS=$("$PY" -I tools/tv.py 'cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME' 2>/dev/null \
    | tr -d '\r' | grep -oE '^ *[A-Za-z0-9_.]+/[A-Za-z0-9_.$]+' | sed -E 's/^ +//' | sort -u || true)
  # Nothing left to answer Home once the list is applied, or the TV could not say who does: either
  # way the owner is the only Home this run knows about, so it stays enabled.
  # The setup wizard and the settings apps answer HOME too, but they are not launchers: on Google TV,
  # setupwraith taking Home is the crash loop this guard exists to prevent. They don't count.
  # A vendor's own fallback does not count either: TCL Android TV sets register
  # com.tcl.keycustomfunctionservice/.FallbackHome (and com.android.tv.settings/.system.FallbackHome),
  # a placeholder the firmware shows when no launcher is enabled, not a home the viewer can use.
  # Nor does a factory-test app: on a Xiaomi Mi TV 4A com.duokan.factorytest/.FactoryLauncher answers
  # HOME, and handing Home to it leaves the viewer on the factory test screen.
  OTHERS=$(printf '%s\n' "$CLAIMS" | grep -vE '/[^ ]*FallbackHome$' | cut -d/ -f1 | sort -u \
    | grep -viE 'factorytest|factory_test|factorymode' \
    | grep -vxF "$HOME_PKG" | grep -vxF "$PKGS" \
    | grep -vxF -e com.google.android.tungsten.setupwraith -e com.android.tv.settings -e com.android.settings -e com.android.provision || true)
  if [ -z "$OTHERS" ]; then
    echo "Skipping $HOME_PKG: paket ini yang pegang Home, dan tidak ada aplikasi lain yang bisa menjawab Home," >&2
    echo "terpasang, dan tetap aktif. Jadikan launcher baru sebagai Home dulu (launcher/build-and-install.sh), lalu jalankan" >&2
    echo "daftar ini lagi untuk menonaktifkan yang lama; atau set FORCE_HOME=1 kalau kamu masih bisa menjangkau TV." >&2
    PKGS="$(printf '%s\n' "$PKGS" | grep -vxF "$HOME_PKG" || true)"
  fi
fi
# Neither query named Home, so the guard above could not run at all. Say so: a silent skip looks
# exactly like a check that passed, and on a set too old for the launcher there is nothing to lose by
# keeping every listed package (#795).
if [ -n "$PKGS" ] && [ -z "$HOME_PKG" ] && [ -z "${FORCE_HOME:-}" ]; then
  echo "Could not tell which app owns Home di TV ini, jadi penjaga Home tidak jalan: baik" >&2
  echo '"cmd package resolve-activity" (ada sejak Android 7) maupun "dumpsys activity" tidak menyebutkannya.' >&2
  echo 'Cek sendiri bahwa daftar tidak memuat launcher kamu sebelum dijalankan. Launcher pengganti' >&2
  echo 'butuh Android 8.0 (minSdk 26), jadi di set yang lebih lama stock launcher adalah satu-satunya Home' >&2
  echo 'yang ada: kalau dinonaktifkan, tidak ada yang bisa dibuka, dan set tanpa pemulihan tidak ada jalan balik (#795).' >&2
fi

# Only the package's own "requested permissions" block counts: a vendor app in a shared UID would otherwise
# match through its shared user's permissions and be skipped by mistake.
# A package that asks for android.permission.TV_INPUT_HARDWARE is usually the software layer under the
# HDMI, AV and tuner inputs: routing, the HDCP handshake and scaling live in it. Disable one and the port
# still notices the cable but shows "No Signal" for good; re-enabling the pieces in batches does not bring
# the picture back, a full restore and a cold boot do (#321, OnePlus TV on Android 11). Keep those
# packages; TVINPUT_ALLOW=1 disables them anyway.
# The permission alone is a loose test and does produce false positives: on a Vestel shandao set the
# MediaTek USB media player asks for it and implements no input at all. The decisive check is which
# packages serve the service, `pm query-services --components -a android.media.tv.TvInputService`, but it
# stays out of this guard on purpose: the command is missing on the older firmware this repo also covers,
# and a guard that fails open costs someone their HDMI ports. So the skip still goes by the permission,
# and the message tells the owner how to tell a false positive from the real thing.
if [ -n "$PKGS" ] && [ -z "${TVINPUT_ALLOW:-}" ]; then
  # One round trip: the loop runs on the TV, where `dumpsys package` prints the permissions a package
  # asks for. Every name was checked as a bare package name above, so none of them can add shell syntax
  # here. </dev/null so the client cannot read this script's stdin (that is the piped batch below);
  # tr -d '\r' because adb can end lines with CRLF, which would leave a CR on every name.
  NAMES="$(printf '%s ' $PKGS)"
  # Trailing `; true`: the loop's status is the last package's `grep -q`, which is nonzero whenever
  # the last listed package is not an input driver — the common case — and would look like a failed
  # check. Only a real failure (tv.py cannot reach the TV) should reach the warning below.
  if TV_HW="$("$PY" -I tools/tv.py \
      "for p in $NAMES; do dumpsys package \$p | sed -n -E '/requested permissions:/,/(install|runtime) permissions:|User [0-9]+:/p' | grep -q TV_INPUT_HARDWARE && echo \$p; done; true" \
      </dev/null 2>/dev/null | tr -d '\r')"; then
    for p in $TV_HW; do
      # grep without -q: with pipefail an early exit could SIGPIPE printf and fail the run (see above).
      printf '%s\n' "$PKGS" | grep -xF "$p" >/dev/null || continue
      echo "Skipping $p: paket ini minta android.permission.TV_INPUT_HARDWARE, jadi ini lapisan perangkat lunak" >&2
      echo "di bawah input HDMI/AV/tuner. Kalau dinonaktifkan, input itu tetap mendeteksi kabel tapi layar tetap" >&2
      echo "hitam (\"No Signal\"), dan mengaktifkan ulang sepotong-sepotong tidak mengembalikan gambar: yang" >&2
      echo "mengembalikan adalah restore penuh plus cold boot (#321)." >&2
      echo "Minta izin itu bukan bukti: aplikasi bawaan bisa memintanya tanpa mengurus input sama sekali. Yang" >&2
      echo "memastikan adalah paket mana yang benar-benar melayani layanan input:" >&2
      echo "  pm query-services --components -a android.media.tv.TvInputService" >&2
      echo "Kalau $p tidak ada di daftar itu, ini false positive di set kamu, seperti pemutar media USB MediaTek" >&2
      echo "yang ternyata begitu (examples/cecotec-shandao). Cek setiap input setelah reboot sebelum" >&2
      echo "percaya, lalu set TVINPUT_ALLOW=1 untuk menonaktifkannya." >&2
      PKGS="$(printf '%s\n' "$PKGS" | grep -vxF "$p" || true)"
    done
  else
    # Do not skip anything on a guess, but say the check did not run: a black input is easy to blame on
    # the cable, and the ports are the one thing the owner cannot test from here.
    echo "Could not check paket mana di daftar yang mengurus input HDMI/AV (tv.py gagal). Cek setiap" >&2
    echo "input setelah proses ini: port yang mendeteksi kabel tapi tetap hitam berarti salah satunya" >&2
    echo "dinonaktifkan, dan hanya restore penuh plus cold boot yang mengembalikannya (#321)." >&2
  fi
fi

# The state undo needs goes to disk before the first command reaches the TV, and a failed save stops
# here with the TV untouched: a record written afterwards would describe a TV that is already broken,
# and a retry must not overwrite the one taken before the first change (#239).
"$PY" -I tools/recovery.py --save "$LIST"

{
  [ -n "$PKGS" ] && printf '%s\n' "$PKGS" | sed 's/^/pm disable-user --user 0 /'
  echo "pm list packages -d"
} | "$PY" -I tools/tv.py --batch

if [ "$ADB_SECURE" = "0" ]; then
  echo "Peringatan: TV ini melaporkan ro.adb.secure=0, jadi ADB jaringan di port 5555 menerima perangkat mana pun di" >&2
  echo "jaringan kamu tanpa prompt \"Allow\". Setelah selesai, matikan USB debugging dan" >&2
  echo "Network/ADB debugging di TV lalu reboot (README: \"Isn't ADB access a security hole?\")." >&2
fi
