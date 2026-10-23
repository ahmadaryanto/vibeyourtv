#!/usr/bin/env python3
"""Keadaan yang akan diubah daftar debloat, supaya undo benar-benar undo (#239).

    recovery.py --save <debloat list>                baca TV dan tulis catatannya
    recovery.py --commands <list> [stock launcher]   cetak batch untuk tools/tv.py --batch yang
                                                     mengembalikan keadaan tercatat

`tools/restore.sh` dulu mengaktifkan setiap paket di daftar, termasuk yang memang sudah
dinonaktifkan pemiliknya, dan mengembalikan Home serta skala animasi ke nilai tetap, bukan ke
yang ada sebelumnya. Kedua skrip sekarang lewat catatan ini: `disable.sh` menulisnya sebelum
perintah pertama sampai ke TV, dan `restore.sh` memutarnya ulang.

Catatan ada di ~/.cleanyourtv/, satu per TV dan daftar. Yang pertama yang menang: percobaan ulang
setelah daftar setengah jalan tidak boleh mencatat keadaan rusak sebagai yang asli. Jalanan
berikutnya hanya menambah nama yang baru masuk daftar, dengan keadaan sebelum daftar ini menonaktifkannya.
"""
import hashlib
import json
import os
from pathlib import Path
import re
import sys
import time

