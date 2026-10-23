# Master-G 2K Smart TV (KTC `jagalchi`, Android 14): sesi Google TV satu hari

Satu sesi dilaporkan pemilik 2026-10-22, dengan prompt lewat Wi-Fi dari PC Windows. Ini catatan,
bukan daftar beberapa hari: semua di bawah dinonaktifkan dan dicek dalam satu duduk, plus satu reboot.
`debloat.txt` menyusul setelah set dipakai beberapa hari.

| Field | Observed value |
|---|---|
| Brand / maker | `MASTER-G` (retail brand, sold in Chile) / `KTC` |
| Model string | `2K SMART TV`, device `jagalchi`, product `KTC_MASTER_G_2K_SA_25GV1` |
| System | Google TV on Android 14 (SDK 34), `armeabi-v7a`, 1280x720 at 213 dpi |
| Firmware | `MASTER-G/KTC_MASTER_G_2K_SA_25GV1/jagalchi:14/UKN4.250714.046/13878873:user/release-keys` |
| Packages | 147 (139 system, 8 user) |
| Network | Wi-Fi |

## ADB

- Port 5555 terbuka setelah USB debugging nyala; `adb connect` pertama menjawab `failed to authenticate` dan
  prompt "Allow USB debugging?" muncul di TV. Wireless debugging juga nyala dan mengiklankan port acak kedua,
  yang `adb connect` tidak bisa pakai tanpa pairing.
- `adb install` launcher jalan normal (tanpa dialog konfirmasi).

## Ambil alih Home

- Launcher bawaan: `com.google.android.apps.tv.launcherx` (HOME priority 2). Klaim HOME di set:
  `launcherx`, `com.google.android.tungsten.setupwraith/.RecoveryActivity`, launcher, dan
  `com.android.tv.settings.system.FallbackHome`.
- `cmd package set-home-activity` mencetak `Success` dan Home tetap membuka Google TV.
- Hanya `launcherx` nonaktif, Home jatuh ke `RecoveryActivity` setupwraith (membuka layar Settings terakhir).
  Menonaktifkan `setupwraith` juga memberi launcher Home.
- Jaringan Wi-Fi tersimpan milik `setupwraith` (`cname=com.google.android.tungsten.setupwraith` di
  `dumpsys wifi`). Pemilik lupa dan gabung lagi di Settings dulu, setelah itu owner
  `android.uid.system:1000`, barulah `setupwraith` dinonaktifkan; TV tetap di Wi-Fi. Ini
  langkah Home-safety yang sama seperti `aipro2k` dan `tcl-75qm751g-qm7`.
- Dicek reboot: tidak ada yang mengaktifkan diri lagi, launcher tetap Home.

## Yang dinonaktifkan (`pm disable-user --user 0`, tidak ada uninstall)

- Serah terima Home: `com.google.android.apps.tv.launcherx`, `com.google.android.tungsten.setupwraith`.
- Layanan iklan/ML Android 14: `com.android.adservices.api`, `com.android.ondevicepersonalization.services`,
  `com.android.federatedcompute.services`.
- Pilihan pemilik: `com.electronic.manual` (manual di TV), `com.google.android.play.games`, dan aplikasi yang
  pemilik tidak pakai (Prime Video, Netflix, Mercado Play, iQIYI, Jellytelly).

## Sengaja dibiarkan

- Penerima Chromecast (`com.google.android.apps.mediashell`), Play Store, update firmware, asisten suara
  (`com.google.android.katniss`, terikat mikrofon remote), aplikasi Live TV
  `com.mediatek.wwtv.tvcenter.plus` (launcher `live_tv_package`) dan pemutar media
  `com.mediatek.wwtv.mediaplayer`.
- Paket pabrik KTC, tidak pernah diluncurkan atau dinonaktifkan: `com.ktc.tv.factory`, `com.ktc.serialport`.
- Sudah nonaktif di pabrik: `com.cltv.hybrid`, `fusion.android.tv.demo`,
  `com.mediatek.wwtv.tvcenter.overlay`, `com.mediatek.tvinput.overlay`. `com.android.tv.settings` juga muncul di
  `pm list packages -d` tetapi Settings tetap terbuka normal.

## Konfigurasi launcher yang dipakai

Dock: `com.google.android.youtube.tv`, `com.disney.disneyplus`, `com.wbd.stream`, `cl.movistarplay`;
`live_tv_package` dan aplikasi tersembunyi `com.mediatek.wwtv.tvcenter.plus`. Diverifikasi dengan screenshot (dock, grid,
Settings, pill Wi-Fi). Switching input HDMI lewat pill Inputs tidak diuji.
