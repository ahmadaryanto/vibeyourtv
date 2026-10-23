# Alur: Catatan docs dan pemecahan masalah

**Kapan dipakai:** entri pemecahan masalah, perbaikan `PROMPT.md`, atau perubahan docs lain.

- `docs/TROUBLESHOOTING.md` dikelompokkan di bawah heading (Connecting, Working on the TV, lalu satu heading
  per masalah yang lebih besar). Entri adalah bullet yang dibuka dengan gejala dalam huruf tebal, lalu penyebab dan perbaikannya, dengan
  model TV dan firmware tempat itu terlihat.
- Perbaikan untuk hal yang bikin agenmu macet biasanya masuk ke `PROMPT.md` sebagai satu kalimat tambahan, tetap
  netral merek. Pengetahuan khusus merek masuk ke `examples/` atau `docs/devices/`. Perubahan pada aturan keamanan
  itu sendiri diserahkan ke maintainer.
- Beberapa tes mengunci fakta di catatan `docs/devices/`, `PROMPT.md`, dan `docs/TROUBLESHOOTING.md` (berkas
  `tests/test_*_note.py`). Kalau kamu mengubah salah satu bagian itu, jalankan suite dan pertahankan faktanya.
- Docs dan README tetap sejalan: daftar, theme, add-on, atau alat baru mendapat barisnya di tempat yang lain juga punya baris.

Lalu jalankan tes: `python3 -m unittest discover -s tests -v`.
