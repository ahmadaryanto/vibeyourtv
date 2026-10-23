#!/usr/bin/env python3
"""Rekam jejak Perfetto di TV di sekitar satu skenario launcher, untuk lihat waktu habis di mana.

tools/bench.py memberi angka utama (latensi tombol, jank, cold start, kembali ke Home, PSS) tanpa
instal tambahan. Ini merekam linimasa lengkap: irisan tiap thread, siklus hidup launcher
(onCreate, onResume, layout, draw) dan linimasa frame, supaya perubahan bisa ditelusuri ke kerja
yang dihilangkan. tools/trace_report.py mengubah folder jejak ini jadi angka. Baca
tools/trace.md dulu: di sana skenario mana yang dipakai dan apa yang bisa menyesatkan.

Merekam tidak butuh apa pun di luar tools/requirements.txt; klien perfetto sudah ada di Android 9
ke atas. Set TV_HOST ke IP TV (dan TV_PORT kalau bukan 5555). Usage:

    tools/trace.py home --runs 5 --out traces/before
    tools/trace.py cold|coldsys|home|nav --runs N --out DIR [--package PKG] [--app COMPONENT]
    tools/trace.py custom --setup 'input keyevent KEYCODE_HOME' --setup 'sleep 2' \\
                          --step 'input keyevent KEYCODE_DPAD_RIGHT' --step 'sleep 1' --out DIR

Skenario (setup jalan sebelum rekaman mulai, jadi tidak masuk jejak):
    cold     aplikasi lain di depan, launcher di-force-stop, lalu dimulai dengan `am start -W`
    coldsys  Home di depan, launcher di-force-stop: Android menyalakannya lagi sendiri
    home     aplikasi lain di depan, lalu tombol HOME: kembali ke Home sehari-hari
    nav      Home di depan, lalu 24 gerak D-pad berjarak 0,35 dtk
    custom   perintah shell --setup dan --step milikmu; langkah `sleep N` menentukan lamanya
"""
import argparse
import base64
import json
import os
import re
import sys
import time

PACKAGE = re.compile(r"^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z][A-Za-z0-9_]*)+$")
COMPONENT = re.compile(r"^[A-Za-z][A-Za-z0-9_.]*/[A-Za-z0-9_.$]+$")
REMOTE = "/data/misc/perfetto-traces/cleanyourtv.pftrace"   # the folder perfetto may write to on Android 9 and 10
NAV_KEYS = ("RIGHT RIGHT RIGHT LEFT LEFT LEFT DOWN RIGHT DOWN LEFT DOWN UP "
            "UP RIGHT UP LEFT DOWN DOWN RIGHT RIGHT UP UP UP LEFT").split()
START_MARGIN_S = 3    # recording starts in the background; give it time before the first step
END_MARGIN_S = 2

def _varint(value):
    """Unsigned protobuf integer; the config only uses non-negative values."""
    out = bytearray()
    while value > 127:
        out.append((value & 127) | 128)
        value >>= 7
    out.append(value)
    return bytes(out)


def _field(number, value):
    """The config's two wire types: unsigned integers and length-delimited strings/messages."""
    if isinstance(value, str):
        value = value.encode("utf-8")
    if isinstance(value, bytes):
        return _varint((number << 3) | 2) + _varint(len(value)) + value
    return _varint(number << 3) + _varint(value)


def perfetto_config(package, duration_ms):
    """Binary TraceConfig: Android P has no --txt parser; later versions accept binary too.

    Field numbers come from the Android P schema, including FtraceConfig and ProcessStatsConfig:
    https://android.googlesource.com/platform/external/perfetto/+/android-9.0.0_r1/protos/perfetto/config/
    """
    ftrace = b"".join(_field(1, event) for event in (
        "sched/sched_switch", "sched/sched_wakeup", "sched/sched_waking",
        "power/cpu_frequency", "power/cpu_idle", "task/task_newtask", "task/task_rename",
        "sched/sched_process_exit"))
    ftrace += b"".join(_field(2, category) for category in (
        "am", "wm", "view", "gfx", "input", "dalvik", "res", "binder_driver"))
    # FtraceConfig.atrace_apps=3, buffer_size_kb=10, drain_period_ms=11.
    ftrace += _field(3, package) + _field(10, 16384) + _field(11, 250)
    sources = (
        _field(1, "linux.ftrace") + _field(2, 0) + _field(100, ftrace),
        _field(1, "linux.process_stats") + _field(2, 1) + _field(103, _field(2, 1)),  # scan_all_processes_on_start
        _field(1, "android.surfaceflinger.frametimeline"),
        _field(1, "android.packages_list"),
    )
    # TraceConfig.buffers=1, data_sources=2, duration_ms=3. Each DataSource.config=1.
    # BufferConfig.size_kb=1 and fill_policy=4 (RING_BUFFER=1).
    buffers = b"".join(_field(1, _field(1, size) + _field(4, 1)) for size in (65536, 4096))
    return buffers + b"".join(_field(2, _field(1, source)) for source in sources) + _field(3, int(duration_ms))


