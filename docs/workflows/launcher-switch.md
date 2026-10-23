# Alur: Menambah sakelar launcher yang mati secara bawaan

**Kapan dipakai:** perilaku atau tampilan launcher yang tidak muat di key konfigurasi yang sudah ada.

Inti hanya menerima sakelar kalau tidak ada yang berubah sampai theme atau `config.xml` menyalakannya:

1. Tambah key ke `launcher/app/src/main/res/values/config.xml` dengan komentar yang menjelaskan fungsinya dan
   bahwa theme bisa mengaturnya, bawaan mati (`false`, `0`, atau perilaku yang sekarang).
2. Baca di `launcher/app/src/main/java/com/gathin/tvhome/Config.java`, dari resource dan dari
   JSON theme, seperti key yang sudah ada misalnya `show_stats`.
3. Tambah satu baris ke tabel Keys di `themes/README.md`.
4. Kalau menambah baris di Launcher settings, taruh string-nya di `launcher/app/src/main/res/values/strings.xml`
   dan setiap folder `values-*`.
5. Dengan key mati, tidak ada yang baru berjalan: tidak ada receiver, layanan, penulisan, atau pemberian izin di skrip
   pasang. Bot memeriksa ini di diff, bukan di deskripsi.
6. Tambah tes unit JVM di `launcher/app/src/test/java/com/gathin/tvhome/` untuk logika yang bisa diuji
   tanpa TV.
7. Buat tetap kecil: satu sakelar sebaiknya menambah beberapa ratus byte sampai sekitar 1 KB pada APK. Bangun dan periksa gerbang
   ukuran; gerbangnya keras.
8. Verifikasi di TV sungguhan dengan sakelar nyala dan mati, D-pad dari setiap tepi, dan lampirkan screenshot.
9. Kalau bisa, jalankan pemeriksaan regresi di [`perf-ab.md`](perf-ab.md) dengan
   sakelar nyala: baseline sebelum mulai, build-mu sesudahnya, dan angkanya di PR. Opsional, tapi diterima.

Yang khusus satu TV masuk ke `config.xml` atau theme, bukan ke kode. Perbaikan bug yang membetulkan perilaku salah tidak butuh
sakelar. Jangan menambah dependensi di `launcher/app/build.gradle` tanpa issue dulu.

Lalu jalankan tes: `python3 -m unittest discover -s tests -v`.
