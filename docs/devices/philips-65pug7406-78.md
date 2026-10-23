# Philips 65PUG7406/78: catatan kompatibilitas launcher awal

Dicek di TV pemilik sungguhan dalam satu sesi ADB. Ini **bukan daftar debloat yang sudah diuji**: perubahan paket spesifik pemilik, dan tidak ada uji stabilitas beberapa hari atau regresi cold boot. Jangan salin perubahan di bawah sebagai daftar paket yang aman.

## Kenali firmware persisnya

| Field | Observed value |
|---|---|
| Retail model | Philips `65PUG7406/78` |
| Android / SDK | Android TV 11 / 30 |
| Software version | `TPM215L_R.105.000.097.000` |
| Software build date | 2024-11-25 |
| Android security patch | 2024-11-01 |
| About-screen model | `2020/2021 UHD Android TV` |
| `ro.product.device` | `PH0M_EA_T32` |
| `ro.product.name` | `PH0M_LT_T32` |

Model generik di layar About bukan model ritel. Cocokkan pengenal firmware sebelum menerapkan daftar apa pun untuk set Philips lain.

## Serah terima HOME yang diamati di unit ini

Aktivitas HOME asli adalah `com.google.android.tvlauncher/.MainActivity`. Di firmware ini, `cmd package set-home-activity` melaporkan sukses sementara launcher bawaan masih aktif, tetapi HOME tetap mengarah ke launcher bawaan. Setelah launcher pengganti terinstal dan launcher bawaan dinonaktifkan untuk user 0, HOME mengarah ke `com.gathin.tvhome/.MainActivity` dan launcher terverifikasi berjalan.

Ini mencatat satu serah terima sukses di unit ini; bukan bukti bahwa setiap TV Philips Android 11 butuh urutan yang sama. Uji launcher memakai checkout sumber `022c57da7c4f07466141229cb5625f6ae4fd7ba0`, jadi bukan validasi cabang main upstream saat ini.

## Perubahan aplikasi pilihan pemilik

Pemilik kemudian menonaktifkan Live TV (`com.mediatek.wwtv.tvcenter`) dan Assistant / pencarian suara (`com.google.android.katniss`). Itu preferensi individu, bukan default yang direkomendasikan. Dampaknya pada saluran antena, kontrol suara, switching HDMI, dan pintasan remote tidak diuji sepenuhnya.

Paket produsen dan rekomendasi lain diubah saat setup yang sama, termasuk paket yang dihapus untuk user 0. Kebijakan kontribusi VibeYourTV memakai daftar `pm disable-user` yang bisa dibalik dan meminta beberapa hari pemakaian sebelum menyebut daftar teruji; penghapusan spesifik pemilik itu sengaja tidak dijadikan daftar debloat di sini.

## Batas validasi

- Model TV dan pengenal perangkat lunak dikonfirmasi di Pengaturan Android.
- Aktivitas HOME asli dikonfirmasi, launcher pengganti diinstal, dan aktivitasnya diverifikasi setelah serah terima.
- Tidak ada cold boot, stabilitas multi-hari, scan antena, pemutaran saluran, switching HDMI, casting, atau uji remote fisik penuh setelah perubahan paket.
- ADB dipakai untuk setup. Matikan debugging dan cabut otorisasi debugging saat perawatan selesai.
