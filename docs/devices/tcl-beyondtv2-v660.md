# TCL BeyondTV2 (RTD2851, R851T02 V660): catatan rendering

Dicek di TV sungguhan 2026-10-21, satu sesi, dengan launcher turunan proyek ini (Eddy TV,
fork khusus TCL). Ini sifat GPU dan firmware, bukan satu launcher, jadi
dicatat untuk siapa pun menyesuaikan Home di papan ini.

## Pengenal perangkat

| Field | Observed value |
|---|---|
| Model / device | `BeyondTV2` / `BeyondTV` |
| Board | `RTD2851` |
| Android / API | Android TV 9 / 28, 32-bit userspace |
| Build fingerprint | `TCL/BeyondTV2/BeyondTV:9/PPR1.180610.011/AP08:user/release-keys` |
| Firmware | `V8-R851T02-LF1V660` (client `TCL-LA-RT2851-S1`, Latin America) |
| Reported memory | 2 GB (`2097152 kB`), zRAM ~800 MB |
| Remote | `TCL_RC802VLA` (Bluetooth, mic) + IR |

Ini firmware V660, build, client dan fingerprint sama dengan daftar di
[`examples/tcl-beyondtv2-latin-america/`](../../examples/tcl-beyondtv2-latin-america/debloat.txt): pakai daftar
itu untuk debloat. Catatan ini hanya biaya menggambar launcher di GPU ini.

## Mengapa catatan ini

Saat menambah banner berita opt-in (1680x500) ke Home, beberapa pilihan menggambar biasa jauh
lebih mahal dari seharusnya di papan ini. Setiap baris di bawah A/B pada APK sama dengan satu hal
diubah, diukur sebagai **frame janky** (`dumpsys gfxinfo`, frame di atas 16,7 ms) selama 20 tekan
D-pad setelah Home tenang.

| What | Cost on this board | What replaced it |
|---|---:|---|
| `canvas.clipPath` (rounded rect) over the banner | ~8 points of janky frames | fill a pre-built rounded `Path` with a `BitmapShader` |
| `setClipToOutline(true)` on the same view | 83% janky frames | not used: it forces a layer per frame |
| Full-bleed `<shape>` gradient, 1700x250, redrawn each frame | ~42 points | constant scrim baked into the bitmap once |
| `android:shadowRadius` on four TextViews | ~34 points | removed; the scrim gives the contrast |
| Two `LinearGradient` objects built inside `onDraw` | ~1.5 ms per frame | built in `onSizeChanged` |
| A static banner behind the grid's spring animation | ~2-3 ms per frame of the spring | hardware layer on the banner (`setLayerType`) |

Dua kesimpulan yang mengejutkan:

- **`clipPath` fallback software di sini.** Clip bulat pada view besar mengubah setiap frame jadi
  rasterisasi CPU seluruh banner, foto termasuk. Terlihat "fotonya mahal"
  sampai foto diuji tanpa foto sama sekali: biayanya tetap.
- **Layer lebih murah daripada digambar ulang.** Banner statis direkam ulang setiap frame animasi
  spring fokus grid padahal tidak ada yang berubah; `LAYER_TYPE_HARDWARE` menurunkan frame
  kembali ke angka yang sama saat banner absent.

## Metode

- Steady state, bukan "since process start": `force-stop`, buka Home, tunggu 20 s decode/cache/layout,
  `dumpsys gfxinfo reset`, 20 tekan D-pad 0,4 s terpisah, baca setelah 3 s.
- Frame dicocokkan ke tombol lewat `framestats`; kolom 1-based di `awk` (`$1` Flags, `$2`
  IntendedVsync, `$14` FrameCompleted). `OldestInputEvent` `INT64_MAX` untuk key event dan tidak bisa
  dipakai sebagai awal frame.
- Lima run atau lebih per sisi. Satu run bisa menangkap rotasi/refresh dan membaca 10 poin lebih tinggi;
  frame terlambat berkelompok, cara membedakan transisi dari biaya per-frame.

## Satu keanehan firmware

`am send-trim-memory <pid> <level>` menolak level background di firmware ini saat layanan aksesibilitas
menjaga proses launcher di `curProcState=2`, meski aplikasi lain di depan. Jadi
`onTrimMemory` untuk level background hanya bisa diuji tekanan sistem nyata di sini, bukan perintah
shell.

Angka lengkap, kode dan tujuh perbaikan:
<https://github.com/eddysangalli/CleanMyTCL-Android9/blob/main/benchmarks/results/2026-10-21-rss-row.md>
