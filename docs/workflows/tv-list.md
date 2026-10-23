# Alur: Menambah atau memperbarui daftar TV

**Kapan dipakai:** kamu sudah menguji daftar debloat sebuah TV selama beberapa hari dan ingin membagikannya, atau kamu punya unit kedua atau perbaikan untuk daftar yang sudah ada.

1. Cek `examples/README.md` dulu. Kode produk dan firmware yang sama dengan folder yang sudah ada biasanya
   unit kedua atau re-badge: tambahkan catatanmu ke folder itu, bukan folder baru.
2. Buat `examples/<brand>-<model>/debloat.txt`, huruf kecil dengan tanda hubung. Salin format
   `examples/xiaomi-mi-tv-4a/debloat.txt`:
   - Baris pertama komentar dengan model persis, versi Android, dan build firmware
     (`tools/tv.py --probe` mencetaknya).
   - Lalu baris Apply dan Undo, paket launcher bawaan dan Live TV kalau berbeda, serta apa yang dibiarkan dan kenapa.
   - Satu paket per baris, dikelompokkan di bawah komentar yang menamai kelompoknya, dengan komentar di ujung untuk yang
     tidak jelas. Tanpa duplikat, di luar komentar hanya nama paket.
3. Cantumkan hanya paket yang benar-benar kamu nonaktifkan dan kamu pakai selama beberapa hari. Catat yang kamu aktifkan lagi.
   Tandai entri yang belum diuji sebagai komentar.
4. Tambah satu baris ke tabel di `examples/README.md` (`` | `<folder>/` | TV | OS | Notes | ``) dan satu ke
   tabel TV di `README.md` (`| TV | OS |`). Sel TV harus teks yang sama di keduanya; `tests/test_examples.py`
   membandingkannya.
5. Biarkan hitungannya (angka `TV models` di teks alt kartu, baris merek): CI hanya memeriksanya
   di `main`, dan diperbaiki saat merge.
6. `README.md` untuk catatan khusus TV dan `config.json` dengan dock diterima di folder itu. Jangan menambah
   skrip pasang atau pulihkan, penyiapan Private DNS atau AdGuard, atau langkah `pm uninstall`: folder itu hanya
   menonaktifkan, dan `tools/disable.sh` serta `tools/restore.sh` yang mengerjakan sisanya.

Model yang punya catatan tetapi belum punya daftar beberapa hari masuk ke `docs/devices/<brand-model>.md`, dengan satu baris di daftar catatan
di akhir `examples/README.md`. Folder di `examples/` harus berisi paket.

## Memperbarui daftar yang sudah ada

Jaga header tetap akurat: tambahkan model, firmware, dan wilayah unit kedua, jangan mengganti yang pertama.
Jelaskan setiap paket yang ditambah atau diaktifkan lagi di komentar. Daftar yang menonaktifkan pemutakhir firmware menjelaskan kenapa di
header; lebih baik membiarkannya aktif. `pm hide` atau `pm block` butuh undo yang sesuai di header. Suntingan pada
daftar orang lain adalah penilaian, jadi sebutkan di PR TV mana yang kamu uji dan berapa lama.

Lalu jalankan tes: `python3 -m unittest discover -s tests -v`.
