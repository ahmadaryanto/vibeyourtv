# Cara PR VibeYourTV ditinjau

VibeYourTV (fork dari Gathin23/cleanyourtv) adalah prompt plus alat yang men-debloat Android TV / Google TV / Fire TV lewat
ADB dan launcher asli yang mungil (`launcher/`, Java polos, ~474 KB, untuk TV 1 GB). Kontributor kebanyakan menambah
daftar TV yang sudah diuji di `examples/<tv>/`, terjemahan, dokumen, opsi launcher kecil, tema, add-on, dan alat.

## Kebijakannya: inti kecil plus hub yang terindeks rapi
Repo utama tetap kecil dan sederhana: prompt, daftar TV yang sudah diuji, aturan aman, dan launcher kecil yang stabil
di bawah gerbang ukuran APK (`.github/workflows/ci.yml`, saat ini 491,520 B). Ide yang lebih besar tinggal di sebelahnya,
dan masing-masing mendapat rumah. Pilih rumah terkecil yang muat (CONTRIBUTING, "Ide saya taruh di mana?"):

| Idenya | Taruh di | Vonis |
|---|---|---|
| Model TV, perbaikan, terjemahan, dokumen, perbaikan alat | digabung di sini | merge / merge-with-fix |
| Sakelar launcher kecil: **off by default**, string di **setiap** lokal (atau hanya config), kecil | digabung di sini | merge / merge-with-fix |
| Tampilan baru (warna, wallpaper, gaya dock) yang kunci config yang ada bisa bawa | `themes/<name>/` dengan screenshot TV sungguhan | theme |
| Fitur yang bisa jadi aplikasinya sendiri yang kecil (kartu, widget, screensaver, statistik) | `addons/<name>/`, kartu modul yang tidak pernah menyentuh `launcher/` | addon |
| Tulis ulang besar di dalam launcher, izin atau akses jaringan baru, port Android lama (minSdk lebih rendah), pengambilalihan Home Fire TV, launcher alternatif, perubahan default untuk setiap TV | fork kontributor plus satu baris di SHOWCASE.md | showcase |
| Aturan aman atau PROMPT.md, CI/alur kerja, gerbang APK, paket yang dilindungi | maintainer | hold |

- showcase, addon, dan theme adalah penutupan yang baik (atau, untuk theme, pemindahan): ucapkan terima kasih kepada penulis untuk sesuatu yang spesifik,
  sebutkan di mana idenya muat dan mengapa inti tetap kecil. Itu bukan penilaian atas karyanya. Jangan pernah mengerjakan ulang fitur
  besar menjadi yang kecil; tawarkan baris showcase sebagai gantinya.
- Inti menerima sakelar hanya kalau tidak mengubah apa pun sampai tema atau config.xml menghidupkannya, stringnya ada di
  setiap lokal (atau tidak ada baris setelan), dan muat di anggaran ukuran (beberapa ratus byte sampai ~1 KB; gerbangnya keras).
  Default yang berubah untuk setiap TV adalah pekerjaan ulang (taruh nilai baru di tema atau config contoh TV itu).
- Perbaikan bug launcher yang membetulkan perilaku salah tidak butuh bendera; sudah diuji di perangkat, boleh digabung.
- Perubahan pada jalur Home/peluncuran (tombol Home, boot, kembali ke Home, skrip pemasangan) biasanya butuh dijalankan di
  TV sungguhan: hold kecuali penulis menjalankannya di TV dan perubahannya kecil.

## Vonis
- **merge**: benar, di dalam cakupan, diuji di tempat yang penting, tidak ada yang perlu diubah.
- **merge-with-fix**: perubahan mekanis kecil membuatnya bisa digabung (tambah baris README/examples, string yang hilang
  di satu atau dua lokal, buang berkas liar, komentari entri yang belum diuji, pangkas sel SHOWCASE jadi satu baris).
  Sebutkan persis apa yang diubah di `fix`.
- **rework**: layak ada tapi penulis harus mengubah sesuatu yang nyata. Satu permintaan yang jelas.
- **close**: duplikat, sudah tersusul, di luar cakupan, atau melawan aturan dasar. Bersikap baik; tunjuk yang sudah mendarat.
- **showcase / addon / theme**: lihat tabel di atas.
- **hold**: keputusan maintainer dibutuhkan (panggilan cakupan atau selera, perubahan aturan aman, biner siap pakai), atau
  harus dijalankan dulu di TV sungguhan.

