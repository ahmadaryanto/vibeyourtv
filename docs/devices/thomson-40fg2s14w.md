# Thomson 40FG2S14W (Expressluck `nagata`, papan CVTE, Android TV 14): launcher jalan, Home tetap Google TV

Satu sesi ADB dilaporkan pemilik, satu unit ([#1035](https://github.com/Gathin23/cleanyourtv/issues/1035)).
Launcher pengganti terinstal dan terbuka, dock lima aplikasi dan navigasi D-pad jalan. Yang tidak jalan
Home: di papan ini Home tetap Google TV selama launcher bawaan aktif, seperti aturan CVTE minta. Ini **bukan daftar debloat teruji** - enam paket privasi dan feedback
dinonaktifkan dan dibaca kembali setelah reboot, tanpa beberapa hari pemakaian - dan bukan profil Home teruji.

| Field | Observed value |
|---|---|
| Retail model | Thomson `40FG2S14W`, Android's model string `Thomson TV` |
| Maker / ODM | Expressluck (CVTE board), device `nagata` |
| System | Android TV 14 (SDK 34) |
| Firmware | build `UKNV.260317.001` |
| Stock Home | `com.google.android.apps.tv.launcherx` |
| CVTE components | `com.cvte.tv.at` present, so the board counts as CVTE for the [rule in PROMPT.md](../../PROMPT.md) even though its stock launcher is Google's |

## Home

- `cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity` mencetak `Success`, dan resolver Home
  tetap memilih `com.google.android.apps.tv.launcherx`: tekan Home membuka Google TV.
- Menonaktifkan `com.google.android.tungsten.setupwraith` sendiri tidak mengubah itu; resolver tetap memilih
  `launcherx`.
  Disabling `com.google.android.tungsten.setupwraith` on its own did not change that; the resolver still picked
  `launcherx`. (Before disabling that package on any Google TV set, read
  `dumpsys wifi | grep -o 'cname=[^ ]*'`: a network saved during first setup belongs to it and goes with it.)
- Peran Home sistem melaporkan `com.gathin.tvhome` sementara Home tetap membuka Google TV, jadi pemegang peran bukan
  resolver. Apa yang Home lakukan: `cmd package resolve-activity --brief -a android.intent.action.MAIN -c
  android.intent.category.HOME`, yang juga dibaca Home guard `tools/disable.sh`.
- Serah terima sendiri tidak rusak di firmware ini: `launcher/build-and-install.sh`, yang memulai launcher
  baru dan menonaktifkan bawaan, memang meninggalkan launcher sebagai Home ter-resolve. Di papan CVTE tanpa
  profil teruji launcher bawaan tetap aktif ([set JVC yang hang di animasi boot dengan ADB
  hilang, #741](../TROUBLESHOOTING.md)), jadi `setupwraith` dan launcher bawaan keduanya diaktifkan lagi dan Home
  kembali ke Google TV.
- Belum dicoba, dan yang sesi berikutnya di papan ini butuhkan: cek `FallbackHome` dan direct-boot-aware di
  [TROUBLESHOOTING.md](../TROUBLESHOOTING.md) yang memutus apakah launcher bawaan bisa dinonaktifkan tanpa
  boot yang tidak selesai, dan apakah Home bertahan reboot setelahnya. Sampai seseorang menjalankannya di papan
  persis ini, anggap set hanya debloat.

## Dinonaktifkan, dan dibaca kembali setelah reboot

`pm disable-user --user 0`, dibaca kembali setelah reboot, tidak ada uninstall:

- `tv.anoki.acr.anokiacroptin` - pengenalan konten otomatis Anoki.
- `com.android.adservices.api`, `com.android.ondevicepersonalization.services`,
  `com.android.federatedcompute.services` - layanan iklan Privacy Sandbox Android 14 dan ML on-device.
- `com.google.android.feedback`, `com.android.tv.feedbackconsent` - pengunggah laporan bug dan dialog
  persetujuan TV untuk itu.

## Sengaja tetap aktif

Aplikasi streaming pemilik, asisten suara, Chromecast, Miracast dan updater firmware. Enam paket
di atas seluruh perubahan; catatan ini tidak membawa daftar di luar itu.

## Apa yang catatan ini tidak membuktikan

- Satu unit, satu firmware, satu sesi: tidak ada cold boot, tidak ada reboot kedua setelah upaya Home, tidak ada screenshot,
  dan tidak ada cek HDMI, Live TV, casting, suara atau remote fisik.
- Hasil Home milik `UKNV.260317.001` di `nagata`. Papan yang launcher bawaannya
  `com.cvte.tv.launcher.*`, atau build lebih baru, bisa menyerahkan Home berbeda - dan laporan pemilik di set
  semacam itu layak jadi bagian di sini alih-alih salinan paragraf ini.
- Tidak ada folder `examples/` untuk model ini: daftar butuh beberapa hari pemakaian dulu (CONTRIBUTING).
- ADB dimatikan setelah sesi.
