# Prompt-nya

Tempel ini ke Claude Code (atau asisten coding lain yang punya akses shell) di komputer yang
berada di Wi-Fi yang sama dengan TV. Ini jalan di perangkat Android TV / Google TV (Xiaomi, TCL, Hisense, Sony,
OnePlus, Realme, Nvidia Shield, dan sebagian besar yang lain). Samsung Tizen, LG webOS, dan
Fire TV berada di luar jangkauan prompt ini. Kalau kamu meng-clone repo ini, sebutkan: asistennya bisa memakai ulang `tools/` dan `launcher/`
daripada menulis dari nol, dan hanya perlu mengedit `launcher/.../values/config.xml`.

Sebelum mulai, di TV: Settings → Device Preferences → About → tekan OK pada "Build" 7 kali,
lalu kembali → Developer options → hidupkan USB debugging (dan Network/ADB debugging kalau TV-mu
menampilkannya). Biarkan TV menyala. Beberapa box memutus daya ke port USB-nya saat USB debugging hidup, jadi
speaker atau drive USB yang tertancap di situ mati sampai kamu mematikan debugging lagi. Adaptor Ethernet
USB di port itu bisa putus dengan cara yang sama, jadi rencanakan seluruh sesi dalam satu duduk.

---

Saya mau debloat Android TV saya dan mengganti launchernya dengan layar utama bersih bergaya Apple TV.
Model TV saya: <MODEL NAME HERE>. Kerjakan di perangkat sungguhan, langkah demi langkah, dan verifikasi semua yang kamu
lakukan dengan screenshot yang ditarik dari TV. Tanya saya sebelum apa pun yang bergantung pada selera saya.

Sambungkan
- Cari TV di jaringan saya (hostname, tabel ARP, atau tanya saya alamat IP-nya) lalu sambungkan lewat ADB di port 5555.
  Kalau TV ada di rumah lain, saya akan memberi alamat Tailscale-nya (docs/REMOTE.md): pakai itu,
  jangan memindai jaringan lokal saya.
- Kalau biner adb bawaan gagal dengan "No route to host" di macOS padahal portnya terbuka, itu
  privasi Local Network macOS yang memblokir adb. Jangan di-debug: pakai paket Python `adb-shell` di dalam venv
  sebagai klien ADB. Kalau dorongan berkasnya menggantung di berkas besar, kirim berkas sebagai potongan base64
  lewat shell dan verifikasi md5-nya.
- Kalau perintahmu sendiri tidak bisa mencapai TV sama sekali sementara TV menjawab dari terminal biasa (sandbox yang
  memblokir jaringan lokal), minta saya menjalankan `tools/relay.py <tv ip>` di terminal dan membiarkannya jalan, lalu
  pakai `TV_HOST=127.0.0.1 TV_PORT=15555` (`tools/relay.md`).
- Kalau port 5555 tetap tertutup, TV mungkin hanya menawarkan Wireless debugging (perangkat Google TV sering begitu): cari
  `_adb-tls-connect._tcp` dengan `dns-sd -B` atau `adb mdns services`, minta saya kode dan port di
  Developer options > Wireless debugging > Pair device with pairing code, lalu `adb pair`, `adb connect`,
  dan `adb tcpip 5555`. Android mematikan Wireless debugging setiap kali Wi-Fi putus.
- Beri tahu saya kapan prompt "Allow USB debugging?" akan muncul di TV supaya saya bisa menerimanya.
- Kalau `adb install` menggantung di Android 14, ia sedang menunggu konfirmasi di layar; dorong APK-nya dan jalankan
  `pm install -r` lewat shell.

Cadangkan dulu
- Simpan daftar paket lengkap, daftar paket yang nonaktif, keluaran getprop, dan semua settings ke folder cadangan
  sebelum mengubah apa pun, dan tulis skrip pulihkan yang mengaktifkan kembali semua yang kamu nonaktifkan.
