# Proyektor JMGO G9S (China, `m7642_JmGO_H7A0`, MediaTek m7642)

Android TV 9 (SDK 28), firmware `1.1.52` (`PPR2.180905.006.A1`, userdebug/dev-keys), build low-RAM
(`ro.config.low_ram=true`), 32-bit `armeabi-v7a`, 1920x1080 @ 320 dpi, 80 paket terinstal.
`ro.product.model` generik `JmGO`; string board dan device `m7642_JmGO_H7A0`.

Laporan satu sesi, bukan daftar beberapa hari. Delapan paket dinonaktifkan, `tools/check.py`
laporkan tidak drift dan Home mengarah ke `com.gathin.tvhome/.MainActivity` sebelum reboot matikan ADB
final. Reboot dan beberapa hari pemakaian belum diamati, jadi paket demo dibiarkan
dikomentari di bawah sampai tombol Input remote dicek saat nonaktif.

## Home dan input

- Launcher bawaan: `com.jmgo.launcher/.home.desktop.activity.JGHomeActivity`.
- HOME juga mengarah ke `com.jmgo.tv/.MainActivity` (aplikasi input HDMI/AV) dan
  `com.jmgo.fallbackhome/com.jmgo.settings.FallbackHome` (placeholder firmware).
  Karena `com.jmgo.tv` jawab HOME, Home guard menganggapnya fallback usable. **Jangan**
  masukkan `com.jmgo.launcher` ke daftar debloat: instal pengganti dulu, konfirmasi punya Home,
  lalu nonaktifkan launcher bawaan
  (`launcher/build-and-install.ps1 -StockLauncher com.jmgo.launcher`).
- Tidak ada antena atau tuner. `com.jmgo.tv` hanya buka sumber HDMI/AV; tidak ada apl Live TV tuner untuk
  `live_tv_package`.
- Pass `com.jmgo.launcher` ke `tools/restore.sh` / `restore.ps1 -Stock` saat undo.

## Paket dinonaktifkan (satu sesi)

```text
# Retail demo, but it registers TV_INPUT_BUTTON receivers on this firmware; keep it commented
# until the remote's Input key is tested with it off.
# fusion.android.tv.demo

# MStar media test program, not a user app
com.mstar.android.media.tests

# Google partner-row customizer; no Google launcher on this set, inert
com.google.android.leanbacklauncher.partnercustomizer

# JMGO music backends and local media player (USB/local playback stops with the player off)
com.jmgo.music
com.jmgo.os.music
com.jmgo.player

# JMGO browser and image viewer
com.jmgo.browser
com.jmgo.imagebrowser4k
```

## Sengaja dipertahankan

- Casting: `com.jmgo.holacast.x` (HappyCast/AirPlay) dan `com.jmgo.hilink`.
- Suara: `com.bajintech.assistant` dan `com.jmgo.ai.voice`.
- Remote ponsel: `com.softwinner.tvremoteserver`.
- Toko aplikasi: `com.jmgo.appshop` dan `com.dangbeimarket`.
- Konten: `com.ktcp.video` (Tencent), `com.xiaodianshi.tv.yst` (Bilibili TV), `com.tencent.karaoketv`.
- Optik dan remote: `com.jmgo.hippo` (penanganan tombol, brightness), `com.jmgo.autokeystone`,
  `com.jmgo.tofautofocus`, `com.jmgo.autocolortemp`, `com.jmgo.a2dpsinkservice` (mode speaker
  Bluetooth), `com.jmgo.remotecontrolota`.
- Update firmware: `com.jmgo.update`.
- Setiap paket `com.mediatek.*` (input TV, arbitrator input, dispatcher hotkey) dan setiap
  paket sistem `com.android.*`.

## ADB

- `ro.adb.secure=0`: ADB jaringan di 5555 menerima host mana pun di LAN tanpa prompt Allow.
- `adb_enabled=0` saja tidak menutup port: `service.adb.tcp.port` dan `persist.adb.tcp.port`
  masih 5555. Keduanya diset `-1`, TV di-reboot, dan `tools/adb-closed.py <tv ip>`
  melaporkan port tertutup.
- Cabut kunci debugging tersimpan butuh layar opsi developer TV; tidak bisa dari shell di build ini.
