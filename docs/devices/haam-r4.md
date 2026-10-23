# HAAM HM4K75S11TND: memilih tuner satelit

Dicek di HM4K75S11TND ritel yang model Android `R4`, Android 11 / API 30,
Realtek `rtd2851m`, software `V8-T851MGL-0001103`, fingerprint
`HAAM/HAAM/R4:11/RTT7.221026.001/3172:user/release-keys`.
Observasi routing launcher, bukan daftar debloat atau klaim kompatibilitas firmware.

![Inputs menu on the tested HAAM R4](haam-r4-inputs.png)

## Beberapa tuner, satu apl Live TV

Pemain `com.apps.livetv`. Terpisah dari paket yang menyediakan
input. Firmware ini mendaftarkan keempat ini sebagai input sistem `TYPE_TUNER`:

| Label | Input ID |
|---|---|
| ATV | `com.apps.atv/.atvinput.AtvInputService/HW33619968` |
| CABLE | `com.apps.dtv/.tvinput.DTVTvInputService/HW33685504` |
| ANTENNA | `com.apps.dtv/.tvinput.DTVTvInputService/HW33685505` |
| SATELLITE | `com.apps.dtv/.tvinput.DTVTvInputService/HW33685506` |

Heuristik tuner otomatis memilih ATV di sini: ID tidak mengandung `analog`.
Meski prefer input digital juga tidak memberi launcher mana dari kabel,
antena atau satelit yang parabola pemilik terhubung. URI passthrough untuk input satelit
juga membuka AV di firmware ini. Rute yang jalan adalah
**koleksi saluran** input satelit, memakai `TvContract.buildChannelsUriForInput`
dan `TvContract.Channels.CONTENT_TYPE`, dibatasi `com.apps.livetv`.

Untuk konfigurasi persis ini, gabungkan kunci ini ke `config.json` tema:

```json
{
  "live_tv_package": "com.apps.livetv",
  "live_tv_input_id": "com.apps.dtv/.tvinput.DTVTvInputService/HW33685506"
}
```

Atau set string setara di `launcher/app/src/main/res/values/config.xml`.
ID harus cocok tuner sistem terdaftar persis; dapatkan ID sendiri dari
`tools/tv.py 'dumpsys tv_input'` alih-alih salin angka model lain.
Jika input pilihan hilang, atau pemain terkonfigurasi tidak resolve
koleksi saluran, baris buka apl Live TV normal. Tidak diam-diam
pilih tuner lain. Biarkan `live_tv_input_id` kosong untuk pertahankan seleksi otomatis
dan routing OEM.

Input terdaftar tidak berarti dikonfigurasi atau usable. ID salah tapi
ada bisa diterima apl Live TV dan buka layar setup tuner kosong,
jadi fallback di atas tidak jalan. Pakai tuner terverifikasi di TV sendiri,
atau kosongkan `live_tv_input_id` untuk seleksi otomatis. Di
[cek komunitas Philips 65PUS8102/12](https://github.com/Gathin23/cleanyourtv/pull/534#issuecomment-6315693748)
(Android TV 8.0, TPM171E / MediaTek), HW0 buka pemain sementara HW9 terdaftar
buka layar setup kosong. Cek intent dan aktivitas fokus,
bukan cek gambar, dan ID itu jangan disalin ke TV lain.

Ini memilih input, bukan program tertentu, posisi orbit satelit atau
frekuensi. Database saluran dan pemain TV masih mengontrol tuning. Tidak
scan, renumerasi saluran, ganti driver tuner atau ubah setting parabola.

## Bukti dan batas

Di launcher kustom pemilik, memilih koleksi satelit setelah AV
buka input satelit dan program existing. Pemilik konfirmasi gambar
dan suara normal. Sesi TV-input native melaporkan `HW33685506`.
Perubahan yang diusulkan juga dibangun terhadap commit upstream `687b5ee` dan diuji
sebagai aplikasi terpisah di TV sama. Dengan ID satelit eksplisit,
launch aktivitas memakai URI koleksi saluran dibatasi paket dan sesi
TV-input aktif melaporkan `HW33685506`. Dengan ID sengaja tidak ada, launcher
mengirim intent launch leanback normal ke apl Live TV
terkonfigurasi. Up/Down/Left/Right dicek di baris pertama dan terakhir popup Inputs;
Back menutup popup. Screenshot di atas dari build uji itu.

Konfirmasi gambar/suara di atas milik build kustom sebelumnya;
build uji terpisah diverifikasi lewat intent launch dan sesi input.
Jangan anggap screenshot kosong bidang video sebagai reception gagal; cek
sesi dan tanya yang menonton TV.

TV melaporkan AV terhubung meski jack kosong. Port HDMI tidak aktif
melaporkan standby apakah kabel terpasang atau tidak. Laporan state itu tidak bisa
membuktikan kehadiran kabel fisik. Perubahan seleksi tuner ini tidak mengubah
indikator koneksi atau menyembunyikan baris input.