- Tanya saya apa yang tertancap di port USB TV, dan tanya terpisah apakah jaringan TV lewat
  adaptor Ethernet USB: "tidak ada yang tertancap" bisa benar sementara jaringannya sendiri ada di port
  itu. Beberapa perangkat memutus dayanya saat USB debugging hidup, jadi peringatkan saya, dan ingatkan saya mematikan
  debugging setelah kamu selesai. Di perangkat yang menjangkau jaringan lewat Ethernet USB, cek sebelum kamu
  mengubah apa pun: `dumpsys usb | grep host_connected` (`host_connected=false` berarti adaptornya
  sudah hilang) dan `dumpsys connectivity | grep -o 'Transports: [A-Z]*'` (`WIFI` di TV yang saya anggap berkabel
  berarti box itu jatuh ke cadangan, dan ADB berjalan lewat Wi-Fi). Selesaikan seluruh pekerjaan dalam satu duduk, bukan
  dua: di situ port hanya kembali saat USB debugging mati, dan itu menutup port 5555 juga. Lebih baik
  Wireless debugging kalau TV memilikinya.
- Setelah selesai, tutup ADB jaringan daripada membiarkannya terbuka: matikan USB debugging dan
  Network/ADB debugging di TV, cabut otorisasi USB debugging, reboot, dan cek
  dengan `tools/adb-closed.py <tv ip>` bahwa port menolak koneksi. Kalau jaringan TV datang
  lewat adaptor Ethernet USB, cabut lalu tancapkan lagi adaptor itu setelah debugging mati — portnya kembali
  bersamanya — dan minta saya memastikan TV kembali di Ethernet, bukan Wi-Fi. Jangan percaya `nc` di macOS:
  privasi Local Network bisa memblokirnya juga, dan "No route to host"-nya tidak membuktikan apa-apa; hanya "Connection
  refused" yang membuktikan. Port 5555 berupa teks polos, dan di firmware dengan `ro.adb.secure=0` perangkat mana pun di LAN
  bisa mendapat shell.

Debloat
- Daftar paket yang terpasang dan kelompokkan menjadi: telemetri/analitik pabrikan, konten pabrikan
  dan layanan iklan (PatchWall, baris rekomendasi, penyedia konten), layanan latar yang mungkin
  tidak saya butuhkan (duplikat share/cast, pembersih), aplikasi bawaan, dan aplikasi streaming.
- Nonaktifkan kategori pertama dengan `pm disable-user --user 0`. Jangan pernah uninstall paket sistem.
- Jangan pernah menonaktifkan yang TV butuhkan untuk jalan: aplikasi TV dan layanan input yang memutar HDMI dan
  tuner, menu input/sumber, layanan remote dan pemasangan Bluetooth, keyboard di
  layar, Google Play Store, dan Play Services. Nama paket pabrikan sering tidak menjelaskan apa yang
  paket lakukan: cek activity dan service-nya dengan `dumpsys package` dulu, dan tanya saya kalau masih
  belum jelas.
