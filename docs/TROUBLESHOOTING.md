# Pemecahan masalah

Hal-hal yang sempat bikin kami atau kontributor kerepotan, biar kamu tidak kena juga. Perintah memakai `tools/tv.py`; cara menyiapkannya ada di
[README](../README.md#doing-it-by-hand).

## Connecting

- **macOS: `adb connect` bilang "No route to host"** padahal portnya terbuka. macOS baru memperlakukan
  `adb` bertanda tangan Google sebagai aplikasi sendiri di privasi Local Network dan memblokirnya diam-diam.
  `tools/tv.py` sama sekali tidak memakai biner itu.
- **Tidak ada sakelar "wireless debugging"** di sebagian TV, termasuk Xiaomi. USB debugging saja sudah membuka
  ADB jaringan di port 5555.
- **USB debugging abu-abu dan entri Wireless debugging juga tidak ada**, terlihat di kotak operator
  ([Airtel Xstream, Android 11](https://github.com/Gathin23/cleanyourtv/issues/227)). Sebagian firmware ISP
  menghapus kedua sakelar, dan kemudian tidak ada shell yang bisa dijangkau: `tools/tv.py` gagal,
  `adb connect <ip>:5555` habis waktu, begitu juga pemeriksaan port. Timeout itu firmware, bukan
  Wi-Fi, router, atau kabel, jadi jangan membangun ulang jaringan untuk ini. Abu-abu, bukan
  hilang, menunjuk ke kebijakan pemilik perangkat atau kunci provisioning; sakelar kembali setelah
  pembaruan firmware dari ISP, atau tidak sama sekali. Tanpa salah satu dari dua sakelar itu, repo ini tidak punya lawan bicara.
- **Opsi pengembang tersembunyi, atau Build number minta kata sandi**, terlihat di kotak ISP (FPT Play
  650, Android TV 14): mengetuk Build tujuh kali membuka permintaan kata sandi, bukan membuka menu.
  Sering yang terkunci hanya jalan masuknya, bukan layarnya: TvSettings tetap punya
  `com.android.tv.settings/.system.development.DevelopmentActivity`, dan aplikasi mana pun bisa membukanya lewat
  namanya atau aksi `android.settings.APPLICATION_DEVELOPMENT_SETTINGS`. Pasang aplikasi yang
  membuka aktivitas menurut nama (Activity Launcher) dari Play Store, buka layar itu, dan nyalakan
  USB debugging di sana. Kalau tidak ada aplikasi yang bisa membukanya, lihat bullet kotak operator di atas.
- **USB debugging never opens port 5555** di sebagian set Google TV (salah satunya Philips rakitan Skyworth).
  Pasangkan sekali lewat Wireless debugging dengan `adb` bawaan (`adb pair <ip>:<port> <code>`,
  lalu `adb connect`), jalankan `adb tcpip 5555`, dan `tools/tv.py` jalan sejak itu. `dns-sd -B
  _adb-tls-pairing._tcp` (atau `adb mdns services`) menampilkan port pairing selagi kodenya di layar.
  Android mematikan Wireless debugging setiap Wi-Fi putus; nyalakan lagi dan `adb` tersambung kembali
  tanpa kode baru. Port 5555 dari `adb tcpip` biasanya hanya bertahan sampai reboot berikutnya. Saat menutup,
  switch Wireless debugging off on its own: di Chromecast with Google TV 4K (`sabrina`)
  mematikan USB debugging meninggalkan port Wireless debugging yang masih menerima koneksi, dan
  `tools/adb-closed.py` hanya memeriksa 5555. `adb mdns services` mencantumkan port itu selagi terbuka.
- **Perangkat USB mati selagi USB debugging nyala**, terlihat di Xiaomi Mi Box S dan Xiaomi TV
  Box S generasi ke-3 ([MiTV-AFMU0, Google TV 14](https://github.com/Gathin23/cleanyourtv/issues/737)):
  firmwarenya mengalihkan port USB ke mode perangkat ADB, yang memutus daya ke speaker atau
  drive USB. **Adaptor USB Ethernet duduk di port yang sama**, jadi jaringan ikut hilang dan kotak
  jatuh ke Wi-Fi tanpa bilang. `dumpsys usb | grep host_connected` mencetak `false`;
  `dumpsys connectivity | grep -o 'Transports: [A-Z]*'` mencetak `WIFI` di set yang pemiliknya sebut berkabel.
  `eth0` telanjang yang macet di `NO-CARRIER` adalah MAC tanpa soket milik papan ini, bukan adaptornya — di
  TCL 65C735 adaptornya muncul sebagai `eth1`. Berapa lama port bertahan saat debugging berbeda per firmware:
  dua kotak Xiaomi kehilangannya begitu debugging nyala, sedangkan 65C735 mempertahankan adaptor sampai
  reboot dengan debugging masih nyala menaruh port di mode gadget (`sys.usb.config=adb`, `Mstar-udc`) dan
  tidak pernah kembali ([header daftar itu](../examples/tcl-65c735/debloat.txt)). Bagaimanapun tidak ada
  jalan kembali dari shell di set tanpa Wireless debugging, dan mematikan USB debugging
  menghentikan `adbd` juga, jadi rencanakan satu duduk, bukan dua: matikan debugging di akhir, cabut-pasang
  adaptor, dan pastikan Ethernet sebelum mengembalikan TV. Launcher tidak pernah butuh debugging.
  Chromecast with Google TV 4K (`sabrina`, `UTTC.250917.004`) mempertahankan adaptor USB Ethernet-nya dengan
  USB debugging nyala: `eth0` tetap tercantum, dan Ethernet tersambung selagi debugging masih nyala. Ia mencetak
  `host_connected=false` selagi adaptor ada sebagai `eth0`, jadi di sana `host_connected=false` bukan
  bukti adaptornya hilang ([daftarnya](../examples/google-chromecast-gtv-4k/debloat.txt)).
- **Tutup port 5555 kalau sudah selesai**: teks polos, dan shell untuk siapa pun di LAN kalau `ro.adb.secure=0`. Langkahnya di FAQ README.
- **TV duduk di VLAN atau jaringan IoT terpisah** dan `tools/tv.py` habis waktu padahal debugging
  nyala: router menahan komputer dari menjangkaunya. Tambah aturan firewall sementara dari
  komputermu ke TV hanya pada tcp/5555, dan hapus lagi saat menutup ADB. Membuka seluruh
  segmen, atau memindahkan TV ke jaringan utama untuk seterusnya, membatalkan alasan pemisahannya.
- **Windows PowerShell 5.1 menambahkan byte-order mark** ke teks yang dipipa ke program, yang mengubah
  perintah batch pertama jadi acak. `tv.py` sekarang mengupasnya, dan skrip `.ps1` memipa UTF-8 bersih.
- **"Allow USB debugging?" muncul lagi** padahal `adb devices` sudah jalan: `tv.py` memakai
  kunci sendiri di samping yang sudah didaftarkan platform-tools. Sekarang ia memakai ulang `~/.android/adbkey` kalau
  ada. Setiap percobaan dengan kunci yang belum diterima mengantre satu prompt lagi di TV, jadi tutup semuanya
  (Back) sebelum menguji dengan tekanan tombol.
- **Git Bash menulis ulang path** seperti `/data/local/tmp` jadi path Windows. Skrip menyetel
  `MSYS_NO_PATHCONV=1` untukmu. Setel `JAVA_HOME` dan `ANDROID_HOME` sebelum membangun di sana.

## Working on the TV

- **Perintah `pm` yang dirantai** (`pm disable a; pm disable b`) bisa berhenti diam-diam setelah yang pertama. Jalankan
  satu per panggilan shell; `--batch` melakukan persis itu.
- **Dorongan besar menggantung** lewat klien Python. Pakai `tools/b64push.py` untuk APK.
- **`tools/tv.py reboot` never comes back.** `reboot` adalah perintah shell, dan adbd berhenti sebelum
  mengirim `CLSE` terakhir shell, jadi klien menunggu balasan yang tidak akan datang: dua menit,
  lalu galat, di TCL 65C935 (`G05_4K_GB`, Google TV 11, AR2101,
  [#995](https://github.com/Gathin23/cleanyourtv/issues/995)). Rantai yang reboot lalu memeriksa
  sesuatu macet di situ. Pakai `tools/tv.py --reboot`: ia mengirim reboot lewat layanan ADB sendiri
  dan akan return once the reboot request is accepted. TV masih menyala ulang pada saat itu, jadi
  wait for the TV to restart dan biarkan ia menjawab lagi (`tools/tv.py --probe`) sebelum perintah berikutnya.
- **`logcat` tanpa baris yang kamu harapkan bukan bukti tidak ada yang terjadi.** Di TCL 55C728
  (`G05_4K_GB`, Google TV 11) buffer utama 1 MiB (`persist.logd.size.main`), dan log HDMI, ARC, serta media milik TV
  mengisinya jauh di bawah semenit; sekitar nyala ia menampung sekitar 30
  detik. Peristiwa bangun atau boot yang dibaca semenit kemudian sudah hilang. Perbesar buffer untuk
  sesi dengan `tools/tv.py 'logcat -G 16M'` (`logcat -g` menampilkan ukurannya; reboot mengembalikan
  ukuran firmware), atau biarkan `logcat` berjalan dari sebelum peristiwanya. Untuk suara, `dumpsys audio` menyimpan
  riwayat peristiwa pemutar yang lebih awet daripada log.
- **Every way of disabling a package is refused**, bukan cuma satu cara. Di Fire TV Box 2015 (`sloane`,
  Fire OS 5 / Android 5.1) `pm disable-user --user 0` mengembalikan `SecurityException`,
  `pm uninstall --user 0` mengembalikan `DELETE_FAILED_INTERNAL_ERROR`, dan `pm hide` melaporkan berhasil
  sementara `pm list packages -d` tetap kosong ([#577](https://github.com/Gathin23/cleanyourtv/issues/577)).
  Baca `pm list packages -d` setelah setiap percobaan, bukan kode keluar — `pm hide` adalah yang
  lies. Kalau ketiganya menolak, firmware ini does not take a debloat list at all: tidak ada jalan
  shell mengitarinya, baris bersponsor tetap di layar utama, dan pekerjaan berguna yang tersisa adalah
  sakelar privasi milik TV plus pemblokiran DNS (Pi-hole dengan daftar iklan smart-TV; laporan memakai
  daftar `AmazonFireTV` milik Perflyst). Fire OS 6 adalah kasus sebaliknya: `pm disable-user` jalan dan sekitar
  sepuluh paket Amazon dilindungi, yang
  [`examples/amazon-fire-tv-stick-4k-2018/debloat.txt`](../examples/amazon-fire-tv-stick-4k-2018/debloat.txt)
  cantumkan, dengan cadangan DNS yang sama untuk endpoint mereka.
- **The Input key and CEC die after the hidden factory menu is opened**, terlihat di set Haier/CVTE
  (Durabrand `DURAT32ISDB`, `dupont`, Android TV 11 build `RTM6.230109.232`,
  [#470](https://github.com/Gathin23/cleanyourtv/issues/470)). `com.cvte.tv.hkcfac` adalah
  aplikasi produksi pabrikan: `dumpsys package` menampilkannya sebagai komponen pabrik, dan ia punya
  aktivitas yang bisa diluncurkan, jadi "lihat dulu paket ini apa" sudah cukup untuk memicunya. Membukanya
  menyetel `customer_factory_mode=true` di `/vendor/tvcertificate/fdata.ini` dan
  `vendor.mtk.factory.disable.input=1`. Tombol Input lalu tidak melakukan apa-apa dan HDMI-CEC mati, dan
  pada boot berikutnya `com.at.factory.factorymanager` menonaktifkan Play services, Netflix,
  `com.google.android.katniss` dan `com.google.android.apps.mediashell` — `pm enable` dari shell
  ditolak (refused) untuk itu, jadi casting dan toko tetap hilang sampai modenya dibersihkan. Baca
  paketnya, jangan memulainya. Kalau sudah terlanjur dibuka, mulai, kirim keycode keluar pabrik
  dua kali **dengan aplikasi itu in front**, reboot, lalu nyalakan CEC lagi:
  ```sh
  tools/tv.py 'am start -n com.cvte.tv.hkcfac/.MainActivity'
  tools/tv.py 'input keyevent 218; input keyevent 218'
  tools/tv.py --reboot             # returns once ADB accepts it; wait for the TV to come back
  tools/tv.py 'settings put global hdmi_control_enabled 1'
  ```
  Tekan tombol Input sungguhan sesudahnya dan cek paketnya kembali:
  `tools/tv.py 'pm list packages -e | grep -E "gms|netflix|mediashell|katniss"'`. Lebih lanjut tentang unitnya di
  [catatan perangkat](devices/durabrand-durat32isdb.md).
- **Stremio kelihatan seperti servernya mati setelah debloat**, terlihat di Xiaomi MiTV-MZTU0 (`river`,
  Google TV 14, [daftar](../examples/xiaomi-tv-a-pro-2026/debloat.txt)). Aplikasinya terbuka, add-on
  termuat, dan tidak ada yang pernah diputar, yang terbaca seperti gangguan server lokal. Bukan itu: dengan
  `com.google.android.apps.mediashell` nonaktif, `stremio_server_process` milik Stremio tetap
  berjalan, tetap mendengarkan di 11470/12470, tetap memegang koneksi ESTABLISHED ke `api.strem.io`,
  dan `logcat -b crash` bersih. Build Android TV Stremio memutar lewat mediashell, jadi server
  sehat dan jalur putarnya yang hilang. `pm enable --user 0 com.google.android.apps.mediashell`
  memperbaikinya di set itu tanpa reboot dan tanpa pasang ulang. Cek ini sebelum menyalahkan add-on atau
  jaringan:
  ```sh
  tools/tv.py 'pm list packages -d | grep mediashell'     # output here = this is the cause
  tools/tv.py 'pm enable --user 0 com.google.android.apps.mediashell'
  ```
  Jangan masukkan paket itu ke daftarmu kalau kamu memakai Stremio di TV, atau cast dari HP.
- **Picking HDMI 1 in the Inputs menu opened an unrelated app**, terlihat di RCA Android TV 9
  (firmware Toptech/Zeasn `RTK2851P`, Realtek RTD2851P,
  [#805](https://github.com/Gathin23/cleanyourtv/issues/805)). Launcher beralih ke input passthrough
  dengan `ACTION_VIEW` yang implicit pada URI kanal input
  (`vnd.android.cursor.item/channel`), dan hanya mengirim `input_action` theme kalau tidak ada yang mengklaim
  intent itu sama sekali. "Tidak ada yang mengklaim" adalah jebakannya: firmware itu tidak punya aplikasi Live TV untuk URI itu,
  dan ES File Explorer (`com.estrongs.android.pop/.app.transferstation.FileTransferStationActivity`)
  mendeklarasikan filter `VIEW` untuk tipe MIME yang sama, jadi HDMI 1 membuka pengelola berkas dan
  `input_action` tidak pernah tercapai. Set tanpa penangan setidaknya menampilkan "Not available on this TV";
  set dengan satu penangan yang salah membuka aplikasi itu. Tanya aplikasi mana yang akan mengambil baris sebelum menyalahkan
  launcher:
  ```sh
  tools/tv.py 'cmd package query-activities --brief -a android.intent.action.VIEW -t vnd.android.cursor.item/channel'
  ```
  `live_tv_package` tidak menolong: ia diterapkan sebagai `setPackage` hanya pada Live TV row, dan baris
  HDMI serta AV tidak membawa paket. Sampai launcher memeriksa bahwa penangannya adalah aplikasi input TV,
  sembunyikan barisnya: theme dengan `source_panel_service` terisi dan `live_tv_package` empty membuat
  pil Input membuka source panel milik TV (yang dilakukan pelapor), atau nonaktifkan aplikasi yang
  menjawab. Laporkan yang lain, jangan membacanya sebagai kebiasaan firmware: kasus penangan yang salah needs
  a source change, not a setting.
- **`adb exec-out screencap -p` menghasilkan PNG rusak** di sebagian set MediaTek (Xiaomi, dan smart TV Fire TV
  milik Amazon): binernya mencetak `Init wrapper sys mutex successful` di stdout lebih dulu.
  Simpan di TV lalu tarik berkasnya: `screencap -p /data/local/tmp/s.png`, atau potong
  segala sesuatu sebelum header `\x89PNG`. `tools/tv.py --screenshot shots/home.png` melakukan simpan,
  tarik, dan bersihkan dalam satu perintah, dan mengatakannya kalau tangkapannya 0 byte.
- **Play Protect memblokir launcher** ("unsafe app blocked, built for an older version of Android")
  dan `pm install` menggantung, lalu gagal dengan `INSTALL_FAILED_VERIFICATION_FAILURE`, saat menyasar
  SDK 29. Sekarang menyasar 34; kalau kamu membangun checkout lama, bangun ulang.
- **`pm install` gagal dengan `INSTALL_FAILED_OLDER_SDK`** di set yang lebih tua dari launcher: `minSdk`
  APK adalah lantai waktu kompilasi, jadi TV di Android 7.0/7.1 (SDK 24/25) tidak bisa menerima
  build yang diterbitkan sama sekali ([#576](https://github.com/Gathin23/cleanyourtv/issues/576)). Baca kedua angka sebelum
  mengubah apa pun: `tools/tv.py 'getprop ro.build.version.sdk'` dan
  `grep minSdk launcher/app/build.gradle` (26, yaitu Android 8.0 / API 26, saat tulisan ini).
  `launcher/build-and-install.sh` sekarang menanyakan TV dulu dan berhenti dengan kedua angka, jadi tidak ada yang
  dibangun atau didorong untuk set yang akan menolaknya; `build-and-install.ps1` hanya melaporkannya setelah
  `pm install`, yang gagal dengan aman dan launcher bawaan tidak tersentuh. Menurunkan `minSdk` adalah perubahan
  sumber, bukan setelan, dan artinya merawat salinanmu sendiri: launcher memakai AndroidX dan
  panggilan framework yang lebih baru dari API 25, jadi harapkan galat kompilasi atau launcher yang mulai lalu
  jatuh. APK yang dihasilkan dengan membangun ulang proyek di tempat lain (asisten, pembangun daring, zip
  "rebuilt for API 25" dari forum) bukan sesuatu yang bisa diperiksa repositori ini, dan skrip pasang
  hanya mendorong APK yang mereka kompilasi sendiri: build yang ditandatangani kunci lain tidak
  terpasang di atas yang ini (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`), jadi copot yang lama dulu kalau kamu
  menempuh jalan itu, dan biarkan launcher bawaan aktif sampai yang baru benar-benar mulai. Di set di bawah
  `minSdk`, setengah debloat dan privasi tetap berlaku, dari layar utama milik TV: yang tidak bisa dipasang
  adalah launchernya, bukan daftarnya. Kalau baris `Failure [...]` menyebut
  `INSTALL_FAILED_VERIFICATION_FAILURE`, itu bullet di atas — pemverifikasi pemasang firmware,
  bukan level API. `build-and-install.sh` adalah skrip yang berhenti sebelum mendorong.
- **Screenshot keluar kosong** selagi Live TV, input HDMI, atau Netflix di layar, terlihat di TCL
  dan [Sony KDL-49W800G](https://github.com/Gathin23/cleanyourtv/issues/92). Tekan Home sebelum
  `screencap`. Tangkapan kosong tidak mengatakan apa-apa tentang koneksi ADB atau pemutaran; cek itu
  terpisah.
- **Screenshot keluar identik, bukan kosong**, terlihat di Amazon Fire TV Stick 4K generasi ke-2
  (AFTKRT, karat, Fire OS 8) tepat setelah reboot: `screencap -p` terus mengembalikan PNG non-kosong
  yang sama byte per byte (MD5 sama) di aktivitas yang dilanjutkan yang sama sekali berbeda, berjarak menit. Ia
  kelihatan seperti tangkapan (hitam) sungguhan dan menghabiskan waktu debug sungguhan sebelum membandingkan `md5` dua tarikan
  membongkarnya. `dumpsys SurfaceFlinger --display-id` menampilkan `no identification data` untuk Display 0
  saat itu. Jangan percaya tarikan `screencap` sendirian di sini: bandingkan dengan tarikan lain, atau
  cek `dumpsys activity activities | grep mResumedActivity` dan peristiwa tombol sungguhan
  (`adb shell getevent -lt`).
- **The whole UI is huge and clipped, and it is not the launcher**, terlihat di Sony K-85XR70
  (`BRAVIA 4K AE2`, Android 14, firmware Tiongkok,
  [#590](https://github.com/Gathin23/cleanyourtv/issues/590)). UI-nya berjalan di 1920×1080 pada panel 4K
  memang begitu ([#29](https://github.com/Gathin23/cleanyourtv/issues/29)), dan dua boot yang terkonfirmasi
  muncul dengan kerapatan override 640 di mana 320 yang bekerja: semuanya berukuran ganda, ubin terpotong, dan
  kelihatan seperti bug kisi. Baca override sebelum menyentuh tata letak:
  ```sh
  tools/tv.py 'wm size'          # Physical size vs Override size
  tools/tv.py 'wm density'       # Physical density vs Override density
  tools/tv.py 'settings --user 0 get secure display_density_forced'
  ```
  `Override size` yang tertinggal dari percobaan resolusi lama adalah hal pertama yang perlu dijelaskan, dan
  empty saved density is a normal Android default, bukan kerusakan. `wm density reset` memperbaiki
  sesi; di firmware ini ia kembali sebagai 640 pada boot berikutnya, dan `wm density 320` saja tidak
  menempel karena meninggalkan setelan tersimpan kosong. Taruh `settings --user 0 put secure
  display_density_forced 320` sesudahnya, reboot, dan baca ketiga baris kembali sebelum mempercayainya.
  Jangan salin angkanya ke TV lain: catat ketiga nilai dulu, dan urungkan dengan `wm density reset`
  plus `settings delete secure display_density_forced`. Catatan perangkat:
  [Sony K-85XR70](devices/sony-k85xr70-ae2.md).
- **Nonaktifkan launcher bawaan hanya setelah yang baru terpasang**, atau tombol Home mendarat di
  aplikasi uji pabrik. Luncurkan penggantinya dengan tangan dulu. `set-home-activity` bisa melaporkan
  `Success` sementara HOME masih mengarah ke launcher bawaan (prioritas 2) sampai yang itu dinonaktifkan.
  Dan cek paket mana yang benar-benar menangani HOME sebelum menonaktifkan apa pun menurut nama: di Sony
  di atas, `com.google.android.leanbacklauncher` punya penerima boot tetapi tidak punya aktivitas HOME.
  `tools/disable.sh` sekarang melakukan pemeriksaan itu sendiri: kalau daftar memegang aplikasi yang memiliki Home dan
  tidak ada yang lain di TV yang menjawab HOME, ia melewatkan entri itu dan mengatakannya (`FORCE_HOME=1` menimpa),
  jadi menerapkan daftar sebelum serah terima tidak lagi meninggalkan TV tanpa launcher. Terapkan daftar
  lagi setelah skrip pasang menjadikan launcher baru sebagai Home, untuk menonaktifkan yang lama. Aplikasi uji
  pabrik tidak dihitung sebagai yang lain: di Xiaomi Mi TV 4A
  `com.duokan.factorytest/.FactoryLauncher` menjawab HOME, jadi penjaga mengeluarkannya, bersama paket
  bernama `*factorytest*`, `*factory_test*`, atau `*factorymode*`.
- **TV tidak lagi boot setelah daftar yang menonaktifkan paket inti Android** (Mi TV
  Stick asli, [#884](https://github.com/Gathin23/cleanyourtv/issues/884)). Daftar seorang agen mengambil
  `com.google.android.permissioncontroller`, `com.google.android.packageinstaller`,
  `com.google.android.ext.services`, `com.google.android.ext.shared`,
  `com.google.android.modulemetadata` dan overlay `com.google.android.overlay.modules.*`, plus
  launcher bawaan dan setupwraith, dan stick tidak pernah kembali. Itu bagian dari Android
  sendiri, bukan bloat. `tools/disable.sh`, `disable.ps1`, dan `check.py --fix` sekarang menolak daftar yang
  menyebutnya (atau `com.android.providers.*` selain kalender, kontak, kamus pengguna, dan
  nomor diblokir, `com.android.systemui`, `com.android.shell`, `com.android.phone`,
  `com.android.settings`, `com.android.tv.settings`, atau `android`), tanpa sakelar untuk menimpanya.
  Kalau sebuah set sudah macet dan masih menjawab ADB, `pm enable` masing-masing paket itu lalu reboot;
  kalau ADB juga hilang, reset pabrik dari menu pemulihan biasanya satu-satunya jalan kembali.
- **The TV stays on the boot logo after a list, and disabling the only Home app is enough to do
  it**, terlihat di set era KitKat ([COOP COTV-0001](https://github.com/Gathin23/cleanyourtv/issues/795):
  Realtek `rtd298x`, Android 4.4.2, SDK 19). 17 paket dinonaktifkan lewat ADB Ethernet, masing-masing
  menjawab `new state: disabled-user` dan `pm list packages -d` membaca kembali ketujuh belas; launcher
  pabrik `com.haiersoft.launcherproject2995hk` ada di antaranya tanpa pengganti, karena
  launcher di sini butuh Android 8.0. Setelah siklus daya, set duduk di robot hijau dan
  `INICIALIZANDO` selama 20+ menit, melewati pemutusan daya keras dan mati/nyala lain dari remote.
  Pelapor lalu mengisolasinya: menonaktifkan paket yang satu itu reproduces the hang dan mengaktifkannya lagi mem-boot
  set, jadi ukuran batch dan pemutusan daya bukan penyebabnya. Dua hal yang perlu dibawa pulang. Pertama,
  layar yang macet tidak berarti setnya hilang: yang ini masih answered ADB on Ethernet selagi
  bertuliskan `INICIALIZANDO`, dan `pm enable` pada setiap paket dari cadangan sebelum perubahan plus reboot
  membawanya kembali (langkah pemulihannya ada di [catatan perangkat](devices/coop-cotv-0001.md)). Kedua, paket
  vendor yang kelihatan tidak berhubungan bisa jadi tempat launcher berjalan — di sini menonaktifkan `com.tsb.tv`
  stopped the launcher dengan "Se ha detenido la aplicación lancherProject", bukan merusak
  boot — makanya daftar dilanjutkan dalam batches dengan reboot dan melihat layar di antaranya,
  terutama di set yang tidak bisa menerima launcher: biarkan launcher bawaan, dan biarkan ADB di
  Ethernet (KitKat tidak punya sakelar debugging jaringan, dan Wi-Fi menutup 5555 di yang ini).
  `tools/disable.sh` melewatkan pemilik Home kalau bisa menamainya; sejak
  [#795](https://github.com/Gathin23/cleanyourtv/issues/795) ia menanyakan `dumpsys activity` kalau `cmd
  package` tidak ada (Android 6 dan yang lebih tua, di mana perintah itu tidak ada) dan memperingatkan kalau keduanya
  tidak menjawab. Peringatan itu berarti penjaga tidak berjalan, bukan bahwa daftarnya aman. Tanpa ADB dan
  tanpa citra pemulihan vendor, set yang macet seperti ini still out of reach.
- **Xiaomi pasar Tiongkok menolak Home bawaan meskipun `set-home-activity` melaporkan berhasil**,
  diverifikasi di MiTV-AETP0 (dofus), Android 9, firmware 22.9.2.851: framework menyaring kandidat
  HOME pihak ketiga dengan daftar izinkan nama paket. Menonaktifkan launcher bawaan lalu memilih
  `FallbackHome`. Build ID aplikasi yang opt-in berhasil dengan launcher bawaan masih aktif;
  lihat [catatan perangkat dan langkah mundur](devices/xiaomi-mitv-aetp0-dofus.md). Ini berbeda
  dari pemilih home bawaan yang hilang atau remote yang memancarkan ALL_APPS.
- **The new launcher still doesn't own Home** setelah yang bawaan dinonaktifkan, terlihat di Google TV
  (Xiaomi MiTV-MOOR2). Aplikasi sistem lain menjawab HOME: `com.google.android.tungsten.setupwraith`,
  dengan `.MainActivity` pada prioritas filter intent 4 dan `.RecoveryActivity` pada prioritas 1. Kamu
  tidak bisa mengalahkannya, dan jalan pintas yang kelihatan jelas tidak menolong: Android mengabaikan `android:priority` pada
  filter HOME untuk aplikasi yang tidak ada di citra sistem, `cmd package set-home-activity` mencetak
  `Success` tanpa mengubah apa pun (bahkan setelah reboot), dan `pm disable-user` pada salah satu
  komponen setupwraith ditolak dengan SecurityException. Menonaktifkan seluruh
  paket `com.google.android.tungsten.setupwraith` yang membebaskan slotnya. Itu wizard
  penyiapan pertama kali, jadi pemakaian sehari-hari tidak terpengaruh, tetapi aktifkan lagi sebelum reset pabrik — cantumkan di
  `debloat.txt`-mu dan `tools/restore.sh` melakukannya untukmu. Diukur lagi di Google TV
  Streamer (`kirkwood`, `UTTK.260317.003`): launcher yang dibangun dengan `android:priority="3"` pada filter HOME-nya
  (`aapt2 dump xmltree` menemukannya di APK terpasang) tetap tidak punya baris `mPriority` di
  `dumpsys package`, dan `cmd role get-role-holders android.app.role.HOME` menyebut launcher baru
  sementara tombol HOME tetap mendarat di `setupwraith/.RecoveryActivity`. `cmd package resolve-activity`
  dan `query-activities` menjawab dengan pemenang tabel resolver, yang tidak harus
  aktivitas yang mengambil fokus: `dumpsys activity activities | grep mFocusedApp` adalah kebenaran di lapangan,
  dan itu yang perlu dicek sebelum mengatakan pengambilalihan berhasil.
- **TV lepas dari Wi-Fi untuk seterusnya begitu setupwraith dinonaktifkan**, terlihat di Google TV 14 (Sony
  BRAVIA 9 II, Philips R8G, Skyworth SWTV-24NA-4K NF, Xiaomi TV Box S generasi ke-3 MiTV-AFMU0) dan di TCL
  G07 pada Google TV 11 (build AR17; di AR20 jaringannya milik `android.uid.system`). Jaringan Wi-Fi
  yang disimpan selama wizard pertama kali milik setupwraith, dan Android menghapus jaringan tersimpan sebuah aplikasi
  saat aplikasi itu dinonaktifkan. TV-nya baik-baik saja tetapi luring, jadi ADB dan remote HP berhenti
  bekerja, dan mulai ulang tidak membawa jaringan kembali. Cek sebelum menonaktifkannya: `tools/tv.py "dumpsys wifi" | grep -o 'cname=[^ ]*'`.
  Kalau yang disebut setupwraith, lupakan jaringan dan gabung lagi di Settings > Network & Internet (lalu
  milik `android.uid.system`), atau pakai Ethernet. `tools/disable.sh` melewatkan setupwraith selagi ia
  memiliki jaringan (`FORCE_SETUPWRAITH=1` menimpa). Sudah luring? Gabung lagi dari TV dengan
  remote-nya sendiri atau papan ketik USB, lalu nyalakan Wireless debugging lagi kalau sedang dipakai. Menyambung ulang tidak
  cukup; lupakan jaringannya dulu (Xiaomi MiTV-MOSR4, di mana ini juga mematikan USB debugging).
- **Reboot di layar pemulihan setupwraith mereset pabrik TV** (Xiaomi MiTV-MOSR4). Nonaktifkan
  setupwraith sebelum launcherx. Kalau `mFocusedApp` menampilkan `.RecoveryActivity`, aktifkan launcherx
  dulu dan jangan reboot. [Catatan](devices/xiaomi-mitv-mosr4.md).
- **Home berjalan berhari-hari, lalu hitam setelah pemutusan daya** (Acer R4G dan Panasonic 4KTV-3MUP,
  MOKA `R4_GTV`, Google TV 14): firmware mengaktifkan lagi setupwraith pada setiap boot dingin dan
  `.RecoveryActivity`-nya mengambil Home. `logcat | grep RecoveryActivity` menampilkannya mulai beberapa kali
  per detik. Aktifkan lagi launcherx seperti di atas. [Catatan](devices/acer-r4g.md).
- **Black screen at every boot after the launcher change, LED on, remote dead** (Vision
  2024VISION43STV, CVTE, Android 13: [daftarnya](../examples/vision-2024vision43stv/debloat.txt) punya
  detailnya). Dengan enkripsi berbasis berkas (`getprop ro.crypto.type` bilang `file`) Android memulai Home sebelum
  buka kunci pertama, dan hanya aktivitas yang sadar direct-boot yang bisa mulai saat itu. Sebagian besar firmware punya
  `FallbackHome` yang menggantikan sampai buka kunci; sebagian, seperti papan CVTE ini, tidak, dan boot
  tidak pernah selesai. Launcher repo ini sadar direct-boot; sebagian besar launcher lain tidak. Cek keduanya
  sebelum menonaktifkan launcher bawaan:
  ```sh
  tools/tv.py 'cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
  tools/tv.py 'dumpsys package <new.launcher.package> | grep -o "[A-Z_]*DIRECT_BOOT_AWARE"'
  ```
  Tidak ada `FallbackHome` di yang pertama dan tidak ada apa-apa dari yang kedua: biarkan launcher bawaan aktif dan setel
  Home hanya dengan `cmd package set-home-activity`. Baris itu bukan bukti serah terima terjadi: di set
  yang launcher bawaannya mendeklarasikan prioritas HOME, ia bisa mencetak `Success` dan tidak mengubah apa pun, dengan peran Home
  sudah menyebut launcher baru sementara Home masih membuka yang bawaan, jadi baca resolver-nya
  (`tools/tv.py 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'`).
  Kalau ia tetap menyebut launcher bawaan, set itu hanya untuk debloat sampai seseorang menjalankan dua pemeriksaan ini di
  papan yang persis itu (Thomson 40FG2S14W, Expressluck `nagata`, Android TV 14 `UKNV.260317.001`,
  [catatan](devices/thomson-40fg2s14w.md)).
- **Macet di animasi boot setelah perubahan launcher, ADB hilang** (JVC LT-43N7115, CVTE
  `TP.SK706S.PC822`, Android 11, [#741](https://github.com/Gathin23/cleanyourtv/discussions/741)): launcher bawaan
  `com.cvte.tv.launcher.space` dinonaktifkan dan pembantu tombol remote diaktifkan sebelum satu reboot.
  Tidak ada yang dicopot, jadi coba urutan ini: tunggu sepuluh menit sekali; boot ke mode aman (tahan Power di
  remote selama animasi di banyak set), yang mematikan aplikasi tambahan, lalu aktifkan lagi launcher bawaan
  di Settings > Apps > tampilkan aplikasi sistem; terus coba `adb connect <ip>:5555` selama beberapa
  menit tepat setelah nyala dan jalankan `pm enable com.cvte.tv.launcher.space` serta
  `settings put secure enabled_accessibility_services ""` kalau ia menjawab (hotspot HP dengan nama dan kata sandi
  jaringan lama menggantikan router yang hilang); hapus partisi cache dari menu pemulihan.
  Reset pabrik juga berhasil tetapi menghapus semuanya, jadi itu yang terakhir. Prompt sekarang membiarkan launcher bawaan
  aktif di papan CVTE tanpa daftar yang teruji.
- **Akun & profil kosong setelah launcher bawaan dinonaktifkan** (TCL 75C845X2, `G08_4K_GB`,
  Google TV 14, [#304](https://github.com/Gathin23/cleanyourtv/discussions/304)). Akunnya masih
  ada (`dumpsys account` mencantumkannya); halamannya tinggal di `com.google.android.apps.tv.launcherx`, dan selagi
  launcherx aktif ia mengambil Home kembali (`set-home-activity` mencetak `Success` dan tidak mengubah apa pun, dan
  aktivitas home-nya tidak bisa dinonaktifkan dari shell). Untuk berganti profil: `pm enable --user 0
  com.google.android.apps.tv.launcherx`, ganti di Settings, lalu nonaktifkan lagi dan setel Home terakhir.
- **Pencarian suara untuk lagu atau video bilang "Google Assistant is having trouble processing your request"**
  begitu launcherx dinonaktifkan, sementara perintah aplikasi ("buka YouTube") tetap jalan. Terlihat di onn 4K Streaming
  Box (`YOC`, Google TV 14, `URO4.260304.011.B1.15051976`). Google TV menampilkan hasil itu di layar utama
  miliknya: Assistant mengirim `VIEW https://tv.google.com/...` ke launcherx
  (`.gateway.LauncherDeepLink`, lalu `.home.HomeActivity` mencantumkan hasilnya, dan memilih satu membuka YouTube
  menurut paket). Dengan launcherx nonaktif tidak ada yang bisa dibuka, dan logcat menampilkan `Failed to execute
  provider.OPEN with status: UNKNOWN_ERROR` dalam 100 ms. Dua hal membuatnya kelihatan seperti yang lain:
  tepat setelah launcherx sempat berjalan, Assistant terus menyajikan hasil untuk sementara meskipun dinonaktifkan
  lagi, dan kegagalan baru kembali setelah reboot; dan tidak ada yang di daftar debloat yang penting — dengan setiap
  paket lain diaktifkan lagi ia gagal dengan cara yang sama. Mode hanya aplikasi (Settings > Accounts & sign-in) bukan jalan
  keluar: ia mematikan Assistant. Yang berhasil: biarkan launcherx aktif (lalu ia jadi Home, lihat entri
  di atas) dan buka launcher ini dari baris aplikasinya, di mana pencarian suara berjalan normal karena layar
  hasil muncul di atasnya; atau cari di dalam YouTube dengan mikrofon di layarnya. Komunitas Projectivy menemukan
  yang sama (hasil suara "need the launcher X search results page").
- **Tombol mikrofon remote tidak melakukan apa-apa, atau membuka Assistant dan ia tidak pernah mendengarmu**, terlihat di
  Xiaomi TV Box S generasi ke-3 (`MiTV-AFMU0`, `twilight`, Google TV 14, `V816.0.7.0.UZFAABX`). Dua
  paket harus tetap aktif, dan mengaktifkan hanya salah satunya yang membuat ini sulit
  didiagnosis: `com.google.android.katniss` adalah Assistant itu sendiri, dan
  `com.google.android.tv.remote.service` adalah jembatan Google TV untuk remote fisik, yang
  membawa mikrofon Bluetooth remote. Dengan keduanya nonaktif tombol tidak melakukan apa-apa. Dengan hanya
  `katniss` kembali, Assistant terbuka, mengambil `AudioRecord` (`inputSource 6`, VOICE_RECOGNITION) dan
  merekam keheningan, karena audio tidak pernah meninggalkan remote. `pm enable` pada keduanya memulihkan
  tombol tanpa reboot (jalankan `pm enable` sekali per paket). Untuk membedakan dua kegagalan: `dumpsys package
  com.google.android.katniss | grep enabled` dan yang sama untuk `tv.remote.service` (`enabled=3` berarti
  nonaktif), lalu `logcat -d | grep AudioRecord` tepat setelah menekan tombol: sesi yang
  terbuka dan tertutup tanpa pengenalan adalah jembatan yang hilang, bukan Assistant yang rusak. Daftar AFMU0
  sekarang mempertahankan keduanya; daftar yang masih menonaktifkan `com.google.android.tv.remote.service` adalah
  `rca-ai-pont`, `sharp-aquos-tve21f`, `sony-bravia-4k-gb-us`, `sony-xbr-65x900f`,
  `sony-xbr-x900e`, `tcl-32s5k`, `tcl-40s5400a`, `tcl-55s470g`, `tcl-beyondtv2-android-11`,
  `tcl-g09-google-tv`, `tcl-g10-google-tv`, `xgimi-horizon-s-pro`, `xgimi-mogo-3-pro`, dan
  `xiaomi-mi-box-s`. TV-TV itu belum diuji untuk ini, tetapi yang remote-nya punya
  mikrofon bisa menunjukkan gejala yang sama.
- **Casting from a phone fails with "Untrusted device" after setupwraith is disabled**, terlihat di
  Chromecast with Google TV HD (`boreal`) pada Google TV 14 build UTTC.250917.004, build tempat
  daftar `examples/google-chromecast-gtv-4k/` dibuat. YouTube di HP bilang "Untrusted device.
  ... couldn't be verified", dan logcat mengulang `cast_ClientAuthCredsProvisioning: Loading factory
  credentials for 1P device`, `Failed to find provider info for ...CastInfoProvider` dan
  `cast_shell: No private key, hence empty signature`. Penerima Cast
  (`com.google.android.apps.mediashell`) memuat kredensial perangkat pabrik dari penyedia konten
  di dalam setupwraith (`...tv.setupwraith.libs.castinfo.CastInfoProvider`), jadi menonaktifkan wizard
  membawa kredensialnya. Aktifkan lagi dan dalam semenit log menampilkan
  `SetupWraith|CastInfoProvider: querying cast infos` dan `Created singleton instance of
  ClientAuthSigner`, dan casting jalan. Kamu tidak bisa mempertahankan penyedia dan hanya melepas Home:
  `pm disable-user` pada `setupwraith/.RecoveryActivity` ditolak dengan `SecurityException: Shell
  cannot change component state`. Jadi debloat tanpa setupwraith dan launcherx, atau re-enable setupwraith after each boot, tunggu `ClientAuthSigner`, lalu nonaktifkan lagi: penerima menyimpan
  kredensial di memori, jadi casting bertahan until the next reboot — dan hanya di proses itu.
  `am force-stop com.google.android.apps.mediashell` diikuti penerima yang kembali mencetak
  `Retry loading factory credentials` dan `No private key, hence empty signature` lagi, jadi apa pun
  yang memulai ulangnya (jatuh, force-stop, pembaruan) butuh urutan yang sama. Mengaktifkan lagi
  wizard tidak selalu cukup setelah reboot: di Chromecast with Google TV 4K (`sabrina`,
  UTTC.250917.004) tidak ada baris `ClientAuthSigner` dalam 3 menit dan HP bahkan tidak mencantumkan
  perangkat. Mulai ulang penerima selagi setupwraith aktif: `am force-stop
  com.google.android.apps.mediashell`, lalu `am start-foreground-service -n
  com.google.android.apps.mediashell/.CastReceiverService` (setelah force-stop tidak ada yang memulainya
  lagi), dan baris penandatangan serta `MdnsAppFilter: Done announcing.` menyusul dalam hitungan detik. Dilaporkan di perangkat
  Chromecast milik Google (`boreal`) dan, dengan baris log yang sama, di Google TV Streamer
  (`kirkwood`, `UTTK.260317.003`), di mana `CastInfoProvider` juga tinggal di dalam APK setupwraith;
  daftar Xiaomi Mi Box S generasi ke-2 mengatakan casting dari Pixel tetap jalan di sana dengan
  setupwraith mati.
- **Casting disappears although the Cast receiver is still running**, terlihat di TCL 75QM751G (QM7,
  Google TV 14) setelah daftarnya, dan kesimpulan "hidup bukan berarti bisa ditemukan" yang sama di TCL
  BeyondTV2 (Android TV 9, R851T02 V660) di utas yang sama. HP dan PC berhenti melihat TV,
  `com.google.android.apps.mediashell` nyala dan mendengarkan di 8008/8009, dan tidak ada
  baris `No private key` atau `CastInfoProvider` — jadi ini not the setupwraith case di atas, dan
  mengaktifkan lagi setupwraith tidak menolong. Cek penemuan, bukan prosesnya:
  `tools/tv.py "logcat -d -s cast_shell | grep -E 'Done announcing|responses sent'"` dan
  `tools/tv.py "dumpsys activity service com.google.android.apps.mediashell | grep WargService"`.
  Penerima yang sedang mengumumkan mencatat `MdnsAppFilter: Done announcing.`,
  `responses sent in 60 seconds: N` dan `Gcm.Register.Success=2`, dengan `WargService` nyala. Kalau itu
  hilang sementara prosesnya hidup, penerimanya menggantung: laporan QM7 melihat koneksinya duduk
  di `CLOSE_WAIT` pada 8009 dengan data yang belum dibaca. `tools/tv.py "am force-stop
  com.google.android.apps.mediashell"` memulai ulangnya (TV memulainya lagi sesuai permintaan), dan
  pengumuman kembali dalam hitungan detik, tanpa reboot. Catatan perangkat:
  [TCL 75QM751G](devices/tcl-75qm751g-qm7.md).
- **TV lepas dari Wi-Fi setiap menit tepat setelah pemasangan**, terlihat di Google TV 14 (Xiaomi
  MiTV-MZTU0): `.RecoveryActivity` setupwraith yang sama mengambil Home dan system_server crash-looped
  di situ. Skrip pasang sekarang memulai launcher baru sebelum menonaktifkan yang bawaan, menyetel
  preferensi home terakhir (Android menjatuhkannya setiap kali himpunan aplikasi HOME berubah), lalu memeriksa ke mana
  Home mengarah dan mengaktifkan lagi launcher bawaan kalau bukan yang baru. Nonaktifkan setupwraith seperti
  di atas. Untuk memastikan ini bug itu: `logcat -b crash` menampilkan `FATAL EXCEPTION IN SYSTEM PROCESS:
  android.display`, dan log utama menampilkan `setupwraith/.RecoveryActivity` dimulai setiap
  sekitar 200 ms. Di Google TV Streamer (`kirkwood`, `UTTK.260317.003`) lingkaran yang sama datang dengan
  tanda tangannya sendiri — `IllegalArgumentException: positionChildAt: container=Task=... is not a child of
  container=Task=1 current parent=null` di `WindowContainer.positionChildAt` /
  `Task.moveToBack` / `TransitionController.tryStartCollectFromQueue` — dan berulang kira-kira setiap
  47 d, setiap siklus menjatuhkan setupwraith dan SystemUI dengan `DeadSystemException` juga. Kalau kamu
  sudah di dalam lingkaran:
  `tools/tv.py 'pm enable --user 0 com.google.android.apps.tv.launcherx'`.
- **`theme.sh` gagal dengan "couldn't create file: Permission denied"**, terlihat di TCL G07 (Android 11):
  shell tidak ada di grup yang boleh menulis `Android/data`. `theme.sh` menaruh berkas di
  `/data/local/tmp` dan menulisnya lewat `ThemeProvider` launcher, yang membuatnya sebagai
  launcher; `cp` hanya cadangan untuk launcher yang dipasang sebelum penyedia itu ada: jalankan
  `launcher/build-and-install.sh` sekali, lalu dorong theme lagi.
- **Dorongan theme mencetak "is on the TV" dan tidak ada yang berubah**, terlihat di Android 14: shell *bisa* menulis
  `Android/data` di sana, jadi `cp` berhasil — tetapi berkas yang dibuatnya milik shell (`ls -ln` menampilkan
  `2000 1078`, bukan uid launcher) dan launcher mencatat `W TvHome.Config: ignoring
  .../config.json: open failed: EACCES (Permission denied)`. Menimpa berkas yang ditulis launcher sendiri
  mempertahankan pemiliknya, jadi ini hanya menggigit berkas yang belum ada: kelihatan baik di set yang sudah
  pernah di-theme dan tidak melakukan apa-apa pada pemasangan baru atau tepat setelah pasang ulang. `theme.sh` sekarang menghapus
  salinan shell dan menulis lewat `ThemeProvider` lebih dulu (penyedia tidak bisa memotong berkas yang dimiliki shell);
  di checkout yang lebih tua, mendorong theme lagi setelah `launcher/build-and-install.sh` menulis
  berkas sekali juga memperbaiki kepemilikan.
- **Google TV kembali setelah beberapa tekanan remote**, terlihat di XGIMI (MoGo 3 Pro, Google TV 14).
  Tombol aplikasi remote (YouTube, Netflix, Prime Video), tombol yang bisa diprogram, tombol profil, dan daya
  semuanya mengirim tombol F yang sistem serahkan ke `com.xgimi.xrmservice/.XgimiGlobalKeyBroadcast` sebagai tombol global
  (`tools/tv.py 'dumpsys window' | grep -A40 mKeyMapping` mencantumkannya). Penerima itu menjalankan
  `pm enable com.google.android.apps.tv.launcherx` pada setiap tombol itu, jadi launcher bawaan mengambil Home kembali.
  Tombol yang tercantum di `app_keys` diambil layanan tombol sebelum pengiriman tombol global, jadi penerima XGIMI tidak pernah
  melihatnya: `examples/xgimi-mogo-3-pro` mencantumkan tombol aplikasi. Daya (F11) tidak bisa diambil alih, karena
  penanganan tidur dan menu daya butuh izin sistem. Untuk yang satu itu, jalankan dengan `HOME_KEY_SERVICE=1` dan setel
  `stock_home_redirect` ke `com.google.android.apps.tv.launcherx`: kalau ia kembali, layar utamanya dikirim
  langsung kembali ke sini, dan `tools/disable.sh` dengan daftar menonaktifkannya lagi.
- **Tidak ada papan ketik di layar** setelah berganti launcher, terlihat di Xiaomi: metode masukan bawaan
  menunjuk ke papan ketik Baidu yang tidak terpasang. Perbaikan:
  `tools/tv.py 'ime set com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME'`
- **Launcher pihak ketiga** sering memungut biaya untuk hal dasar seperti wallpaper sendiri. Makanya repo ini
  punya milik sendiri: VibeYourTV.
- **Zona waktu salah, jam meleset 30 atau 60 menit**, terlihat di Xiaomi di Iran: TV memilih
  zona tetangga, dan firmware yang lebih tua membawa data zona waktu dari sebelum Iran menghapus waktu musim
  panas pada 2022 (di Android 10 dan yang lebih baru, cek dengan
  `tools/tv.py 'cat /apex/com.android.tzdata/etc/tz/tz_version'`). Setel zonanya sendiri:
  `tools/tv.py 'settings put global auto_time_zone 0; cmd alarm set-timezone Asia/Tehran'`.
  Kalau jam lalu meloncat sejam ke depan di musim semi, offset tetap menjaganya benar:
  `tools/tv.py 'cmd alarm set-timezone GMT+03:30'`. Yang sama berlaku untuk negara mana pun yang aturannya
  berubah setelah firmware dibangun.
- **Pil jaringan bilang "Wi-Fi" alih-alih nama jaringan** setelah `pm clear com.gathin.tvhome`
  atau pasang ulang dari nol: membersihkan data launcher juga menjatuhkan izin lokasi yang
  dibutuhkan Android 10 untuk membaca SSID. `pm grant com.gathin.tvhome android.permission.ACCESS_FINE_LOCATION`
  (skrip pasang melakukan ini).
- **Home menjadi "Select a Home app"** setelah `tools/restore.sh` diikuti menerapkan ulang daftar: menonaktifkan
  paket yang juga mengklaim HOME (PatchWall di Xiaomi) membuat Android menjatuhkan bawaannya. Skrip pasang
  sekarang memilih launcher bawaan yang dikenal di antara kandidat sendiri; kalau tidak, pilih satu di
  TV atau berikan `STOCK_LAUNCHER=<package>`.
- **Stremio gagal setiap aliran dengan `ConnectException: Failed to connect to /127.0.0.1:11470`**,
  dan hanya Force stop yang membawanya kembali sampai reboot berikutnya. Ini bukan debloat. Ini regresi Stremio
  di 1.11 ([stremio-bugs#2866](https://github.com/Stremio/stremio-bugs/issues/2866)):
  `startServer()` hanya berjalan begitu siklus hidup setidaknya STARTED, jadi proses yang dibangunkan Android di
  latar tidak pernah memulai server lokal, dan membuka aplikasi memakai ulang proses itu. Di TV
  yang membangunkan adalah `BootReceiver` milik Stremio yang menyegarkan kanal layar utama, jadi ini terjadi pada setiap
  boot. Meninggalkan aplikasi dan membukanya lagi tidak menolong (pid sama); proses baru mana pun menolong, termasuk `am kill`.
  Mencegah proses yang dibangunkan boot menempel memperbaiki ini untuk seterusnya:
  `tools/tv.py 'cmd appops set com.stremio.one RUN_ANY_IN_BACKGROUND ignore; cmd appops write-settings'`.
  Perintah kedua bukan opsional: app-ops ditulis ke disk dengan malas, dan di set Vestel shandao
  op itu kembali ke `allow` setelah reboot berikutnya tanpanya, yang kelihatan persis seperti
  jalan keluar yang tidak bekerja. Aplikasi yang dimulai di latar depan tidak terpengaruh, dan efek sampingnya, bahwa
  kanal layar utama hanya disegarkan selagi Stremio terbuka, tidak menghabiskan apa pun begitu baris rekomendasi
  memang dinonaktifkan.
- **Setiap paket dalam satu batch aktif lagi setelah reboot yang mengikutinya**, terlihat di Hisense
  HiSmartTV A4 (xiaoyushan, Android TV 11, `RTT2.221108.001` / `00.00.01.18`). Kelihatan seperti firmware
  yang mereset daftar pada setiap boot, tetapi ini soal waktu reboot. Android menulis keadaan paket
  ke disk sekitar sepuluh detik setelah perubahan (di AOSP, `WRITE_SETTINGS_DELAY`). `tools/tv.py reboot`
  yang dikirim langsung setelah `tools/disable.sh` datang sebelum penulisan itu, jadi TV boot dengan keadaan lama.
  `dumpsys package <name>` menampilkan `enabled=0` (bawaan) untuk setiap paket, dan `pm list packages -d`
  kosong. Paket yang sama dinonaktifkan, diberi 12 detik, lalu reboot tetap di `enabled=3`
  (disabled-user), begitu juga setiap batch yang diterapkan dengan cara itu. Tunggu 15 detik setelah perintah `pm` terakhir
  sebelum reboot, penantian yang sama yang sudah dijelaskan daftar teruji di antara batch. Ini padanan
  keadaan paket dari penulisan app-ops yang malas di atas.
- **Pembaruan firmware diam-diam membatalkan pembersihan.** Pembaruan terpasang di atas sistem yang sudah dibersihkan, jadi
  setiap paket yang tercantum aktif lagi, Home kembali ke launcher bawaan, dan preload baru
  muncul yang tidak disebut daftar mana pun. `tools/venv/bin/python -I tools/check.py <list>` melaporkan dua
  yang pertama dan keluar 1; `--fix` menonaktifkannya lagi dan menaruh launcher kembali di Home. Preload baru tidak
  bisa diketahuinya, jadi setelah pembaruan besar bandingkan `tools/tv.py 'pm list packages'` dengan daftar
  sekali dengan tangan. Di TV yang daftarnya menonaktifkan pemutakhir, ini hanya terjadi kalau kamu memasang
  pembaruan dengan sengaja: aktifkan pemutakhir, perbarui, `check.py --fix`, nonaktifkan lagi.

## Accessibility helper enabled but the remote button does nothing

`HomeKeyService` dan `InputKeyService` yang opsional hanya menyaring tombol remote setelah Android mengikat
mereka. Pesan `enabled` dari pemasang berarti setelan tertulis, bukan bahwa layanan terikat.
Baca keadaannya tanpa mengubah apa pun:

```sh
tools/tv.py 'am get-current-user'
tools/tv.py 'settings --user current get secure enabled_accessibility_services'
tools/tv.py 'dumpsys accessibility'
```

| Pembantu | Komponen di daftar layanan aktif | Tombol yang disaring |
| --- | --- | --- |
| Tombol Home | `com.gathin.tvhome/.HomeKeyService` | `KEYCODE_ALL_APPS` (bukan `KEYCODE_HOME` biasa) |
| Tombol Input | `com.gathin.tvhome/.InputKeyService` | `KEYCODE_TV`, `KEYCODE_TV_INPUT`, plus keycode di `input_key_keycodes` dan `app_keys` (`config.xml` atau theme) |

Force stop launcher (Settings > Apps, aplikasi pembersih, `am force-stop`) menurunkan kedua pembantu dari
daftar itu. Dengan `restore_key_services` nyala (`config.xml` atau theme) dan `WRITE_SECURE_SETTINGS` diberikan
(`RESTORE_KEY_SERVICES=1` saat pasang, `-RestoreKeyServices` di Windows), Home menaruh kembali pembantu yang tadinya
berjalan ke daftar kali berikutnya ia mulai; pembantu yang dimatikan di Settings tetap mati.

- **Aktif** berarti komponen ada di daftar terpisah titik dua milik pengguna itu (kelas bisa tertulis
  penuh, `com.gathin.tvhome/com.gathin.tvhome.HomeKeyService`). Cocokkan seluruh komponen.
- **Terikat** berarti ia muncul di bagian `Bound services` milik pengguna itu pada dump (`services` di build
  yang lebih tua). Beri beberapa detik setelah mengaktifkan lalu baca lagi. Label di dump bisa diterjemahkan atau
  dipakai bersama layanan lain; baris yang berisi "Home" bukan bukti.
- **Bekerja** berarti tombol fisik melakukan hal yang benar dari dalam aplikasi lain. Tombol yang disuntikkan
  lewat ADB membuktikan navigasi, bukan bahwa remote mengirim kode yang disaring pembantu ini. Ia bahkan tidak
  sampai ke pembantu: `input keyevent` melewatkan penyaringan tombol aksesibilitas, jadi `KEYCODE_TV` yang disuntikkan tidak
  melakukan apa-apa meskipun tombol Input sungguhan bekerja.
- **Which keycode** a dead button sends: `dumpsys window policy` mencantumkan "global keys" yang dirutekan Android
  ke satu aplikasi (di Google TV beberapa pergi ke launcherx, yang nonaktif), dan firmware Xiaomi mencatat setiap
  tekanan sebagai `handleAction<keycode>` di `logcat`. Firmware TCL Google TV mencatatnya sebagai
  `TclPhoneWindowManager: handleKeyBeforeQueueing, key = 4099, down = true` (TCL 55C728, Google TV 11);
  baca log tepat setelah tekanan, karena buffer utamanya di sana menampung jauh di bawah semenit.
  Taruh di `input_key_keycodes` (membuka Input) atau
  `app_keys` (`keycode:package`, atau `hold:keycode:package` untuk tekan lama), pasang ulang dengan
  `INPUT_KEY_SERVICE=1` dan tekan tombol sungguhan.
  Tekanan yang dilaporkan `logcat` sebagai `KEY_UNKNOWN` atau `KEY_PROGRAM` tidak bisa dicocokkan dengan keycode sama sekali,
  karena setiap tombol yang tidak terpetakan berbagi kode itu: pakai scancode Linux dari
  `tools/tv.py 'getevent -lt'` di `settings_key_scancodes` dan pasang dengan `HOME_KEY_SERVICE=1`
  (remote RC-36 mengirim girnya di scancode 240 dan tombol notifikasinya di 362 dengan cara itu; lihat
  [catatan FPD CG50-C3](devices/fpd-cg50-c3.md)).
  Tidak setiap set menjawab dua jalan itu ([#853](https://github.com/Gathin23/cleanyourtv/issues/853),
  PEAQ "AI PONT DE", `DVB99PQ` / Changhong `changi`, Google TV 14, `UKNV.260504.001`): pemakaian
  kendali-konsumen HID mencetak `MSC_SCAN` evdev yang tidak harus `KeyEvent.getScanCode()`
  yang dilaporkan framework, jadi nilai yang dibaca dengan `getevent -lt` bisa tidak cocok dengan apa pun di
  `settings_key_scancodes` (gir set itu mencetak `MSC_SCAN 000c009f`, dan baik 159 maupun 786591, dua
  ejaan itu, tidak melakukan apa-apa), dan `logcat -v time -s InputReader:D`, filter yang disarankan `config.xml`,
  mencetak no key line sama sekali di sana, untuk tombol mana pun, hanya `Reconfiguring input devices`. Yang
  benar-benar diserahkan Android datang dari `Log.d` sementara di puncak `onKeyEvent` pembantu yang mencetak
  `getKeyCode()`, `getScanCode()`, dan `getDeviceId()`, dibangun ulang dengan `HOME_KEY_SERVICE=1` dan dibaca dengan
  `tools/tv.py "logcat -s TvHomeKeys:D"` selagi menekan tombol sungguhan: set itu melaporkan
  `action=0 keyCode=83 scanCode=240 deviceId=9`, dan `settings_key_keycodes = [83]` membuka
  Settings TV seketika. Jadi kode `getevent` yang tidak cocok dengan apa pun is not proof the press never arrived, dan
  mengonsumsi tombol di pembantu juga menahannya dari penerima vendor
  `com.homwee.factory/.FactoryKeyReciver`, yang dituju `dumpsys window policy` untuk `KEYCODE_NOTIFICATION`
  begitu launcher bawaan hilang.
- **Semua tombol vendor mati bersamaan** dan `logcat` bilang `GLOBAL_BUTTON ... process is bad`:
  penerima tombol global jatuh pada launcherx yang nonaktif dan Android berhenti memulainya. Petakan setiap
  keycode seperti itu di pembantu, juga untuk aplikasi yang nonaktif. Lihat [catatan MOSR4](devices/xiaomi-mitv-mosr4.md).
- **The list still names the helper but `accessibility_enabled` is `0`**, jadi tidak ada yang terikat dan
  tombol tidak melakukan apa-apa (bukan force stop di bawah): di TCL C6K Pro (`G08`, Android 14,
  [#1047](https://github.com/Gathin23/cleanyourtv/issues/1047)) sebuah reboot meninggalkan
  `enabled_accessibility_services` yang menyebut `com.gathin.tvhome/com.gathin.tvhome.HomeKeyService` sementara
  `accessibility_enabled` membaca `0`, `dumpsys accessibility` menampilkan `Bound services:{}` dan tombol Home
  membuka Google TV. `restore_key_services` tidak memperbaiki keadaan ini: `ServiceRepair` menulis
  `accessibility_enabled 1` hanya di jalur yang juga menaruh kembali pembantu yang hilang ke daftar, dan di sini
  daftarnya lengkap. Tulis benderanya kembali, lalu baca dump lagi; mengalihkan layanan mati lalu nyala
  di Aksesibilitas, yang dilakukan pemilik, membawa pengalihan kembali untuk sesi itu. Apakah
  bendera bertahan melewati reboot berikutnya was not retested di set itu, dan apa yang membersihkannya is not established.
  Di TCL 65C655 Pro (`G03`, Android 11, [`examples/tcl-55c725`](../examples/tcl-55c725/README.md)) setiap
  satu dari tiga reboot meninggalkan keadaan yang sama, dan di sana menulis bendera kembali saja tidak mengikat apa pun; membersihkan
  `enabled_accessibility_services` dan menulisnya kembali, seperti yang dilakukan `build-and-install`, berhasil:

  ```sh
  tools/tv.py 'settings get secure accessibility_enabled'
  tools/tv.py 'settings put secure accessibility_enabled 1'
  tools/tv.py 'dumpsys accessibility'
  ```

Force-stop mematikan pembantu untuk seterusnya: setelah `am force-stop com.gathin.tvhome` (atau Force stop di
Settings > Apps) Android menghapus layanan launcher dari daftar aktif, dan mereka tidak kembali
saat launcher mulai lagi (terlihat di TCL 55C715, Android TV 9). Aktifkan lagi dengan cara yang sama seperti
pertama kali, dan hindari force-stop launcher selagi menguji.

Aktif tetapi tidak pernah terikat menunjuk ke pembatasan firmware pada mulai-otomatis layanan yang di-sideload (sebagian build TCL
mencatat penolakan `OP_AUTO_START`). Lihat `logcat` untuk kegagalan pengikatan sebelum mengubah setelan,
dan do not rewrite the enabled-services list atau memberi app-ops sebagai perbaikan selimut. Di TCL 55S470G (Google
TV 12) barisnya `[TclAppBoot] ... calleeComponent = 'com.gathin.tvhome/.HomeKeyService', bootType =
'bind_service', isAllow = 'false', reason = 'callee_does't_have_OP_AUTO_START_permission.'`, dan
`appops get com.gathin.tvhome` menampilkan `APP_AUTO_START: ignore`. Mengizinkan op yang satu itu hanya untuk launcher
dan menyimpannya (`appops set com.gathin.tvhome APP_AUTO_START allow; appops write-settings`) membuat layanan
terikat sendiri setelah mulai ulang; mengalihkan layanan mati lalu nyala di Aksesibilitas berhasil sampai mulai
ulang berikutnya. App-ops sampai ke disk dengan malas: tanpa `write-settings`, mulai ulang segera setelah `set` menjatuhkan
perubahan dan penolakan kembali. Terlihat di TCL C89B (Google TV 14, `G08`, `UTT2.250416.001 AU02`), yang lognya
membaca `[TclAppBoot]:forbid bind service ComponentInfo{com.gathin.tvhome/com.gathin.tvhome.HomeKeyService}`
dan yang `appops get`-nya menamai op `AUTO_START`: pakai nama yang dicetak `appops get`. Saat melapor, beri
model, build, pengguna saat ini, dan baris yang relevan yang sudah disamarkan, bukan dump mentah.

## SHIELD TV phone app connects, but no key does anything

Di NVIDIA SHIELD, aplikasi HP SHIELD TV menemukan kotak dan tersambung, tetapi D-pad, OK, Back, dan
tombol daya di aplikasi tidak melakukan apa-apa. Memulai ulang server remote membawanya kembali sampai
reboot berikutnya. Pembantu tombol bukan penyebabnya: ini terjadi juga dengan mereka mati.

Tombol aplikasi lewat `com.nvidia.shield.remote.server`, yang menyuntikkannya lewat
`virtualinput.InputService` miliknya, sebuah `TvRemoteProvider` yang diikat layanan remote TV Android saat boot.
Baca keadaannya tanpa mengubah apa pun:

```sh
tools/tv.py "logcat -d | grep -E 'Key injection failed|Beyonder_VIS'"
tools/tv.py "dumpsys activity services com.nvidia.shield.remote.server | awk '/\* ServiceRecord/{inb=/virtualinput.InputService/} inb' | grep -c IntentBindRecord"
```

`Key injection failed` dengan `Beyonder_VIS ... mProvider ... isInitialized=false`, dan hitungan `0`, adalah
gangguan ini: layanannya berjalan tetapi Android tidak pernah mengikatnya. `1` adalah layanan yang terikat dan bekerja.

Penyebab: `com.google.android.tvrecommendations` nonaktif. Dengan itu nonaktif, penyedia tidak terikat setelah
boot. Dibagi dua dengan reboot per langkah di SHIELD 2017 (`darcy`) dan dikonfirmasi di yang 2015/2017
(`foster_e`), keduanya di SHIELD Experience dengan Android 11: setiap reboot dengan itu mati meninggalkan hitungan `0`,
setiap reboot dengan itu nyala memberi `1`. Perbaikan:

```sh
tools/tv.py "pm enable com.google.android.tvrecommendations"
```

Tunggu sekitar 20 seconds sebelum reboot: Android menulis keadaan paket ke disk dengan penundaan, dan reboot
langsung setelah `pm enable` bisa kehilangan perubahan. `am force-stop com.nvidia.shield.remote.server` membuat
Android mengikat penyedia seketika, tetapi hanya sampai boot berikutnya. Daftar SHIELD di `examples/` membiarkan
`tvrecommendations` aktif karena alasan ini.

## Fire TV (Fire OS): debloat jalan, launcher tidak bisa mengambil Home

Kenapa Google TV berbeda: di Google TV aplikasi pihak ketiga bisa mengambil peran HOME, jadi launcher,
bawaan `set-home-activity`, dan penonaktifan launcher bawaan semuanya jalan. Fire OS mempertahankan Home untuk launcher
milik Amazon, dan alat yang sama gagal di sana. Dicek di Amazon Fire TV, model AFTDEC012E (nama kode ginza),
Android 11 (SDK 30), build RS8185.3885N:

- `cmd package query-activities` untuk HOME: `com.amazon.tv.launcher/.ui.HomeActivity_vNext` pada prioritas 950,
  `com.amazon.firehomestarter` pada 1. Launcher pihak ketiga yang dipasang dengan `android:priority="1000"` pada
  filter HOME-nya tetap duduk di 0. Fire OS mengabaikan prioritas aplikasi non-sistem.
- `cmd package set-home-activity com.gathin.tvhome/.MainActivity` mencetak `Success`, dan HOME tetap mengarah
  ke launcher Amazon.
- `pm disable-user --user 0` pada `com.amazon.tv.launcher`, pada komponennya `HomeActivity_vNext`, dan pada
  paket home Fire OS yang lain ditolak: `Cannot disable a protected package`.
- Menyadap tombol Home dengan AccessibilityService tidak berhasil: `onKeyEvent` tidak menerima apa-apa untuk
  tombol yang disuntikkan (`input keyevent KEYCODE_HOME`) maupun untuk jendela 40 detik tekanan remote.

Debloat: `tools/disable.sh examples/amazon-fire-tv-aftdec012e/debloat.txt` menonaktifkan telemetri, ACR, belanja, dan
paket tutorial. Urungkan dengan `tools/restore.sh examples/amazon-fire-tv-aftdec012e/debloat.txt com.amazon.tv.launcher`.

## Remapping the remote's app buttons without a key helper

Proses milikmu sendiri bisa mengambil tombol yang menjadi alasan pembantu ada, tanpa mengikat layanan aksesibilitas,
dan pengulangan otomatis tombol yang dikorbankan pembantu tetap ada: layanan penyaring tombol mendorong setiap tombol lewat jalur
saringan yang menjatuhkan pengulangan, sementara membaca perangkat masukan remote membiarkan masukan Android apa adanya.
Ia harus berjalan sebagai pengguna `shell`, karena aplikasi biasa tidak bisa membuka `/dev/input/event*`, jadi ini bukan
sesuatu yang bisa ditumbuhkan launcher — ini skrip dan pengawas di TV. Di Google TV Streamer
(`kirkwood`, Google TV 14, `UTTK.260317.003`) itu satu-satunya cara punya keduanya: `HomeKeyService` saja,
`InputKeyService` saja, dan keduanya bersama-sama masing-masing menghentikan tanjakan setelah kira-kira enam langkah, dan tanjakan kembali
begitu layanan dilepas ([#796](https://github.com/Gathin23/cleanyourtv/issues/796)).

Remote Streamer muncul sebagai `Google TV Remote Keyboard`, dan ini tombol yang dikirimnya:

| Button | HID usage | Android keycode |
| --- | --- | --- |
| YouTube | `0x000c0077` | `BUTTON_3` (190) |
| Star | `0x000c0186` | `MACRO_1` (313) |
| Netflix | `0x000c0078` | `BUTTON_4` |

Cari node menurut nama, bukan nomor: `tools/tv.py 'getevent -pl'` mencantumkan setiap perangkat masukan dengan namanya
dan kode tombol yang dideklarasikannya, dan `/dev/input/event<N>` tempat remote mendarat tidak stabil antar boot,
jadi pengawas yang dipaku ke `event5` berhenti bekerja setelah reboot berikutnya dan terbaca sebagai tombol mati lagi.

Pembaca cannot consume the press. `getevent` mengamati peristiwa dan Android tetap menyerahkannya ke
apa pun yang menjawabnya hari ini, jadi aksi bawaan tombol yang dipetakan ulang juga jalan: petakan tombol Netflix dan
Netflix starts too, lalu duduk di tumpukan aktivitas. Pembantu mengonsumsi tombol yang dipetakannya — makanya
penerima vendor di bullet "Which keycode" di atas tidak pernah melihatnya — jadi tombol yang harus kamu
tekan, bukan digandakan, tetap butuh pembantu dan tukar pengulangannya.

Yang menjalankannya lagi setelah reboot adalah bagian yang tidak gratis. Pengguna `shell` membaca
`/dev/input/eventX` di unit ini tanpa root dan skrip yang disimpan di `/data/local/tmp/` berjalan, tetapi tidak ada
yang memulai dirinya sendiri di sana: Android tidak memberi pengguna `shell` cron atau kait init tanpa root, jadi pengawas
hilang after every reboot dan harus dimulai lagi dari sesi debugging yang kamu tutup sesudahnya. Dua
hal gagal diam-diam saat kamu melakukannya: skrip harus detached from the `adb shell` session atau ia mati bersamanya,
dan pengawas tidak boleh memulai salinan kedua. Jangan menyelesaikan reboot dengan membiarkan TCP ADB nyala supaya
sesuatu yang lokal bisa memulai ulangnya — port 5555 yang terjangkau dari LAN adalah shell di TV untuk apa pun di
jaringan, makanya langkah penutup di [README](../README.md#questions-people-ask) mematikannya.

## A key helper is bound and a held remote button stopped repeating

Tahan volume naik atau turun dan volume bergerak one step per press instead of ramping. Remote bisa
baik-baik saja, begitu juga debloat: tanjakan kembali begitu tidak ada layanan aksesibilitas penyaring tombol yang terikat,
dan `getevent -lt` menunjukkan apakah pengulangan masih datang dari remote selagi tombol ditahan. Di
TCL 55P71B Pro (`G09`, Google TV 12, `STT2.230203.001.AS23`) ini semua atau tidak sama sekali — `HomeKeyService`
sendiri, `InputKeyService` sendiri, dan keduanya bersama-sama semuanya merusaknya, dengan `settings put secure
enabled_accessibility_services ''` plus `accessibility_enabled 0` memulihkannya. Dump menampilkan
`capabilities=8` (`CAPABILITY_CAN_FILTER_KEY_EVENTS`), jadi platform merutekan setiap tombol lewat jalur saringan
yang menjatuhkan pengulangan, sebelum aplikasi melihatnya; pembantu milik launcher mengembalikan `false` untuk tombol
yang tidak mereka petakan, jadi mereka tidak mengonsumsi tombol volume. Yang sama dilaporkan di Xiaomi MiTV-MOSR4
(`irobot`, Android 11, `RTM5.220609.003.1265`) dengan pembantu milik launcher, keduanya dan `InputKeyService`
sendiri (#944), di mana empty list was not tried karena itu juga melepas tombol yang menjadi alasan pembantu
dipasang. onn 4K Streaming Box (`YOC`, Google TV 14, `URO4.260304.011.B1.15051976`) melakukan yang sama dengan
`HomeKeyService` sendiri; di sana `key_filter` false (`config.xml` atau theme) membawa pengulangan kembali dengan pembantu
tetap terikat, jadi pembantu yang dipertahankan untuk sesuatu selain tombol tidak harus mengorbankannya. Cek keadaan dulu, jangan
menulis ulangnya buta:

```sh
tools/tv.py 'settings get secure enabled_accessibility_services'
tools/tv.py 'settings get secure accessibility_enabled'
tools/tv.py 'dumpsys accessibility'
```

Ini tukaran, bukan cacat yang harus diperbaiki: tombol yang menjadi alasan pembantu ada (tombol gir yang mengirim
`KEYCODE_NOTIFICATION` 83, tombol aplikasi yang mengirim keycode vendor seperti 4062) dijawab oleh home
bawaan, jadi di set yang daftarnya menonaktifkan launcherx hanya pembantu yang bisa menjawabnya. Pertahankan pengulangan dan
pasang tanpa `INPUT_KEY_SERVICE=1`/`HOME_KEY_SERVICE=1`, atau pertahankan tombol dan terima satu langkah per
tekanan, atau biarkan launcherx aktif dan jangan ikat apa pun, atau biarkan layanan terikat dan hentikan penyaringan:
`"key_filter": false` di `config.json` theme — bool yang sama di `config.xml` untuk build ulang — membuat
mereka menjatuhkan `FLAG_REQUEST_FILTER_KEY_EVENTS` saat jalan, jadi tanjakan kembali dan tombol yang mereka
tangani mati, tukaran yang sama tanpa penulisan `settings`. Tulis kembali string persis yang dicetak pembacaan
pertama; daftar kosong juga unbinds TalkBack. Pembagian dua, keycode, dan batas:
[catatan G09](devices/tcl-55p71b-g09-key-repeat.md).

## Satu aplikasi menolak diputar sementara TV yang lain daring

Private DNS pemblokir iklan adalah tambahan yang umum dan resolver yang menjawab bukan bukti pemutaran
tidak terpengaruh. Pemutar juga memanggil domain pengukuran dan analitik, dan blok di sana kembali sebagai
galat milik aplikasi sementara TV tetap daring. Di Master-G MGA3200 (`ATV R1`, `R1`, SCBC, Realtek,
Android TV 9, `PTT1.201001.001`) dengan `dns.adguard-dns.com` dalam mode hostname, Max (`com.wbd.stream`)
mencatat `ERR_CONNECTION_REFUSED` untuk endpoint Braze dan Conviva-nya dan macet, sementara setiap aplikasi lain
terputar dan pemeriksaan konektivitas milik Android lolos (#1060). Memblokir `pixel.spotify.com` menurunkan Spotify
dari Freebox Player POP (`examples/freebox-player-pop`) dengan cara yang sama. Jadi kalau satu aplikasi gagal dan yang
lain terputar, kembalikan modenya sebelum men-debug aplikasi, lalu coba judul yang sama sekali lagi — di
MGA3200 itu membersihkan galatnya:

```sh
tools/tv.py 'settings put global private_dns_mode opportunistic'
```

Mode kegagalan yang lain terbaca berbeda: kalau TV sendiri bilang "No internet", resolvernya mati
atau tidak terjangkau, dan perintah yang sama memulihkannya. Apa yang dilakukan resolver opsional, dan berapa biayanya,
ada di README pada "Can it block ads inside apps too?".

Saat melaporkan bagaimana sebuah aplikasi berjalan di TV-mu, sebutkan sampai mana ia sampai, karena setiap langkah bisa gagal
sendiri: terpasang, terbuka, bisa dipakai dengan remote fisik, masuk akun, dan memutar judul dengan suara.
Sampai di layar masuk bukan pemutaran. Kutip kode galat penuh aplikasi (Netflix `UI-800-3`,
misalnya) alih-alih "tidak bekerja", karena itu yang dicari halaman bantuan aplikasi itu sendiri (#1008).
