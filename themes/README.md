# Tema

Tema adalah folder berisi `config.json` dan, kalau mau, gambar: `wallpaper.jpg` atau koleksi `wallpapers/`.
Push tema mengubah dock, layout, dan tampilan launcher di TV tanpa build ulang APK: launcher membaca
`config.json` sebagai overlay di atas `config.xml` bawaan saat startup berikutnya. Pilihan di Launcher
settings menang sampai kamu pilih Follow theme. Push tema atau `tools/theme.sh --reset` juga menghapus
pilihan kolom dan motion yang disimpan di TV.

```bash
TV_HOST=192.168.1.50 tools/theme.sh themes/aurora          # push a theme and restart Home
TV_HOST=192.168.1.50 tools/theme.sh --reset                # back to the built-in config.xml
powershell -ExecutionPolicy Bypass -File tools\theme.ps1 -TvHost 192.168.1.50 themes\aurora
```

| Theme | Fungsinya | Screenshot |
|---|---|---|
| `default/` | Tampilan bawaan ditulis sebagai JSON, titik awal buat tema sendiri | <img src="default/screenshot.jpg" width="320" alt="default theme"> |
| `aurora/` | Wallpaper aurora gelap (teal dan violet di navy, lebih gelap di bawah) dengan warna latar selaras | <img src="aurora/screenshot.jpg" width="320" alt="aurora theme"> |
| `dussehra/` | Kulit festif Dussehra (berakhir 21 Okt 2026): toran marigold, rangoli emas samar, diya untuk Wi-Fi, lonceng Inputs, marigold Settings, hujan kelopak | <img src="dussehra/screenshot.jpg" width="320" alt="dussehra theme"> |
| `glass/` | Wallpaper navy gelap cahaya teal/violet lembut, rak atas cinematic, dock kaca buram, tombol icon-only, glow fokus warna app, glide tanpa overshoot | <img src="glass/screenshot.jpg" width="320" alt="glass theme"> |
| `ruska/` | Senja musim gugur Finlandia ("ruska") di danau: garis pantai spruce, birch warna gugur, daun jatuh, kode `make_wallpaper.py`. Glow warna app tidak menutupi foto; dock 70% opaque, danau terlihat; glow fokus warna app tanpa overshoot | <img src="ruska/screenshot.jpg" width="320" alt="ruska theme"> |
| `scenes/` | 14 lanskap ilustrasi gelap di `wallpapers/`, berganti tiap jam; kode `tools/scenes_wallpapers.py` | <img src="scenes/screenshot.jpg" width="320" alt="scenes theme"> |
| `six-columns/` | Enam tile kecil per baris, satu baris grid terlihat di bawah dock | <img src="six-columns/screenshot.jpg" width="320" alt="six-columns theme"> |
| `sweden/` | Tiga foto Swedia dari Wikimedia Commons (aurora Abisko, Vasabron senja, Skeppsbron malam), berganti tiap jam dengan tempat dan fotografer di bawah tombol; enam tile kecil dock kaca buram, tombol bulat icon-only, tanpa hero name. Foto CC BY / CC BY-SA, bukan MIT: `sweden/CREDITS.md` | <img src="sweden/screenshot.jpg" width="320" alt="sweden theme"> |

Setiap folder tema punya `screenshot.jpg` Home dengan tema itu di TV asli. `tools/theme.sh` tidak pernah
push file itu; hanya untuk preview sebelum kamu coba.

## Keys

Semua key opsional; key yang hilang memakai nilai dari `config.xml`, key asing atau tipe salah diabaikan,
dan file yang tidak bisa di-parse diabaikan seluruhnya, jadi tema tidak bisa merusak Home.

Setiap key punya (atau menunggu) screenshot di TV asli di [`docs/switches.md`](../docs/switches.md).
Key baru butuh screenshot di sana dalam PR yang sama.

