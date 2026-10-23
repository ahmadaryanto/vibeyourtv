# Sony XR-75X91K (firmware China): catatan kompatibilitas launcher

Laporan perangkat launcher saja, bukan daftar debloat teruji. Model ritel
diberikan pemilik; Android menampilkan model platform generik.
Observasi di bawah memakai launcher lokal kustom turunan
upstream commit `31eb392042009d41bc4ab4b9afadc253db7ab6d6`.
Bukan uji cabang main upstream saat ini.

## Firmware yang diamati

| Field | Observed value |
|---|---|
| Owner-reported retail model | Sony `XR-75X91K` |
| Manufacturer | `Sony` |
| Android model | `BRAVIA 4K VH21` |
| Device | `BRAVIA_VH21` |
| Android / API | `12` / `31` |
| Firmware build | `BRAVIA_VH21_M_CN-user 12 STT2.230505.001.S136 682241 release-keys` |
| Application ABIs | `armeabi-v7a,armeabi` |

Contoh `sony-bravia-4k-vh21` ada untuk KD-65X85K dengan firmware Australia
`M_PA` dan paket Home bawaan berbeda. Berbagi `BRAVIA_VH21` tidak
membuktikan pilihan debloatnya berlaku untuk firmware `M_CN` ini.

## Home bawaan dan serah terima

Komponen Home asli di unit ini:

```text
com.dangbei.TVHomeLauncher/com.dangbei.leard.leradlauncher.ui.sony.main.MainActivity
```

APK launcher referensi terinstal sukses dan start eksplisit
`com.gathin.tvhome/.MainActivity` selesai `Status: ok`. Perintah berikut
dipakai di shell ADB TV:

```sh
cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity
cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
input keyevent 3
```

Resolver dan aktivitas foreground keduanya mengidentifikasi launcher baru setelah
tombol Home disuntik. Paket `com.dangbei.TVHomeLauncher` asli
tetap aktif. Tidak ada paket tambahan dinonaktifkan untuk setup ini, dan
helper aksesibilitas Home/Input opsional launcher tidak diaktifkan.

Saat memakai skrip instal yang biasanya menonaktifkan launcher bawaan,
pakai opsi biarkan-bawaan-aktif (`STOCK_LAUNCHER=none`) dan verifikasi
kepemilikan Home sendiri. Di build teruji ini, disable paket tidak perlu.

Untuk perencanaan restore, catat komponen dan state enablement asli
sebelum mengubah. Komponen asli di atas target restore Home
yang dimaksud; mengembalikan Home ke sana tidak dilatih di laporan ini.

## Live TV dan HDMI

Paket Live TV/player `com.sony.dtv.tvx`; aktivitas launcher mengarah
ke `com.sony.dtv.tvx/.MainActivity`. Pakai `live_tv_package=com.sony.dtv.tvx` di
`config.xml`, dan opsional masukkan paket itu ke `hidden_apps` agar tile
diwakili menu Inputs.

Menu Inputs mencantumkan HDMI 1-4, AV dan Live TV. Pilih HDMI 2 bawa
pemain TV Sony ke foreground dengan sesi input eksternal
(`com.sony.dtv.tvinput.external/.ExternalTvInputService/HW3`). Tombol Home disuntik
kemudian kembali ke launcher kustom. Ini verifikasi routing,
bukan kualitas gambar/suara sumber eksternal atau reception tuner.

TV tidak mengembalikan screenshot usable saat menampilkan input HDMI di
cek itu. Aktivitas player dan sesi input dipakai sebagai bukti;
screenshot launcher tersedia setelah kembali Home.

## Ruang lingkup validasi

- Instal APK, start launcher eksplisit, resolusi Home preferensi dan
  kembali lewat tombol Home ADB dicek di unit ini.
- Navigasi arah dasar, launch apl terinstal dan kembali Home
  dilatih. Cek ini tidak membuktikan pemutaran video atau login akun.
- Restart proses launcher mempertahankan pengaturan launcher lokal.
- Tidak ada rekomendasi debloat baru. State apl/layanan existing
  dipertahankan; tidak ada inventori paket mentah.
- Cold boot, reboot TV, standby/wake, casting, input suara, update firmware,
  periferal USB, reception tuner, setiap tombol remote fisik dan stabilitas beberapa hari
  tidak diverifikasi terpisah.

Laporan ini sengaja tidak memuat alamat jaringan, serial hardware, Android
ID, detail akun, preferensi apl terinstal, screenshot dan dump mentah.
