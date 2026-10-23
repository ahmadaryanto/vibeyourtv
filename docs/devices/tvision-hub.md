# TVision Hub (SEI Robotics, S905X2): laporan satu sesi dengan daftar yang jalan

Laporan dari [#577](https://github.com/Gathin23/cleanyourtv/issues/577): satu sesi, hasil
hari yang sama saja. Itu sebab tujuh paket dicatat di sini alih-alih `examples/` -
CONTRIBUTING minta beberapa hari memakai daftar, dan belum ada yang reboot, standby atau
restore yang ini.

## Kenali perangkat

| Field | Reported value |
|---|---|
| Retail name | TVision Hub (T-Mobile's Google TV dongle) |
| Maker | SEI Robotics |
| SoC | Amlogic S905X2 |
| Android | Google TV, with a T-Mobile operator layer |
| Build fingerprint | not recorded in the report |

Baris identitas sengaja tipis: laporan tidak membawa `ro.build.fingerprint`,
`ro.product.device` atau level patch Android, jadi cocokkan paket di bawah dengan output
`dumpsys package` sebelum menerapkan apa pun di sini ke unit TVision lain, dan jangan anggap
OTA meninggalkannya di tempat sama.

## Yang dinonaktifkan pemilik

Tujuh paket, `pm disable-user --user 0`, sesi yang sama:

| Package | What it is |
|---|---|
| `com.nes.tvbugtracker` | bug tracker |
| `com.nes.tvbugreportsender` | bug-report sender |
| `com.google.android.tvrecommendations` | recommendation rows |
| `com.sei.hitv.explore` | SEI's content browse app |
| `com.google.android.videos` | Play Movies |
| `com.android.camera2` | camera app (the dongle has none) |
| `com.google.android.play.games` | Play Games |

Tidak ada uninstall, jadi `tools/restore.sh` bisa mengembalikan ketujuh. Laporan tidak menyebut paket
Home bawaan: baca sebelum membatalkan serah terima, karena `restore.sh` butuh sebagai
argumen launcher di set yang launcher bawaannya bukan `com.google.android.tvlauncher`:

```sh
TV_HOST=<hub ip> tools/venv/bin/python -I tools/tv.py \
  'cmd package resolve-activity -c android.intent.category.HOME'
```

## Home

Launcher dibangun, diinstal dan dikonfirmasi sebagai Home di hub ini, dengan layar home bersih
setelahnya - jadi lapisan operator di dongle ini tidak memblokir Home pihak ketiga seperti beberapa
build operator. Laporan tidak bilang apakah `com.google.android.tungsten.setupwraith` harus
pergi dulu (di onn 4K Pro laporan yang sama iya, dan installer mengembalikan serah terima
sampai dinonaktifkan), jadi cek pemilik Home dengan `dumpsys activity activities | grep
mFocusedApp` setelah takeover alih-alih percaya baris sukses installer.

## Dua keanehan koneksi

- **ADB tidak connect sampai hub di-restart.** Tidak ada yang berubah di sisi host; `adb connect` yang sama
  jalan setelah reboot.
- **Run build pertama hang di langkah TV-connect `launcher/build-and-install.sh`.**
  `adb disconnect` lalu run ulang selesai normal. Jika skrip build macet di dongle Amlogic, disconnect dan reconnect sebelum anggap kegagalan build.

## Limits

Satu sesi, tanpa siklus reboot atau standby, tanpa run restore, tanpa screenshot. Anggap daftar sebagai
titik awal yang pernah terlihat jalan sekali.