## Daftar
- Daftar **menonaktifkan, jangan pernah uninstall**. Jangan pernah menonaktifkan yang TV butuhkan: layanan TV/input/tuner, menu input,
  pemasangan remote dan Bluetooth, keyboard, Play Store, dan Play Services. Menonaktifkan
  `com.google.android.tungsten.setupwraith` biasa di daftar Google TV (mengembalikan Home tapi mengorbankan casting;
  header daftar harus mengatakannya). Paket pabrik/AT boleh dinonaktifkan, jangan pernah dijalankan.
- Jangan pernah paket inti Android (#884): pengontrol izin, pemasang paket, ext services/shared,
  metadata modul, overlay mainline (`com.google.android.overlay.modules*`), systemui, shell, phone,
  settings, tv.settings, `android`, dan `com.android.providers.*` (calendar, contacts, userdictionary, dan
  blockednumber boleh). `tools/disable.sh` menolak daftar seperti itu, jadi ini **rework**: hapus mereka.
- Langkah `pm uninstall` di folder daftar → **rework**: jaga folder itu hanya menonaktifkan.
- Penyiapan Private DNS / AdGuard di dalam folder daftar dibuang saat penggabungan (README utama sudah punya
  ekstra DNS opsional); catatan bahwa "Private DNS works on this set" boleh.
- Skrip pasang/pulihkan milik folder itu dibuang saat penggabungan: README menunjuk ke `tools/disable.sh` dan
  `tools/restore.sh`.
- Entri yang penulis tandai belum diuji dikomentari saat penggabungan sampai teruji. Pembaru firmware milik TV
  lebih baik tetap aktif; kalau sebuah daftar menonaktifkannya, header harus menyebut alasannya.
- Kode produk dan firmware yang sama dengan folder yang sudah ada di main: biasanya unit kedua, re-badge, atau ukuran
  saudara dari perangkat yang sama → **rework**: lipat catatannya ke folder itu, sebagai unit kedua atau varian (#648, #751,
  #752, #794, #798 semuanya dikembalikan karena ini). Model berbeda yang daftarnya membuat pilihan yang jelas berbeda
  boleh punya foldernya sendiri (#653, #768). Ini panggilan penilaian: setel keyakinan sesuai itu.
- Suntingan daftar yang sudah ada di main (unit kedua, catatan, koreksi) adalah panggilan penilaian; putuskan hanya saat
  perubahannya jelas benar.
- Daftar baru butuh barisnya di README dan examples/README (hilang → merge-with-fix: tambah barisnya).
- `pm hide`/`pm block` butuh pembatalan unhide/unblock yang cocok di header.

## Cakupan dan selera
- Launcher tetap kecil, offline, dan cepat: tanpa izin INTERNET, tanpa fitur jaringan (pembaruan mandiri,
  kripto, API cuaca pernah ditolak), tanpa pustaka berat, tanpa biner siap pakai di repo (bahkan sebagai artefak
  CI).
- Perubahan aturan aman di PROMPT.md, logika keamanan Home di skrip, aturan TCL China,
  CI/alur kerja, atau gerbang ukuran APK butuh maintainer: **hold**.
- Duplikat: model/folder TV yang sama sudah digabung atau ada di PR terbuka lain → close atau lipat ke situ.
- Satu desain per fitur: kalau dua PR melakukan hal yang sama, simpan satu (merge atau rework yang lain ke dalamnya). Keluarga menu ubin
  tahan-OK adalah satu desain (#464, dengan #746); menu ubin lain dilipat ke situ (pemicu tombol Menu di #687).
- PR yang membundel beberapa bagian bagus yang bisa dipisah mendapat **rework**: pecah (daftarkan tiap bagian). Daftar sebuah perangkat plus
  perbaikan kecil untuk perangkat yang sama boleh satu PR. Daftar yang dibundel di dalam PR fitur launcher pergi sebagai PR sendiri.
- PR yang bentrok dengan main mendapat permintaan rebase yang baik (**rework**) yang menyebut berkas yang bentrok, apa pun
  yang lain benar tentangnya (disetujui 22 Okt). Bentrok hanya di README.md, examples/README.md, atau SHOWCASE.md bukan
  itu: tabel-tabel itu digabung secara union saat merge, jadi rebase tidak dibutuhkan.
- Duplikat: saat PR terbuka yang lebih awal, atau PR yang digabung dalam seminggu terakhir, memperbaiki issue yang sama di berkas yang sama
  (atau menambah baris yang sebagian besar sama), PR yang belakangan adalah duplikatnya: **close** dan sebut yang satunya (#873 setelah
  #842, #947 setelah #924). Seri bertumpuk (satu PR dibangun di atas yang lain) bukan duplikat.
- PR yang sudah berupa add-on (`addons/<name>/`) dinilai berdasarkan kelayakannya (merge, merge-with-fix, rework),
  jangan pernah dikirim "ke addons/". `gradle/wrapper/gradle-wrapper.jar`-nya dibutuhkan CI add-on, bukan
  biner siap pakai. Add-on yang menduplikasi yang sudah ada (sumber sama, kartu sama) dilipat ke situ (#972).
- Berkas yang tidak ditaut apa pun (screenshot, berkas akar yang nyasar, perubahan build yang tidak terkait) dibuang saat penggabungan.
- Baris pers dan liputan butuh sumber yang bisa dibuka dan dicek; sisakan yang tidak bisa diverifikasi.
- Dokumen dan README harus tetap sejalan: daftar baru butuh barisnya di README dan examples/README.

## Gaya komentar
Pendek, manusiawi, berterima kasih: sebut satu hal spesifik yang bagus dari PR. Sebut apa yang berubah saat digabung. Permintaan
rework satu baris. Penutupan baik dan menyebut di mana ide bisa tinggal sebagai gantinya. Jangan pernah menyebut siapa atau apa yang menulis
tinjauan.

## Dipelajari dari keputusan lalu (sudah ditinjau)
- Apa pun yang melawan aturan dasar ("no root, nothing uninstalled, you build it yourself") **ditutup** dengan baik,
  bahkan kalau hanya dokumen atau baris showcase: eksploit root, mengakali perlindungan toko aplikasi, build privat atau
  yang dimodifikasi yang orang lain tidak bisa ulang dari repo ini, dan tambahan pada alur PROMPT di luar debloat
  (DNS seluruh sistem atau pemblokiran iklan). Tunjuk versi publik yang bisa diulang kalau ada.
  (APK siap pakai yang di-commit ke repo ini tetap **hold**: maintainer yang memutuskan.)
- Cek "off by default" di diff, bukan di deskripsi: kuncinya ada di config.xml, JSON tema, dan
  tabel themes/README, dan dengan mati tidak ada yang baru berjalan (tidak ada receiver, tidak ada tulisan, tidak ada pemberian izin di
  skrip pemasangan). Kalau sesuatu berjalan saat mati → **rework**.
- Saat dua PR terbuka membangun helper atau perbaikan yang sama, pilih satu sebagai rumahnya dan minta penulis yang lain melipat
  bagian mereka (rework, bukan hold). Kalau PR lain sudah memperbaiki hal yang sama dengan lebih lengkap, tutup yang lebih sempit.
- PR kontributor yang isi utamanya alur CI baru yang membangun atau menerbitkan rilis: **close** dengan baik.
- PR launcher yang menambah izin INTERNET atau akses jaringan → **showcase**; kalau membawa daftar TV, minta
  daftar itu sebagai PR sendiri di komentar yang sama.

## Dipelajari dari putaran 21–22 Okt (sudah ditinjau)
- PR terjemahan yang memindahkan string hard-code ke resources diterima; perubahan build yang tidak terkait yang menumpang
  (test runner di build.gradle) dan screenshot yang tidak ditaut apa pun dibuang saat penggabungan (#640).
- Perubahan Home-guard atau skrip pemasangan butuh dry run di TV yang penuntut Home-nya termasuk launcher uji-pabrik
  (`com.duokan.factorytest/.FactoryLauncher` di Xiaomi), bukan hanya tes unit (#565).
- PR launcher yang mengubah default untuk setiap TV (kisi pustaka Aplikasi, crossfade di setiap rotasi,
  screensaver jam yang menampilkan wallpaper) pergi ke showcase daripada dikembalikan untuk rework (#672, #673, #674); sebuah
  sakelar kecil yang hanya butuh defaultnya dimatikan tetap rework.
