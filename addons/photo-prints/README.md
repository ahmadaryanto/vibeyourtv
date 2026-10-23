# Photo Prints

Foto sendiri sebagai screensaver: tiga print di langit waktu, tiap foto utuh (tidak pernah crop) dengan
sudut membulat dan bulan/tahun diambil. Tiap 15 detik geser ke kiri satu per satu, tiga baru dari kanan,
sedikit miring. Garis di bawah menampilkan jam dan tanggal, bergeser sedikit tiap sweep agar tidak burn-in.

Di Home VibeYourTV juga module card **Photo Prints**: OK memulai slideshow yang sama, Back keluar.

- Package: `io.github.shivamsingh262.photoprints`
- Penulis: [@shivamsingh262](https://github.com/shivamsingh262)
- Diuji di: Sony BRAVIA KD-65X82L (Google TV 12)

## Permissions (izin)

None. App membaca foto dari folder sendiri di TV, tanpa permission, tanpa jaringan
(tanpa `INTERNET`): nothing leaves the TV. Tanpa layanan background: jalan hanya saat screensaver atau slideshow di layar.

## Build dan pasang

JDK 17 dan Android SDK 34, seperti launcher. Dari root repo:

```bash
cd addons/photo-prints && ./gradlew assembleRelease && cd ../..
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py --push addons/photo-prints/app/build/outputs/apk/release/app-release.apk /data/local/tmp/addon.apk
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'pm install -r /data/local/tmp/addon.apk; rm -f /data/local/tmp/addon.apk'
```

Tekan Home: launcher tanya sekali apakah card ditampilkan.

## Tambah foto

Foto ada di folder app di TV:
`/sdcard/Android/data/io.github.shivamsingh262.photoprints/files/photos`. Di Mac, `photos.sh` mengisi dari
folder, resize tiap foto ke 1280 px dulu (HEIC included) dan menulis tanggal ambil ke nama:

```bash
TV_HOST=<tv ip> addons/photo-prints/photos.sh ~/Pictures/TV
TV_HOST=<tv ip> addons/photo-prints/photos.sh --clear     # remove them all
```

`jpg`, `jpeg`, `png`, atau `webp` yang disalin ke folder itu juga jalan. Nama berakhiran tanggal,
`something-20231202.jpg`, jadi tanggal print; else EXIF kamera jika ada, else tanpa tanggal. Foto ditambah saat TV nyala muncul sweep berikutnya.
Tanpa foto, layar menunjukkan folder untuk copy.

`photos.sh` dan `adb push` diuji di Sony di atas, app membaca file yang shell copy. Beberapa firmware tidak (#921 TCL G10); print kosong, layar tetap folder.

## Pakai sebagai screensaver

Settings > System > Ambient mode or Screen saver > **Photo Prints** (nama menu tergantung TV), atau:

```bash
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'settings put secure screensaver_components io.github.shivamsingh262.photoprints/.PrintsDream'
```

## Hapus

```bash
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'pm uninstall io.github.shivamsingh262.photoprints'
```

Folder foto ikut terhapus. Jika dipakai sebagai screensaver, pilih yang lain di Settings.
