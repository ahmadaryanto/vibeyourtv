# Membantu seseorang di rumah lain

Semua yang ada di repo ini menganggap komputermu dan TV ada di Wi-Fi yang sama. Kalau TV-nya di
rumah orang tua atau teman, pasang tautan [Tailscale](https://tailscale.com) pribadi antara
komputermu dan rumah mereka. TV lalu berperilaku persis seperti di jaringanmu: prompt,
`tools/tv.py`, screenshot, dan pemasangan launcher VibeYourTV semuanya jalan tanpa perubahan, dengan alamat `100.x.y.z`
alih-alih alamat lokal. Paket personal Tailscale gratis.

**Jangan pernah meneruskan port 5555 di router mereka ke internet.** ADB adalah kendali penuh atas TV, dan
bot memang memindai internet untuk port ADB yang terbuka.

## Yang kamu butuhkan

- Tailscale di komputermu, sudah masuk. Aplikasi Mac dan Windows mengambil rute subnet sendiri;
  di Linux jalankan juga `sudo tailscale set --accept-routes`. Komputermu tidak boleh menjadi perangkat
  [tagged](https://tailscale.com/kb/1068/tags), karena mesin yang di-tag tidak bisa menjangkau yang dibagikan.
- Seseorang di dekat TV dengan remote, di telepon bersamamu sekitar dua puluh menit.
- Salah satu dari dua penyiapan di bawah.

## Opsi A: Tailscale di TV

Penyiapan paling sederhana kalau TV bisa menjalankannya:

- **Android TV dan Google TV:** Tailscale dari Play Store.
- **Fire TV:** Tailscale dari Amazon Appstore, di sebagian besar perangkat dari 2018 ke atas. Tidak di Fire TV
  Stick 4K Select, yang menjalankan Vega OS (dan yang itu memang tidak bisa di-debloat lewat ADB).

Membuka aplikasinya menampilkan kode QR. Lalu salah satu:

1. **Mereka menyimpannya di tailnet mereka sendiri dan membagikan TV-nya kepadamu** (disarankan untuk keluarga). Mereka
   memindai kode dengan ponsel dan masuk dengan akun gratis mereka sendiri. Lalu, di konsol
   admin (Machines → TV itu → **Share**), mereka mengirim undangan dan kamu menerimanya. Kamu melihat
   satu TV itu saja dan tidak ada yang lain di rumah mereka, dan mereka bisa mencabutnya kapan saja.
2. **TV itu bergabung ke tailnetmu.** Mereka memindai kode dan kamu masuk di ponsel mereka dengan akunmu.
   Lebih cepat, tetapi TV itu jadi salah satu perangkatmu sampai kamu menghapusnya.

Alamat `100.x.y.z` TV tampil di aplikasi Tailscale di TV dan di daftar mesinmu.

## Opsi B: perangkat pembantu sebagai subnet router

Pakai ini kalau Tailscale tidak mau terpasang di TV, atau mereka lebih suka tidak memasangnya di sana. Perangkat lain
di rumah mereka meneruskan koneksimu ke alamat lokal biasa TV:

- Mac, PC Windows, atau mesin Linux yang tetap nyala selama sesi, atau Raspberry Pi untuk
  penyiapan tetap;
- Apple TV, lewat opsi Subnet Router di aplikasi Tailscale-nya;
- NAS Synology, dengan Tailscale dari Package Center. Halaman Tailscale-nya (DSM, atau port 5252) punya
  bagian Subnet router, tetapi hanya-baca kalau dibuka dari perangkat yang di-tag. Tanpa SSH, tugas
  Task Scheduler sekali pakai (Control Panel → Task Scheduler → Create → Triggered Task →
  User-defined script, user root, lalu Run dan hapus) menjalankan
  `/var/packages/Tailscale/target/bin/tailscale set --advertise-routes=192.168.1.0/24`;
  setelannya tetap tersimpan;
- bukan ponsel: aplikasi Android dan iOS tidak bisa merutekan jaringan rumah mereka.

Pembantu itu harus ada di tailnet **kamu**, karena mesin yang dibagikan tidak membawa rute subnetnya
ke tailnet lain. (Kalau mereka sudah menjalankan tailnet, misalnya di kantor, kebalikannya juga jalan: mereka
menambahkanmu sebagai pengguna, kamu menambahkan tailnet mereka sebagai akun kedua di aplikasi Tailscale dan beralih
ke situ untuk sesi itu.) Pasang Tailscale di situ dan kirim tautan masuk yang ditampilkannya ke dirimu sendiri; buka
dalam keadaan masuk ke akunmu. Lalu umumkan jaringan rumah mereka (tiga angka pertama alamat
TV, dari setelan jaringan TV):

```bash
# Mac (aplikasi mandiri tanpa CLI: /Applications/Tailscale.app/Contents/MacOS/Tailscale set ...)
sudo tailscale set --advertise-routes=192.168.1.0/24
# Windows, PowerShell sebagai administrator, setelah IP forwarding dinyalakan
tailscale set --advertise-routes=192.168.1.0/24
# Linux, setelah IP forwarding dinyalakan (https://tailscale.com/kb/1019/subnets)
sudo tailscale set --advertise-routes=192.168.1.0/24
```

Setujui rute di konsol adminmu: Machines → pembantu itu → Subnets → Edit. Kamu lalu menjangkau
TV di alamat lokalnya, misalnya `TV_HOST=192.168.1.40`.

**Kedua jaringan tidak boleh tumpang tindih.** Kalau rumah mereka memakai rentang yang sama dengan rumahmu (keduanya
`192.168.1.x`, misalnya), komputermu tidak bisa membedakan TV mereka dari perangkat di rumah. Pakai opsi A, atau
sambung dari jaringan lain.

## Menyambungkan

Minta mereka menyalakan debugging persis seperti di [Quick start](../README.md#quick-start), lalu:

```bash
TV_HOST=100.x.y.z tools/tv.py --probe
```

Mereka menerima "Allow USB debugging?" di TV selagi kamu di telepon. Sisa prompt
jalan seperti biasa; beri tahu asistenmu bahwa TV-nya jauh dan berikan alamat ini, supaya tidak
memindai jaringanmu sendiri.

**TV yang hanya menawarkan Wireless debugging** (sebagian set Google TV): penemuan mDNS tidak menyeberangi
terowongan, jadi mereka membacakan tiga hal dari Opsi pengembang > Wireless debugging: alamat IP
dan port di layar itu, dan, di bawah Pair device with pairing code, port pairing dan kodenya.
Dengan `adb` bawaan:

```bash
adb pair 100.x.y.z:<pairing port> <code>
adb connect 100.x.y.z:<connect port>
adb -s 100.x.y.z:<connect port> tcpip 5555
TV_HOST=100.x.y.z tools/tv.py --probe
```

Dengan opsi B, pakai alamat lokal TV, bukan `100.x.y.z`.

## Hal yang lebih penting kalau kamu tidak di sana

- **Wi-Fi adalah satu-satunya jalan masukmu.** Apa pun yang menjatuhkan TV dari Wi-Fi menjatuhkanmu juga, dan hanya
  orang di dekat TV yang bisa memperbaikinya. Di Google TV, cek siapa pemilik jaringan tersimpan sebelum menonaktifkan
  setup wizard (`dumpsys wifi | grep -o 'cname=[^ ]*'`, lihat
  [TROUBLESHOOTING.md](TROUBLESHOOTING.md)). Kalau yang tertulis setupwraith, minta mereka lupa dan gabung lagi
  ke jaringan di Settings dulu.
- **Tanya, jangan menebak.** Pertanyaan daftar centang di prompt (aplikasi mana, dock, casting, suara,
  pembaruan) untuk mereka jawab, begitu juga apa yang tertancap di port USB.
- **Tinggalkan skrip pulihkan bersama mereka**, atau setidaknya simpan di tempat kamu bisa menjalankannya lain kali
  kamu tersambung.

## Sesudahnya

1. Mereka mematikan USB debugging dan Wireless debugging di Opsi pengembang. Launcher tetap
   jalan.
2. Cabut aksesnya: mereka mencabut share, atau kamu menghapus TV atau pembantu dari tailnetmu
   (atau mematikan rutenya) di konsol admin. Copot Tailscale dari TV kecuali mereka memakainya.

Pernah melakukan ini di TV yang sesuatu berjalan berbeda? Tambah satu baris di sini lewat pull request.
