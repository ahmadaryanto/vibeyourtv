#!/usr/bin/env python3
"""Apa yang TV ini jalankan untuk suara, ACR, dan telemetri, beserta bukti tiap nama (#444).

    privacy.py                      audit TV yang tersambung dan cetak laporannya
    privacy.py --write <file>       tulis kandidat sebagai daftar debloat untuk tools/disable.sh
    privacy.py --write <file> --include-low
                                    juga paket yang hanya cocok dari namanya

Yang jujur adalah buktinya, bukan kategorinya. Di firmware yang sebagian besar paketnya jalan di bawah
`android.uid.system`, sekitar lima puluh di antaranya "punya" RECORD_AUDIO dan tidak ada yang merekam;
pemindaian izin per paket akan menuduh kelima puluh itu pendengar. Jadi audit ini menyebut nama yang
ditunjuk firmware sendiri, komponen yang sudah dikenal beserta tugasnya, yang hanya cocok dari nama,
dan yang punya jejak akses mikrofon di riwayat AppOps yang kembali. Audit ini tidak pernah mengklaim
sebuah paket sedang mendengarkan.

Penonaktifan diserahkan ke `tools/disable.sh`, jadi jalur undo-nya sama dengan daftar lain: catatan
yang ditulis sebelum perubahan pertama, diputar ulang oleh `tools/restore.sh`.
"""
import os
import re
import sys

# Names from the TV end up inside commands run by its shell, so nothing that could carry shell syntax
# goes through. Same rule as tools/recovery.py.
PACKAGE_RE = re.compile(r"[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+")

# What we know about specific packages, from reports in examples/ and docs/ rather than from the name.
# `category` drives the section in the report; `note` is what the owner reads before disabling it.
CATALOGUE = {
    "com.google.android.katniss": (
        "voice",
        "Google Assistant dan pencarian suara; tombol mic di remote dan pencarian suara di dalam aplikasi mati tanpanya",
    ),
    "com.google.android.speech.pumpkin": (
        "voice",
        "model hotword offline, tidak terpakai begitu asisten di atas dimatikan",
    ),
    "org.droidtv.amazonalexa": (
        "voice",
        "Amazon Alexa di set Philips; mikrofonnya ada di remote, bukan di panel",
    ),
    "tv.inscape.tvclient.inscapeacr": (
        "acr",
        "klien Inscape ACR: mengenali apa yang ada di layar, termasuk HDMI, dari gambarnya, dan hanya butuh "
        "INTERNET, not RECORD_AUDIO",
    ),
    "com.skyworth.aisense": (
        "acr",
        "ACR Coocaa/Skyworth, dipakai untuk menarget iklan yang didorong ke set",
    ),
    "org.droidtv.candeebug": (
        "telemetry",
        "analitik \"DAC\" di set Philips",
    ),
    "org.droidtv.gel": (
        "telemetry",
        "analitik \"GEL\"; jalan di dalam proses candeebug, jadi keduanya ikut",
    ),
    "org.droidtv.usagelogger": (
        "telemetry",
        "statistik boot, jumlah tombol remote, dan telemetri panel",
    ),
    "org.droidtv.tv.frameworklogger": (
        "telemetry",
        "log framework dan reboot",
    ),
    "com.google.android.feedback": (
        "telemetry",
        "laporan masukan ke Google",
    ),
}

# A package found only by its name is reported apart and never written into the list by default:
# `com.example.voicecontrol` may be the panel's own audio routing, and nothing here can tell. The
# stem matches at the start of a name segment, which catches `voicecontrol` and `micd` while leaving
# `vending` or `playground` alone; the probe cap keeps the noise bounded.
NAME_HINT = re.compile(
    r"(?:^|\.)(acr|analytics|assistant|alexa|hotword|inscape|listen|microphone|privacy|speech|"
    r"telemetry|tracker|tracking|voice)",
    re.IGNORECASE,
)

VOICE_SETTINGS = ("voice_interaction_service", "voice_recognition_service", "assistant")
TITLES = (
    ("voice", "Voice assistants and wake word — asisten suara dan kata pemicu"),
    ("acr", "ACR (identifies what is on screen, no microphone needed) — mengenali layar, tanpa mikrofon"),
    ("telemetry", "Telemetry and analytics — telemetri dan analitik"),
    ("unknown", "Matched by name only, nothing else known — hanya cocok dari nama, yang lain tidak diketahui"),
)
# `dumpsys package` is slow on old firmware; a TV with fifty name matches is not worth ten minutes.
MAX_PROBES = 25
SYSTEM_UID = 1000


def shell(dev, command):
    return (dev.shell(command, read_timeout_s=120.0) or "").strip()