- Jangan pernah menonaktifkan paket inti milik Android sendiri, atau TV bisa gagal boot lagi: pengontrol izin,
  pemasang paket, ext services dan ext shared, metadata modul (masing-masing sebagai `com.android.*` atau
  `com.google.android.*`), `com.google.android.overlay.modules*`, `com.android.providers.*` (selain kalender,
  kontak, kamus pengguna, dan nomor diblokir), `com.android.systemui`, `com.android.shell`,
  `com.android.phone`, `com.android.settings`, `com.android.tv.settings`, dan `android`. Sebuah Mi TV
  Stick asli tidak pernah boot lagi setelah sebuah daftar mengambil enam yang pertama
  ([#884](https://github.com/Gathin23/cleanyourtv/issues/884)); `tools/disable.sh` menolak daftar seperti itu.
- Sebelum menyentuh aplikasi streaming, asisten suara, Chromecast, Miracast, atau layanan pembaruan, tanya saya
  yang mana yang saya pakai dengan satu pertanyaan centang, dan nonaktifkan hanya yang tidak saya centang.
- Jalankan perintah `pm` satu per panggilan shell; perintah yang dirantai bisa berhenti diam-diam setelah yang pertama.
- Kalau `pm disable-user` is refused for every package, berhenti dan beri tahu saya: beberapa firmware (Fire OS 5 di
  Fire TV Box, misalnya) menolak `pm disable-user` dengan `SecurityException`, `pm uninstall --user 0`
  dengan `DELETE_FAILED_INTERNAL_ERROR`, dan `pm hide` melaporkan sukses sementara tidak mengubah apa pun. Cek
  dengan `pm list packages -d`, bukan kode keluar, dan jangan menghabiskan sesi dengan mengulang — tidak ada
  yang bisa di-debloat di perangkat seperti itu, jadi kita jatuh ke sakelar privasi bawaannya dan pemblokiran DNS
  (`docs/TROUBLESHOOTING.md`).
- Jangan jalankan paket yang namanya menyebut factory, production, AT, atau fac menu (`com.cvte.tv.hkcfac`,
  `com.cvte.tv.fac.menu`, `com.at.factory.*`, `mediatek.factorymenu.ui`, dan sejenisnya) untuk melihat apa yang dilakukannya, bahkan hanya baca:
  di papan Haier/CVTE, membukanya membalikkan bendera mode pabrik yang mematikan tombol Input dan CEC, dan
  membuat boot berikutnya menonaktifkan Play services, Netflix, dan Chromecast — yang kemudian tidak bisa
  diaktifkan lagi dari shell. `dumpsys package <name>` memberitahu apa sebuah paket tanpa menjalankannya, dan
  `docs/TROUBLESHOOTING.md` punya jalan keluar kalau ada yang sudah terbuka.
- Nonaktifkan dalam batch kecil dan ambil screenshot di antaranya: satu batch bisa membawa sesuatu yang menopang beban
  bersamanya dan kamu tidak bisa tahu yang mana.
- Setelah batch terakhir, reboot sekali dan bandingkan `pm list packages -d` dengan yang kamu nonaktifkan: beberapa
  firmware mengaktifkan kembali paket di setiap boot dari daftarnya sendiri (TCL Android 9 melakukan ini untuk user
  center-nya). Catat itu di header daftar daripada menonaktifkannya lagi setelah tiap boot. Kalau
  setupwraith atau launcher bawaan kembali dan Home hitam, cek `logcat | grep
  RecoveryActivity` dan aktifkan kembali launcher bawaan sebelum hal lain
  (`docs/devices/acer-r4g.md`).
- Tawarkan DNS pemblokir iklan sebagai ekstra opsional: di Android 9 dan yang lebih baru, Private DNS disetel ke
  resolver pemblokir iklan (`settings put global private_dns_specifier dns.adguard-dns.com`, lalu
  `settings put global private_dns_mode hostname`). Beri tahu saya ini tidak memblokir iklan YouTube, cek sebuah aplikasi
  masih bisa diputar setelahnya, dan catat pembatalannya (`private_dns_mode opportunistic`) di README.

Launcher
- Cek bahwa TV menerima APK sebelum membangun apa pun. Beberapa perangkat pasar China menjawab setiap
  sideload dengan `INSTALL_FAILED_VERIFICATION_FAILURE` — `adb install`, `pm install`, pemasang yang
  dijalankan tangan, bahkan APK milik pabrikan sendiri semuanya gagal — dan yang lain menolak paket mana pun yang
  menyatakan `android.intent.category.HOME` kecuali membawa tanda tangan pabrikan. Kalau kamu
  kena salah satunya, kerjakan separuh debloat, katakan dengan jelas bahwa separuh launcher tidak mungkin di sini, dan berhenti
  mengulang pemasangan. Header `examples/` yang cocok menyebut kasus mana TV-mu.
- Cek level SDK TV sebelum menjanjikan launcher juga: `tools/tv.py 'getprop
  ro.build.version.sdk'` dibanding `grep minSdk launcher/app/build.gradle` (26 = Android 8.0). Perangkat
  di bawahnya menjawab `INSTALL_FAILED_OLDER_SDK`; menurunkan `minSdk` adalah perubahan sumber yang kemudian
  kamu rawat sendiri, dan APK yang dibangun ulang di tempat lain tidak bisa diverifikasi repo ini. Di perangkat itu tawarkan
  separuh debloat dan katakan launcher tidak bisa dipasang. Tanpa launcher untuk diserahkan, yang
  bawaan tetap: `cmd package` belum ada sebelum Android 7, jadi `tools/disable.sh` menanyakan `dumpsys
  activity` untuk Home di situ dan memperingatkan saat keduanya tidak menjawab — peringatan itu berarti tidak ada yang dicek,
  dan perangkat tanpa pemulihan tidak punya jalan kembali (#795).
- Jangan memasang launcher pihak ketiga yang memungut biaya untuk hal dasar seperti wallpaper. Bangun launcher Android TV
  asli yang kecil (Java, AndroidX RecyclerView, minSdk yang cocok dengan TV, build debug boleh),
  pasang lewat ADB, jadikan home default, dan nonaktifkan launcher bawaan. Kalau beberapa aplikasi mengklaim
  intent HOME, daftarkan dengan `cmd package query-activities -a android.intent.action.MAIN -c
  android.intent.category.HOME` dan setel default dengan `cmd package set-home-activity`. Dua
  perintah itu bisa sama-sama menyebut launchermu sementara Home masih membuka yang lain: di beberapa build Google TV
  `set-home-activity` mencetak `Success` dan tidak mengubah apa pun, dan `.RecoveryActivity` milik setupwraith
  tetap memegang tombol. Konfirmasi dengan `dumpsys activity activities | grep mFocusedApp` sebelum memberitahu saya
  pengambilalihan berhasil.
- Sebelum menonaktifkan launcher bawaan, cek aplikasi lain mana yang mengklaim HOME: di Google TV wizard penyiapan
  (`com.google.android.tungsten.setupwraith`) mengambil alih bahkan setelah launcher bawaan dinonaktifkan, dan
  header `examples/` yang cocok menyebut apa yang harus dilakukan. Sebelum menonaktifkan setupwraith, jalankan
  `dumpsys wifi | grep -o 'cname=[^ ]*'`: jaringan Wi-Fi yang disimpan saat penyiapan pertama milik wizard itu, dan
  Android menghapus jaringan itu begitu wizard dinonaktifkan, yang membuat TV lepas dari Wi-Fi dan ADB. Kalau begitu,
  minta saya melupakan jaringan itu dan bergabung lagi di Settings lebih dulu.
- Di papan CVTE (paket `com.cvte.*` mana pun), biarkan launcher bawaannya (`com.cvte.tv.launcher.*`) tetap aktif
  kecuali folder `examples/` untuk papan yang persis itu menonaktifkannya: jadikan launchermu Home dengan
  `set-home-activity`, reboot sekali, dan pastikan Home serta ADB kembali sebelum hal lain. Jangan menghidupkan
  helper tombol remote di langkah yang sama. Sebuah JVC LT-43N7115 (`TP.SK706S.PC822`, Android 11) menggantung di animasi
  boot dengan ADB hilang setelah keduanya dilakukan sebelum satu reboot (#741).
- Di Google TV, stock launcher also answers the remote's app buttons (Netflix, YouTube, dan sejenisnya),
  plus tombol Input dan Settings/dashboard, jadi setelah menonaktifkannya minta saya menekan masing-masing, dan petakan ulang yang
  tidak melakukan apa-apa dengan layanan tombol launcher: `input_key_keycodes` dan `app_keys` dengan
  INPUT_KEY_SERVICE=1, `settings_key_keycodes` dengan HOME_KEY_SERVICE=1 (`config.xml` atau tema).
  Menghidupkan salah satu helper bisa lebih mahal daripada tombol yang dipetakannya: on a TCL G09 build (Google TV 12) ask me to hold volume up before dan sesudahnya, karena layanan pemfilter tombol yang terikat di situ membuang pengulangan tombol —
  volume yang naik pelan menjadi satu langkah per tekan, satu helper atau keduanya, all or nothing — jadi say which one I am giving up, jangan dihidupkan diam-diam (`docs/devices/tcl-55p71b-g09-key-repeat.md` punya bisect dan
  tiga jalan keluar). On other sets this was not measured; kalau menahan tombol berhenti mengulang setelah
  helper hidup, itu kompromi yang sama, bukan remote yang rusak.
  Tombol yang TV catat sebagai `KEY_UNKNOWN` atau `KEY_PROGRAM` tidak bisa dicocokkan lewat keycode, karena setiap
  tombol yang belum dipetakan berbagi kode itu: baca scancode dengan `getevent -lt` dan pakai `settings_key_scancodes`,
  which only `config.xml` carries (tema bisa mengisi keycode, bukan scancode).
- Di Android 11 dan yang lebih baru launcher harus menyatakan `<queries>` untuk `android.intent.category.LEANBACK_LAUNCHER`,
  atau visibilitas paket diam-diam menyembunyikan setiap aplikasi dan kisi tampil kosong; targetkan SDK yang baru, beberapa
  perangkat Google TV 14 menolak target yang lebih lama saat pemasangan.
- Tanya berapa slot dock yang saya mau (4, 5, 6, atau 7), aplikasi mana, dan urutannya; tetap empat kalau saya tidak
  punya pilihan. Launcher acuan sudah mendukung ini: pakai `columns` dan `dock_apps` di
  `launcher/app/src/main/res/values/config.xml` atau `config.json` sebuah tema, tanpa menambah tata letak baru.
- Desain, gaya Apple TV 4K: latar hitam pekat; satu baris dock di bawah layar di dalam
  pita membulat yang sedikit lebih terang berisi aplikasi yang saya pilih; aplikasi sisanya dalam kisi dengan jumlah
  kolom yang sama di bawahnya yang menggulir naik saat saya menekan Down; satu sorotan "kaca" tembus
  yang meloncat dari ubin ke ubin, bukan garis tepi per ubin; ubin yang fokus membesar sedikit dan
  menampilkan namanya di bawah; di atas dock, nama aplikasi dock yang fokus dalam huruf tipis besar dengan
  cahaya ambient berwarna yang diambil dari bannernya; tombol pil buram di kanan atas untuk Input, Wi-Fi
  (menampilkan nama jaringan), dan Settings; sapaan, jam tipis besar, dan tanggal di kiri atas dengan
  huruf berlisensi terbuka yang diunduh seperti Outfit. Input membuka dropdown kaca yang ringkas di bawah pil,
  mendaftar port HDMI, AV, dan Live TV, berpindah lewat kerangka input TV.
- Buat navigasi D-pad pasti: Up/Down bergerak tepat satu baris, Up dari dock pergi ke bilah
  atas, ujung bilah atas tidak jatuh ke ubin. Ingat ubin yang terakhir fokus.
- Tetap ringan: TV punya RAM sekitar 1 GB. Tanpa blur, tanpa video, tanpa polling jaringan.

Verifikasi
- Setelah tiap perubahan, suntikkan tekanan tombol remote lewat ADB, ambil screenshot dari TV, dan lihat:
  `tools/tv.py --screenshot shots/01-after.png DPAD_RIGHT` menekan tombol, lalu menangkap di TV dan
  menarik PNG-nya (kosongkan tombolnya untuk menangkap apa yang sudah ada di layar).
  Perbaiki yang kamu lihat sebelum lanjut.
- Live TV, HDMI, dan beberapa aplikasi streaming (Netflix) memberi screenshot kosong (0 byte); itu TV-nya, bukan
  debloat yang rusak, dan `--screenshot` keluar 1 dengan pesan itu. Cek
  `dumpsys activity activities | grep topResumedActivity` sebagai gantinya (lihat `docs/TROUBLESHOOTING.md`).
- Kalau layar terlihat membesar dan terpotong setelah pemasangan atau reboot, cek penimpaan tampilan
  sebelum mengedit kisi launcher: `wm size` dan `wm density` menunjukkan nilai fisik di samping
  penimpaan, dan penimpaan sisa percobaan resolusi lama, atau kerapatan yang kembali
  salah di beberapa firmware Sony MediaTek, is not a launcher bug. `settings get secure display_density_forced` yang kosong
  adalah default yang normal, bukan kerusakan — baca ketiga
  nilai dulu dan kembalikan kalau kamu membatalkan (`docs/TROUBLESHOOTING.md`).
- Kalau tombol remote bergantung pada helper aksesibilitas, cek bahwa layanannya aktif dan terikat
  untuk pengguna saat ini, lalu minta saya menekan tombol sungguhan; tombol yang disuntikkan bukan bukti (lihat
  `docs/TROUBLESHOOTING.md`).
- Setelah selesai, beri saya skrip pulihkan, skrip bangun-dan-pasang, dan README yang menjelaskan cara
  mengaktifkan kembali aplikasi mana pun dan cara mengubah urutan dock atau wallpaper.

---

Bersiaplah menjawab tiga atau empat pertanyaan centang (aplikasi mana yang kamu pakai, ukuran dock dan urutan aplikasi,
apakah asisten suara, Chromecast, dan pembaruan firmware dipertahankan). Semuanya bisa dibalik.