def recording_ms(steps):
    """How long to record: the steps' sleeps plus a margin either side."""
    slept = sum(float(s.split()[1]) for s in steps if re.fullmatch(r"sleep \d+(\.\d+)?", s.strip()))
    return int((START_MARGIN_S + slept + END_MARGIN_S) * 1000)


def scenario(name, launcher_component, other_component, setup=(), steps=()):
    """(setup, steps) shell commands for a scenario. Setup runs before the recording starts."""
    package = launcher_component.split("/")[0]
    other = f"am start -n {other_component}" if other_component else None
    if name == "cold":
        return [other, "sleep 2", f"am force-stop {package}", "sleep 2"], \
               [f"am start -W -n {launcher_component}", "sleep 4"]
    if name == "coldsys":
        return ["input keyevent KEYCODE_HOME", "sleep 2"], [f"am force-stop {package}", "sleep 5"]
    if name == "home":
        return ["input keyevent KEYCODE_HOME", "sleep 2", other, "sleep 4"], ["input keyevent KEYCODE_HOME", "sleep 4"]
    if name == "nav":
        moves = []
        for key in NAV_KEYS:
            moves += [f"input keyevent KEYCODE_DPAD_{key}", "sleep 0.35"]
        return ["input keyevent KEYCODE_HOME", "sleep 2"], moves + ["sleep 1"]
    return list(setup), list(steps)


def parse(argv):
    p = argparse.ArgumentParser(description="Rekam jejak Perfetto di TV di sekitar skenario launcher. "
                                            "Lihat tools/trace.md.")
    p.add_argument("scenario", choices=["cold", "coldsys", "home", "nav", "custom"])
    p.add_argument("--runs", type=int, default=5, help="berapa jejak yang direkam (default 5)")
    p.add_argument("--out", default="traces", help="folder untuk jejaknya (default traces/)")
    p.add_argument("--package", default="com.gathin.tvhome", help="paket launcher (default com.gathin.tvhome)")
    p.add_argument("--app", metavar="COMPONENT",
                   help="aplikasi lain untuk cold dan home, sebagai package/activity (default: dicari di TV)")
    p.add_argument("--setup", action="append", default=[], metavar="CMD", help="custom: satu perintah setup")
    p.add_argument("--step", action="append", default=[], metavar="CMD", help="custom: satu langkah yang direkam")
    args = p.parse_args(argv)
    if not PACKAGE.match(args.package):
        p.error(f"not a package name: {args.package!r}")
    if args.app and not COMPONENT.match(args.app):
        p.error(f"--app needs package/activity, bukan {args.app!r}")
    if not 1 <= args.runs <= 50:
        p.error("--runs harus antara 1 dan 50")
    if args.scenario == "custom" and not args.step:
        p.error("custom butuh at least one --step")
    if args.scenario != "custom" and (args.setup or args.step):
        p.error("--setup dan --step hanya untuk custom scenario")
    return args


def record_one(dev, setup, steps, package, out_path, run_shell=None):
    """Run the setup, record while the steps run, then pull the trace to out_path."""
    run_shell = run_shell or (lambda cmd: dev.shell(cmd, read_timeout_s=120.0))
    for cmd in setup:
        _run(run_shell, cmd)
    config = base64.b64encode(perfetto_config(package, recording_ms(steps))).decode()
    said = run_shell(f"rm -f {REMOTE}; echo {config} | base64 -d | perfetto -c - -o {REMOTE} --background 2>&1")
    if re.search(r"not found|inaccessible|[Ee]rror", said):
        raise RuntimeError("perfetto tidak mulai di TV (sudah ada sejak Android 9 and later): " + said.strip())
    # Newer --background clients print their PID. Watch /proc for it: SELinux refuses `kill -0` on perfetto's
    # process from the shell (TCL C755), which would look like a finished recording and pull an empty file.
    pid = next((w for w in said.split() if w.isdigit()), None)
    alive = f"[ -d /proc/{pid} ] && echo running" if pid else "pidof perfetto >/dev/null && echo running"
    time.sleep(START_MARGIN_S)
    for cmd in steps:
        _run(run_shell, cmd)
    time.sleep(END_MARGIN_S)
    for _ in range(60):   # the config's duration ends the recording; wait for the file to be written
        if run_shell(alive).strip() != "running":
            break
        time.sleep(1)
    dev.pull(REMOTE, out_path, transport_timeout_s=300.0, read_timeout_s=300.0)
    run_shell(f"rm -f {REMOTE}")
    size = os.path.getsize(out_path)
    if size == 0:
        os.remove(out_path)
        raise RuntimeError("perfetto menulis empty trace; jalankan dengan --runs 1 dan lihat tools/trace.md")
    return size


