# Alur: Menambah entri pers

**Kapan dipakai:** artikel tentang proyek ini.

1. Tambah baris `| Outlet | [Headline](<url>) | Date |` ke bagian negara yang tepat di `docs/PRESS.md`, dalam
   bahasa judulnya sendiri. Tautannya harus terbuka dan menampilkan artikelnya: bukan salinan sindikasi, bukan daftar
   yang dibuat otomatis, bukan yang tidak bisa dicek.
2. Perbarui hitungan di keempat tempat yang diperiksa `tests/test_press_counts.py`: baris pengantar `docs/PRESS.md`,
   baris indeksnya, teks alt kartu README ("and N more: N articles from N countries") dan `MORE` di
   `tools/readme_featured.py`. Kalau kamu tidak bisa menggambar ulang `docs/featured.png` dan `docs/card.jpg` dengan
   `tools/readme_featured.py` dan `tools/readme_card.py`, katakan di PR.

Lalu jalankan tes: `python3 -m unittest discover -s tests -v`.
