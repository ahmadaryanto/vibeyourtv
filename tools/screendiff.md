# tools/screendiff.py

Memastikan screenshot sebelum dan sesudah identik di luar area yang kamu mask, lalu menulis pasangan berdampingan dengan detail pribadi diburamkan. Setiap PR performa mengklaim "tidak ada perubahan visual"; ini membuktikannya.

```bash
tools/venv/bin/pip install -r tools/requirements-perf.txt     # Pillow
tools/venv/bin/python tools/screendiff.py shots/before.png shots/after.png \
    --mask 90,40,900,220 --blur 1265,125,1525,197 --out shots/before-after.png
```

| Kode keluar | Yang dicetak | Arti |
|---|---|---|
| 0 | `identical (1 region masked): ...` | Tidak ada piksel di luar mask yang beda lebih dari 8 |
| 1 | `different: N pixels differ ..., inside x0,y0,x1,y1` | Cari penyebab sebelum posting |
| 2 | an error | Ukuran beda, kotak di luar gambar, file tidak terbaca |

| Opsi | Catatan |
|---|---|
| `--mask x0,y0,x1,y1` | Dikecualikan dari perbandingan. Bisa diulang. Hanya untuk yang berubah sendiri: jam, salam, baris stats, caption wallpaper. |
| `--blur x0,y0,x1,y1` | Dipixelkan di kedua screenshot pasangan. Bisa diulang. Untuk nama Wi-Fi, foto akun. Tidak mempengaruhi perbandingan. |
| `--out PNG` | Pasangan berlabel, dengan `--scale` (default 0.5). |
| `--diff PNG` | Piksel yang beda, putih di atas hitam. |
| `--threshold N` | Selisih channel yang diabaikan (default 8, noise kompresi). |

## Kotak

Kotak = `x0,y0,x1,y1` dalam piksel screenshot. Di TV 1080p dengan layout default:
- **Salam, jam, dan tanggal:** `90,40,900,220`.
- **Nama jaringan:** sekitar `1265,125,1525,197`.

Bar atas bergeser dengan config, layout Hebrew membaliknya. Baca bounds pill Wi-Fi di TV kamu:

```bash
tools/venv/bin/python -I tools/tv.py 'uiautomator dump /data/local/tmp/ui.xml >/dev/null; grep -o "id/btnWifi\"[^>]*bounds=\"[^\"]*\"" /data/local/tmp/ui.xml; rm /data/local/tmp/ui.xml'
```

## Aturan

- **Ambil kedua screenshot dengan cara yang sama.** Tombol sama, tunggu sama, tema dan wallpaper sama:
  `tools/tv.py --screenshot shots/before.png HOME DPAD_RIGHT`.
- **Jangan mask area yang disentuh perubahanmu.**
- **Jangan naikkan `--threshold` supaya perbedaan hilang.** Kalau memang berubah, jelaskan di PR.
- **Bandingkan Home, bukan Live TV, HDMI, atau Netflix.** Itu sering kosong di kebanyakan TV.

Metode before/after lengkap untuk PR ada di [`docs/workflows/perf-ab.md`](../docs/workflows/perf-ab.md).