# Every name from the TV or from the list ends up inside a command run by the TV's shell, so nothing
# that could carry shell syntax is allowed through.
PACKAGE_RE = re.compile(r"[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+")
HOME_RE = re.compile(r"[A-Za-z_][\w.]*/[\w.$]+")
SCALE_RE = re.compile(r"-?\d+(\.\d+)?")
SETTINGS = ("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
# This project's own launcher. When it already owned Home before debloating, restoring Home to it
# would leave the stock launcher off, so restore goes back to the stock one instead.
CUSTOM_LAUNCHER = "com.gathin.tvhome"
VERSION = 1
# Core Android packages no list may disable: with one of these off, Android itself can stop booting
# (#884: an original Mi TV Stick that lost its permission controller, package installer, ext services,
# module metadata and mainline overlays never came back). The content providers in CORE_OK are the
# exception, since tested lists turn them off and no TV feature uses them. disable.sh and disable.ps1
# carry the same two patterns (tests/test_core_packages.py keeps the three in step).
CORE_RE = re.compile(r"^(android|com\.android\.systemui|com\.android\.shell|com\.android\.phone|com\.android\.settings|com\.android\.tv\.settings|com\.(google\.)?android\.(permissioncontroller|packageinstaller|ext\.services|ext\.shared|modulemetadata)|com\.google\.android\.overlay\.modules.*|com\.android\.providers\..*)$")
CORE_OK = re.compile(r"^com\.android\.providers\.(calendar|contacts|userdictionary|blockednumber)$")


def core_packages(names):
    """The names in a list that must never be disabled, in list order."""
    return [n for n in names if CORE_RE.match(n) and not CORE_OK.match(n)]


def read_list(path):
    """Package names from a debloat list, in file order.

    A BOM and CRLF from a Windows editor are dropped, as is a trailing `# note`; anything that is not
    a bare package name stops the run before the TV is touched.
    """
    text = Path(path).read_text(encoding="utf-8-sig", errors="replace")
    names = []
    for number, line in enumerate(text.splitlines(), 1):
        line = line.split("#", 1)[0].strip()
        if not line:
            continue
        name = line.split()[0]
        if not PACKAGE_RE.fullmatch(name):
            raise SystemExit(f"{path}:{number}: not a package name: {name!r}")
        names.append(name)
    if not names:
        raise SystemExit(f"{path}: no package names di daftar ini")
    return names


def record_path(list_path, host):
    """One record per TV and list. The list is identified by its absolute path, so a copy of the
    same file somewhere else gets its own record."""
    tag = re.sub(r"[^A-Za-z0-9.-]", "_", host or "no-host")
    digest = hashlib.sha1(str(Path(list_path).resolve()).encode()).hexdigest()[:8]
    return Path.home() / ".cleanyourtv" / f"recovery-{tag}-{digest}.json"


def load_record(path):
    try:
        with open(path) as f:
            record = json.load(f)
    except (OSError, ValueError) as e:
        raise SystemExit(f"{path}: not a usable recovery record ({e}).\n"
                         "File-nya dibiarkan di tempat: pindahkan sengaja, percobaan ulang tidak boleh "
                         "mengganti keadaan yang dicatat sebelum perubahan pertama.")
    if record.get("version") != VERSION or not isinstance(record.get("packages"), dict):
        raise SystemExit(f"{path}: format catatan pemulihan tidak dikenal; pindahkan sengaja.")
    # The record is replayed on the TV's shell, so it gets the same checks as the list and the TV's
    # answers did when it was written: a hand-edited or damaged file stops here, before any command.
    for name, state in record["packages"].items():
        if not PACKAGE_RE.fullmatch(name):
            raise SystemExit(f"{path}: not a usable recovery record (not a package name: {name!r}); "
                             "pindahkan sengaja.")
        if state not in ("absent", "disabled", "enabled"):
            raise SystemExit(f"{path}: not a usable recovery record (keadaan {name} tidak dikenal: "
                             f"{state!r}); pindahkan sengaja.")
    home = record.get("home")
    if home is not None and not (isinstance(home, str) and HOME_RE.fullmatch(home)):
        raise SystemExit(f"{path}: not a usable recovery record (bukan komponen Home: {home!r}); "
                         "pindahkan sengaja.")
    if not isinstance(record.get("settings", {}), dict):
        raise SystemExit(f"{path}: not a usable recovery record (settings bukan pemetaan); "
                         "pindahkan sengaja.")
    return record


def write_record(path, record):
    # Written next to its final name first: a crash halfway through must not leave a truncated record
    # that then blocks every retry as "already recorded".
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_name(path.name + ".tmp")
    with open(tmp, "w") as f:
        json.dump(record, f, indent=2, sort_keys=True)
        f.write("\n")
        f.flush()
        os.fsync(f.fileno())
    os.replace(tmp, path)


def shell(dev, command):
    return (dev.shell(command, read_timeout_s=120.0) or "").strip()


def query_packages(dev):
    installed = {line.split(":", 1)[1].strip()
                 for line in shell(dev, "pm list packages").splitlines()
                 if line.startswith("package:")}
    if not installed:
        raise SystemExit("TV returned no packages, jadi tidak ada yang dicatat dan tidak ada yang akan "
                         "dinonaktifkan. Cek koneksinya (tools/tv.py --probe) lalu coba lagi.")
    disabled = {line.split(":", 1)[1].strip()
                for line in shell(dev, "pm list packages -d").splitlines()
                if line.startswith("package:")}
    return installed, disabled


def query_home(dev):
    answer = shell(dev, "cmd package resolve-activity --brief -a android.intent.action.MAIN -c "
                        "android.intent.category.HOME")
    candidate = answer.splitlines()[-1].strip() if answer else ""
    # Android answers "android" (the chooser) when nothing handles HOME; that is not a component and
    # must never be written into a set-home-activity command.
    return candidate if HOME_RE.fullmatch(candidate) else None


def connect():
    # Imported here so --commands works on a machine where adb_shell is not installed: printing the
    # restore batch needs no TV. tv.py itself exits with a readable message when TV_HOST is unset.
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    try:
        import tv
    except ImportError as e:
        raise SystemExit(f"tidak bisa memuat tools/tv.py ({e}).\njalankan dulu: python3 -m venv tools/venv && "
                         "tools/venv/bin/pip install -r tools/requirements.txt")
    return tv.connect()


def package_state(name, installed, disabled):
    return ("absent" if name not in installed
            else "disabled" if name in disabled
            else "enabled")


def save(list_path, host):
    path = record_path(list_path, host)
    if path.exists():
        record = load_record(path)
        packages = read_list(list_path)
        # The list may have been edited since the record was taken. Entries already in the record are
        # never overwritten (a retry must not record the broken state as the original), but names it
        # does not hold yet have not been touched by this list, so their state now is their state
        # before cleanup. Without them, restore would leave those packages disabled for good.
        added = [name for name in packages if name not in record["packages"]]
        print(f"keeping the recovery record dari {record.get('created')}: {path}")
        if not added:
            print("tidak ada yang dicatat lagi: isinya keadaan sebelum jalanan pertama.")
            return 0
        dev = connect()
        try:
            installed, disabled = query_packages(dev)
        finally:
            dev.close()
        for name in added:
            record["packages"][name] = package_state(name, installed, disabled)
        write_record(path, record)
        print(f"menambah {len(added)} nama yang masuk daftar sejak itu, entri yang sudah tercatat "
              f"tidak diubah: {', '.join(added)}")
        return 0

    packages = read_list(list_path)
    dev = connect()
    try:
        installed, disabled = query_packages(dev)
        home = query_home(dev)
        scales = {key: shell(dev, f"settings get global {key}") for key in SETTINGS}
    finally:
        dev.close()

    # The launcher that owns Home may not be in the list, but it is what undo has to put back.
    home_package = home.split("/", 1)[0] if home else None
    targets = packages + ([home_package] if home_package and home_package not in packages else [])
    record = {
        "version": VERSION,
        "created": time.strftime("%Y-%m-%dT%H:%M:%S%z"),
        "host": host,
        "list": str(Path(list_path).resolve()),
        "home": home,
        "settings": scales,
        "packages": {name: package_state(name, installed, disabled) for name in targets},
    }
    write_record(path, record)

    already = sum(1 for state in record["packages"].values() if state == "disabled")
    print(f"mencatat {len(record['packages'])} paket di {host} "
          f"({already} sudah nonaktif, dibiarkan oleh undo) -> {path}")
    print(f"HOME sebelumnya: {home or 'tidak ada komponen yang menjawab'}")
    print("balikin dengan tools/restore.sh <daftar yang sama>; simpan file ini, ini satu-satunya salinan.")
    return 0


def restore_commands(list_path, stock, host):
    """The batch that puts the recorded states back, one command per line."""
    packages = read_list(list_path)
    if stock and not PACKAGE_RE.fullmatch(stock):
        raise SystemExit(f"not a package name: {stock!r}")
    path = record_path(list_path, host)
    commands = []

    if not path.exists():
        sys.stderr.write(
            f"no recovery record untuk TV dan daftar ini ({path}).\n"
            "Semua paket di daftar akan diaktifkan, dan Home serta skala animasi dikembalikan ke\n"
            "nilai bawaan. Paket yang sudah nonaktif sebelum pembersihan ikut diaktifkan:\n"
            "cek daftarnya sebelum menjalankan ini.\n")
        for name in packages:
            commands.append(f"pm enable {name}")
        if stock:
            commands.append(f"pm enable {stock}")
        commands.append(f"cmd package set-home-activity --user 0 $(cmd package resolve-activity "
                        f"--brief -a android.intent.action.MAIN -c android.intent.category.HOME "
                        f"{stock or 'com.google.android.tvlauncher'} | tail -n 1)")
        commands.extend(f"settings put global {key} 1.0" for key in SETTINGS)
        commands.append("pm list packages -d")
        return commands

    record = load_record(path)
    states = record["packages"]
    re_enable = [name for name, state in states.items() if state == "enabled"]
    for name in re_enable:
        commands.append(f"pm enable {name}")
    untouched = [(name, state) for name, state in states.items() if state != "enabled"]
    for name, state in untouched:
        sys.stderr.write(f"  leaving {name} alone ({state} saat catatan diambil)\n")

    # The stock launcher comes back unless the owner had disabled it before cleanup: the list may
    # have disabled it even when it was not the recorded Home (e.g. the custom launcher owned Home).
    fallback = stock or "com.google.android.tvlauncher"
    if states.get(fallback) != "disabled" and fallback not in re_enable:
        commands.append(f"pm enable {fallback}")

    home = record.get("home")
    if home and home.split("/", 1)[0] != CUSTOM_LAUNCHER:
        commands.append(f"cmd package set-home-activity --user 0 {home}")
    else:
        # Nothing handled HOME before cleanup (fresh TV, chooser only), or this project's launcher
        # did, so there is no stock component to replay; ask the TV to resolve the stock launcher the
        # way restore.sh always did.
        commands.append(f"cmd package set-home-activity --user 0 $(cmd package resolve-activity "
                        f"--brief -a android.intent.action.MAIN -c android.intent.category.HOME "
                        f"{fallback} | tail -n 1)")
    for key in SETTINGS:
        value = str(record.get("settings", {}).get(key, "")).strip()
        if value == "null":
            commands.append(f"settings delete global {key}")
        elif SCALE_RE.fullmatch(value):
            commands.append(f"settings put global {key} {value}")
        else:
            sys.stderr.write(f"  melewati {key}: catatan menyimpan {value!r}, bukan angka\n")
    # disable.sh adds names the list gained to the record before disabling them, so a name still
    # missing here was added after the last disable run and never disabled by it: leave it alone.
    edited = [name for name in packages if name not in states]
    if edited:
        sys.stderr.write(f"not in the record, leaving alone: {', '.join(edited)}\n")
    commands.append("pm list packages -d")

    sys.stderr.write(f"mengembalikan keadaan yang dicatat pada {record.get('created')}: "
                     f"mengaktifkan lagi {len(re_enable)}, membiarkan {len(untouched)} seperti semula.\n")
    return commands


def main():
    args = sys.argv[1:]
    host = os.environ.get("TV_HOST", "")
    if len(args) == 2 and args[0] == "--save":
        return save(args[1], host)
    if len(args) in (2, 3) and args[0] == "--commands":
        if not host:
            sys.stderr.write("Set TV_HOST to your TV's IP address, misalnya TV_HOST=192.168.1.50\n")
        stock = args[2] if len(args) == 3 else "com.google.android.tvlauncher"
        for command in restore_commands(args[1], stock, host):
            print(command)
        return 0
    sys.stderr.write("cara pakai: recovery.py --save <debloat list>\n"
                     "       recovery.py --commands <debloat list> [stock launcher package]\n")
    return 2


if __name__ == "__main__":
    sys.exit(main())
