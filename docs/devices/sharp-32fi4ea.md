# Sharp 32FI4EA / AQUOS-TVE19G (Android TV 11)

Laporan perangkat awal untuk dua TV Sharp 32FI4EA milik pemilik. Keduanya melaporkan `AQUOS-TVE19G` dan Android 11 lewat ADB.

- Set A: build `RTOB.250806.001`, incremental `13890547`.
- Set B: build `RTOB.240311.001`, incremental `11556578`.
- Tanggal uji: 2026-10-21. Pemilik mengonfirmasi kedua set sudah restart dan aplikasi diuji.

Beberapa hari pemakaian normal belum didokumentasikan. Ini catatan perangkat awal, bukan daftar debloat reusable yang terverifikasi.

## Launcher dan paket TV

- HOME bawaan: `com.google.android.tvlauncher/.MainActivity`.
- HOME pengganti: `com.gathin.tvhome/.MainActivity` (launcher referensi v2.4, dibangun menarget SDK 34).
- Live TV: `com.mediatek.wwtv.tvcenter`.
- Input TV: `com.mediatek.tvinput`.
- Layanan TV: `com.mediatek.tv.service`.

Di set A, memilih HOME pengganti saja tidak menggantikan launcher bawaan. Launcher pengganti diinstal dan dibuka sukses sebelum launcher bawaan dinonaktifkan. Biarkan launcher bawaan terinstal beserta datanya untuk pemulihan. `com.google.android.setupwraith` dipertahankan; tidak dilaporkan sebagai handler HOME saat inspeksi.

Dock dikonfigurasi untuk YouTube, Netflix, Plex, Prime Video dan TV 2 Play. Resolusi HOME, akses Settings dan menu input dicek di kedua set. Menu input mencantumkan HDMI 1-3, Component, Composite dan Live TV; ini tidak membuktikan sinyal HDMI atau fungsi HDMI-CEC. Tidak perlu helper aksesibilitas untuk HOME.

## Paket yang benar-benar dinonaktifkan saat setup ini

Berikut dinonaktifkan dengan `pm disable-user --user 0`, bukan di-uninstall:

- Kedua set: `com.google.android.tvlauncher` - launcher bawaan, hanya setelah launcher pengganti terverifikasi.
- Kedua set: `fusion.android.tv.demo` - aplikasi toko/demo di `/system/app/UMCShopMode`.
- Kedua set: `com.google.android.feedback` - komponen feedback.
- Hanya set A: `tv.anoki.acr.controller` - pengontrol ACR; tidak ada di set B.

Nama paket ini mencatat apa yang diubah, bukan rekomendasi menjalankan perintah disable blanket di firmware lain. Komponen Freeview sudah nonaktif sebelum perubahan ini dan tidak diatribusikan ke setup ini. `com.google.android.tvrecommendations` dipertahankan, termasuk aktivitas terkait notifikasi.

## Fungsi yang dipertahankan dan batas uji

Aplikasi streaming, Google Play Store dan layanan, Chromecast, Assistant, komponen update sistem, layanan TV/input, keyboard dan Bluetooth dipertahankan. Mempertahankan tidak berarti casting, kontrol suara, atau instalasi firmware diuji terpisah.

Pemilik mengonfirmasi pemutaran streaming dan tombol fisik Home, Input, Netflix dan YouTube di set A, lalu mengonfirmasi reboot dan uji aplikasi di kedua set. Tombol pintasan fisik tidak dikonfirmasi terpisah untuk set B. Tidak ada kerusakan yang butuh paket diaktifkan lagi dilaporkan saat cek awal ini. Tidak ada klaim peningkatan performa terukur atau stabilitas jangka panjang.

AdGuard Private DNS juga dikonfigurasi dan divalidasi Android di kedua set sebagai preferensi rumah tangga opsional. Bukan bagian perubahan paket atau syarat launcher ini.

## Pemulihan

Simpan pengaturan asli dan status paket enabled/disabled setiap set sebelum mengubah. Aktifkan kembali paket yang diubah dengan `adb shell pm enable --user 0 PACKAGE`. Untuk home bawaan, aktifkan lagi `com.google.android.tvlauncher` dan pilih `com.google.android.tvlauncher/.MainActivity` sebagai HOME. Utamakan state asli tercatat alih-alih mengaktifkan semua paket sembarangan.

Daftar teruji di `examples/` bisa menyusul setelah beberapa hari pemakaian normal dan konfirmasi lebih lanjut fitur TV yang dipertahankan.
