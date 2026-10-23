# AGENTS.md

Panduan untuk agen coding (Claude Code, Codex, Gemini CLI, Cursor, Copilot, dan lainnya) yang kerja di
repo ini, VibeYourTV. Orang mulai dari [README.md](README.md) dan [CONTRIBUTING.md](CONTRIBUTING.md);
file ini mengulang aturan yang sama dalam bentuk yang agen butuhkan, lalu menaut ke sana untuk detailnya.

## Pertama: kamu lagi mengerjakan yang mana?

| Kamu diminta | Lakukan ini |
|---|---|
| Bereskan TV: debloat, pasang launcher, dorong tema | Baca [PROMPT.md](PROMPT.md) dan ikuti persis, di TV sungguhan. Pakai alat di `tools/` dan launcher di `launcher/`, jangan menulis yang baru; satu-satunya file yang perlu kamu ubah adalah `launcher/app/src/main/res/values/config.xml`, atau `config.json` sebuah tema, untuk TV ini. Jangan edit dokumen, tes, atau kode lain di repo, dan jangan buka issue atau pull request kecuali orangnya memintamu. |
| Ubah repo ini: daftar TV, perbaikan, terjemahan, tema, dokumen | Lewati bagian berikutnya dan lanjut dari [Peta repo](#repo-map). |

### Membereskan TV: aturan yang tidak bisa ditawar

[PROMPT.md](PROMPT.md) adalah prosedurnya. Aturan amannya, singkatnya:

- **Nonaktifkan, jangan pernah uninstall.** `pm disable-user --user 0`, satu paket per panggilan shell. Tidak ada
  `pm uninstall`, tidak ada root, tidak ada yang di-flash. Setiap langkah harus bisa dibatalkan `tools/restore.sh`.
- **Cadangkan dulu.** Daftar paket, daftar yang nonaktif, `getprop`, dan settings ke `backup/` sebelum perubahan
  pertama. `tools/disable.sh` juga menulis catatannya sendiri di `~/.cleanyourtv/`; simpan daftar yang kamu terapkan.
- **Biarkan yang TV butuhkan untuk jalan:** aplikasi TV dan layanan input di balik HDMI dan tuner,
  menu input/sumber, pemasangan remote dan Bluetooth, keyboard di layar, Play Store, dan Play
  Services. Kalau nama paket tidak menjelaskan apa-apa, baca `dumpsys package <name>`, lalu tanya.
- **Jangan pernah menonaktifkan paket inti Android:** pengontrol izin, pemasang paket, ext
  services/shared dan metadata modul (`com.android.*` dan `com.google.android.*`),
  `com.google.android.overlay.modules*`, `com.android.providers.*` (selain kalender, kontak, kamus pengguna,
  dan nomor diblokir), `com.android.systemui`, `com.android.shell`, `com.android.phone`,
  `com.android.settings`, `com.android.tv.settings`, `android`. Tanpa mereka Android bisa gagal boot (#884);
  `tools/disable.sh`, `disable.ps1`, dan `check.py --fix` menolak daftar seperti itu, tanpa penimpaan.
- **Jangan pernah menjalankan paket pabrik atau uji** (`com.cvte.tv.hkcfac`, `mediatek.factorymenu.ui`, dan sejenisnya),
  bahkan hanya untuk melihat. Di beberapa papan itu membalikkan bendera mode pabrik yang shell tidak bisa urungkan.
- **Jaga Home guard.** Launcher bawaan baru dinonaktifkan setelah launcher baru terpasang dan
  terkonfirmasi sebagai Home (`dumpsys activity activities | grep mFocusedApp`). Di Google TV, cek
  `dumpsys wifi | grep -o 'cname=[^ ]*'` sebelum menonaktifkan setupwraith: kalau dialah pemilik jaringan Wi-Fi
  tersimpan, menonaktifkannya membuat TV lepas dari Wi-Fi dan ADB. `tools/disable.sh` dan
  `launcher/build-and-install.sh` sudah menegakkan ini. Pakai `FORCE_HOME=1` atau `FORCE_SETUPWRAITH=1` hanya pada
  situasi yang pesan skrip itu sendiri jelaskan, dan hanya dengan persetujuan orangnya.
  - Di papan CVTE (paket `com.cvte.*` mana pun), biarkan launcher bawaannya (`com.cvte.tv.launcher.*`) tetap aktif
    kecuali folder `examples/` untuk papan yang persis itu menonaktifkannya. Jadikan launcher baru sebagai Home dengan
    `cmd package set-home-activity`, reboot sekali, dan pastikan Home serta ADB kembali sebelum hal lain.
    Jangan menghidupkan helper tombol remote di langkah yang sama: sebuah TV JVC menggantung di animasi boot dengan ADB hilang
    setelah keduanya dilakukan sebelum satu reboot (#741).
- **Tanya dulu sebelum menyentuh yang keluarga pakai.** Aplikasi streaming, asisten suara, Chromecast, Miracast, dan
  pembaru firmware masuk satu pertanyaan centang; nonaktifkan hanya yang orangnya tidak centang.
- **Nonaktifkan dalam batch kecil** dan ambil screenshot di antaranya (`tools/tv.py --screenshot shots/01.png`).
  Live TV, HDMI, dan beberapa aplikasi streaming memberi screenshot kosong; itu TV-nya, bukan kerusakan.
- **Tombol yang disuntikkan bukan bukti** untuk tombol remote yang lewat helper aksesibilitas: minta
  orangnya menekan tombol sungguhan. `am force-stop com.gathin.tvhome` mematikan helper itu.
- **Tutup ADB setelahnya.** USB debugging dan Network/ADB debugging mati, otorisasi dicabut, reboot,
  lalu `tools/adb-closed.py <tv ip>` harus melaporkan port tertutup. Port 5555 berupa teks polos.
- **Berhenti dan katakan** saat TV menolak: `pm disable-user` ditolak untuk setiap paket, sideload yang
  gagal verifikasi, atau SDK di bawah `minSdk` 26 launcher. Jangan mengulang dalam putaran, dan jangan turunkan
  `minSdk` atau menyerahkan APK yang dibangun di tempat lain.

Cadangan orang itu (`backup/`), screenshot (`shots/`), catatan (`work/`), dan kunci ADB
(`tools/adbkey/`) ada di `.gitignore` dan tidak boleh pernah di-commit. Kalau orangnya mau
membagikan daftar TV-nya setelahnya, ikuti [`docs/workflows/tv-list.md`](docs/workflows/tv-list.md).

## Repo map

| Path | Apa itu |
|---|---|
| `PROMPT.md` | Prompt yang orang tempel ke agen mereka. Netral merek; aturan aman tinggal di sini. |
| `README.md` | Halaman depan, dengan tabel TV dan tabel Tools. `README.zh-CN.md` dan `README.vi.md` adalah terjemahan. |
| `CONTRIBUTING.md` | Panduan kontributor untuk orang. "Ide saya taruh di mana?" adalah tabel rutenya. |
| `SECURITY.md` | Pelaporan kerentanan secara privat. |
| `SHOWCASE.md` | Proyek yang dibangun di atas repo ini dan tinggal di repositori mereka sendiri. |
| `CODE_OF_CONDUCT.md` | Termasuk aturan jangan memublikasikan pengenal perangkat, nama Wi-Fi, dan IP. |
| `examples/` | Satu folder per model TV yang sudah diuji: `debloat.txt`, kadang `README.md` dan `config.json`. Terindeks di `examples/README.md`. |
| `docs/` | `docs/TROUBLESHOOTING.md`, kontrak kartu modul (`docs/modules.md`), `docs/devices/` untuk model yang ada catatannya tapi belum punya daftar teruji, dan [alur kerja](#workflows). |
| `launcher/` | Launcher acuan, paket `com.gathin.tvhome`: Java polos, AndroidX RecyclerView dan DynamicAnimation, `minSdk` 26, tanpa izin `INTERNET`. |
| `themes/` | Satu folder per tema: `config.json`, `screenshot.jpg` wajib, wallpaper opsional. Kunci didokumentasikan di `themes/README.md`. |
| `addons/` | Aplikasi kartu modul opsional, masing-masing proyek Gradle sendiri, tidak pernah ikut terpasang ke launcher. |
| `tools/` | Klien ADB (`tv.py`) dan skrip-skripnya, tercantum di tabel Tools README. Skrip shell punya kembaran PowerShell. Cara pakai tiap skrip ada di baris pertamanya, dan yang besar punya panduan di sampingnya (`tools/*.md`): baca sebelum menjalankan skrip. |
| `tests/` | Rangkaian `unittest` Python: format daftar, tabel README, jumlah pers, tema, add-on, Home guard, dan alat terhadap TV palsu. |
| `.github/workflows/` | `ci.yml` (tes, build launcher, gerbang ukuran APK), `addons.yml`, `maintainer-bot.yml`. |
| `.github/maintainer/MAINTAINER.md` | Kebijakan tinjauan yang maintainer dan bot PR terapkan. |
| `.github/pull_request_template.md` | Daftar centang PR. Isi. |

## Perubahan taruh di mana

Intinya sengaja kecil. Pilih rumah terkecil yang muat (CONTRIBUTING, "Ide saya taruh di mana?"):

| Perubahannya | Taruh di |
|---|---|
| Daftar TV, perbaikan, terjemahan, dokumen, perbaikan alat | Pull request di sini |
| Sakelar launcher kecil yang mati secara default | Pull request di sini: lihat [`docs/workflows/launcher-switch.md`](docs/workflows/launcher-switch.md) |
| Tampilan baru: warna, wallpaper, dock, kolom, yang kunci config yang ada bisa bawa | `themes/<name>/` |
| Widget atau ubin yang bisa jadi aplikasinya sendiri yang kecil | `addons/<name>/`, sebagai kartu modul yang tidak pernah menyentuh `launcher/` |
| Launcher yang sama sekali baru, restyle berat, port ke platform lain, `minSdk` yang lebih rendah, izin atau akses jaringan baru, pengambilalihan Home Fire TV, default yang berubah untuk setiap TV | Repositorimu sendiri, plus satu baris di `SHOWCASE.md` |
| Aturan aman di `PROMPT.md`, logika keamanan Home di skrip, paket yang dilindungi, CI dan alur kerja, gerbang ukuran APK | Buka issue untuk maintainer; jangan mengubahnya di PR |

Setiap PR ditinjau maintainer dan bot yang menerapkan `.github/maintainer/MAINTAINER.md`: baca
vonisnya sebelum PR yang tidak sepele. Perubahan pada jalur Home atau peluncuran (tombol Home, boot, kembali ke Home,
skrip pemasangan) biasanya ditahan sampai dijalankan di TV sungguhan.

## Workflows

Metode langkah demi langkah di `docs/workflows/`. Baca yang cocok sebelum mulai; masing-masing dibuka dengan kapan
memakainya dan ditutup dengan pemeriksaan yang dijalankan.

- [`tv-list.md`](docs/workflows/tv-list.md): tambah daftar debloat TV yang sudah diuji beserta baris README-nya, atau perbarui satu.
- [`launcher-switch.md`](docs/workflows/launcher-switch.md): tambah sakelar launcher yang mati secara default, di dalam gerbang ukuran.
- [`translation.md`](docs/workflows/translation.md): tambah bahasa launcher, atau isi string yang hilang dari satu bahasa.
- [`theme.md`](docs/workflows/theme.md): tambah tema dari kunci yang ada, dengan screenshot dari TV sungguhan.
- [`addon.md`](docs/workflows/addon.md): tambah aplikasi kartu modul yang tidak pernah menyentuh `launcher/`.
- [`showcase.md`](docs/workflows/showcase.md): daftarkan proyek yang tinggal di repositorinya sendiri.
- [`press.md`](docs/workflows/press.md): tambah artikel dan perbarui keempat jumlah pers.
- [`docs-notes.md`](docs/workflows/docs-notes.md): tambah entri pemecahan masalah atau perbaikan `PROMPT.md`, dengan fakta yang dipatok tetap ada.
- [`perf-ab.md`](docs/workflows/perf-ab.md): pemeriksaan opsional perubahan launcher terhadap regresi kinerja dengan
  `tools/bench.py`, dan ukur perbaikan atau pemeriksaan yang gagal dengan trace, laporan, dan flame chart.

## Sebelum membuka PR

| Pemeriksaan | Perintah atau aturan |
|---|---|
| Tes, seperti yang CI jalankan | `python3 -m unittest discover -s tests -v` setelah `python3 -m venv tools/venv` dan `tools/venv/bin/pip install -r tools/requirements.txt`. Di `main`, CI menyetel `CLEANYOURTV_STRICT=1`, yang juga memeriksa jumlah model TV di README. |
| Launcher, kalau kamu menyentuh `launcher/` | JDK 17, Android SDK platform 34, build-tools 34.0.0. Di `launcher/`: `./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease`. Lint harus tetap nol error. |
| Gerbang ukuran APK | `launcher/app/build/outputs/apk/release/app-release.apk` harus di bawah 491,520 B (480 KB). Linux: `stat -c %s`, macOS: `stat -f %z`. |
| Baris README | Daftar baru punya barisnya di `README.md` dan `examples/README.md`; tema di `themes/README.md`; add-on di `addons/README.md`. |
| Satu topik per PR | Daftar TV tidak pernah menumpang di dalam PR fitur launcher. Daftar sebuah perangkat plus perbaikan kecil untuk perangkat yang sama boleh satu PR. |
| Dicoba di | Sebutkan TV, versi Android, dan firmware yang kamu coba, di baris "Tested on" templat PR. |
| TV sungguhan | Perubahan launcher dicek di TV sungguhan, bukan hanya emulator: `tools/tv.py --screenshot shots/after.png DPAD_DOWN`. D-pad dari setiap tepi (Up, Down, Left, Right). Tombol yang ditangani helper aksesibilitas perlu tekanan di remote sungguhan, dan force-stop launcher mematikan helper itu (`docs/TROUBLESHOOTING.md`). |
| Screenshot | Setiap perubahan visual punya screenshot dari TV di deskripsi PR. |
| Kinerja | Klaim kinerja menampilkan `tools/bench.py` sebelum dan sesudah, plus angka trace. Untuk perubahan launcher lain, pemeriksaan regresi di [`docs/workflows/perf-ab.md`](docs/workflows/perf-ab.md) opsional tapi diterima. |
| Berkas liar | Tidak ada berkas yang tidak ditaut apa pun: screenshot milik deskripsi PR, bukan pohon repo. |

## Aturan keras

Jangan pernah lakukan ini di PR:

- **Tidak ada APK atau biner siap pakai** di repo, bahkan sebagai artefak CI. Orang membangun dari sumber.
- **Tidak ada uninstall.** Tidak ada `pm uninstall` di daftar atau skrip. `pm hide` atau `pm block` hanya dengan
  pembatalan yang cocok tertulis di header daftar.
- **Tidak ada jaringan di launcher:** tidak ada izin `INTERNET`, tidak ada fitur jaringan, tidak ada analitik atau telemetri, tidak ada
  layanan latar atau polling, tidak ada blur atau video. Sasarannya TV dengan RAM 1 GB.
- **Tidak ada dependensi launcher baru** tanpa membahasnya di issue lebih dulu.
- **Tidak ada perubahan aturan aman** di `PROMPT.md`, Home guard dan pemeriksaan aman lain di
  `tools/disable.sh`, `tools/check.py`, dan `launcher/build-and-install.sh`, atau daftar paket yang TV
  butuhkan, tanpa maintainer.
- **Jangan mengubah default untuk setiap TV.** Perilaku baru mati sampai tema atau `config.xml` menghidupkannya.
- **Pembaru firmware lebih baik tetap aktif** (dikomentari di daftar); daftar yang menonaktifkan satu harus menyebut
  alasannya di header.
- **Tidak ada penyiapan Private DNS atau AdGuard, dan tidak ada skrip pasang atau pulihkan, di dalam folder daftar.** README
  sudah punya ekstra DNS opsional; catatan bahwa Private DNS jalan di TV itu boleh.
- **Tidak ada rahasia atau pengenal** di commit, daftar, atau screenshot: tidak ada alamat IP, nama Wi-Fi, email akun,
  nomor seri, Android ID, alamat MAC, atau kunci ADB. Pakai `<tv ip>` atau `192.168.1.50` di dokumen.
- **Jangan edit `.github/workflows/`** atau gerbang ukuran APK di PR biasa. PR yang isi utamanya alur CI
  baru yang membangun atau menerbitkan rilis ditutup.
- **Tidak ada root, tidak ada cara mengakali perlindungan toko aplikasi, tidak ada build privat** yang orang lain tidak bisa ulang dari repo ini.
- **Tidak ada tambahan pada alur PROMPT** di luar debloat dan launcher.
- **Jangan longgarkan `minSdk`** (26) atau izin launcher; itu fork untuk `SHOWCASE.md`.
- **Blur atau pangkas** nama Wi-Fi, nama orang, dan kota (bilah atas, `greeting_name` sebuah tema) sebelum
  screenshot pergi ke mana pun yang publik. Tidak ada screenshot Setelan TV yang menampilkan akun, email, atau nama perangkat.
  Dari `tools/tv.py --probe`, `getprop`, dan cadangan, kutip hanya model, versi Android, dan build firmware.
- **Masalah keamanan dilaporkan secara privat**, seperti yang [SECURITY.md](SECURITY.md) jelaskan, jangan di issue publik.

## Gaya commit dan PR

- Judulnya `area: what changed`, dengan kata biasa, dengan issue dalam tanda kurung kalau ada:
  `examples: add Epson LS800 laser projector (Askey HA90, Amlogic), Android TV 14`,
  `docs: an HDMI row opens any app that claims the channel MIME type (#805)`,
  `Launcher: show_inputs_pill, to hide the Inputs pill`. Areanya folder atau berkas tempat perubahan itu:
  `examples`, `Launcher`, `docs`, `tools`, `tests`, `themes`, `addons`, `SHOWCASE`, `README`, `Press`.
- Isinya satu atau dua kalimat tentang apa yang berubah dan TV tempat dicoba, lalu daftar centang templat.
  `Fixes #N` atau `Closes #N` kalau menyelesaikan sebuah issue.
- Satu topik per PR. Kecil boleh. Bersikap baik di tinjauan.
- Kontribusi dirilis di bawah lisensi MIT repo.

## Tempat bertanya

Pertanyaan ke [GitHub Discussions](https://github.com/Gathin23/cleanyourtv/discussions); bug, daftar TV, dan
ide yang butuh perubahan launcher atau dependensi baru masuk issue dulu, dengan templat di
`.github/ISSUE_TEMPLATE/`.
