# Alur: Menambah add-on

**Kapan dipakai:** widget atau ubin yang bisa jadi aplikasi kecil sendiri, ditampilkan lewat kartu modul.

1. Buat `addons/<name>/` sebagai proyek Gradle sendiri (`settings.gradle` dan modul `app`), dengan
   nama paket sendiri.
2. Ia berbicara ke launcher hanya lewat kontrak kartu modul (`docs/modules.md`, `docs/specs/card.md`).
   Tidak ada yang berubah di bawah `launcher/`, dan `launcher/` tidak pernah menyebut `addons/`. Kalau idenya butuh perubahan
   launcher, itu bukan add-on: buka issue.
3. `README.md`-nya punya bagian Permissions yang menyebut setiap izin dan alasannya, plus akses jaringan atau
   layanan latar kalau ada. Tanpa telemetri kecuali opt-in, tanpa iklan, karya senimu sendiri.
4. Tambah satu baris ke tabel di `addons/README.md` yang menautkan `<name>/`, beserta TV tempat diuji.
5. Jangan taruh APK di repo. `tests/test_addons.py` memeriksa semua ini; `.github/workflows/addons.yml` membangun
   add-on yang berubah dengan `./gradlew --no-daemon assembleRelease`.

Lalu jalankan tes: `python3 -m unittest discover -s tests -v`.
