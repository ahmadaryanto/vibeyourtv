# Kontribusi

Terima kasih sudah membantu lebih banyak TV lepas dari iklannya. Tiga jenis kontribusi sangat diterima.

Kerja bareng agen coding? Arahkan ke [`AGENTS.md`](AGENTS.md): aturan yang sama, ditulis untuk agen.

## Ide saya taruh di mana?

Launcher sengaja tetap kecil, jadi pilih rumah terkecil yang muat:

| Idemu | Taruh di |
|---|---|
| Model TV, perbaikan, terjemahan, sakelar kecil yang mati secara default | Pull request di sini (bagian 1 sampai 3 di bawah); sakelar baru dengan screenshot di [`docs/switches.md`](docs/switches.md) |
| Tampilan baru: warna, wallpaper, dock, kolom | [`themes/`](themes/README.md), dengan screenshot dari TV sungguhan |
| Widget atau ubin yang bisa jadi aplikasinya sendiri yang kecil | [`addons/`](addons/README.md), sebagai kartu modul dengan screenshot dari TV sungguhan; tidak pernah menyentuh `launcher/` |
| Launcher yang sama sekali baru, restyle berat, port ke platform lain | Repomu sendiri, plus satu baris di [`SHOWCASE.md`](SHOWCASE.md) (bagian 4) |

