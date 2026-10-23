# Alur: Menambah atau memperbaiki terjemahan launcher

**Kapan dipakai:** bahasa launcher baru, atau string yang hilang dari bahasa yang sudah ada.

1. Buat `launcher/app/src/main/res/values-<qualifier>/strings.xml` dan terjemahkan setiap string di
   `launcher/app/src/main/res/values/strings.xml`, lewati yang bertanda `translatable="false"`. Pertahankan
   placeholder (`%1$s`, `%d`) dan escape XML.
2. Tambah locale ke `launcher/app/src/main/res/xml/locales_config.xml`.
3. Tambah bahasa ke daftar Languages di `README.md`.
4. Untuk memperbaiki string yang hilang di locale yang sudah ada, tambahkan ke setiap folder `values-*` yang kamu bisa, dengan urutan yang sama seperti
   `strings.xml` bawaan. String yang hilang jatuh kembali ke bahasa Inggris (`launcher/app/lint.xml`), tetapi sakelar baru
   butuh string-nya di setiap locale.
5. Memindahkan teks yang tertulis langsung di kode ke resource diterima; jangan ikutkan perubahan build yang tidak berhubungan.
6. Bangun launcher, atur bahasa TV (Android 13+: `tools/tv.py 'cmd locale set-app-locales com.gathin.tvhome --locales <tag>'`)
   dan lampirkan screenshot. Bahasa kanan-ke-kiri membalik tata letak seperti Ibrani; `tests/test_hebrew_resources.py`
   memeriksa `layout-iw` tetap memuat setiap view.

Lalu jalankan tes: `python3 -m unittest discover -s tests -v`.
