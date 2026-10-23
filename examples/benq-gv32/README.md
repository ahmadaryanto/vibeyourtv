# BenQ GV32 (proyektor portabel Google TV 14)

Board buatan SDMC (product `BenQ_GV01`, device `himalaya`, MediaTek m7332, RAM 1,8 GB), firmware
UKNV.260626.001. Kedua bagian jalan: daftar debloat dan launcher VibeYourTV sebagai Home. Diuji 21 Oktober 2026.

| File | Isinya |
|---|---|
| `debloat.txt` | 8 packages yang dinonaktifkan, dikelompokkan dan diberi komentar, plus apa yang dipertahankan dan kenapa (`build-and-install.sh` juga menonaktifkan launcherx) |
| `config.json` | Urutan dock, tile tersembunyi, tombol Settings dan profil; push dengan `tools/theme.sh` |

Semua perintah dijalankan dari root repo, dengan IP proyektor di `TV_HOST`.

## Menyalakan ADB

Settings → Device Preferences → About → tekan OK pada **Build** 7 kali, lalu Developer options →
**Wireless debugging** → **Pair device with pairing code**. Lalu dengan `adb` Google:

```bash
adb pair <ip>:<pairing port> <code>
adb connect <ip>:<port>        # the IP address & Port line on the Wireless debugging screen
adb tcpip 5555                 # so tools/tv.py works
```

Port connect acak dan diumumkan lewat mDNS, yang tidak menembus NAT WSL2; scan port proyektor 30000–50000
yang menerima `adb connect` biasanya menemukannya.

## Instal, dari nol atau ulang

1. **Pindahkan jaringan Wi-Fi dari setup wizard dulu.** Jaringan yang disimpan saat setup pertama milik
   `com.google.android.tungsten.setupwraith`, dan Android menghapusnya begitu setupwraith dinonaktifkan. Di proyektor: Settings → Network & Internet → jaringan kamu → Forget, lalu sambung lagi. Cek dengan `tools/tv.py "dumpsys wifi | grep -o 'cname=[^ ]*'"` (harus `android.uid.system`).
2. Jalankan:

```bash
TV_HOST=<projector-ip> tools/disable.sh examples/benq-gv32/debloat.txt
TV_HOST=<projector-ip> HOME_KEY_SERVICE=1 launcher/build-and-install.sh
TV_HOST=<projector-ip> tools/theme.sh examples/benq-gv32
```

`disable.sh` sendiri melewati setupwraith selama masih memegang Wi-Fi, jadi langkah 1 penting untuk
serah terima launcher: dengan setupwraith aktif, `RecoveryActivity`-nya mengklaim Home di belakang launcherx.

## Tombol remote ("RCI078/079", Bluetooth)

Tanpa stock launcher ini tetap jalan sendiri: Home, Back, volume, D-pad, tombol YouTube, Netflix, Prime Video, Input (switcher input Google), focus, auto-keystone, tombol bantuan BenQ (Support Center), dan mic.

Dua tombol hanya dijawab launcherx dan mati tanpanya. `config.json` memetakannya lewat scancode, butuh HomeKeyService launcher (`HOME_KEY_SERVICE=1` di atas):

| Tombol | Scancode (Linux key) | Dipetakan ke |
|---|---|---|
| Settings (gear) | 189 (`KEY_F19`) | Android Settings |
| Profile (person) | 433 (`KEY_LOGOFF`) | Plex (profile switcher Google TV ada di launcherx) |

**Trade-off:** selama accessibility service yang memfilter key terikat, firmware ini mematikan repeat tombol tahan, jadi tahan Volume Up hanya naik satu langkah, seperti di [TCL G09](../../docs/devices/tcl-55p71b-g09-key-repeat.md). Sudah dicek dua arah: ramp volume kembali begitu helper lepas. Kalau mau ramp, jangan pakai `HOME_KEY_SERVICE=1`, atau matikan helper:

```bash
TV_HOST=<projector-ip> tools/tv.py "settings put secure enabled_accessibility_services ''; settings put secure accessibility_enabled 0"
```

## Aktifkan lagi satu app

```bash
TV_HOST=<projector-ip> tools/tv.py 'pm enable com.instagram.airwave'
```

Pakai nama paket dari `debloat.txt`. Untuk mengembalikan semua dan Home ke launcherx:

```bash
TV_HOST=<projector-ip> tools/restore.sh examples/benq-gv32/debloat.txt com.google.android.apps.tv.launcherx
TV_HOST=<projector-ip> tools/theme.sh --reset
```

Matikan key helper seperti di bagian Remote, dan `tools/tv.py 'pm uninstall com.gathin.tvhome'` menghapus launcher.

## Ubah dock atau wallpaper

Edit `dock_apps` di `config.json` (nama paket, kiri ke kanan; `columns` jumlah slot), lalu push lagi:

```bash
TV_HOST=<projector-ip> tools/theme.sh examples/benq-gv32
```

Untuk wallpaper, taruh `wallpaper.jpg` (1920x1080) di samping `config.json` dan push dengan cara yang sama;
`themes/README.md` punya opsi dim dan rotasi. `tools/theme.sh --reset` kembali ke config bawaan launcher.

## Selesai

Matikan **USB debugging** dan **Wireless debugging** di Developer options, cabut otorisasi debugging,
restart, dan pastikan ADB tertutup:

```bash
tools/venv/bin/python tools/adb-closed.py <projector-ip>
```
