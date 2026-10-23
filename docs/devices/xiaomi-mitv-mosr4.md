# Xiaomi MiTV-MOSR4 (irobot): unit kedua, firmware EU

Google TV / Android 11, build `RTM5.220609.003/1585` (sama unit pertama), RAM 1,4 GB.
Daftar: [`examples/xiaomi-mitv-mosr4/debloat.txt`](../../examples/xiaomi-mitv-mosr4/debloat.txt).
Laporan hari pertama, dua reboot.

## Urutan takeover

Menonaktifkan launcherx sementara setupwraith masih aktif meninggalkan Home di
`setupwraith/.RecoveryActivity`. ADB hilang saat Wi-Fi disimpan ulang, TV
di-reboot dalam state itu, setup jalan lagi dan TV **factory reset**.

Jika `mFocusedApp` menampilkan `.RecoveryActivity`, aktifkan launcherx dulu. Jangan reboot di situ.

Yang jalan, dicek reboot:

1. Simpan ulang Wi-Fi (di bawah), nyalakan USB debugging lagi.
2. Nonaktifkan setupwraith sementara launcherx masih aktif.
3. Nonaktifkan launcherx, `set-home-activity`, tekan Home, cek `mFocusedApp`.

## Wi-Fi

Jaringan tersimpan saat setup milik setupwraith (`dumpsys wifi`, `cname=`). Reconnect
mempertahankan owner itu (ID jaringan sama). Lupakan dan gabung lagi; lalu milik
`com.android.tv.settings`. Melupakan mengalihkan USB debugging mati di TV ini.

## Tombol remote

| Button | Scancode | Keycode |
| --- | --- | --- |
| Input | `000c00b2` | 178 `TV_INPUT` |
| Settings | `000c018f` | 176 `SETTINGS` |
| YouTube | `000c0188` | 190 `BUTTON_3` |
| Netflix | `000c008e` | 193 `BUTTON_6` |
| Prime Video | `000c00b0` | 194 `BUTTON_7` |

Dengan launcherx nonaktif kelima tombol tidak melakukan apa-apa. Global key untuk
`com.xiaomi.android.tvsetup.partnercustomizer/.GlobalKeyReceiver`, yang memulai aktivitas launcherx
dan crash. Setelah beberapa crash Android berhenti memulainya:

```
Unable to launch app com.xiaomi.android.tvsetup.partnercustomizer/1000 for broadcast
  Intent { act=android.intent.action.GLOBAL_BUTTON ... }: process is bad
```

Tangkap tombol sebelum sampai sana:

```sh
INPUT_KEY_SERVICE=1 HOME_KEY_SERVICE=1 TV_HOST=<tv ip> launcher/build-and-install.sh
TV_HOST=<tv ip> tools/theme.sh examples/xiaomi-mitv-mosr4
```

`config.json` memetakan 190/193/194. Tombol terpetakan yang aplikasinya nonaktif ditelan. Dicek dengan
remote sungguhan (tanpa baris `GLOBAL_BUTTON`, tanpa crash), memakai filter kunci setara di
launcher mandiri; belum dengan `com.gathin.tvhome`. Tombol Netflix ke receiver ini,
bukan overseaservice. Tombol mic tetap jalan.

Kedua helper merugikan live key repeat remote di set ini: tahan volume naik/turun gerak satu langkah
per tekan, dengan `InputKeyService` saja maupun keduanya, dan `dumpsys accessibility` menampilkan layanan terikat
di `capabilities=8` (`CAPABILITY_CAN_FILTER_KEY_EVENTS`) - [#944](https://github.com/Gathin23/cleanyourtv/issues/944).
Disabling launcherx while setupwraith was still enabled left Home on
`setupwraith/.RecoveryActivity`. Bisect, cara kembali ke salah satu perilaku dan cara repeat hilang ada di
[catatan G09](tcl-55p71b-g09-key-repeat.md). Kontrol daftar kosong tidak dicoba di sini: mengosongkan daftar
melepas tombol yang bagian ini pasang helper untuk, jadi "the ramp returns with nothing bound"
pengukuran catatan itu, bukan set ini.

## Lainnya

- `com.mitv.tvhome.mitvplus` diaktifkan lagi setiap boot.
- `com.xiaomi.mitv.manualhelp` ada di sini (`/system/customer-app`).
- Switching HDMI: `ACTION_VIEW` pada URI passthrough buka `com.mitv.livetv/.tv.MainActivity`.
- `screencap -p` cetak banner sebelum PNG; tangkap ke `/sdcard` dan pull.
- Nama Wi-Fi di bar atas butuh Location nyala.
