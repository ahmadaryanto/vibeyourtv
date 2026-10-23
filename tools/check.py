#!/usr/bin/env python3
"""Apa yang dibalik update firmware, dibanding daftar yang sudah diterapkan (#359).

    check.py <debloat list> [launcher package]        laporkan drift; keluar 1 kalau ada
    check.py --fix <debloat list> [launcher package]  nonaktifkan lagi yang hidup kembali dan kembalikan
                                                      launcher ke Home

Update firmware mengaktifkan lagi paket di daftar, mengembalikan Home ke stock launcher, dan menambah
aplikasi bawaan baru, dan tidak ada di repo ini yang menyadarinya: tidak ada yang menjalankan disable.sh
lagi, dan restore.sh malah membatalkan pembersihan. Perintah ini membandingkan TV dengan daftar dan
keluar 1 kalau TV-nya drift, jadi cocok untuk cron; --fix menerapkan daftar lagi tanpa menyentuh catatan
pemulihan (paket sudah dicatat sebelum jalan pertama, jadi undo tetap berarti undo).

Yang dibaca adalah daftar, bukan catatan: catatan menyimpan wajah TV *sebelum* dibersihkan, yang
dibutuhkan restore, sedangkan target di sini adalah daftar itu sendiri.

--fix menjalankan pemeriksaan aman yang sama dengan disable.sh sebelum menonaktifkan apa pun (jaringan
Wi-Fi milik setupwraith, paket di bawah input HDMI/AV), karena update firmware justru saat hal itu
hidup lagi: update bisa menyalakan setupwraith di TV yang jaringannya miliknya, dan perbaikan yang
melewati pemeriksaan itu akan melepaskan TV dari Wi-Fi tanpa jalan masuk.

Kode keluar: 0 kalau selaras (atau sudah diperbaiki), 1 drift, perbaikan yang belum selesai, atau paket
yang dibiarkan nyala oleh pemeriksaan aman, 2 kalau TV tidak bisa dibaca.
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import recovery  # noqa: E402  (needs the tools/ directory on the path first)

# Same three special cases as disable.sh: these answer HOME but they are not launchers, so Home
# landing on one of them is the crash loop the Home guard exists to prevent, not a working Home.
NOT_LAUNCHERS = {"com.google.android.tungsten.setupwraith", "com.android.tv.settings",
                 "com.android.settings", "com.android.provision"}
# A vendor's own fallback is a placeholder the firmware shows when no launcher is enabled, not a home
# the viewer can use: TCL sets register com.tcl.keycustomfunctionservice/.FallbackHome (and
# com.android.tv.settings/.system.FallbackHome). disable.sh already refuses to count one as a
# launcher (#435); --fix must not move Home onto one either, or the next boot lands in the placeholder.
FALLBACK_HOME_RE = re.compile(r"/\S*FallbackHome$")
# A factory-test app is not one either: com.duokan.factorytest/.FactoryLauncher answers HOME on a
# Xiaomi Mi TV 4A, and Home on it is the factory test screen (disable.sh skips these too).
FACTORY_HOME_RE = re.compile(r"factorytest|factory_test|factorymode", re.I)
# The first-run wizard's app. Android deletes a Wi-Fi network saved by it the moment it is disabled.
SETUPWRAITH = "com.google.android.tungsten.setupwraith"
INPUT_PERMISSION = "android.permission.TV_INPUT_HARDWARE"
# Only the package's own "requested permissions" block counts: a vendor app in a shared UID would
# otherwise match through its shared user's permissions, the same reason disable.sh scopes its grep.
REQUESTED_RE = re.compile(r"requested permissions:(.*?)"
                          r"(?:\n\s*(?:install|runtime) permissions:|\n\s*User \d+:|$)", re.S)
HOME_QUERY = ("cmd package resolve-activity --brief -a android.intent.action.MAIN "
              "-c android.intent.category.HOME")
CLAIMS_QUERY = ("cmd package query-activities --brief -a android.intent.action.MAIN "
                "-c android.intent.category.HOME")


def home_of(component):
    return component.split("/", 1)[0] if component else None


def home_activities(dev):
    """Every HOME activity the TV can start, as package/activity, in the TV's order."""
    found = []
    for line in recovery.shell(dev, CLAIMS_QUERY).splitlines():
        candidate = line.strip()
        if recovery.HOME_RE.fullmatch(candidate):
            found.append(candidate)
    return found


