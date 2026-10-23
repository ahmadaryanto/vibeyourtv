# Menguji TV Hub

## Otomatis (jalan di komputer mana pun, dan di CI)

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

- `M3uParserTest`: atribut, koma dalam tanda kutip, `#EXTGRP`, `#EXTVLCOPT`, skema dilewati, BOM dan CRLF, input kosong dan non-playlist.
- `XmltvParserTest`: format waktu dan offset, filter wanted-channel, jendela waktu, cocokkan display-name, waktu rusak, entitas, panduan kosong.
- `ChannelMergerTest`: merge hanya by `tvg-id`, merge off, key stabil untuk duplikat, id panduan by name, urutan nomor saluran.
- `ProvidersTest`: alamat Dispatcharr, lineup HDHomeRun (DRM dilewati, transcode), CRC paket discovery, redaksi log.

Cek card masuk APK: `aapt2 dump xmltree app/build/outputs/apk/debug/app-debug.apk --file res/xml/module_card.xml`
harus menampilkan `label="TV Hub"`, `caption="Live channels"`, `spec=1` dan `accent=#ff4fa3e0`.
Ini tidak membuktikan apa pun di TV; sisa halaman ini yang membuktikan.

## Sumber demo

```bash
python3 tools/make_fixtures.py <this computer's LAN IP>
python3 -m http.server --directory fixtures/out 8000
```

| Address | Yang diuji |
| :- | :- |
| `http://IP:8000/demo.m3u` | Enam entri: satu stream HLS jalan (Big Buck Bunny, CC BY), stream 404, stream fallback, saluran tanpa panduan, host unreachable, dan entri `udp://` dilewati. Header menyebut `demo.xml` |
| `http://IP:8000/backup.m3u` | Berbagi `tvg-id` dengan saluran 3: merge on, 3 gagal di demo dan main dari sini |
| `http://IP:8000/demo.xml` | Listing tiga saluran dari satu jam lalu sampai dua hari ke depan (regenerasi tiap hari) |
| `http://IP:8000/empty.m3u` | Playlist kosong: sumber error, yang lain tidak rusak |
| `http://IP:8000/invalid.xml` | Panduan rusak: "The guide is not valid XMLTV", saluran tetap |

Untuk stream yang tidak keluar jaringan, file HLS atau MPEG-TS lewat http juga jalan, misalnya
`ffmpeg -re -f lavfi -i testsrc2=size=1280x720:rate=25 -f lavfi -i sine -c:v libx264 -c:a aac -f hls -hls_time 4 -hls_list_size 6 -hls_flags delete_segments fixtures/out/local.m3u8`.

## Di TV

Pasang: `adb install -r app/build/outputs/apk/debug/app-debug.apk`. Catat model TV dan versi Android tiap run.

### 1. Pasang dan launch pertama
- [ ] TV Hub muncul di baris app (Android TV / Google TV) atau Apps > Your Apps (Fire TV) dengan banner.
- [ ] Launch pertama: halaman welcome; **Skip for now** buka Home dengan "No sources yet".
- [ ] Settings > Run setup again membuka welcome lagi.

### 2. Sources
- [ ] Tambah `demo.m3u`; **Check** lapor "Found 5 channels, 1 skipped and a guide address".
- [ ] Setelah simpan, Sources menampilkan 5 saluran, "1 entries skipped", dan waktu.
- [ ] Alamat salah (`http://IP:8000/nope.m3u`) menampilkan "Nothing at that address (HTTP 404)" di Check dan, jika disimpan, merah di Sources.
- [ ] `empty.m3u` error; `invalid.xml` sebagai sumber XMLTV "not valid XMLTV"; saluran demo tetap.
- [ ] Hentikan http.server dan tekan Refresh: sumber error, saluran dari refresh terakhir tetap.
- [ ] Matikan sumber: salurannya hilang dari Live TV dan Guide. Nyalakan lagi: kembali.

### 3. Guide dan daftar
- [ ] Guide: baris saluran 1–5; 1–3 punya listing, 4 "No guide information", garis now di waktu sekarang.
- [ ] Up/Down pindah saluran, Left/Right antar programme; Left dari programme pertama kembali ke menu.
- [ ] OK pada programme on air memutar; OK pada yang later buka detail Watch dan Favorite.
- [ ] Hold OK: bintang muncul; halaman Favorites dan chip Favorites mencantumkannya.
- [ ] Live TV: kategori (All, Favorites, Demo, Sport, Films), cari "spo" temukan Demo Sport, angka "3" juga.
- [ ] Home: Recently watched terisi setelah play; baris Favorites; baris Apps jika app dipilih.