def connect():
    # Imported here so the module can be imported (tests, --help) without adb_shell installed.
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    try:
        import tv
    except ImportError as e:
        raise SystemExit(f"tidak bisa memuat tools/tv.py ({e}).\njalankan dulu: python3 -m venv tools/venv && "
                         "tools/venv/bin/pip install -r tools/requirements.txt")
    try:
        return tv.connect()
    except Exception as e:
        raise SystemExit(f"cannot reach the TV di {os.environ.get('TV_HOST', 'TV_HOST')}: {e}\n"
                         "Cek TV_HOST dan USB debugging masih nyala (tools/tv.py --probe).")


def query_packages(dev):
    installed = []
    for line in shell(dev, "pm list packages").splitlines():
        if not line.startswith("package:"):
            continue
        name = line.split(":", 1)[1].strip()
        if PACKAGE_RE.fullmatch(name) and name not in installed:
            installed.append(name)
    if not installed:
        raise SystemExit("TV returned no packages, jadi tidak ada yang bisa diaudit. Cek "
                         "koneksinya (tools/tv.py --probe) lalu coba lagi.")
    disabled = {line.split(":", 1)[1].strip()
                for line in shell(dev, "pm list packages -d").splitlines()
                if line.startswith("package:")}
    return installed, disabled


def voice_setting_owners(dev):
    """package name -> the secure settings that name it (the firmware's own pick for the assistant)."""
    owners = {}
    for key in VOICE_SETTINGS:
        value = shell(dev, f"settings get secure {key}")
        if not value or value == "null" or "/" not in value:
            continue
        package = value.split("/", 1)[0].strip()
        if PACKAGE_RE.fullmatch(package):
            owners.setdefault(package, []).append(key)
    return owners


def probe(dev, name):
    """What the package declares about the microphone, and its returned AppOps history."""
    dump = shell(dev, f"dumpsys package {name}")
    uid = None
    declared = None
    if dump:
        found = re.search(r"\buserId=(\d+)", dump)
        uid = int(found.group(1)) if found else None
        declared = "RECORD_AUDIO" in dump
    ops = shell(dev, f"appops get {name} RECORD_AUDIO")
    return {"uid": uid, "declared": declared, "appops": ops}


def used_microphone(ops):
    """True for recorded access, False for no history, None for unknown output."""
    if not ops or "error" in ops.lower() or "exception" in ops.lower():
        return None
    lines = [line.strip() for line in ops.splitlines()]
    for line in lines:
        if not line.startswith("RECORD_AUDIO: "):
            continue
        # A mode (including a UID override) or rejectTime is not evidence of access.
        # AppOps prints time only for a recorded access, using TimeUtils.formatDuration.
        if line.endswith(" (running)") or re.search(
                r"; time=(?:0|[+-](?:\d+[dhms])*\d+ms) ago(?:;| \(running\)|$)", line):
            return True
    return False if "No operations." in lines else None


def classify(name, owners, probed):
    """(category, note, evidence lines) for one package."""
    category, note = CATALOGUE.get(name, ("unknown", "matched by name only (hanya cocok dari namanya)"))
    evidence = []
    if name in owners:
        # The firmware nominated it, which outranks both the catalogue and the name.
        category = "voice" if category == "unknown" else category
        if name not in CATALOGUE:
            note = "layanan ucapan milik TV sendiri"
        evidence.append(f"named by the TV: {', '.join(owners[name])}")

    uid = probed.get("uid")
    if uid == SYSTEM_UID:
        # About fifty packages on such firmware show RECORD_AUDIO granted because they share the
        # system UID; listing that per package is how a permission scan ends up crying wolf.
        evidence.append("shares the system UID (uid 1000): izin yang kelihatan milik sistem, "
                        "bukan bukti paket ini merekam")
    elif probed.get("declared"):
        evidence.append("declares RECORD_AUDIO")

    used = used_microphone(probed.get("appops", ""))
    if used is False:
        evidence.append("appops: no RECORD_AUDIO history returned (tidak ada jejak yang kembali)")
    elif used is True:
        evidence.append("appops: recorded RECORD_AUDIO access di riwayat yang kembali "
                        "(baca lengkapnya dengan `appops get <package> RECORD_AUDIO`)")
    else:
        evidence.append("appops: RECORD_AUDIO access is unknown dari keluaran ini")
    return category, note, evidence


def audit(dev):
    installed, disabled = query_packages(dev)
    owners = voice_setting_owners(dev)

    candidates = [name for name in installed if name in CATALOGUE or name in owners]
    hinted = [name for name in installed if name not in candidates and NAME_HINT.search(name)]
    probes = (candidates + hinted)[:MAX_PROBES]

    entries = []
    for name in probes:
        category, note, evidence = classify(name, owners, probe(dev, name))
        entries.append({
            "name": name,
            "category": category,
            "state": "disabled" if name in disabled else "enabled",
            "note": note,
            "evidence": evidence,
        })
    return {
        "installed": installed,
        "disabled": disabled,
        "entries": entries,
        "skipped": max(0, len(candidates) + len(hinted) - len(probes)),
    }


def sections(entries):
    for category, title in TITLES:
        group = [e for e in entries if e["category"] == category]
        if group:
            yield title, group


