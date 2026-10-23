# Keamanan

VibeYourTV bicara ke TV lewat ADB dan memasang launcher di situ, jadi bug keamanan di sini bisa
sampai ke ruang tamu orang. Laporkan apa pun yang kamu temukan secara privat, bukan lewat issue publik.

## Cara melapor

Pakai pelaporan kerentanan privat GitHub: buka
[tab Security](https://github.com/Gathin23/cleanyourtv/security) lalu pilih **Report a vulnerability**.
Hanya maintainer yang melihat laporan itu. Kalau jalur itu tidak bisa kamu pakai, email
[hello@itsgathin.com](mailto:hello@itsgathin.com) dengan "Security" di subjek.

Sertakan apa yang kena (prompt, alat di `tools/`, launcher, atau daftar debloat),
langkah untuk mengulanginya, dan apa yang bisa penyerang lakukan dengannya.

## Yang masuk hitungan

- Launcher (`com.gathin.tvhome`) melakukan sesuatu yang tidak seharusnya: menjangkau jaringan, membuka
  data ke aplikasi lain, atau dipaksa membuka sesuatu yang tidak seharusnya.
- Alat (`tools/tv.py`, `disable.sh`, `restore.sh`, `theme.sh`, `recovery.py`) menjalankan perintah
  di perangkat atau komputer yang bukan sasarannya, atau salah menangani path dan input.
- Daftar debloat atau prompt yang membuat TV tidak bisa dipulihkan dengan `tools/restore.sh`.
- Apa pun di pull request yang menjalankan kode di mesin kontributor atau maintainer.

ADB memang kuat dari desainnya. Menghidupkan USB debugging atau network debugging membuat komputer mana pun di
jaringanmu bisa minta koneksi, dan TV menanyakan izinmu. Matikan debugging kalau sudah selesai.

## Selanjutnya

Kamu dapat balasan dalam beberapa hari. Perbaikan masuk ke `main` dengan kredit untukmu, kecuali kamu lebih
suka anonim. Beri perbaikan itu kesempatan yang wajar untuk rilis sebelum kamu memublikasikan detailnya.
