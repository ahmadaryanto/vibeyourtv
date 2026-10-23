# Sky

Latar belakang opsional yang mengikuti jam: biru malam sampai fajar, biru siang yang lembut, lalu
cakrawala hangat yang puncaknya jam tujuh malam. Di konfigurasi bawaan ini mati, jadi
launcher VibeYourTV yang dibangun dari repo ini tetap berperilaku seperti sebelumnya sampai sebuah theme menyalakannya.

```json
{ "sky_intensity": 60 }
```

![Latar belakang saat senja](sky-sunset.jpg)

Dorong itu sebagai `config.json` sebuah theme, lalu mulai ulang Home:

```bash
TV_HOST=192.168.1.50 tools/theme.sh themes/your-theme
```

## Yang digambar

Gradien layar penuh dalam tiga pita — atas, tengah, bawah — dengan cahaya hangat lembut di dekat cakrawala.
Posisi horizontalnya mengikuti matahari: kiri saat matahari terbit, kanan saat terbenam. `sky_intensity` adalah
opasitasnya, 0 sampai 100; warnanya sendiri diinterpolasi di antara beberapa titik jangkar sepanjang
hari, jadi perubahannya mengambang, bukan berpindah di jam yang tetap.

Letaknya di belakang semuanya, di atas wallpaper kalau ada, dan paletnya sengaja gelap:
jam putih, sapaan, nama hero, dan pita dock tetap kontras pada
nilai bawaan 60.

## Where the sun is

Kalau tidak ada yang diatur, hari yang digambar sky adalah satu hari sedang yang tetap: sunrise 07:30, sunset 19:00,
dihitung dari jam TV sendiri. Cahaya hangatnya jadi memuncak jam tujuh malam di mana pun TV
berada, dan posisinya berjalan dari kiri ke kanan sepanjang hari itu — yang di sebagian besar Eropa jatuh beberapa
jam sebelum matahari terbenam sungguhan di musim panas, dan sesudahnya di musim dingin.

Untuk mengunci sky ke matahari sungguhan, isi `latitude` dan `longitude` dalam derajat desimal (selatan dan barat
negatif) di `launcher/app/src/main/res/values/config.xml`, sebelum membangun launcher:

```xml
<string name="latitude" translatable="false">54.69</string>
<string name="longitude" translatable="false">25.28</string>
```

Sky lalu meregangkan jangkar yang sama ke matahari terbit dan terbenam sungguhan di tempat itu: persamaan posisi
matahari NOAA di `Sun.java`, dijalankan di TV dari tanggal hari ini dan zona waktu sistem. Tanpa
jaringan, tanpa izin lokasi, tanpa yang perlu ditanya, dan sepersepuluh derajat hanya menggeser matahari terbenam
sekitar setengah menit, jadi koordinat kota sudah cukup.

Dua jenis hari tetap memakai hari tetap itu: hari tanpa matahari terbit atau terbenam sama sekali (polar night, atau
midnight sun), dan hari yang hitungannya menaruh matahari terbenam lewat tengah malam, seperti Reykjavik di bulan Juni.
Tidak ada yang ditebak dari zona waktu: zona waktu hanya menentukan jam mana yang dipakai untuk menghitung menit.

**Sebuah theme tidak bisa mengatur ini.** `sky_intensity` datang dari `config.json` theme, tetapi tempatnya adalah
setelan build launcher — `SkyView` membaca dua string resource itu dan `Config` tidak pernah mencarinya
di theme. Theme yang ingin matahari yang benar menuliskannya di README-nya sendiri, dengan dua baris di atas.

## Biaya

`Sky` murni matematika — tanpa tipe Android, tanpa jam sendiri; pemanggil mengirim menit sejak
tengah malam, makanya paletnya diuji di JVM. `SkyView` merender satu bitmap 480×270
dan menggambar ulang sekali semenit, dari detak menit yang sama yang memperbarui jam. Tidak ada yang dianimasikan
dan tidak ada yang diminta: yang berubah cuma latar belakang, dan TV dengan RAM 1 GB tidak punya yang
perlu dikejar.

Mematikan key ini mengembalikan latar polos (atau `wallpaper`/`background_color` theme)
pada Home berikutnya.