def print_report(result):
    installed = len(result["installed"])
    enabled = installed - len(result["disabled"])
    print(f"Audit TV di {os.environ.get('TV_HOST', 'TV_HOST')}: {installed} paket, "
          f"{enabled} aktif.\n")
    for title, group in sections(result["entries"]):
        print(title)
        for entry in group:
            print(f"  {entry['state']:<9} {entry['name']}")
            print(f"            {entry['note']}")
            for line in entry["evidence"]:
                print(f"            {line}")
        print()
    if not result["entries"]:
        print("Tidak ada asisten suara, klien ACR, atau paket telemetri yang dikenali dari nama di TV ini.")
        print("Itu bukan surat sehat: artinya tidak ada nama di sini yang dikenali.")
        print()
    if result["skipped"]:
        print(f"{result['skipped']} kecocokan nama lagi tidak diperiksa (audit berhenti di "
              f"{MAX_PROBES}); cek manual dengan `dumpsys package <name>`.")
        print()
    print("Apa yang bisa dilakukan")
    print("  tools/privacy.py --write privacy.txt        # baris suara, ACR, dan telemetri di atas")
    print("  TV_HOST=<tv ip> tools/disable.sh privacy.txt   # menerapkan, catatan undo ditulis dulu")
    print("  TV_HOST=<tv ip> tools/restore.sh privacy.txt   # undo")
    print()
    print("What this audit cannot tell you: whether audio ever leaves the TV. `appops` hanya menunjukkan op "
          "terjamah, bukan apa yang dilakukan dengan alirannya, dan di banyak set the microphone is in the "
          "remote rather than the panel. Tidak ada di daftar ini yang terbukti merekam, dan tidak ada "
          "yang membuktikan sebaliknya.")


def list_lines(result, include_low):
    """The debloat list for the candidates the audit actually identified."""
    keep = [e for e in result["entries"]
            if e["category"] != "unknown" or include_low]
    if not keep:
        raise SystemExit("nothing identified to write: yang ketemu hanya kecocokan nama. Jalankan audit "
                         "lagi untuk membacanya, atau kasih --include-low untuk menulis itu juga.")
    host = os.environ.get("TV_HOST", "")
    lines = [
        f"# Audit privasi TV di {host or 'TV_HOST'}, ditulis oleh tools/privacy.py.",
        "# Kandidat suara, ACR, dan telemetri yang TV ini jawab. Semuanya DINONAKTIFKAN, tidak",
        "# di-uninstall, dan undo-nya sama dengan daftar lain:",
        f"#   TV_HOST={host or '<tv ip>'} tools/disable.sh privacy.txt",
        f"#   TV_HOST={host or '<tv ip>'} tools/restore.sh privacy.txt",
        "#",
        "# Tiap baris menjelaskan paketnya dan kenapa ada di sini. Tidak ada di bawah yang terbukti merekam",
        "# apa pun: audit melaporkan apa yang firmware dan paket nyatakan.",
    ]
    for title, group in sections(keep):
        lines.append("")
        lines.append(f"# {title}")
        for entry in group:
            if entry["state"] == "disabled":
                # Already off: writing it would only make restore.sh re-enable what the owner
                # disabled on purpose.
                continue
            why = entry["note"]
            if entry["evidence"]:
                why += f" ({entry['evidence'][0]})"
            lines.append(f"{entry['name']:<45} # {why}")
    return lines


def write_list(path, result, include_low):
    if os.path.exists(path):
        # A second audit of the same TV may legitimately differ, but silently replacing a list the
        # owner is about to apply is how a hand edit disappears.
        raise SystemExit(f"{path} already exists; pindahkan atau pilih nama lain.")
    lines = list_lines(result, include_low)
    with open(path, "w") as f:
        f.write("\n".join(lines) + "\n")
    names = sum(1 for line in lines if line and not line.startswith("#"))
    print(f"menulis {names} paket ke {path}")
    print(f"terapkan:   TV_HOST={os.environ.get('TV_HOST', '<tv ip>')} tools/disable.sh {path}")
    print(f"undo:    TV_HOST={os.environ.get('TV_HOST', '<tv ip>')} tools/restore.sh {path}")
    print("baca dulu: komentarnya menjelaskan apa yang ikut hilang.")


def main():
    args = sys.argv[1:]
    include_low = "--include-low" in args
    args = [a for a in args if a != "--include-low"]
    if args and (len(args) != 2 or args[0] != "--write"):
        sys.stderr.write("usage: privacy.py                       audit TV yang tersambung\n"
                         "       privacy.py --write <file> [--include-low]\n"
                         "                                    tulis kandidat sebagai daftar debloat\n")
        return 2

    dev = connect()
    try:
        result = audit(dev)
    finally:
        dev.close()

    if args:
        write_list(args[1], result, include_low)
    else:
        print_report(result)
    return 0


if __name__ == "__main__":
    sys.exit(main())
