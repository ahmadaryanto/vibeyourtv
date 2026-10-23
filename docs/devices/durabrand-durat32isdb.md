# Durabrand DB Android TV 4K (`DURAT32ISDB`, dupont): laporan pemilik satu hari

Laporan pemilik
[#470](https://github.com/Gathin23/cleanyourtv/issues/470): satu sesi dan satu hari pemakaian, tanpa
launcher terinstal. It is **not** a tested list in the `examples/` sense — CONTRIBUTING asks for
several days of living with a list before it goes there — jadi paket dan dua jebakan
dicatat di sini, dan catatan bisa naik ke `examples/` setelah pemilik
menjalankannya lebih lama.

## Kenali firmware

| Field | Reported value |
|---|---|
| Retail model | Durabrand DB Android TV 4K |
| Product | `DURAT32ISDB` |
| Device | `dupont` |
| SoC | `m7332` (Haier / CVTE board) |
| Android / SDK | Android TV 11 / 30 |
| `ro.build.display.id` | `RTM6.230109.232` |
| Security patch | 2023-08-01 |

Papan keluarga Haier/CVTE sama dengan
[`examples/haier-android-tv-dvb2k/`](../../examples/haier-android-tv-dvb2k/debloat.txt) dan
[`examples/haier-h32s80efx/`](../../examples/haier-h32s80efx/debloat.txt), tapi set paket
berbeda: unit ini punya apl disclaimer mstar, panel input MediaTek, dan launcher konten sistem
yang firmware restart setiap boot. Terapkan daftar itu di sini hanya setelah cek setiap
nama dengan `dumpsys package`.

## Yang dinonaktifkan pemilik

Sepuluh paket, `pm disable-user --user 0`, satu hari tanpa kerusakan dilaporkan.

| Package | What it is |
|---|---|
| `fusion.android.tv.demo` | Retail demo (`HRetailDisplay_2K`) |
| `com.haier.apps` | Store shortcuts |
| `com.haier.apps.evoSmart` | Store / partner promo |
| `com.mstar.android.tv.disclaimercustomization` | Store disclaimer screen |
| `com.google.android.feedback` | Google feedback |
| `com.google.android.videos` | Play Movies |
| `com.google.android.play.games` | Play Games |
| `com.google.android.youtube.tvmusic` | YouTube Music |
| `com.android.printspooler` | Printing (no printer on a TV) |
| `com.google.android.syncadapters.calendar` | Calendar sync for an account nobody signs in with |

Pemilik juga set tiga skala animasi ke `0.5` dan melaporkan RAM bebas dari 495 MB ke
550-651 MB dari 1,34 GB, plus tidak ada demo retail, disclaimer toko atau promo Haier/Okko saat boot.
Netflix, YouTube, Disney+, Max, Prime Video, Jellyfin, HDMI, CEC dan casting masih jalan di
akhir sesi.

## Dibiarkan, dan mengapa

- `com.google.android.tvrecommendations` dan `com.haier.launcher.haiermatrix` come back on every
  boot: `com.at.factory.factorymanager` memanggil `enableApp` untuk keduanya, dan yang kedua apl sistem
  re-enabled during the boot package scan. Disabling them looks like it worked until the next
  restart. ([PR #434](https://github.com/Gathin23/cleanyourtv/pull/434) is the tool that reports
  this kind of drift; until it is merged, check by hand with
  `tools/tv.py 'pm list packages -d | grep -E "tvrecommendations|haiermatrix"'` after a reboot.)
- `com.haier.tv.hmagicboard` menerima hot key remote, dan `com.cvte.tv.hkcfac` apl pabrik
  yang juga menerimanya - lihat jebakan di bawah sebelum sentuh salah satu.
- `com.google.android.tungsten.setupwraith` punya jaringan Wi-Fi tersimpan di firmware ini. Lupakan dan
  gabung lagi sebelum nonaktifkan, atau TV offline permanen:
  lihat [entri Wi-Fi setupwraith](../TROUBLESHOOTING.md).
- `*partnercustomizer` dan `com.android.location.fused` dipertahankan.

## Tombol Input milik apl TV MediaTek, bukan `tvlauncher`

Panel di belakang tombol Input remote dari `com.mediatek.wwtv.tvcenter`, bukan
`com.google.android.tvlauncher`. Setelah swap launcher tombol Input tetap jalan
meski `tvlauncher` nonaktif, tapi panel overlay apl TV sendiri: `InputKeyService` launcher referensi tidak perlu, dan `INPUT_KEY_SERVICE=1` jangan diset di unit ini berdasarkan
daftar Haier lain.

## Menu produksi tersembunyi jebakan mahal

Membuka `com.cvte.tv.hkcfac` (aplik "production" dengan ikon launcher yang tidak dipakai) set
`customer_factory_mode=true` di `/vendor/tvcertificate/fdata.ini` dan
`vendor.mtk.factory.disable.input=1`. Tiga hal mengikuti:

- tombol Input berhenti dan CEC mati;
- boot berikutnya `factorymanager` menonaktifkan Play services, Netflix, `com.google.android.katniss`
  dan `com.google.android.apps.mediashell`, dan shell tidak bisa aktifkan paket itu lagi;
- casting dan store mati sampai factory mode dibersihkan.

Jalan keluar tanpa root, dilaporkan: start apl, kirim keycode exit factory 218 dua kali **saat
di depan**, reboot, lalu nyalakan CEC lagi:

```sh
tools/tv.py 'am start -n com.cvte.tv.hkcfac/.MainActivity'
tools/tv.py 'input keyevent 218; input keyevent 218'
tools/tv.py --reboot               # returns once ADB accepts it; wait for the TV to come back
tools/tv.py 'settings put global hdmi_control_enabled 1'
```

Keycode exit hanya jalan dengan apl di foreground, jadi dua key event jangan dikirim
sebelum digambar. Jebakan sama dalam prosa, di entri "perintah yang merusak" lain,
ada di [TROUBLESHOOTING.md](../TROUBLESHOOTING.md).

## Ruang lingkup validasi

- Satu hari pemakaian, satu pemilik, tanpa soak multi-hari dan tanpa kampanye reboot di luar exit factory-mode.
- Tidak ada launcher kustom terinstal: Projectivy jalan, pemilik kembali launcher bawaan
  preferensi. Serah terima Home, helper Input/Home launcher dan `restore.sh` tidak dilatih
  di unit ini.
- Daftar paket milik pemilik, direproduksi di sini dilaporkan; tidak dibaca ulang dari TV oleh
  penulis catatan, dan tidak ada screenshot `dumpsys package`.
- Angka RAM milik pemilik, diukur sebelum dan sesudah satu sesi.
