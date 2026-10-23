#!/usr/bin/env python3
"""Cek port ADB jaringan TV sudah tertutup, setelah debugging dimatikan (tidak perlu nc).

    adb-closed.py <tv ip[:port]> [port]   port default-nya TV_PORT atau 5555; IP-nya TV_HOST

Keluar 0 kalau port menolak koneksi atau tidak menjawab, 1 kalau masih terbuka, dan 2
kalau pengecekan itu sendiri tidak jalan (nama tidak ketemu, tidak ada rute ke TV).
"""
import os
import socket
import sys

TIMEOUT_S = 3.0


def split_host_port(endpoint):
    """Split host:port or [ipv6]:port into (host, port_or_none)."""
    if endpoint.startswith("["):
        end = endpoint.find("]")
        if end != -1:
            host = endpoint[1:end]
            rest = endpoint[end + 1:]
            if rest.startswith(":"):
                return host, rest[1:]
            return host, None
    elif endpoint.count(":") == 1:
        host, port = endpoint.split(":", 1)
        return host, port
    return endpoint, None


def main():
    args = sys.argv[1:]
    if len(args) > 2 or args[:1] in (["-h"], ["--help"]):
        sys.stderr.write(__doc__)
        return 2
    raw_host = args[0] if args else os.environ.get("TV_HOST", "")
    if not raw_host:
        sys.stderr.write("cara pakai: adb-closed.py <tv ip> [port]\n")
        return 2
    host, endpoint_port = split_host_port(raw_host)
    if len(args) == 2:
        port_str = args[1]
    elif len(args) == 1 and endpoint_port is not None:
        port_str = endpoint_port
    else:
        port_str = endpoint_port or os.environ.get("TV_PORT") or "5555"

    try:
        port = int(port_str)
        if not (1 <= port <= 65535):
            raise ValueError
    except ValueError:
        sys.stderr.write(f"not a port number: {port_str!r} (bukan nomor port)\n")
        return 2

    try:
        with socket.create_connection((host, port), timeout=TIMEOUT_S):
            pass
    except ConnectionRefusedError:
        print(f"{host}:{port} is closed: the TV refused koneksinya. Port-nya sudah tertutup.")
        return 0
    except socket.timeout:
        # Some firmware drops instead of refusing, but so does a TV that is off or at another address.
        print(f"{host}:{port} tidak menjawab dalam {TIMEOUT_S:.0f} dtk: dianggap tertutup, kecuali TV mati atau bukan "
              "di alamat ini.")
        return 0
    except OSError as e:
        sys.stderr.write(f"tidak bisa cek {host}:{port} ({e}). TV-nya nyala dan di jaringan yang sama?\n")
        return 2
    sys.stderr.write(f"{host}:{port} is still open: ADB jaringan masih nyala. Di TV, matikan USB debugging dan\n"
                     "Network/ADB debugging, reboot, lalu cek lagi.\n")
    return 1


if __name__ == "__main__":
    sys.exit(main())
