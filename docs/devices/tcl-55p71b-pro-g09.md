# TCL 55P71B Pro: unit G09 kedua, filter tombol membunuh repeat remote

Dicek di TV sungguhan satu sesi 2026-10-22 (India, Wi-Fi Shan7anu_5G), setelah factory reset,
lalu diverifikasi ulang hari berikutnya. Daftar pendamping: [`examples/tcl-g09-google-tv/`](../../examples/tcl-g09-google-tv),
paket dan tabel tombol remote cocok unit ini.

## Kenali firmware, bukan hanya nama model generik

| Field | Observed value |
|---|---|
| Retail model | `55P71B Pro`, supplied by the owner; not exposed by `ro.product.model` |
| `ro.product.manufacturer` / `brand` | `TCL` |
| `ro.product.model` | `Smart TV Pro` |
| `ro.product.name` (product) | `G09_4K_GB` |
| `ro.product.device` | `G09` |
| `ro.hardware` / `ro.board.platform` | `amlogic` / `t5m` |
| `ro.product.board` | `TC8000` |
| Android / SDK | `12` / `31` |
| `ro.build.display.id` / firmware | `STT2.230203.001.AS23 release-keys` |
| Security patch | `2025-09-05` |
| `ro.product.cpu.abilist` | `armeabi-v7a,armeabi` only (32-bit; no `arm64`, no `x86`) |
| `ro.adb.secure` | `1` |

Unit Brazil di `examples/tcl-g09-google-tv/` melaporkan nama product sama di firmware
`STT2.230203.001 AS24`, jadi keluarga firmware sama satu build lebih awal. `Smart TV Pro` dan
`G09` saja tidak menjamin model ritel — beberapa lini TCL membaginya.

## Volume ditahan tidak repeat selama layanan filter tombol terikat

Temuan catatan ini ada, dan alasan `key_filter` di `launcher/` (issue #763):

- `HomeKeyService` + `InputKeyService` aktif → tahan volume naik/turun di remote langkah sekali per
  tekan.
- Salah satu saja → masih rusak.
- Semua layanan aksesibilitas mati (`enabled_accessibility_services` kosong, `accessibility_enabled 0`)
  → repeat jalan lagi.

Di build ini jalur `AccessibilityInputFilter` menjatuhkan auto-repeat tombol untuk layanan yang minta
`FLAG_REQUEST_FILTER_KEY_EVENTS`; all-or-nothing. `RemoteKeys.applyKeyFilter` biarkan layanan
tetap terikat dengan flag dicabut (`"key_filter": false` di tema), konfigurasi unit ini akhirnya —
tradeoff tombol remote di bawah mati, pilihan pengguna.

## Tick volume hilang dengan launcher bawaan

Beep TV setiap tekan volume hilang setelah launcherx nonaktif, dan tidak kembali: setiap paket debloat
diaktifkan lagi per batch (`pm enable`), TV reboot, tidak ada baris logcat
coba suara saat tekan (`com.tcl.systemui.shared` masih gambar bar volume).
`STREAM_SYSTEM` melaporkan `streamVolume:1` di build ini apapun `volume_system`, tapi itu
ujung buntu diagnostik — tick tampaknya dari home bawaan,
bukan satu paket.

## Tombol remote

| Button | Keycode | Scancode | Note |
|---|---|---|---|
| Gear (Settings) | 83 (`KEYCODE_NOTIFICATION`) | 561 | dead without a helper; stock home opened its panel |
| Netflix | 4062 | 472 | IR; `com.tcl.ttvs`'s `KeyDownBehaviorReceiver` logs `keyDown：keyString=4062 type=IR` but launches nothing |
| Prime Video | 4099 | 473 | same path (from the G09 example's table) |
| YouTube | 4065 | 569 | same path |

Ditangkap dengan `tools/tv.py 'logcat -d | grep handleKeyBeforeQueueing'` saat tekan remote sungguhan —
tombol disuntik tidak pernah sampai filter aksesibilitas. Tombol volume datang `source=0x301, deviceId=6`,
scanCode 115.

## Serah terima Home

Sama contoh G09: `launcherx` punya Home (priority 2), `setupwraith/.RecoveryActivity` klaim
saat launcherx nonaktif, dan jaringan Wi-Fi tersimpan milik setupwraith setelah reset —
`dumpsys wifi | grep -o 'cname=[^ ]*'` menampilkan `cname=com.google.android.tungsten.setupwraith` sampai
jaringan dilupakan dan gabung lagi, lalu `cname=com.android.tv.settings`. Baru then setupwraith
nonaktif dan `build-and-install.sh` dijalankan ulang; cek recovery sendiri menangkap serah terima
gagal dan mengembalikan launcherx pertama kali, seperti dirancang.

## Yang dipertahankan yang contoh nonaktifkan

Pilihan pemilik, diverifikasi jalan: `com.google.android.apps.mediashell` (Chromecast), `com.tcl.miracast`
+ `com.tcl.magiconnectfree` (mirroring), `com.tcl.airplay2` + `com.amlogic.airplay` (AirPlay),
`com.google.android.katniss` (Assistant), `com.tcl.hearaid`, `com.tcl.ttvs` (update firmware; juga punya
`KeyDownBehaviorReceiver`), `com.google.android.tv.remote.service`, `com.google.android.feedback`.
Set disabled contoh sendiri juga bersih di sini.

## Limits

- Satu unit, dua hari; daftar keep di atas preferensi, bukan rekomendasi.
- Tick tidak dipulihkan; mekanisme tidak diketahui, jangan anggap paket sama
  bertanggung jawab di build G09 lain.
- `key_filter` diverifikasi di build launcher sendiri dari `main` plus patch #763;
  tidak ada di build lebih lama — alternatif manual nonaktifkan kedua layanan.
