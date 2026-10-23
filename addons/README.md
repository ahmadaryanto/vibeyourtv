# Add-on

App Android TV kecil opsional yang terhubung ke launcher sebagai **module card**, ada di repo ini
supaya mudah dicari, di-build, dan direview. Launcher di [`launcher/`](../launcher/) tidak
bergantung padanya: launcher di-build, diuji, dan dicek ukurannya tanpa folder ini, jadi add-on tidak
bisa membesarkan core atau mengubah perilakunya. Kamu pasang add-on hanya kalau mau.

## Add-on

| Add-on | Fungsinya | Penulis | Diuji di | Permissions | Screenshot |
|---|---|---|---|---|---|
| [Frigate Glance](frigate-glance/) | Pop-up kamera Frigate di atas apa pun yang sedang diputar | [NickLD](https://github.com/NickLD) | TCL 65Q650G | overlay, accessibility (tombol card), network, foreground service | wanted |
| [Capacity Stats](capacity-stats/) | RAM tersedia/total dan used/total; storage tersedia/total | [lionlev21](https://github.com/lionlev21) | Android TV 12 (model private) | None | wanted |
| [Weather](weather/) | Cuaca satu kota: sekarang, kualitas udara AS, tiga hari ke depan, dari Open-Meteo (tanpa key) | [@Sumyabazar1](https://github.com/Sumyabazar1) | Hisense SmartTV 4K FFM (lushan), Google TV 12 | `INTERNET` | <img src="weather/screenshot.jpg" width="240" alt="Weather on a TV"> |
| [Photo Prints](photo-prints/) | Foto sendiri sebagai screensaver: print bertanggal di langit waktu, card memulai slideshow | [@shivamsingh262](https://github.com/shivamsingh262) | Sony BRAVIA KD-65X82L (Google TV 12) | None | wanted |
| [Photo screensaver](photo-screensaver/) | Foto dan klip MP4 sendiri sebagai screensaver TV dan sebagai card, zoom lambat dan crossfade (default 20 d); opsional jam, tanggal foto, print di langit, cuaca | [JorickGU](https://github.com/JorickGU) | TCL 43P69K (Google TV 12) | `INTERNET` dan coarse location, hanya jika opsi cuaca nyala | wanted |
| [Prayer Times](prayer-times/) | Waktu sholat hari ini satu kota dari Aladhan (tanpa key), sholat berikutnya + countdown, tanggal Gregorian dan Hijriah | [@majnioui](https://github.com/majnioui) | Xiaomi TV (MiTV-MOOR2), Google TV 11 | `INTERNET` | <img src="prayer-times/screenshot.jpg" width="240" alt="Prayer Times on a TV"> |
| [TV Hub](tv-hub/) | Panduan live TV untuk playlist M3U/XMLTV, Dispatcharr, tuner HDHomeRun, player Media3, pintasan ke app live TV terpasang | [@maxhayim](https://github.com/maxhayim) | MOKA R8G (Android TV 14) | `INTERNET` | <img src="tv-hub/screenshot.jpg" width="240" alt="TV Hub on a TV"> |
| [Headphones](headphones/) | Headphone Bluetooth lebih mudah: arah suara, pasangan + baterai, pintasan pairing, guard diam yang pause/mute speaker saat headphone putus | [@maxhayim](https://github.com/maxhayim) | MOKA R8G (Android 14) | Bluetooth (hanya perangkat paired) | <img src="headphones/screenshot.jpg" width="240" alt="Headphones on a TV"> |
| [TV Static](tv-static/) | Momen off-air setiap layar nyala: gambar dari garis terang, salju/hiss, color bar, atau layar "no signal" lama 3–10 d, lalu fade ke yang sedang tampil; card atur gaya, suara, durasi | [@luyzfernando08](https://github.com/luyzfernando08) | Xiaomi Mi Box S (Android TV 9), Philco by Skyworth (Android TV 14) | foreground service (screen-on watcher), start at boot, display over other apps | <img src="tv-static/screenshot.jpg" width="240" alt="TV Static on a TV"> |
| [Brightness](brightness/) | Redup gambar di bawah minimum TV di semua app (dan picture settings di Realtek), mode malam hangat terjadwal, satu tekan layar normal | [@maxhayim](https://github.com/maxhayim) | MOKA R8G (Android 14) | overlay, foreground service, start at boot | <img src="brightness/screenshot.jpg" width="240" alt="Brightness on a TV"> |
| [Sleep Timer](sleep-timer/) | Timer tidur: 15–120 menit, countdown satu menit di atas playback dengan "15 more minutes", lalu standby; lewat HDMI-CEC TV ikut mati dengan box | [@luyzfernando08](https://github.com/luyzfernando08) | Xiaomi Mi Box S (Android TV 9) | accessibility (lock-screen action only), display over other apps, exact alarms | <img src="sleep-timer/screenshot.jpg" width="240" alt="Sleep Timer on a TV"> |

Widget, tile info, card storage atau smart home: kalau bisa jadi app kecil sendiri, bisa di sini.
Lihat aturan di bawah.

## Cara add-on bekerja

Add-on adalah app Android TV biasa dengan proyek Gradle sendiri di `addons/<name>/`. App mendeklarasikan
card dengan kontrak module-card yang ada: panduan [`docs/modules.md`](../docs/modules.md),
spesifikasi [`docs/specs/card.md`](../docs/specs/card.md). Launcher menemukan card, tanya pemilik TV sekali,
dan menampilkan tile dengan label, caption, dan warna glow add-on. Tidak ada hubungan lain.

Build satu (JDK 17, Android SDK 34, seperti launcher), dari root repo:

```bash
cd addons/<name> && ./gradlew assembleRelease && cd ../..
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py --push addons/<name>/app/build/outputs/apk/release/app-release.apk /data/local/tmp/addon.apk
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'pm install -r /data/local/tmp/addon.apk; rm -f /data/local/tmp/addon.apk'
```

## Aturan

- **App sendiri.** Satu folder per add-on, `addons/<name>/`, proyek Gradle sendiri
  (`settings.gradle`, `app/`), package name sendiri, README sendiri.
- **Hanya lewat spesifikasi module-card.** Add-on tidak mengedit apa pun di `launcher/`, dan
  `launcher/` tidak mereferensikan `addons/`. Kalau ide butuh perubahan di launcher, itu bukan add-on.
  Buka issue dulu.
- **Tanpa APK prebuilt** di repo. Orang build dari sumber, seperti launcher.
- **Permissions di depan.** README add-on mencantumkan setiap permission dan alasannya, plus
  akses jaringan atau layanan background. Aturan [`docs/modules.md`](../docs/modules.md)
  berlaku: tanpa telemetri tanpa opt-in, tanpa iklan, art sendiri.
- **Diuji di TV asli.** Sebut model di README dan tabel di atas.
- **Screenshot.** `addons/<name>/screenshot.jpg`: add-on di TV asli, card di grid atau layarnya sendiri,
  JPEG 16:9 (1280×720 cukup) di bawah 300 KB, di kolom Screenshot tabel. Itu yang orang lihat pertama di sini
  dan di [cleanyour.tv/addons](https://cleanyour.tv/addons). Ambil dengan `tools/tv.py --screenshot shot.png`
  (atau `adb exec-out screencap -p > shot.png`), lalu
  `sips -s format jpeg -s formatOptions 85 -z 720 1280 shot.png --out screenshot.jpg` di Mac.
  Blur yang pribadi: nama Wi-Fi, orang, alamat. Add-on bertanda "wanted" di tabel ada sebelum aturan ini;
  screenshot untuk salah satunya disambut.
- **Penulis maintain.** Issue tentang add-on di-assign ke penulisnya. Add-on yang rusak di Android TV
  terkini, atau tidak di-maintain, bisa dihapus. Histori tetap di git, dan kamu bisa lanjutkan di fork
  yang dicantumkan [`SHOWCASE.md`](../SHOWCASE.md).

## Menambah add-on

Buka pull request yang menambah `addons/<name>/` dan satu baris ke tabel di atas. CI core tidak
terblokir add-on: workflow terpisah build apa pun yang berubah di `addons/`, dan
`tests/test_addons.py` cek aturan folder (proyek Gradle sendiri, README dengan bagian Permissions,
screenshot, tanpa APK, baris di tabel ini menampilkan screenshot) dan bahwa launcher tidak pernah
menyertakan atau mereferensikan folder ini.