| Key | Type | Arti |
|---|---|---|
| `dock_apps` | list of package names | Baris dock, berurutan; app yang tidak ada dilewati |
| `fill_dock` | bool | Isi slot dock kosong dari `dock_apps` dengan app grid pertama (default true); false biarkan kosong |
| `hidden_apps` | list of package names | Tidak pernah ditampilkan sebagai tile |
| `live_tv_package`, `live_tv_action` | string | App Live TV dan, jika tidak punya entry leanback, action untuk membukanya |
| `live_tv_input_id` | string | Tuner sistem persis untuk baris Live TV menu Inputs; membuka koleksi salurannya. Kosong = pilihan otomatis. Input hilang atau koleksi tidak didukung kembali ke launch normal app. [contoh satelit HAAM](../docs/devices/haam-r4.md) |
| `source_panel_service`, `source_panel_action` | string | Panel sumber TV sendiri, untuk set tanpa TvInputManager: service atau activity (BYINTEK U4). Tanpa app Live TV, pill Inputs membukanya langsung |
| `source_panel_extra` | `"name=value"` | Satu int extra dikirim dengan panel sumber (Coocaa OS: `control_center_command=100`) |
| `coocaa_sources` | bool | Coocaa OS (Skyworth, China): cantumkan sumber dari `com.skyworth.tv` di menu Inputs, sebelum panel sumber |
| `launch_actions` | list of `"package=action"` | Tile membuka layar app, bukan start screen; action yang tidak resolve lagi membuka app normal (panduan MagentaTV: `com.telekom.onetv.tv.pl=com.telekom.onetv.tv.pl.action.PROGRAM_GUIDE`) |
| `app_labels` | list of `"package=Name"` | Nama tile menggantikan nama app (MagentaTV sebagai `com.telekom.onetv.tv.pl=TV`) |
| `app_banners` | list of `"package=banner_name"` | Artwork tile dari drawable `banner_*` di launcher (`banner_tv`: magenta dengan glyph Live TV) |
| `input_action` | string | Action dikirim dengan URI saluran baris HDMI/AV jika tidak ada yang menangani `VIEW` standar (XGIMI: `com.xgimi.action.hdmiPlayer`). Hanya set tanpa handler; app yang klaim MIME saluran mengambil baris — cek `query-activities` ([#805](https://github.com/Gathin23/cleanyourtv/issues/805)) |
| `input_uri_scheme` | string | Skema URI passthrough baris HDMI/AV selain `content://`, tanpa MIME (YaOS: `live-tv`, dijawab source player-nya) |
| `input_sources` | list, `config.xml` only | Nomor sumber vendor untuk menu Inputs, `HW<n>=<number>` dari bagian akhir id input; input terpetakan dibuka dengan `input_action` dan int extra `input`; `HW<n>=-` mengecualikan port yang tidak ada. Firmware Xiaomi/Redmi China: [redmi-mitv-mftp0](../examples/redmi-mitv-mftp0/). Tidak dibaca dari tema |
| `columns` | 1 to 8 | Tile per baris; tile mengecil mulai dari 5 ke atas |
| `rows_below_dock` | 0 to 3 | Baris grid terlihat di bawah dock; tile mengecil jika dock, judul, dan baris itu tidak muat tinggi layar |
| `dock_title` | bool | Nama app dock fokus huruf besar di atas dock (`true`); `false` hanya glow |
| `dock_row` | bool | Dock sebagai baris sendiri dengan band (`true`); `false` = app dock jadi tile pertama grid biasa |
| `reorder_button` | bool | Tombol reorder di bar atas (default `false`); reorder selalu di menu long-press Settings |
| `show_network_pill` | bool | Pill Wi-Fi/Ethernet di bar atas (default `true`); `false` menyembunyikannya |
| `show_inputs_pill` | bool | Pill Inputs di bar atas, tampil jika TV punya input (default `true`); `false` menyembunyikan; dengan `top_menu` Inputs tidak ada di menu gear (streamer tanpa HDMI, mis. Chromecast with Google TV); tombol Input remote tidak berubah |
| `icon_only_buttons` | bool | Wi-Fi dan Settings tombol bulat icon-only; Inputs tetap berlabel (default `false`) |
| `icon_only_inputs` | bool | Pill Inputs juga tombol bulat; label tetap sebagai deskripsi TalkBack (default `false`). Bisa dengan/tanpa `icon_only_buttons` |
| `top_menu` | bool | Inputs, Wi-Fi, Settings digabung satu gear bulat di tengah header: OK buka menu tiga itu plus Reorder apps, hold OK menu launcher (default `false`) |
| `launcher_settings_button` | bool | Aksi Settings header membuka Launcher settings langsung dengan ikon home 22dp; mengubah baris Settings saat `top_menu` on. Hold OK tetap module-card dan ordering. Halaman settings mencakup Reorder apps dan TV settings (default `false`) |
| `wifi_status_color` | bool | Ikon pill Wi-Fi hijau saat online, putih saat offline (default `false`) |
| `wifi_label_width` | 0 to 300 | Dengan `icon_only_buttons`, nama jaringan di pill Wi-Fi sampai lebar dp ini, dipotong ellipsis; 0 = hanya ikon |
| `library_button` | bool | App library sebagai tombol bulat di bar atas, di samping Settings; butuh `app_library`. Tutup library kembali ke sana |
| `always_show_names` | bool | Nama di bawah setiap tile, redup sampai fokus, termasuk dock (band membesar); default `false` hanya nama tile fokus |
| `wallpaper_true_color` | bool | Decode wallpaper 32-bit (8 MB di 1080p) bukan RGB_565 (4 MB) yang banding gradasi gelap; untuk TV 2 GB+ (default `false`) |
| `app_library` | bool | Pill App library setelah baris Home terakhir: semua app terpasang, tambah/hapus dari Home (default `false`). Pilihan tetap di TV saat push/reset tema; lihat [docs/app-library.md](../docs/app-library.md) |
| `app_library_selection` | bool | OK toggle centang Home di library; app tercentang dulu, keduanya alfabet. Sembunyikan fokus ke app berikutnya (sebelumnya di akhir). Butuh `app_library`; `false` = perilaku open/long-press standar |
| `library_apps` | list of package names | Paket pre-seed ke set "added" App library saat startup (`library_apps` di config.xml atau `config.json` tema). Tampil di Home seolah ditambah lewat UI library, tetap bisa dihapus (yang dihapus tetap hidden); kosong (off) default |
| `shamsi_date` | bool | Tampilkan tanggal Hijriah di atas Gregorian |
| `greeting_name` | string | Nama setelah salam, mis. `"Mr. Trịnh"` untuk GOOD MORNING, MR. TRỊNH; kosong = salam saja |
| `module_cards` | bool | Cari module card dan tanya (`true`); `false` matikan fitur |
| `reduced_motion` | bool | Gerak lebih tenang untuk GPU lemah: highlight slide tanpa bounce, settle ≤5 frame, animasi ≤90 ms, nama/rak/glow/highlight muncul bukan fade, tanpa entrance tile (default `false`) |
| `boot_to_live_tv` | bool | Buka app Live TV (`live_tv_package`) di Home pertama setelah TV nyala, sekali per boot |
| `focus_overshoot` | bool | Highlight dan tile fokus overshoot sebelum settle (default `true`); `false` glide lurus dengan scroll halus |
| `vpn_app` | string | App yang dibuka pill jaringan jika VPN dikonfigurasi; badge merah "VPN off" saat VPN mati |
| `animations` | bool | Gerak fokus, glass, judul, entrance tile dasar; default true |
| `advanced_effects` | bool | Efek opsional tile terangkat, zoom ikon, warna ambient halus, buka app; default false |
| `show_home_settings_shortcut` | bool | Pertahankan tile Default-home opsional jika TV punya home picker; default false |
| `show_stats` | bool | Satu baris di bawah jam: CPU, memori bebas, storage bebas. Off kecuali di-set |
| `crypto_ticker` | bool | Harga BTC live di bawah tanggal, dari CoinGecko, diperbarui setiap 5 menit selama Home di layar. Off kecuali di-set (default `false`). Launcher meminta `INTERNET` hanya agar baris ini bisa load |
| `handle_view_inputs` | bool | Jawab input switcher sistem (`VIEW_INPUTS`, tombol Input remote) dengan menu Inputs, untuk set yang stock launcher satu-satunya handler (Hisense huangshan); default `false` agar TV dengan picker sendiri tidak dapat chooser |
| `background_color` | `#RRGGBB` | Warna di belakang semua; default hitam |
| `sky_intensity` | 0 to 100 | Backdrop waktu: gradasi gelap mengikuti jam, 0 menyembunyikan (default) |
| `wallpaper` | bool | Tampilkan gambar tema, `wallpaper.jpg` atau koleksi `wallpapers/` (`true`), atau tidak ada (`false`) |
| `wallpaper_rotation_minutes` | 0 to 1440 | Putar gambar di folder `wallpapers/` tema pada interval ini; 0 = satu gambar. Nilai negatif/non-numerik diabaikan; 0 = tidak rotasi, bukan setiap menit |
| `wallpaper_order` | `shuffle` or `sequential` | Cara koleksi maju. `shuffle` (default) tidak mengulang gambar saat ini; `sequential` urut nama file dan wrap. Nilai asing kembali ke `shuffle` |
| `wallpaper_dim` | 0 to 90 | Overlay hitam di atas wallpaper, persen; default 0 tanpa shade |
| `dock_opacity` | 30 to 100 | Opacity band dock, persen, agar wallpaper tembus; default 100 (rak cinematic band sendiri). Launcher settings di TV: 100, 70, 50, 30, menang sampai push tema berikutnya atau Follow theme |
| `wallpaper_captions` | bool | Dengan koleksi rotasi, nama gambar di bawah tombol kanan atas, dari `wallpapers/captions.txt` (`001.jpg<TAB>Dusk over the peaks`, satu per baris); tanpa baris = tanpa nama (default `false`) |
| `wallpaper_moods` | object | Koleksi rotasi per waktu: `{"morning": [...], "evening": [...], "night": [...]}` dengan nama file dari `wallpapers/`. Pagi sunrise–noon, sore sampai +30 menit sunset, malam sisanya, matahari dari `latitude`/`longitude` config.xml (07:00 dan 18:30 tanpa itu); grup hilang = rotasi semua gambar. Default unset |
| `tile_menu` | bool | Hold OK atau Menu remote pada tile: menu kecil Move, Remove from Home (dengan `app_library`), App info. Default `false` = hold OK shortcut ke App info, Menu tidak berubah |
| `festival` | string | Aktifkan kulit festif: pill jaringan jadi diya, Inputs lonceng, Settings marigold, Home pertama hari itu hujan kelopak. `dussehra` dan `diwali` punya salam bawaan |
| `festival_until` | `YYYY-MM-DD` | Hari terakhir kulit festif; mati sendiri setelahnya. Tanggal rusak = mati |
| `greeting` | string | Ganti salam waktu saat kulit festif aktif |
| `accent_color` | `#RRGGBB` | Warna glow fokus saat kulit festif; emas diya jika tidak di-set |
| `settings_key_keycodes` | list of keycodes | Tombol remote buka Settings, untuk `HomeKeyService` (gear TCL Google TV: `[83]`) |
| `settings_key_panel` | bool | Tombol itu buka panel quick settings (TV settings, Display, Network, Apps, Launcher settings) bukan Settings TV |
| `settings_fallback_components` | list, `config.xml` only | Layar vendor dicoba berurutan jika tidak ada yang menjawab action Settings standar (Xiaomi China, TCL/FFALCON, Fire OS — menu asli di launcher), masing-masing `package/activity`; paket pertama terpasang menang; jika tidak ada, intent standar tetap dikirim (bisa tidak buka apa-apa). Fire OS menjawab dengan stub `CTSDummy`, dianggap hilang sehingga fallback jalan. Paket di sini juga harus di `<queries>` launcher (visibilitas paket Android 11); set dengan app settings di luar daftar butuh build, bukan push tema. Tidak dibaca dari tema |
| `restore_key_services` | bool | Setelah force stop launcher, Home mengembalikan helper accessibility remote (`HomeKeyService`, `InputKeyService`) ke daftar enabled; butuh `RESTORE_KEY_SERVICES=1` saat install. Default off |
| `input_key_keycodes` | list of keycodes | Tombol remote lain buka input switcher, untuk `InputKeyService` (`KEYCODE_TV` dan `KEYCODE_TV_INPUT` selalu) |
| `app_keys` | list of strings | Tombol remote buka app, `"190:com.google.android.youtube.tv"` atau `"KEYCODE_BUTTON_3:..."`, atau scancode `"scan:490:..."` untuk remote satu keycode banyak tombol (Sony BRAVIA), untuk `InputKeyService`/`HomeKeyService`. `"input:<input id>"` ganti paket = switch input TV, mis. HDMI (`dumpsys tv_input`). Prefix `hold:` = long-press, mis. `"hold:190:com.netflix.ninja"`; tanpa prefix = tap |
| `key_filter` | bool | `false` = helper tetap enabled tapi tidak filter key. Untuk firmware di mana service accessibility filter key merusak repeat long-press (TCL Google TV Android 12): tombol remote di atas mati, repeat volume kembali |
| `gamepad_passthrough` | bool | `true` = helper tidak filter key saat gamepad terhubung, game lihat tombolnya (Steam Link + Xbox); tombol remote di atas mati sampai cabut. Butuh `key_filter` on. Remote suara dengan joystick tidak dihitung. Default off |
| `stock_home_redirect` | string | Paket stock launcher; jika firmware hidupkan lagi (XGIMI), home screen-nya dialihkan ke sini (`HOME_KEY_SERVICE=1`) |
| `race_calendar` | list of races | Mode race-weekend F1: dari 12 jam sebelum sesi pertama weekend sampai 4 jam setelah balapan, panel di bawah jam menampilkan sesi berikut + countdown (atau live) dan jadwal weekend zona waktu TV. Tulis dengan `tools/race_calendar.py <theme folder>` (musim dari API Jolpica gratis); launcher sendiri tidak online |
| `home_idle_clock_minutes` | 0 to 1440 | Menit Home idle sebelum screensaver jam (jam/tanggal tipis di atas glow lambat, seperti screensaver "Clock" sistem). Tombol apa pun kembali ke Home. Hanya dari Home, tidak di atas Live TV/app; 0 = off (default) |
| `glow_style` | string | Bentuk glow di belakang app fokus, warna app: `radial` (default) atau `horizon`, warna turun dari tepi atas ke hitam; rak cinematic punya sendiri |
| `cinematic_home` | bool | Rak atas cinematic: artwork app dock fokus (soft) memenuhi atas layar warna app, nama besar, "Last opened", titik posisi dock, glow fokus, band dock translucen; default off |
| `cinematic_dots` | bool | Dengan `cinematic_home`, titik posisi dock di bawah nama app (default `true`); `false` tanpa titik |
| `cinematic_white_focus` | bool | Dengan `cinematic_home`, highlight fokus putih di dock, bukan warna app (default `false`); `app_glow` tetap warnai semua tile |
| `cinematic_scrim` | 0 to 100 | Dengan `cinematic_home`, kekuatan scrim gelap di belakang nama rak (default `100`); lebih rendah = wallpaper tembus |
| `cinematic_art` | 0 to 100 | Dengan `cinematic_home`, opacity artwork app fokus (default `50`); `0` = wallpaper tidak tertutup |
| `dock_even_padding` | bool | Padding band dock = jarak antar tile di keempat sisi, sejauh grid mengizinkan (default `false`) |
| `dock_glass` | bool | Band dock kaca buram terang seperti pill bar atas (default `false`); cocok dengan `sky_intensity` atau wallpaper |
| `ambient_glow` | bool | Glow di belakang dock warna app fokus (default `true`); `false` hanya `background_color` atau wallpaper |
| `app_glow` | bool | Highlight fokus glow warna app di setiap tile, seperti `cinematic_home` di dock (default `false`) |
| `fade_rows` | bool | Baris fade saat scroll ke bawah jam, bukan potong keras; wallpaper/sky tembus. Grid draw ke hardware layer (~6 MB VRAM 1080p) (default `false`) |

## Wallpapers

`wallpaper.jpg` digambar full screen di belakang launcher, centre-crop. Pakai gambar gelap, 1920×1080,
di bawah ~300 KB: teks, hero name, dan band dock didesain untuk hitam, dan TV 1 GB decode gambar ke
memori setiap Home start. Gambar lebih besar dari layar discale; gambar yang masih di atas 4K (~8,4 juta
piksel) setelah sampling tidak ditampilkan, warna polos tetap.

Untuk koleksi wallpaper, taruh file JPEG, PNG, atau WebP di folder `wallpapers/` tema di samping `config.json`, lalu set:

```json
{ "wallpaper": true, "wallpaper_rotation_minutes": 30, "wallpaper_dim": 40, "wallpaper_order": "sequential" }
```

Gambar terakhir diingat di TV, jadi restart/reboot lanjut dari situ, bukan dari gambar pertama mode
`sequential`. Interval terlewat saat TV tidur/cabut listrik tidak memutar ulang gambar yang terlewat:
Home paling banyak maju sekali saat bangun. Ganti koleksi mulai dari gambar pertama koleksi baru.
Koleksi satu gambar ditampilkan sekali dan tidak dikosongkan timer. Gambar centre-crop isi layar 16:9;
dimming overlay hitam di atas gambar, di bawah teks launcher.

Saat tema diterapkan, koleksinya menggantikan yang sudah di TV. Nama file hanya huruf, angka, underscore,
titik, dan strip. Gambar dari sumber apa pun; pastikan izin pakai. Launcher mempertimbangkan
hingga 100 gambar non-kosong, maks. 10 MB each, lewati file tidak terbaca, pertahankan background terakhir yang bagus.
Koleksi kosong fallback ke `wallpaper.jpg`, atau warna latar jika tidak ada gambar.
`"wallpaper": false` mematikan background satu gambar dan koleksi.

Pilihan acak tanpa ulang langsung jika ada alternatif. Gambar saat ini dan waktu ganti survive restart
app/proses. Rotasi hanya saat Home terlihat; kembali dari app lain, background terlambat ganti saat itu.
Gambar didecode di worker thread, dibatasi ukuran layar. Tidak ada layanan unduh, permission Internet,
atau scheduler di komputer. Screensaver idle sistem asli tidak diubah.

Di TV, Launcher settings punya baris Wallpaper: pilih satu gambar dari koleksi (atau `wallpaper.jpg`
tema), atau None. Pilihan menampilkan satu gambar dan menghentikan rotasi sampai Theme default, Follow
theme, atau push tema lagi.

## Reduced motion

`"reduced_motion": true` menjaga tampilan tapi menenangkan gerak dan jauh lebih sedikit frame: highlight
slide tanpa bounce, settle ≤5 frame, setiap animasi (zoom tile, dropdown, app library) ≤90 ms, nama/rak/glow/highlight
muncul bukan fade, tanpa entrance tile. Pakai di TV yang GPU-nya tidak sanggup; MediaTek MT5895 Android 10
kasus yang dikenal. Default `false` = gerak normal.

```json
{ "reduced_motion": true }
```

## Time-of-day backdrop

`"sky_intensity": 60` menggambar gradasi gelap di atas wallpaper dan di belakang semuanya, mengikuti jam:
biru malam, siang biru lembut, horizon hangat puncak jam tujuh malam. Glow dekat tepi bawah bergerak kiri–kanan
mengikuti matahari. Warna tetap gelap agar jam putih, hero name, dan band dock kontras. Satu bitmap 480×270,
digambar ulang tiap menit, tidak animasi, jadi TV 1 GB tidak perlu mengejar. `0` = latar polos; default off
(launcher referensi tampil seperti sebelumnya).

```json
{ "sky_intensity": 60 }
```

Puncak malam itu hari referensi tetap, sunrise 07:30 dan sunset 19:00 menurut jam TV. Launcher mengikuti
matahari nyata hanya jika `latitude` dan `longitude` di-set di
`launcher/app/src/main/res/values/config.xml`, setting build yang tema tidak bisa bawa: lihat
[docs/sky.md](../docs/sky.md) untuk dua baris itu dan fallback yang masih dipakai.

## Menambah tema

Buka PR dengan folder baru di sini berisi:

- `config.json`, tema itu sendiri.
- `screenshot.jpg`, **wajib**: Home di TV asli dengan tema di-push, bukan mock-up atau emulator. Ambil
  dengan `tools/tv.py 'screencap -p /sdcard/s.png'` dan
  `tools/tv.py --pull /sdcard/s.png s.png`, simpan JPEG 1280×720 (16:9) di bawah 300 KB,
  misalnya `sips -z 720 1280 -s format jpeg s.png --out screenshot.jpg` di macOS. Fokus di dock agar glow
  dan hero name terlihat. PR tanpa ini gagal CI (`tests/test_themes.py`).
- `wallpaper.jpg` atau `wallpapers/` jika tema memakainya.

Lalu tambahkan baris ke tabel di atas dengan screenshot, seperti yang lain. Dock generik (streaming
besar) kecuali tema untuk TV tertentu, dan sebut TV uji di deskripsi PR.
