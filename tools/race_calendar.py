#!/usr/bin/env python3
"""Masukkan kalender F1 musim ini ke sebuah tema, untuk mode akhir pekan balapan di launcher.

    race_calendar.py <theme folder>              ambil musim yang sedang berjalan dan tulis race_calendar ke
                                                 <theme folder>/config.json, kunci lain tetap
    race_calendar.py <theme folder> --season 2027

Lalu kirim temanya seperti biasa: tools/theme.sh <theme folder>. Launcher tidak pernah online; skrip ini
satu-satunya yang online, sekali, di komputer kamu. Datanya dari Jolpica (https://jolpi.ca), penerus
komunitas gratis dari API Ergast: tanpa kunci, tanpa akun. Jalankan lagi kalau F1 memindah sesi atau
musim baru mulai. Balapan yang sudah lewat tidak dimasukkan.
"""
import argparse
import datetime
import json
from pathlib import Path
import sys
import urllib.request

API = "https://api.jolpi.ca/ergast/f1/{season}.json?limit=100"
# Jolpica's session keys, in weekend order, and the names the launcher shows.
SESSIONS = (
    ("FirstPractice", "FP1"),
    ("SecondPractice", "FP2"),
    ("ThirdPractice", "FP3"),
    ("SprintQualifying", "SPRINT QUALI"),
    ("SprintShootout", "SPRINT QUALI"),   # the 2023 name for the same session
    ("Sprint", "SPRINT"),
    ("Qualifying", "QUALIFYING"),
)


def instant(block):
    """"2026-10-23" + "17:30:00Z" as one UTC timestamp; None when the time is not published yet."""
    if not block.get("date") or not block.get("time"):
        return None
    return f"{block['date']}T{block['time']}" if block["time"].endswith("Z") else f"{block['date']}T{block['time']}Z"


def calendar(races, now):
    out = []
    for race in races:
        sessions = []
        for key, name in SESSIONS:
            if key in race and (start := instant(race[key])):
                sessions.append({"name": name, "start": start})
        start = instant(race)
        if not start:
            continue
        sessions.append({"name": "RACE", "start": start})
        # The launcher keeps a weekend on screen for four hours after the start; a day covers that.
        if datetime.datetime.fromisoformat(start.replace("Z", "+00:00")) < now - datetime.timedelta(days=1):
            continue
        out.append({
            "round": int(race["round"]),
            "name": race["raceName"],
            "place": race.get("Circuit", {}).get("Location", {}).get("locality", ""),
            "sessions": sessions,
        })
    return out


def main():
    parser = argparse.ArgumentParser(description="Masukkan kalender F1 musim ini ke sebuah tema.")
    parser.add_argument("theme", help="folder tema dengan config.json (dibuat kalau belum ada)")
    parser.add_argument("--season", default="current", help="tahun musim, default musim yang sedang berjalan")
    args = parser.parse_args()

    config = Path(args.theme) / "config.json"
    theme = json.loads(config.read_text(encoding="utf-8-sig")) if config.is_file() else {}
    request = urllib.request.Request(API.format(season=args.season), headers={"User-Agent": "cleanyourtv"})
    with urllib.request.urlopen(request, timeout=30) as response:
        table = json.load(response)["MRData"]["RaceTable"]
    now = datetime.datetime.now(datetime.timezone.utc)
    races = calendar(table["Races"], now)
    if not races:
        sys.exit(f"tidak ada balapan mendatang di kalender {table['season']}; coba --season tahun depan")
    theme["race_calendar"] = races
    config.parent.mkdir(parents=True, exist_ok=True)
    config.write_text(json.dumps(theme, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    first = races[0]
    print(f"{len(races)} balapan dari musim {table['season']} -> {config}")
    print(f"berikutnya: ronde {first['round']} {first['name']}, balapan {first['sessions'][-1]['start']}")


if __name__ == "__main__":
    main()
