# Xiaomi Mi Box S (MIBOX4), unit kedua

Mi Box S kedua, dikerjakan dengan `PROMPT.md` dan `examples/xiaomi-mi-box-s/debloat.txt` pada 2026-10-21.
Build sama dengan unit pertama: `Xiaomi/oneday/oneday:9/PI/3933:user/release-keys`, patch keamanan
2021-07-05, SDK 28, RAM 2 GB, 1920x1080, `ro.adb.secure=1`. Dikerjakan dengan `tools/tv.py` (tanpa binary adb)
dan skrip `.ps1`.

## Yang cocok dengan daftar

Setiap paket di daftar ada di unit ini dan bisa dinonaktifkan dengan `pm disable-user --user 0`. Dinonaktifkan
dalam tiga batch dengan screenshot di antaranya (telemetri; PatchWall, baris dan updater; casting dan
preload), lalu launcher, lalu Backdrop setelah `screensaver_components` diset ke `ClockDream`.
Tidak ada yang rusak antar batch.

- Home: `com.google.android.tvlauncher` sebelumnya, seperti unit pertama. Satu-satunya klaim HOME lain
  `com.android.tv.settings/.system.FallbackHome`; tidak ada klaim setupwraith.
- `launcher/build-and-install.ps1` berhasil pertama kali: `set-home-activity` dengan komponen, launcher
  bawaan dinonaktifkan, `mResumedActivity` di `com.gathin.tvhome/.MainActivity`.
- Setelah reboot, 25 paket yang sama tetap nonaktif: tidak ada yang diaktifkan lagi saat boot. Home dan Wi-Fi
  kembali sendiri. `MemAvailable` 1,08 GB sebelum (box sudah jalan lama) dan 1,23 GB tepat setelah
  reboot.
- Pill Wi-Fi hanya menulis "Wifi": Lokasi mati, seperti header daftar.
- Hanya USB debugging saja membuka port 5555.
  USB debugging alone opened port 5555 (`service.adb.tcp.port=5555`, `sys.usb.config=adb`).

## Di mana unit ini beda

- **Tidak ada yang nonaktif sebelum mulai** (`pm list packages -d` kosong). Lima aplikasi yang daftar
  biarkan dikomentari sebagai "already disabled on this unit" (Prime Video, YouTube Music, Play Movies, Play
  Games, Camera) semua aktif di sini; dinonaktifkan pilihan.
- **Google Play Music** (`com.google.android.music`) terinstal dan aktif. Layanan tutup 2020; dinonaktifkan tanpa efek samping.
- **Wi-Fi tersimpan bukan milik setupwraith**: `dumpsys wifi | grep -o 'cname=[^ ]*'` hanya menampilkan
  `android.uid.system:1000` dan `com.google.uid.shared:10011`. setupwraith tidak dibutuhkan untuk
  takeover di build Android TV 9 ini dan dibiarkan.
- **Tombol Apps remote bisa dibuat berguna.** Mengirim scancode 580 (`KEY_APPSELECT`), yang
  `Vendor_2717_Product_32b9.kl` petakan ke `ALL_APPS`. Android mengirim ke `com.android.tv.settings`,
  yang tidak melakukan apa pun (unit pertama sama, juga di launcher bawaan). `HomeKeyService` launcher
  menangkap `KEYCODE_ALL_APPS`, jadi mengaktifkannya (`-HomeKeyService` di
  `launcher/build-and-install.ps1`) menjadikan tombol Apps tombol Home kedua. Dicek dengan remote
  sungguhan dari VLC: tekanan memulai `com.gathin.tvhome/.MainActivity`.
- Tombol lain, dicek dengan remote sungguhan dan `getevent -lt`: Home (`KEY_HOMEPAGE`), Back,
  suara (`KEY_VOICECOMMAND`, Assistant tetap) dan Netflix (`KEY_VIDEO`, scancode 393 `NETFLIX` di
  `.kl` yang sama) semua jalan dengan launcher bawaan nonaktif.

`live_tv_package` kosong (tanpa tuner) bisa didorong sebagai `config.json` tema dengan `tools/theme.ps1`,
jadi `config.xml` tetap default.

## Build di Windows tanpa Android Studio

`launcher/build-and-install.ps1` hanya mencari JDK di `JAVA_HOME` atau `jbr` Android Studio. Zip JDK 17 Temurin
plus Android command-line tools (`sdkmanager "platforms;android-34"
"build-tools;34.0.0"`, SDK di `%LOCALAPPDATA%\Android\Sdk`) jalan dengan `JAVA_HOME` ke JDK.

Sudah dipakai: satu hari sejauh ini; masih perlu dicoba beberapa hari seperti CONTRIBUTING.md minta.
