# Acer R4G 50" (MOKA `R4_GTV`, Google TV 14): firmware mengaktifkan paket lagi setiap boot

Laporan pemilik #1106 oleh [@madhusoodhans](https://github.com/madhusoodhans) (diagnosis juga #1135), India. Empat hari pemakaian tanpa reboot, lalu cabut listrik. Catatan,
bukan daftar: di firmware ini daftar disable-only tidak bisa menjauhkan Home bawaan, jadi tidak ada
folder `examples/` untuk TV ini.

| Field | Observed value |
|---|---|
| Retail model | Acer R4G 50" |
| Manufacturer / device | MOKA / `R4_GTV` |
| Board | Realtek `rtd2851m`, 2 GB RAM, 32-bit ARM |
| Android | Google TV 14 |
| Build | `UKR1.260308`, security patch `2026-04-05` |
| Fingerprint | `ACER/ACER_NFF/R4_GTV:14/UKR1.260308/5597:user/release-keys` |

Panasonic 4KTV-3MUP di #1116 platform MOKA `R4_GTV` sama di build `UKR1.260308`
dan perilaku sama.

## Apa yang terjadi

Pemilik menonaktifkan 31 paket per batch kecil, lalu `com.google.android.apps.tv.launcherx`
dan `com.google.android.tungsten.setupwraith` agar launcher baru pegang Home. Jalan sampai
cold boot pertama. Setelah itu Home hitam sementara volume, HDMI dan tombol apl masih jalan, dan
logcat menampilkan `.RecoveryActivity` setupwraith distart sebagai HOME setiap ~130 ms:

```
I/ActivityTaskManager: START u0 {act=android.intent.action.MAIN cat=[android.intent.category.HOME] ... cmp=com.google.android.tungsten.setupwraith/.RecoveryActivity (has extras)}
```

## Mengapa

`com.android.tv.settings` Acer/Realtek dimodifikasi. Setiap boot `.system.FallbackHome`-nya menjalankan
`SetPmodServiceJob`, memanggil `disableOrEnableApk(1)` dan mengaktifkan kembali setiap paket di daftar
hard-coded 60, kecuali flag pabrik `P_MODE` diset (butuh root). setupwraith ada di daftar. Setelah
kembali, `.RecoveryActivity`-nya (HOME priority 1) mengalahkan launcher pihak ketiga (priority 0)
dan loop mencoba kembalikan launcherx yang nonaktif. Standby dan wake tidak menjalankan job; hanya
cold boot.

Yang shell tidak bisa di sini: `pm hide` butuh `MANAGE_USERS`, dan menonaktifkan hanya `.RecoveryActivity`
ditolak (`Shell cannot change component state`).

## Artinya untuk debloat

- **Biarkan `launcherx` dan `setupwraith` aktif di TV ini.** Launcher baru masih bisa ambil tombol Home
  dengan `HOME_KEY_SERVICE=1` dan `stock_home_redirect=com.google.android.apps.tv.launcherx`,
  cara Panasonic #1116 menjalankannya (issue itu melacak perbaikan cold-boot untuk redirect).
- 20 ini ada di daftar firmware, jadi disable hanya sampai cold boot berikutnya:

  ```
  com.google.android.tungsten.setupwraith
  com.google.android.katniss
  com.google.android.apps.tv.dreamx
  com.google.android.videos
  com.google.android.play.games
  com.google.android.feedback
  com.apps.ota
  com.apps.authota
  com.realsil.ota
  com.google.android.tv.dfuservice
  com.apps.biz.mt
  com.mk.tv.meeting.service
  com.apps.function.audiolink
  com.mk.tv.timeclock
  com.apps.tvmanager
  com.apps.esticker
  com.apps.esticker3
  com.apps.function.gdpr
  com.apps.scast
  com.creative.fastscreen.tv
  ```

- 11 ini tetap nonaktif lewat cold boot (`launcherx` di antaranya, yang membuat Home hitam
  setelah setupwraith kembali):

  ```
  android.autoinstalls.config.scbc.device
  com.alphainventor.filemanager
  com.android.tv.feedbackconsent
  com.apps.mk.wd
  com.apps.wifihotspot
  com.google.android.apps.tv.launcherx
  com.jio.media.stb.ondemand
  com.mk.tv.systemserver
  com.mxtech.videoplayer.television
  com.realtek.leanbacklauncher.partnercustomizer
  com.yupptv.scope.tvapp
  ```

Pemilik melangkah lebih jauh dari aturan disable-only proyek VibeYourTV untuk pegang launcher sendiri sebagai Home.
Langkah itu tidak didokumentasikan di sini.

## Jika Home sudah hitam setelah reboot

Cek ini kasusnya: `tools/tv.py 'logcat -d | grep RecoveryActivity'`. Lalu aktifkan launcher bawaan
segera dan jangan reboot sementara `.RecoveryActivity` pegang layar (reboot di situ
factory-reset Xiaomi MiTV-MOSR4, lihat [TROUBLESHOOTING](../TROUBLESHOOTING.md)):

```sh
tools/tv.py 'pm enable --user 0 com.google.android.apps.tv.launcherx'
```