Semua yang digabung ke `themes/` dan `addons/` muncul di [cleanyour.tv/addons](https://cleanyour.tv/addons).

## 1. Tambahkan model TV-mu

Setiap merek mengirim bloat yang berbeda. Kalau kamu menjalankan prompt (atau mengerjakannya tangan) di TV yang belum ada di
`examples/`, tambahkan. Langkah yang sama untuk agen coding ada di
[`docs/workflows/tv-list.md`](docs/workflows/tv-list.md).

1. Buat `examples/<brand>-<model>/debloat.txt`. Salin format dari
   `examples/xiaomi-mi-tv-4a/debloat.txt`: satu paket per baris, dikelompokkan dengan komentar yang menyebut
   apa kelompok itu, dan komentar di ekor untuk yang tidak jelas ("aktifkan lagi untuk pembaruan
   firmware", "dibutuhkan untuk HDMI-CEC", dan sebagainya).
2. Taruh nama model yang persis, versi Android, dan build firmware di header supaya orang bisa tahu
   apakah cocok dengan milik mereka. `tools/tv.py --probe` mencetak ketiganya.
3. Hanya daftarkan paket yang benar-benar sudah kamu nonaktifkan dan pakai beberapa hari. Catat apa pun yang
   rusak dan apa yang kamu aktifkan kembali.
4. Tambah satu baris ke `examples/README.md` dan satu ke tabel TV di `README.md`: tes membandingkannya,
   jadi daftar yang barisnya hanya di salah satu dari keduanya gagal.
5. Kalau aplikasi Live TV atau launcher bawaan TV-mu punya nama paket yang berbeda, sebutkan di header. Di tempat
   skrip membutuhkan launcher bawaan, `tools/restore.sh` menerimanya sebagai argumen kedua
   (`tools/restore.sh <list> <package>`) dan `launcher/build-and-install.sh` menerimanya di
   environment, sebagai `STOCK_LAUNCHER=<package>` (`-StockLauncher` di kembaran PowerShell, dan `none`
   membiarkannya tetap aktif) — bukan sebagai argumen. Launcher menampilkan aplikasi Live TV-mu di menu Input dari
   `live_tv_package`, di `config.xml` atau sebuah tema.

Jangan pernah mendaftarkan paket untuk *di-uninstall*. Semua yang ada di repo ini menonaktifkan dengan `pm disable-user`, yang
bisa dibalik dari setelan TV sendiri kalau alatnya sudah tidak ada.

Paket milik Android sendiri juga bukan bloat. Jangan pernah mendaftarkan pengontrol izin, pemasang
paket, ext services atau ext shared, metadata modul, overlay `com.google.android.overlay.modules*`,
systemui, shell, phone, settings, tv.settings, `android`, atau paket `com.android.providers.*`
selain kalender, kontak, kamus pengguna, dan nomor diblokir. TV yang kehilangan salah satu
dari mereka bisa tidak pernah boot lagi ([#884](https://github.com/Gathin23/cleanyourtv/issues/884), dan
[apa yang dilakukan kalau sudah terjadi](docs/TROUBLESHOOTING.md#working-on-the-tv)), jadi
`tools/disable.sh`, `disable.ps1`, dan `check.py --fix` menolak daftar seperti itu mentah-mentah, tanpa sakelar untuk
menimpanya, dan pull request yang menyebut mereka dikembalikan untuk dikerjakan ulang.

## 2. Perbaiki launcher

Launcher tinggal di `launcher/` dan sengaja kecil: Java polos, AndroidX RecyclerView dan
DynamicAnimation, tanpa Kotlin, tanpa Compose, tanpa jaringan. Sasarannya TV dengan RAM 1 GB. Sakelar baru mengikuti
[`docs/workflows/launcher-switch.md`](docs/workflows/launcher-switch.md), bahasa baru
[`docs/workflows/translation.md`](docs/workflows/translation.md), dan percepatan atau perlambatan diukur dengan
[`docs/workflows/perf-ab.md`](docs/workflows/perf-ab.md). Di setiap perubahan:

- Tanpa blur, tanpa latar video, tanpa polling, tanpa layanan latar.
- Apa pun yang bergantung pada TV tertentu masuk ke `app/src/main/res/values/config.xml`, bukan ke kode.
- Jaga navigasi D-pad tetap pasti. Uji Up/Down/Left/Right dari setiap tepi.
- Verifikasi di TV sungguhan, bukan hanya emulator. Cara termudah adalah suntikan tombol jarak jauh plus
  screenshot: `tools/tv.py --screenshot shots/after.png DPAD_DOWN` menekan tombol, menunggu sedetik
  (`--wait` mengubah itu), menangkap di TV, dan menarik PNG, membuat foldernya kalau belum ada.
  Sertakan screenshot di pull request-mu.
- Tekan lama (tahan OK di Settings atau di ubin) tidak selalu bisa disuntikkan: `input keyevent --longpress`
  tiba sebagai tekan singkat di beberapa firmware (Android TV 8.0 di Philips membuka aplikasinya). Menulis
  ke perangkat input milik remote sendiri berhasil, karena Android lalu mengulang tombol yang ditahan itu sendiri. Cari
  perangkat dan tombol OK dengan `tools/tv.py 'getevent -pl'` (remote yang mencantumkan `KEY_OK` adalah scancode 352),
  lalu tahan sedetik:
  `tools/tv.py 'e=/dev/input/event1; sendevent $e 1 352 1; sendevent $e 0 0 0; sleep 1.2; sendevent $e 1 352 0; sendevent $e 0 0 0'`.
  Ini membutuhkan pengguna shell di grup `input` (`tools/tv.py id`).
  SELinux bisa menolaknya bahkan saat itu: di TCL 55C728 (Google TV 11, enforcing) shell ada di `input`,
  tetapi `sendevent` gagal dengan `Permission denied` dan `logcat` menampilkan
  `avc: denied { write } ... scontext=u:r:shell:s0 tcontext=u:object_r:input_device:s0`, sementara
  `--longpress` juga tiba sebagai tekan singkat. Di perangkat seperti itu, uji tekan lama dengan remote.

### Pemeriksaan navigasi dan daur hidup otomatis

- Pakai peristiwa tombol D-pad di seluruh tes fokus, termasuk masuk dan keluar Reorder.
  Input sentuh yang disuntikkan bisa masuk ke [mode sentuh](https://developer.android.com/develop/ui/views/touch-and-input/input-events#TouchMode) seluruh sistem Android,
  tempat tombol biasa bisa tidak mengambil fokus. Kalau sebuah tes memakai sentuhan, kirim tombol arah
  dan verifikasi ubin yang fokus sebelum lanjut dengan pernyataan D-pad.
- Setelah Home, transisi aplikasi, atau reboot, tunggu dengan batas waktu sampai UI launcher dan
  jendela yang fokus siap sebelum mengirim navigasi atau menangkap hierarki. Tidur tetap atau
  properti selesai-boot saja tidak menetapkan bahwa Home sudah siap. Panduan
  [UI Automator](https://developer.android.com/training/testing/other-components/ui-automator-legacy#wait)
  Android menunjukkan menunggu launcher dan kondisi UI tertentu.
- Untuk pemeriksaan memori sintetis, catat keadaan proses, tingkat trim yang diminta, dan hasil
  perintah. ActivityManager [bisa menolak tingkat trim latar untuk proses latar depan](https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/am/ActivityManagerService.java).
  Untuk menguji Home yang tersembunyi, taruh aplikasi opak di depan dan verifikasi keadaan proses Home;
  pakai tingkat trim yang didukung versi Android sasaran. Anggap perintah yang ditolak sebagai
  kegagalan penyiapan tes. Callback trim yang terkirim menguji perilaku pemulihan, bukan kinerja
  di bawah tekanan memori sungguhan.

Bangun dengan `TV_HOST=<ip> launcher/build-and-install.sh` (JDK 17, Android SDK platform 34 dan
build-tools 34.0.0; di macOS `brew install openjdk@17 android-commandlinetools` lalu
`sdkmanager "platforms;android-34" "build-tools;34.0.0"`; dengan command-line tools yang lebih baru bentuknya
`android sdk install platforms/android-34 build-tools/34.0.0`).

Ide yang cocok dengan arahnya: screensaver diam dengan jam dan warna ambient, paket ikon per TV
untuk aplikasi tanpa banner, layar setelan untuk mengedit dock dari TV, lokalisasi.
Ide yang tidak cocok: baris rekomendasi, umpan konten, iklan, telemetri.

Tema baru masuk ke `themes/<name>/` dengan `config.json`-nya dan `screenshot.jpg` wajib dari Home di
TV sungguhan dengan tema itu; lihat [Menambah tema](themes/README.md#adding-a-theme).

## 3. Perbaiki prompt dan alat

`PROMPT.md` adalah yang kebanyakan orang benar-benar pakai. Kalau asistenmu tersangkut di suatu tempat, perbaikannya
biasanya milik prompt sebagai satu kalimat tambahan, bukan di skrip. Jaga tetap netral merek; taruh
pengetahuan khusus merek di `examples/`.

`tools/tv.py` dan `tools/b64push.py` ada karena `adb` bawaan diblokir di macOS baru-baru ini. Kalau
kamu menemukan jalan lain yang lebih bersih, buka issue dulu.

## 4. Tunjukkan yang kamu bangun

Membangun launchermu sendiri, port ke platform lain, atau add-on yang terlalu besar atau terlalu pribadi
untuk launcher bersama? Taruh di repositorinya sendiri dan tambah satu baris ke
[`SHOWCASE.md`](SHOWCASE.md). Halaman itu tempatnya launcher alternatif, fork dengan
tampilan berbeda, dan alat di sekitar launcher; repo ini menjaga satu launcher acuan yang kecil dan
menunjuk ke yang lain. Aturan untuk satu baris ada di halaman itu.

Yang tetap di repo ini: daftar debloat, perbaikan, dan fitur kecil yang opt-in untuk launcher
acuan, perbaikan prompt dan alat. Fitur yang bisa jadi aplikasinya sendiri yang kecil masuk ke [`addons/`](addons/README.md)
sebagai kartu modul. Yang masuk showcase: launcher kedua,
restyle, port platform, apa pun dengan layanan latar atau akses jaringan sendiri.

## Pull request

- Satu topik per PR. Kecil boleh.
- Sebutkan TV mana yang kamu coba.
- Tidak ada dependensi baru di launcher tanpa membahasnya di issue.
- Bersikap baik di tinjauan. Kebanyakan kontributor di sini mengerjakan ini untuk orang tua mereka.

Dengan berkontribusi kamu setuju karyamu dirilis di bawah lisensi MIT repo.