### 4. Player
- [ ] Saluran 1 main dalam beberapa detik dengan spinner; banner now/next.
- [ ] Up/Down dan Channel +/- ganti saluran; Left kembali ke sebelumnya; ketik 4 tune saluran 4.
- [ ] Saluran 2 (404) "The stream was not found…" atau "refused it"; OK retry; Up lanjut.
- [ ] Saluran 5 (unreachable) retry, lalu "Can't reach the stream's server".
- [ ] Dengan `backup.m3u` dan merge on, saluran 3 "Trying …backup" dan main.
- [ ] Menu (atau hold OK): daftar audio dan "Subtitles: off".
- [ ] Back kembali ke halaman awal. Tombol Home saat playback, lalu `adb shell dumpsys media.audio_flinger | grep -i tvhub` atau `top`: playback TV Hub tidak lanjut.

### 5. Provider butuh hardware
- [ ] HDHomeRun: **Scan the network** temukan tuner; saluran main (centang H.264 di EXTEND jika ada suara tanpa gambar).
- [ ] Dispatcharr: alamat server dan profil output; jika jaringan TV tidak diizinkan, pesan 403.
- [ ] App streaming: app dikenal terpasang pre-tick; "Show every TV app" daftar sisanya; pintasan Home buka app; uninstall app hilang dari Home.

### 6. Module card VibeYourTV
Di TV dengan VibeYourTV (`com.gathin.tvhome`) atau fork yang mempertahankan kategori card:

```bash
adb shell pm dump io.github.maxhayim.tvhub | grep -B2 -A6 DOCK_CARD
adb shell input keyevent KEYCODE_HOME
adb logcat -d -s TvHomeCards
```

- [ ] Prompt menyebut TV Hub, paketnya, dan dua belas hex pertama key. **Keep hidden** biarkan tile biasa.
- [ ] Hold OK Settings > Module cards: set TV Hub Shown; card mengganti tile, "TV Hub · Live channels" fokus, glow biru di dock.
- [ ] OK pada card buka TV Hub; hold OK buka App info.
- [ ] `adb logcat -d -s TvHomeCards` tanpa baris rejection untuk io.github.maxhayim.tvhub.
- [ ] Uninstall: card hilang; pasang build sama: tanpa prompt baru.

### 7. Platform
| Check | Android TV | Google TV | Fire TV |
| :- | :- | :- | :- |
| Installs from adb | | | |
| Banner in the app list | | | |
| D-pad, Back, Menu work | | | Menu is the ≡ button |
| Text entry opens the TV keyboard | | | |
| HLS demo plays | | | |
| 720p and 4K output: text readable, nothing cut off | | | |

### 8. Hardware terbatas
Di TV 1 GB: tambah playlist ribuan saluran dan panduan XMLTV penuh; ukur refresh;
scroll guide dengan Down tahan: tetap responsif tanpa TV hang. `adb shell dumpsys meminfo io.github.maxhayim.tvhub`
setelah scroll harus jauh di bawah 150 MB total PSS. Settings > Animations off untuk TV lambat.

## Hasil sejauh ini

### 2026-10-22, MOKA R8G (Realtek, Android 14, output 1080p, RAM 2 GB), debug build (Media3 1.5.1, SDK 35; add-on build dengan 1.4.1 dan SDK 34), launcher: home custom tanpa dukungan module-card

Lulus di TV:
- Pasang lewat adb; launch pertama welcome; form M3U dengan keyboard TV; Check "Found 5 channels, 1 skipped and a guide address".
- Header guide (`x-tvg-url`) terbaca; grid Guide dengan logo, garis now, listing, baris "No guide information"; Left/Right/Up/Down pertahankan waktu kursor; detail programme untuk programme later.
- Playback HLS (H.264 + AAC, Media3 READY ~3 d), channel up/down, entry angka, saluran sebelumnya, menu audio/subtitle, Back melepaskan player (`ExoPlayerImpl: Release`).
- Error: pesan stream 404; host unreachable menyerah ~20 d (dulu 62 d sebelum batas retry).
- Merge dan fallback: stream mati saluran 3 gagal dan main dari sumber kedua ~3 d later.
- Favorit dengan hold OK di guide; halaman Favorites dan baris Home; cari by name dan number.
- App streaming: enam app terdeteksi lewat `<queries>` (Pluto TV, Tubi, Plex, YouTube TV, Sling TV, Philo), banner sendiri "Opens the app".
- Upgrade database schema 1 ke 2 pada copy terpasang.
- Memori: ~54 MB PSS saat browsing.

Diperbaiki run ini: guide terhapus saat sumber gagal refresh; fokus ganti halaman setelah kembali dari editor; teks card di belakang logo; fokus zoom terpotong; teks error 404; plural status playlist.

Dicek lagi hari itu dengan build add-on (Media3 1.4.1, SDK 34): launch pertama, tambah demo playlist, playback HLS (READY ~3 d), pesan HTTP 404, player release on Back.

Belum diuji: module card (launcher TV ini tidak baca card), HDHomeRun dan Dispatcharr (tanpa hardware/server), Fire TV, Google TV, mode output 720p dan 4K, playlist dan guide nyata besar.
