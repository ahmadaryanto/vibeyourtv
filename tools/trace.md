# tools/trace.py and tools/trace_report.py

Rekam trace Perfetto launcher di TV, lalu laporkan ke mana waktunya pergi. Untuk angka headline saja (latensi tombol, jank, cold start, PSS), pakai `tools/bench.py`: tidak perlu instal extra. Untuk menggambar hasil di PR, pakai `tools/flamechart.py` ([panduan](flamechart.md)).

## Setup

```bash
tools/venv/bin/pip install -r tools/requirements-perf.txt
```

Rekaman cukup `tools/requirements.txt` dan TV Android 9 ke atas. Run pertama laporan mengunduh mesin query Perfetto (sekitar 13 MB, SHA-256 dicek) ke `~/.local/share/perfetto`. Untuk menghindari itu, unduh `trace_processor_shell` sendiri dan pass `--engine PATH`.

Di Android 9 dan 10, layanan tracing TV harus diaktifkan dulu; default aktif dari Android 11. Lihat [tracing-service setup](https://perfetto.dev/docs/quickstart/android-tracing#starting-the-tracing-services) Perfetto untuk prasyarat `persist.traced.enable` di perangkat non-Pixel. Tool ini tidak mengubah setting itu. Tool mengirim config trace biner, yang Android 9 terima tanpa opsi `--txt` di Android 10.

## Rekam

```bash
TV_HOST=<tv ip> tools/venv/bin/python -I tools/trace.py home --runs 5 --out traces/before
```

| Skenario | Yang direkam | Untuk |
|---|---|---|
| `home` | HOME ditekan saat app lain di depan | Kebanyakan perubahan Home |
| `cold` | `am start -W` setelah force-stop, app lain di depan | Kerja start (`onCreate`, config, font) |
| `coldsys` | `am force-stop` dengan Home di depan; Android restart | Restart setelah Home dibunuh karena memori |
| `nav` | 24 gerakan D-pad, jarak 0,35 s | Fokus, scroll, draw |
| `custom` | `--step CMD` (bisa diulang) setelah `--setup CMD` | Lainnya; step `sleep N` menentukan durasi |

- **Output:** setiap run menulis `<scenario>-<n>.pftrace` (10–20 MB) dan `.json` dengan perintah serta model dan build TV. `traces/` di-gitignore.
- **App lain:** `--app package/activity`, atau default Settings TV.

## Laporan

Pisahkan trace `nav` dari trace start (`home`, `cold`, `coldsys`, `custom`), karena hitungan frame memakai jendela waktu berbeda. Set input dan kedua sisi `--vs` harus keluarga yang sama. Untuk folder yang berisi keduanya, pass file yang cocok seperti `traces/nav-*.pftrace`, bukan foldernya.

```bash
tools/venv/bin/python tools/trace_report.py traces/before --vs traces/after --markdown
```

| Skenario | Angka |
|---|---|
| Start (`home`, `cold`, `coldsys`, `custom`) | Start sampai frame pertama digambar; langkah di dalamnya (`bindApplication`, `onCreate`, `onStart`, `onResume`, layout dan draw pertama); lalu frame yang digambar dan deadline terlewat di 1,5 s berikutnya |
| `nav` | Waktu frame (median, persentil 90); draw RenderThread; tunggu GPU; deadline terlewat |

Dengan `--vs`, tabel ketiga menunjukkan perubahan tiap median. Run di atas 2× median ditandai bintang.

Slice yang masih terbuka saat rekaman berakhir tidak punya durasi terukur. Laporan mengabaikan durasi itu dan hanya menghitung draw selesai. Kalau draw RenderThread pertama belum selesai, waktu first-frame juga tidak tersedia; langkah lifecycle yang selesai tetap dilaporkan.

Waktu frame dan deadline terlewat butuh [FrameTimeline](https://perfetto.dev/docs/data-sources/frametimeline), tersedia Android 12 ke atas. Tanpa datanya untuk proses launcher, metrik ini dihilangkan (atau `–` di samping run terukur). `nav` tetap menghitung draw RenderThread dan melaporkan durasi serta tunggu GPU jika ada; durasi draw bukan waktu frame penuh.

## Aturan

- **Ulangi run.** Rekam 5 run per build, di TV yang sama dengan config, tema, dan wallpaper sama. Bandingkan median.
- **Simpan run bertanda bintang.** Biasanya tekanan memori di TV 1 GB. Laporkan; jangan buang.
- **Jangan jalankan `am start` setelah force-stop dengan Home di depan.** Android sudah restart Home, jadi `onResume` muncul dua kali. Pakai `cold` atau `coldsys`.
- **Cek remote setelahnya.** Force-stop mematikan helper accessibility. `trace.py` mengembalikan daftar setelah run terakhir, tapi di beberapa TCL helper tetap tidak bind (`docs/TROUBLESHOOTING.md`).
- **Cari penghematan GPU di tempat yang benar.** Overdraw lebih sedikit terlihat sebagai tunggu GPU di `nav`, bukan waktu main thread.
- **Jangan harap gain latensi dari kerja off-main-thread.** Kerja hilang, tapi waktu ke frame pertama tidak memendek.
- **Jangan commit file `.pftrace`.** Berisi daftar proses TV.

Metode before/after lengkap untuk PR ada di [`docs/workflows/perf-ab.md`](../docs/workflows/perf-ab.md).
