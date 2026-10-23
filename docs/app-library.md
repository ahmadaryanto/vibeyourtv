# Pustaka aplikasi

## Mode pilihan (opsional)

Isi `"app_library_selection": true` bersama `"app_library": true` di theme,
atau nyalakan kedua boolean di `config.xml`, kalau pustaka dipakai sebagai pemilih
apa yang tampil di Home. Mati secara bawaan, dan perilaku standar di bawah tidak berubah.

Di mode pilihan, OK mengalihkan centang hijau. Aplikasi yang dicentang tampil dulu; kedua
kelompok diurutkan menurut abjad nama yang tampil, memakai locale TV. Setelah menyembunyikan
aplikasi, fokus tetap di dekatnya, pada aplikasi berikutnya (aplikasi sebelumnya kalau sudah di ujung),
bukan ikut pindah ke kelompok kedua. Menambah aplikasi memfokuskan aplikasi itu
di kelompok yang dicentang. Aksi tekan-tahan OK tidak ada di mode ini; mode ini hanya mengatur
pintasan Home dan tidak pernah mencopot aplikasi.

## Perilaku standar

Isi `"app_library": true` di `config.json` theme, atau nyalakan bool `app_library`
di `config.xml`. Mati secara bawaan. Pil kaca kecil muncul setelah
baris terakhir aplikasi Home.

Pustaka horizontal menampilkan aplikasi terpasang yang punya aktivitas peluncur TV atau Android biasa,
dan lebih memilih entri TV kalau keduanya ada. Layanan dan aplikasi
tanpa aktivitas peluncur tidak ikut. Aplikasi Android biasa kadang tetap butuh sentuhan atau mouse.

Aplikasi yang belum ada di Home tampil dulu. Yang baru terpasang dalam tujuh
hari terakhir memimpin kelompok itu, yang terbaru di depan, dengan titik hijau; yang hilang dan lebih lama
diurutkan menurut abjad. Aplikasi yang sudah di Home menyusul menurut abjad, dengan centang dan
warna tipis. Memperbarui aplikasi yang sudah ada tidak menandainya sebagai baru.

- Tekan OK pada aplikasi yang belum ada untuk menambahkannya ke Home. Fokus maju ke aplikasi yang belum ada berikutnya,
  tanpa mengikuti pintasan baru itu melintasi katalog.
- Tekan OK pada aplikasi yang sudah dicentang untuk membukanya.
- Tahan OK untuk Buka, Tambah ke Home atau Hapus dari Home, dan Info aplikasi. Menunya memakai
  resource dropdown Input yang sudah ada dan tetap di atas kartu yang dipilih.
- Back menutup menu dulu, baru pustaka. Turun dari baris Home terakhir, atau
  Kanan dari aplikasi paling ujung, sampai ke pil pustaka; Naik kembali ke baris terakhir.
  Ibrani membalik arah horizontal, jadi Kiri dari aplikasi paling ujung yang sampai ke situ.

Dengan `tile_menu` nyala, menahan OK pada ubin Home juga menawarkan Hapus dari Home, di samping Pindah dan Info aplikasi.
Tombol Menu di remote membuka menu yang sama pada ubin yang fokus. Dengan `tile_menu` mati,
Menu tetap berperilaku biasa. Pemetaan tombol Setelan lebih diutamakan; Menu dibiarkan
pada tombol header, saat popup lain terbuka, dan selama menyusun ulang.
Setiap aplikasi yang dicentang bisa dihapus dari Home, termasuk aplikasi bawaan theme.
Aplikasinya tetap terpasang dan tetap ada di pustaka, di mana satu tekan menambahkannya lagi.
Tidak ada bagian aplikasi tersembunyi dan tidak ada aksi copot pemasangan.

Pilihan disimpan di `library_apps.txt` dan
`library_home_removed.txt` milik launcher, masing-masing paling banyak 128 entri. Setiap berkas ditulis
secara atomik. Berkas yang tidak terbaca dibiarkan apa adanya kalau penyimpanan gagal. Pilihan
bertahan melewati mulai ulang dan pembaruan aplikasi; paket yang hilang atau nonaktif dilewati sampai
kembali. Penghapusan Home yang eksplisit mengalahkan bawaan theme sampai ditambahkan lagi.
Konfigurasi theme, urutan dock, keputusan kartu modul, dan urutan ubin lain yang tersimpan
tidak diubah. Menonaktifkan tombol pustaka tidak menghapus pilihan Home yang sudah ada.

Pustaka memakai penerima paket yang sudah ada untuk menyegarkan diri setelah aplikasi berubah.
Label dan keanggotaan Home di-cache tiap penyegaran; ikon hanya dimuat untuk
kartu yang sedang terikat. Sorotan mengikuti setelan animasi launcher, termasuk
pindah seketika kalau animasi mati. Tidak ada polling, jaringan, tangkapan latar, layanan baru,
izin, atau dependensi. Query manifes tambahan mencakup aktivitas
LAUNCHER biasa di Android 11 dan yang lebih baru.