def launcher_to_use(dev, listed, preferred=None):
    """The HOME activity --fix should switch to: asked for, or the only launcher the list keeps.

    After a firmware update both the stock launcher and the new one answer HOME. Every candidate
    that the list disables is a launcher this run is about to take Home away from, so what is left
    is the one the owner installed. A vendor FallbackHome answers HOME too and is not one of them.
    """
    if preferred:
        if not recovery.PACKAGE_RE.fullmatch(preferred):
            raise SystemExit(f"not a package name: {preferred!r}")
        match = [c for c in home_activities(dev) if home_of(c) == preferred]
        if not match:
            raise SystemExit(f"{preferred} tidak punya aktivitas HOME di TV ini: launcher-nya "
                             f"sudah terpasang? (launcher/build-and-install.sh)")
        return match[0]
    candidates = [c for c in home_activities(dev)
                  if home_of(c) not in listed and home_of(c) not in NOT_LAUNCHERS
                  and not FALLBACK_HOME_RE.search(c) and not FACTORY_HOME_RE.search(home_of(c))]
    if not candidates:
        return None
    own = [c for c in candidates if home_of(c) == recovery.CUSTOM_LAUNCHER]
    return own[0] if own else candidates[0]


def expected_launcher(installed, preferred=None):
    """The package Home should be on: the one asked for, else this repo's when it is installed.

    None when neither is installed: debloat without a launcher, where the stock launcher on Home is
    the intended state and only a Home on a listed package (or no Home at all) is drift.
    """
    wanted = preferred or recovery.CUSTOM_LAUNCHER
    return wanted if wanted in installed else None


def inspect(dev, listed, preferred=None):
    installed, disabled = recovery.query_packages(dev)
    home = recovery.query_home(dev)
    back_on = [name for name in listed if name in installed and name not in disabled]
    gone = [name for name in listed if name not in installed]
    # Home on a package the list disables is drift, and so is Home anywhere but the launcher we
    # expect: build-and-install.sh disables the stock launcher instead of naming it in the list, so
    # an update that turns it back on and hands it Home matches no line of the list at all (#434).
    expected = expected_launcher(installed, preferred)
    home_drifted = (home is None or home_of(home) in set(listed)
                    or (expected is not None and home_of(home) != expected))
    return installed, disabled, home, back_on, gone, home_drifted, expected


def report(list_path, listed, home, back_on, gone, home_drifted, expected=None):
    print(f"mengecek {list_path} ({len(listed)} paket) di {os.environ.get('TV_HOST', '?')}")
    if back_on:
        print(f"  back on ({len(back_on)}): {', '.join(back_on)}")
    if home_drifted:
        if not home:
            why = ""
        elif home_of(home) in set(listed):
            why = ", which this list disables"
        else:
            why = f", not {expected}"
        print(f"  Home is {home or 'nothing (no component answers HOME)'}{why}")
    if gone:
        # Not drift: the update may have removed an app for good, and there is nothing to disable.
        print(f"  not installed any more ({len(gone)}): {', '.join(gone)}")
    drift = len(back_on) + (1 if home_drifted else 0)
    if not drift:
        print(f"nothing drifted: {len(listed) - len(gone)} paket di daftar masih nonaktif dan "
              f"Home is {home}")
        return 0
    print(f"drift: {drift} dari {len(listed)}; terapkan lagi daftarnya dengan --fix, atau jalankan "
          f"tools/disable.sh dan skrip instal secara manual")
    return 1


def setupwraith_owns_wifi(dev):
    """Whether the saved Wi-Fi networks belong to setupwraith; None when the TV could not say."""
    owners = re.findall(r"cname=(\S*)", recovery.shell(dev, "dumpsys wifi"))
    if not owners:
        return None
    return any("setupwraith" in owner for owner in owners)


def asks_for_input_hardware(dev, name):
    """Whether the package itself asks for TV_INPUT_HARDWARE, i.e. it drives the HDMI/AV inputs."""
    block = REQUESTED_RE.search(recovery.shell(dev, f"dumpsys package {name}"))
    return bool(block and INPUT_PERMISSION in block.group(1))


def safety_checks(dev, names):
    """The names disable.sh would leave enabled, each with the reason, as a list of (name, why).

    These are the checks disable.sh runs, in the same order, repeated here for the same reason they
    exist there: each one is a package an owner cannot put back from the couch. FORCE_SETUPWRAITH=1
    and TVINPUT_ALLOW=1 mean what they mean in disable.sh, so a TV on Ethernet or an owner who wants
    the inputs off does not have to remember two switches.
    """
    skipped = []
    if SETUPWRAITH in names and not os.environ.get("FORCE_SETUPWRAITH"):
        owns = setupwraith_owns_wifi(dev)
        if owns is not False:
            # Be safe when the TV could not say: a wrong guess here is the TV off the network.
            why = ("jaringan Wi-Fi tersimpan milik paket ini, jadi menonaktifkannya menghapus jaringan itu dan "
                   "TV lepas dari Wi-Fi: di TV, lupakan jaringan itu lalu sambungkan lagi lewat "
                   "Settings > Network & Internet, kemudian jalankan --fix lagi (atau set "
                   "FORCE_SETUPWRAITH=1 kalau pakai Ethernet)" if owns else
                   "tidak bisa baca aplikasi mana yang punya jaringan Wi-Fi tersimpan (dumpsys wifi gagal atau "
                   "tidak menampilkan apa pun); set FORCE_SETUPWRAITH=1 kalau TV pakai Ethernet")
            skipped.append((SETUPWRAITH, why))
    if not os.environ.get("TVINPUT_ALLOW"):
        for name in names:
            if any(name == skipped_name for skipped_name, _ in skipped):
                continue
            if asks_for_input_hardware(dev, name):
                skipped.append((name, "paket ini minta android.permission.TV_INPUT_HARDWARE, jadi ini "
                                      "lapisan perangkat lunak di bawah input HDMI/AV/tuner: kalau "
                                      "dinonaktifkan port itu tetap hitam dan hanya restore penuh plus "
                                      "cold boot yang mengembalikannya (set TVINPUT_ALLOW=1 untuk menonaktifkan "
                                      "nya)"))
    return skipped


