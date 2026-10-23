# Alur: Menambah theme

**Kapan dipakai:** tampilan baru yang sudah muat di key pada `themes/README.md`.

1. Buat `themes/<name>/config.json` hanya dengan key dari tabel di `themes/README.md`;
   `themes/default/config.json` adalah titik awalnya.
2. Tambah `wallpaper.jpg` atau folder `wallpapers/` kalau memakainya: gelap, 1920x1080, masing-masing di bawah sekitar 300 KB,
   dan karya yang memang hakmu untuk dipakai.
3. Dorong ke TV sungguhan dengan `tools/theme.sh themes/<name>` dan ambil `screenshot.jpg` Home dengan fokus
   di dock: JPEG 1280x720 (16:9), paling besar 300 KB. Bukan mock-up, bukan emulator.
   `tests/test_themes.py` gagal tanpanya.
4. Tambah satu baris ke tabel di `themes/README.md` yang menampilkan screenshot, seperti yang lain.
5. Tampilan yang butuh key baru adalah sakelar launcher dulu, di PR-nya sendiri.

Lalu jalankan tes: `python3 -m unittest discover -s tests -v`.
