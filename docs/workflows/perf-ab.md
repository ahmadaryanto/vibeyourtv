# Alur: mengukur perubahan kinerja launcher, sebelum dan sesudah

**Kapan dipakai:** perubahan apa pun di bawah `launcher/`. Pemeriksaan regresi di bawah opsional untuk sebagian besar
(sekitar enam menit di satu TV), tetapi diterima. Perubahan yang dimaksudkan membuat launcher lebih cepat atau lebih ringan, atau yang
pemeriksaannya gagal, lalu mengikuti metode penuh
yang dipakai #666, untuk menunjukkan seberapa banyak dan ke mana waktunya pergi.

## Pemeriksaan regresi, untuk perubahan launcher apa pun (opsional)

1. **Sebelum mengubah apa pun,** pasang build dasar dengan `launcher/build-and-install.sh` dan jalankan:

   ```bash
   TV_HOST=<tv ip> tools/venv/bin/python tools/bench.py --out-dir work/bench-before
   ```

2. Buat perubahannya, pasang build-mu dengan cara yang sama, dan jalankan `tools/bench.py --out-dir work/bench-after`.
3. Bandingkan kedua berkas `result.json`. Abaikan field yang `null` di keduanya:

   | Field | Gagal kalau nilai sesudahnya |
   |---|---|
   | `response_ms.p50` | lebih dari 10% dan 5 ms di atas sebelum |
   | `return_to_home_ms.p50` | lebih dari 10% dan 10 ms di atas sebelum |
   | `cold_start_ms.p50` | lebih dari 10% dan 30 ms di atas sebelum |
   | `jank_pct` | lebih dari 1 poin di atas sebelum |
   | `pss_mb` | lebih dari 2 MB di atas sebelum |
   | `idle_launcher_cpu_pct` | lebih dari 0,5 poin di atas sebelum |

4. **Kalau sebuah field gagal,** ikuti metode di bawah dengan skenario itu (`home`, `cold`, atau `nav`) untuk menemukan
   biayanya. Perbaiki dan kembali ke langkah 2. Perbaikan yang biasanya berhasil:
   - kerjakan setelah frame pertama;
   - lewati pekerjaan kalau tidak ada yang berubah;
   - dekode gambar pada ukuran tampilannya;
   - simpan hasil, jangan hitung ulang.
5. Taruh keenam field, sebelum dan sesudah, di PR. Kalau sebuah fitur memang harus berbiaya, beri angkanya
   dan alasannya. Setelah berhasil, habiskan satu putaran lagi untuk biayanya.

Beberapa catatan tentang angka:
- **Derau:** dua jalanan build yang sama di TCL C755 berbeda 0 ms pada respons tombol, sampai 45 ms pada
  cold start dan 1 MB pada PSS, dan batasnya berada di atas itu. Jalankan ulang kedua build kalau sebuah field mendarat dekat
  batasnya.
- **Lama jalan:** satu jalanan penuh sekitar 6 menit.
- **TV yang tidak mencatat tombol yang disuntikkan:** `return_to_home_ms` bernilai `null`, dan `response_ms` mencakup
  perjalanan bolak-balik ADB. Tetap bisa membandingkan dua build di TV yang sama.

## Metode penuh

Alat: [`tools/trace.py` dan `tools/trace_report.py`](../../tools/trace.md),
[`tools/flamechart.py`](../../tools/flamechart.md) dan [`tools/screendiff.py`](../../tools/screendiff.md).
Pasang sekali:

```bash
tools/venv/bin/pip install -r tools/requirements-perf.txt
```

Salin daftar centang ini ke catatanmu dan centang:

```
- [ ] 1. Tulis skenario dan irisan yang kamu harapkan menyusut
- [ ] 2. Rekam jejak build dasar, 5 jalanan
- [ ] 3. Rekam jejak build-mu, 5 jalanan
- [ ] 4. Laporan; kalau perubahannya tidak ada, kembali ke kode
- [ ] 5. Gambar flame chart
- [ ] 6. Screenshot identik, atau perubahan visualnya dijelaskan
- [ ] 7. Cek perilaku yang bisa rusak karena perubahanmu
- [ ] 8. Tuliskan
```

## 1. Putuskan sebelum mengukur

Tulis dua hal:
- **Skenarionya:** `home` (kembali ke Home), `cold` (nyala), atau `nav` (D-pad, menggambar).
- **Nama irisan** yang seharusnya dihilangkan atau dipendekkan perubahanmu, misalnya `activityResume`, `RV OnBindView`.

Itu jadi skenario `trace.py` dan `--highlight` `flamechart.py`. Memilihnya setelah melihat
hasil tidak membuktikan apa pun.

## 2–3. Rekam jejak kedua build

Pasang setiap build dengan `launcher/build-and-install.sh`, memakai konfigurasi, theme, dan wallpaper yang sama.
Biarkan Home diam semenit setelah setiap pemasangan, lalu rekam:

```bash
tools/venv/bin/python -I tools/trace.py home --runs 5 --out traces/before   # base build installed
tools/venv/bin/python -I tools/trace.py home --runs 5 --out traces/after    # your build installed
```

## 4. Laporan

```bash
tools/venv/bin/python tools/trace_report.py traces/before --vs traces/after --markdown
```

- **Tidak ada perubahan, atau langkah yang salah yang bergeser:** perbaiki kode sebelum menggambar apa pun.
- **Jalanan berbintang** (satu yang lebih dari dua kali median) tetap di tabel, disebut sebagai pencilan.

## 5. Gambar

```bash
tools/venv/bin/python tools/flamechart.py traces/before traces/after \
    --highlight '<the slices from step 1>' \
    --subtitle '<TV>, <Android version> · magenta: <what they are>' --out chart.png
```

Untuk perubahan pada Home, tambahkan frame setelah awal:

```bash
tools/venv/bin/python tools/flamechart.py traces/before traces/after \
    --window start:1500 --threads RenderThread --highlight '^DrawFrame' --out frames.png
```

## 6. Screenshot

Ambil screenshot yang sama di kedua build, lalu bandingkan:

```bash
tools/venv/bin/python -I tools/tv.py --screenshot shots/before.png HOME DPAD_RIGHT   # each build
tools/venv/bin/python tools/screendiff.py shots/before.png shots/after.png \
    --mask <clock box> --blur <network name box> --out shots/before-after.png
```

`identical` menopang "tidak ada perubahan yang kelihatan". Kalau hasilnya `different`, cari tahu kenapa sebelum memposting.

## 7. Perilaku

Di TV, cek apa yang bisa rusak karena perubahanmu, dan catat yang kamu lihat. Untuk kembali ke Home,
misalnya:
- aplikasi yang dinonaktifkan selagi aplikasi lain di depan sudah hilang saat kembali;
- fokus kembali ke ubin yang kamu tinggalkan;
- Home di atas Home pergi ke ubin pertama.

## 8. Pull request

- **Diuji di:** TV, versi Android, dan firmware.
- **Tabel:** `trace_report.py --markdown` sebelum, sesudah, dan perubahannya, dengan pencilan disebut.
- **Grafik:** masing-masing dengan `--highlight`-nya disebut.
- **Screenshot:** pasangan dari `screendiff.py` dan hasilnya.
- **Pemeriksaan perilaku:** apa yang kamu cek dan apa yang kamu lihat.
- **Jangan ikutkan** `traces/` dan screenshot mentah: besar, dan menyimpan detail TV-mu.
