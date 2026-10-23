# Lumio Vision 7 (55" / 139 cm)

Daftar Lumio pertama di repo. Kontribusi **Subramanya GB**.

- **Model:** Lumio Vision 7, 55" / 139 cm, India
- **Built by:** KTC (ODM), Realtek chipset
- **Device / product:** `khardi` / `KTC_LUMIO_4K_EU_23GV1`
- **OS:** Google TV 14 (Android 14, SDK 34), build `UKRA.251202.032`
- **RAM:** 2.7 GB

## Masuk (ADB)

Google TV, jadi **Wireless debugging** (Developer options). Di unit ini port **5555** `adb tcpip` klasik
sudah terbuka, jadi setelah pairing/`adb connect` handshake `tools/tv.py` langsung jalan di 5555.

## Satu hal penting: launcher butuh setupwraith dimatikan

Begitu stock launcher (`com.google.android.apps.tv.launcherx`) dinonaktifkan, Home jatuh ke
`com.google.android.tungsten.setupwraith/.RecoveryActivity`, yang menegaskan lagi launcherx — jadi launcher
fork tidak memegang Home sampai **setupwraith juga dimatikan**. Kamu tidak bisa menonaktifkan hanya komponen Home
(`pm disable-user … /.RecoveryActivity` mengembalikan `SecurityException` untuk shell), jadi seluruh paket mati.

setupwraith memegang satu jaringan Wi-Fi tersimpan di sini, tapi jaringan **aktif** milik sistem
(`OwnerUid 1000`), jadi menonaktifkan setupwraith **tidak** menjatuhkan Wi-Fi atau ADB. Guard `tools/disable.sh`
sengaja konservatif, jadi butuh `FORCE_SETUPWRAITH=1` (sudah di baris Apply). Jika Wi-Fi aktif unit kamu
milik setupwraith, forget dan sambung lagi di Settings dulu agar jadi milik sistem.

**Trade-off:** menonaktifkan setupwraith bisa merusak Chromecast (receiver Cast `mediashell` baca info perangkat darinya). Enable lagi setupwraith (`pm enable com.google.android.tungsten.setupwraith`) untuk casting
— tapi Home-stealing kembali, jadi fallback ke Home-redirect alih-alih menonaktifkan launcherx.

## Catatan config.xml

- **App Live TV / tuner:** `com.realtek.tv` → set sebagai `live_tv_package`, dan tambahkan ke `hidden_apps` agar
  tidak juga jadi tile grid (menu Inputs sudah menawarkannya).
- **Tombol app remote** (`app_keys`): remote KTC LUMIO VISION kirim **YouTube 190, Netflix 191,
  Prime Video 195, TL;DR 200**. Setelah swap mereka perlu remap, mis. `190:org.smarttube.stable`. Build dengan `INPUT_KEY_SERVICE=1` agar InputKeyService menangkapnya (dan tombol Source buka menu Inputs).
- Passthrough HDMI/AV dan input tuner ATSC/ATV/ISDB/DTMB dilayani paket `com.realtek.*`;
  tidak ada di daftar.

## Catatan lain

- Netflix (`com.netflix.ninja`) **sudah disabled** dari pabrik di unit ini.
- Paket pabrik `com.kgzn.tvfactory` ada — dibiarkan, jangan dibuka.
- Menonaktifkan `com.google.android.katniss` (asisten suara) bebaskan ~100 MB RAM; opsional di daftar.