def other_app(shell, package):
    """An app to leave Home for: the TV's Settings, else the first leanback app that isn't the launcher.

    Never the chooser: on a TCL C755 a plain LAUNCHER lookup resolves to android's ResolverActivity, a dialog.
    """
    candidates = shell("cmd package resolve-activity --brief -a android.settings.SETTINGS").splitlines()
    candidates += shell("cmd package query-activities --brief -a android.intent.action.MAIN "
                        "-c android.intent.category.LEANBACK_LAUNCHER").splitlines()
    for line in candidates:
        line = line.strip()
        if COMPONENT.match(line) and not line.startswith(("android/", package + "/")) and "Resolver" not in line:
            return line
    return None


def _run(run_shell, cmd):
    """A step: `sleep N` waits here (an adb shell sleep would block the connection), anything else runs on the TV."""
    m = re.fullmatch(r"sleep (\d+(?:\.\d+)?)", cmd.strip())
    if m:
        time.sleep(float(m.group(1)))
    else:
        run_shell(cmd)


def main(argv=None):
    args = parse(sys.argv[1:] if argv is None else argv)
    # Imported only now, so --help and argument errors work without TV_HOST set.
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import bench  # noqa: E402  the accessibility helpers bench.py already tested
    import tv  # noqa: E402

    try:
        dev = tv.connect()
    except OSError as e:
        sys.exit(f"cannot reach the TV di {tv.HOST}:{tv.PORT} ({e}). TV-nya nyala dan tidak tidur, dengan network debugging nyala?")
    shell = lambda cmd: dev.shell(cmd, read_timeout_s=120.0)
    try:
        home = shell("cmd package resolve-activity --brief -a android.intent.action.MAIN "
                     f"-c android.intent.category.HOME {args.package}").strip().splitlines()
        launcher = home[-1].strip() if home and "/" in home[-1] else f"{args.package}/.MainActivity"
        other = args.app
        if args.scenario in ("cold", "home") and not other:
            other = other_app(shell, args.package)
            if not other:
                sys.exit("tidak ketemu aplikasi lain untuk pindah; kasih --app package/activity")
        setup, steps = scenario(args.scenario, launcher, other, args.setup, args.step)
        stops = any("force-stop" in c for c in setup + steps)
        # A force-stop can take the launcher's accessibility helpers off the list (tools/bench.py): read them
        # before the first one and put them back after the last run, never before (it would warm a cold start).
        helpers = bench.accessibility_services(_Shell(shell), args.package) if stops else []
        about = {"model": shell("getprop ro.product.model").strip(),
                 "android": shell("getprop ro.build.version.release").strip(),
                 "build": shell("getprop ro.build.display.id").strip()}
        os.makedirs(args.out, exist_ok=True)
        try:
            for run in range(1, args.runs + 1):
                path = os.path.join(args.out, f"{args.scenario}-{run}.pftrace")
                size = record_one(dev, setup, steps, args.package, path, shell)
                with open(path[:-len(".pftrace")] + ".json", "w") as f:
                    json.dump({"scenario": args.scenario, "run": run, "package": args.package,
                               "launcher": launcher, "other_app": other, "setup": setup, "steps": steps,
                               "tv": about, "recorded": time.strftime("%Y-%m-%dT%H:%M:%S")}, f, indent=1)
                print(f"{path}: {size} bytes")
        finally:
            if helpers:
                bench.restore_accessibility(_Shell(shell), args.package, helpers)
    finally:
        dev.close()
    print(f"berikutnya: tools/trace_report.py {args.out}   (lihat tools/trace.md)")


class _Shell:
    """What bench.py's accessibility helpers expect of a TV: a shell()."""

    def __init__(self, run):
        self.shell = lambda cmd, read_timeout_s=None: run(cmd)


if __name__ == "__main__":
    main()