def fix(dev, listed, back_on, home_drifted, preferred):
    failed = False
    # Home first: on Google TV a disabled stock launcher with nothing to replace it is the crash
    # loop the Home guard exists to prevent, so a Home that cannot be fixed stops the run before
    # anything is disabled.
    if home_drifted:
        target = launcher_to_use(dev, listed, preferred)
        if not target:
            print("Home: tidak ada yang bisa dituju: launcher belum terpasang, atau tidak ada yang menjawab "
                  "HOME selain paket yang daftar ini nonaktifkan. Jalankan launcher/build-and-install.sh, "
                  "lalu cek lagi; tidak ada yang diubah di TV.")
            return 1
        recovery.shell(dev, f"cmd package set-home-activity --user 0 {target}")
        home = recovery.query_home(dev)
        if home != target:
            print(f"Home: diminta {target} tapi TV masih mengarah ke {home or 'nothing'}; "
                  f"reboot lalu cek lagi")
            failed = True
        else:
            print(f"Home: set to {target}")
    left_on = []
    if back_on:
        left_on = safety_checks(dev, back_on)
        for name, why in left_on:
            print(f"leaving {name} on: {why}")
        todo = [name for name in back_on if name not in {n for n, _ in left_on}]
        for name in todo:
            recovery.shell(dev, f"pm disable-user --user 0 {name}")
        # Read the TV back instead of trusting the command's silence: a package the firmware pins
        # stays enabled and the run has to say so.
        _, disabled = recovery.query_packages(dev)
        still_on = [name for name in todo if name not in disabled]
        if still_on:
            failed = True
            print(f"still enabled after --fix: {', '.join(still_on)} (firmware mungkin mengunci mereka; "
                  f"jalankan daftarnya manual setelah reboot berikutnya)")
        elif todo:
            print(f"disabled {len(todo)} package(s) yang update hidupkan lagi")
        if left_on:
            # Not a failure of the fix, but not a clean run either: the list asks for something this
            # TV cannot give up safely, and the next check reports the same drift until the owner
            # clears the obstacle (the Wi-Fi network) or accepts it (TVINPUT_ALLOW=1).
            print(f"membiarkan {len(left_on)} package(s) tetap nyala padahal daftar ini menonaktifkannya; TV is not fully "
                  f"back to the list sampai itu dibereskan")
    return 1 if failed or left_on else 0


def main():
    args = sys.argv[1:]
    wanted = "--fix" in args
    args = [a for a in args if a != "--fix"]
    if not 1 <= len(args) <= 2:
        sys.stderr.write("cara pakai: check.py [--fix] <debloat list> [launcher package]\n")
        return 2
    list_path = args[0]
    preferred = args[1] if len(args) == 2 else None
    # read_list validates every name before anything is sent to the TV: its entries end up in the
    # TV's shell, and a list edited by hand since then is not to be trusted.
    listed = recovery.read_list(list_path)
    core = recovery.core_packages(listed)
    if core:
        # The same refusal as disable.sh: --fix would disable these again (#884).
        sys.stderr.write(f"Menolak {list_path}: daftar ini menonaktifkan paket Android inti, dan tanpa paket itu "
                         f"TV bisa gagal nyala lagi (#884): {', '.join(core)}. Hapus baris ini "
                         f"dari daftar lalu jalankan lagi. Tidak ada yang dicek atau diubah.\n")
        return 2

    try:
        dev = recovery.connect()
    except (OSError, ConnectionError) as e:
        sys.stderr.write(f"could not reach the TV ({e}). Cek TV-nya nyala dan TV_HOST sudah benar "
                         f"(tools/venv/bin/python -I tools/tv.py --probe).\n")
        return 2
    try:
        _, _, home, back_on, gone, home_drifted, expected = inspect(dev, listed, preferred)
        code = report(list_path, listed, home, back_on, gone, home_drifted, expected)
        if code and wanted:
            code = fix(dev, listed, back_on, home_drifted, preferred)
    finally:
        dev.close()
    return code


if __name__ == "__main__":
    sys.exit(main())
