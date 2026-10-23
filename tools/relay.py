#!/usr/bin/env python3
"""Teruskan port di komputer ini ke port ADB TV, untuk asisten yang sandbox-nya tidak sampai ke jaringan.

Beberapa asisten kode menjalankan perintah di sandbox yang boleh bicara ke komputer ini tapi tidak ke
perangkat lain di jaringan lokal (Claude Code di macOS, yang diblokir Local Network privacy). Jalankan
ini di terminal biasa, lalu arahkan skrip ke sana:

    tools/venv/bin/python -I tools/relay.py 192.168.1.50          # biarkan tetap jalan
    TV_HOST=127.0.0.1 TV_PORT=15555 tools/venv/bin/python -I tools/tv.py --probe

Usage: relay.py [TV_HOST] [--tv-port 5555] [--port 15555]
Hanya mendengarkan di 127.0.0.1, tidak pernah di jaringan. Baca tools/relay.md dulu.
"""
import argparse
import os
import socket
import sys
import threading

LISTEN = "127.0.0.1"   # loopback only: the relay must never put the TV's ADB port on the network


def pipe(src, dst):
    """Copy one direction until either side closes, then close both directions."""
    try:
        while True:
            data = src.recv(65536)
            if not data:
                break
            dst.sendall(data)
    except OSError:
        pass
    finally:
        for s in (src, dst):
            try:
                s.shutdown(socket.SHUT_RDWR)
            except OSError:
                pass


def serve(srv, tv_host, tv_port):
    while True:
        client, _ = srv.accept()
        try:
            tv = socket.create_connection((tv_host, tv_port), timeout=10)
            tv.settimeout(None)
        except OSError as e:
            print(f"cannot reach {tv_host}:{tv_port}: {e} (TV-nya nyala, dan network debugging masih nyala?)",
                  flush=True)
            client.close()
            continue
        for a, b in ((client, tv), (tv, client)):
            threading.Thread(target=pipe, args=(a, b), daemon=True).start()


def main():
    p = argparse.ArgumentParser(description="Teruskan 127.0.0.1:PORT ke port ADB TV. Lihat tools/relay.md.")
    p.add_argument("tv_host", nargs="?", default=os.environ.get("TV_HOST"),
                   help="alamat IP TV (default: $TV_HOST)")
    p.add_argument("--tv-port", type=int, default=5555, help="port ADB TV (default 5555)")
    p.add_argument("--port", type=int, default=15555, help="port lokal yang didengarkan (default 15555; 0 memilih sendiri)")
    args = p.parse_args()
    if not args.tv_host:
        p.error("kasih alamat IP TV, misalnya  tools/relay.py 192.168.1.50")
    if args.tv_host in ("127.0.0.1", "localhost") and args.tv_port == args.port:
        p.error("itu the relay itself: kasih alamat IP TV, bukan relay ini")

    srv = socket.socket()
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        srv.bind((LISTEN, args.port))
    except OSError as e:
        sys.exit(f"tidak bisa dengar di {LISTEN}:{args.port}: {e}. Is a relay already running? Pilih --port lain.")
    srv.listen(8)
    port = srv.getsockname()[1]
    print(f"relay {LISTEN}:{port} -> {args.tv_host}:{args.tv_port}", flush=True)
    print(f"pakai  TV_HOST={LISTEN} TV_PORT={port}  dengan tools/tv.py dan skrip lain; Ctrl-C untuk berhenti", flush=True)
    try:
        serve(srv, args.tv_host, args.tv_port)
    except KeyboardInterrupt:
        print("relay berhenti", flush=True)
    finally:
        srv.close()


if __name__ == "__main__":
    main()
