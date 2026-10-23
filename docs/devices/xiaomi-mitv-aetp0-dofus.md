# Xiaomi MiTV-AETP0: allowlist Home nama paket

Diuji di MiTV-AETP0 (`dofus`, Amlogic, ~2 GB RAM), Android 9 / API 28,
firmware `22.9.2.851`. Home bawaan `com.mitv.tvhome/.MainActivityUserMode`.
Catatan launcher saja; tidak ada daftar debloat diterapkan.

## Apa yang gagal dan mengapa

APK `com.gathin.tvhome` standar terbuka normal, tetapi set sebagai Home melaporkan
`Success` sementara HOME tetap Xiaomi. Menonaktifkan Xiaomi Home memilih
`com.android.tv.settings/.system.FallbackHome`; mengaktifkan Xiaomi mengembalikan pemakaian normal.
Redirect aksesibilitas jalan di sesi saat ini, tetapi entri enabled-service
dihapus saat reboot.

`Success` while HOME still resolved to Xiaomi. Disabling Xiaomi Home selected
`com.android.tv.settings/.system.FallbackHome`; re-enabling Xiaomi restored normal use.

Analisis read-only framework perangkat ini menemukan `chooseBestActivity` mempertahankan
kandidat HOME sistem atau paket diizinkan `MitvSettings.isThirdpartyHomeAllowed`
sebelum preferensi tersimpan. Lookup whitelist membandingkan nama paket persis
dan flag fitur, tanpa cek sertifikat signing di lookup itu.
XML security-center generik dari layanan cloud-control Xiaomi mencantumkan
`com.ts.launcher` di `third_party_home` dan `boot_autostart`.
XML on-device terlindungi tidak bisa dibaca langsung; instalasi
dan cek Home berikut, bukan respons cloud saja, yang membuktikan kompatibilitas di sini.

## Build dan instal identitas opt-in

Pakai hanya setelah verifikasi perilaku dan allowlist untuk firmware tertentu.
Biarkan launcher bawaan aktif. Cek `pm path com.ts.launcher` dulu:
jika paket sudah terinstal, berhenti alih-alih timpa aplikasi lain.
ID alternatif milik aplikasi lain; simpan build kompatibilitas ini lokal,
dan harapkan konflik signing jika APK aplikasi itu terinstal nanti.

Dari root repo, dengan prasyarat Java/SDK dan Python normal:

```bash
export TV_HOST=192.168.1.50
PY=tools/venv/bin/python
"$PY" -I tools/tv.py 'pm path com.ts.launcher'
"$PY" -I tools/tv.py 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
(cd launcher && ./gradlew assembleRelease -PlauncherApplicationId=com.ts.launcher)
"$PY" -I tools/b64push.py launcher/app/build/outputs/apk/release/app-release.apk /data/local/tmp/tvhome-allowlist.apk
"$PY" -I tools/tv.py 'pm install -r /data/local/tmp/tvhome-allowlist.apk'
"$PY" -I tools/tv.py 'am start -W -n com.ts.launcher/com.gathin.tvhome.MainActivity'
```

Setelah inspeksi layar baru, pilih dan verifikasi Home:

```bash
"$PY" -I tools/tv.py 'cmd package set-home-activity --user 0 com.ts.launcher/com.gathin.tvhome.MainActivity'
"$PY" -I tools/tv.py 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
```

Komponen yang diharapkan `com.ts.launcher/com.gathin.tvhome.MainActivity`. Namespace Java
tetap `com.gathin.tvhome`, jadi `com.ts.launcher/.MainActivity` salah.
Skrip install/tema/restore normal masih menarget `com.gathin.tvhome`; pakai perintah
manual ini untuk build ini. Overlay tema opsional di
`/sdcard/Android/data/com.ts.launcher/files/config.json`. Instalasi asli dan
konfigurasinya terpisah. Tanpa properti Gradle, build mempertahankan
application ID standar.

## Validasi dan pemulihan

Uji on-device memakai sumber launcher 2.0 dengan ID alternatif: instalasi,
resolusi HOME default dan Home dari Settings lulus, dengan Xiaomi Home masih aktif
dan tanpa layanan aksesibilitas launcher. Jellyfin dan Synology DS Video
di dock. Pemilik konfirmasi pengganti jalan setelah reboot. ADB sebentar
terbuka saat reboot lalu tertutup, jadi inspeksi foreground pasca-boot tidak tertangkap.
Build sumber saat ini dicek terpisah di PR; tidak diinstal sebagai bagian
uji reboot itu. Versi firmware lain, persistensi OTA dan pemakaian harian extended
belum diverifikasi.

Untuk restore Home bawaan tercatat di firmware ini:

```bash
"$PY" -I tools/tv.py 'pm enable --user 0 com.mitv.tvhome'
"$PY" -I tools/tv.py 'cmd package set-home-activity --user 0 com.mitv.tvhome/.MainActivityUserMode'
"$PY" -I tools/tv.py 'input keyevent KEYCODE_HOME'
"$PY" -I tools/tv.py 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
```

Aktifkan ADB lagi di Account & Security jika tertutup saat reboot. Start langsung
`MainActivityUserMode` dari shell butuh izin `HOME_STATE` Xiaomi; pakai
tombol Home setelah restore preferensi. Tidak perlu root, ubah properti, instal
aplikasi sistem atau hapus Home bawaan untuk prosedur sukses.

![Alternate-ID launcher on the tested TV](xiaomi-mitv-aetp0-home.png)
