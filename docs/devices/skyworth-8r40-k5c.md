# Skyworth / Coocaa 8R40_K5C (China domestik, Android 8.0)

Laporan firmware Coocaa/Skyworth daratan (`com.tianci.*` / `com.coocaa.*`) di set Realtek RTD285x. Sumber: issue #692. Laporan saudara keluarga lebih tua: #417 (8R145_P3DM / K3 Pro, Android 11), catatan **tidak** transfer langsung.

> **Warning:** Disabling the wrong package crashes the "About this TV" page, which is the only place ADB can be turned on. Recovery is a factory reset. Read the traps before running any debloat list.

## Perangkat

| Field | Value |
| :--- | :--- |
| Retail model | Skyworth / Coocaa K5C (China domestic) |
| Product / device | `8R40_K5C` |
| SoC / board | Realtek `rtd285x` |
| Kernel | Linux 4.4.3, armv7l (32-bit userspace, armeabi-v7a only) |
| Android / SDK | 8.0.0 / 26 |
| Build | `OPR6.170623.012 dev-keys`, type `user`, `ro.debuggable=0` (adb root refused) |
| Security patch | 2017-08-05 |
| SELinux | Permissive |
| RAM | 1 GB |
| Packages | 56 total |

## Hasil

16 paket dinonaktifkan, RAM bebas, iklan boot hilang. Diverifikasi lewat reboot dengan ADB reconnect sendiri dan layar home bawaan utuh. Daftar: `examples/skyworth-8r40-k5c/debloat.txt`.

## Jebakan

### Jebakan 1: debloat bisa hancurkan satu-satunya pintu ADB

- Tidak ada `com.android.settings` dan halaman Developer options. `com.tianci.setting` tidak punya aktivitas develop/debug.
- Switch ADB di `com.coocaa.factory`, lewat Settings → 基本设置 → 设备信息 → 更多信息 → up up down down left right left right.
- Halaman itu bergantung `com.tianci.de` (proses `com.tianci.de:TianciDaemons`) dan `com.skyworthdigital.stb.dataprovider`. Disabling either crashes About instantly. Wi-Fi ADB and port 5555 are then unreachable and a factory reset is the only recovery (the reporter needed two).
- Percobaan kedua `com.coocaa.sky.ccapi` dan/atau `com.tianci.user` crash About sama. Anggap `ccapi` must-keep. `com.tianci.user` ada di daftar final verified-safe, bukti tidak konklusif; nonaktifkan terakhir dan cek About lagi.
- **Jalan keluar (tanpa root):** `com.tianci.setting/.factorysetting.FactorySetting` mendeklarasikan aksi publik tanpa guard `coocaa.intent.action.TIANCI_FACTORY`, jadi aplikasi mana pun bisa buka menu pabrik. Instal Activity Launcher (`de.szalkowski.activitylauncher.oss`, punya entri leanback jadi muncul di launcher TV) **sebelum** debloat dan uji.
- **Alur disarankan:** terapkan batch kecil, buka About setelah setiap batch.

### Jebakan 2: HOME hardcoded; launcher pihak ketiga tidak bisa punya

- `cmd package resolve-activity -c android.intent.category.HOME` mengembalikan "No activity found". Tidak ada paket, launcher bawaan termasuk, mendeklarasikan HOME.
- `com.tianci.system` (SkySystemService) memulai halaman home dari `persist.service.homepage.pkg` / `.cls`, dan tombol Home remote lewat `HotKeyHandler` ke pasangan sama.
- Prop menerima launcher pihak ketiga runtime tapi ditulis ulang saat boot dari read-only `/system/pcfg/8R40_K5C/config/general_config.xml` (`DEFAULT_HOMEPAGE` = `com.tianci.movieplatform/com.coocaa.homepage.vast.HomePageActivity`). Ubah ini butuh root.
- Yang merusak boot:
  - `cmd package set-home-activity ...` ditolak: `cannot be home on user 0` (jalan di unit Android 11 #417).
  - `pm disable-user com.tianci.movieplatform` layar hitam; `sys.boot_completed` tidak pernah set.
  - `pm disable-user app.orchard.tv` (Aerial launcher di beberapa unit) layar hitam juga. Jangan nonaktifkan launcher saat ini.
  - `setprop persist.service.homepage.switch 0` plus arahkan pkg/cls ke launcher pihak ketiga: setelah reboot `adbd` tidak start dan About crash. Biarkan prop itu unset.
- Jawaban praktis sekarang: biarkan launcher bawaan dan buka launcher lain dari My Apps.

### Jebakan 3: iklan video boot dan antrian autostart

- Iklan: video 15 d di `/data/local/bootvideo.ts` (milik system), diputar `com.skyworth.tv`, SDK iklan di `com.coocaa.adsdk`.
- Perbaikan (bertahan reboot, tanpa root):
  ```
  tools/tv.py 'setprop persist.sys.bootvideo.time 1'
  ```
- `bootQueue.xml` di folder pcfg sama start `com.tianci.push` 1,2 d dan `com.tianci.ad` 5,0 d. Juga `persist.sys.ad.startboot = com.tianci.ad/ui.BootViewActivity`.
- Switch pcfg lain ada tapi butuh write ke `/system`: `CURRENT_AD_SERVER` (`hoisin.coocaatv.com`), `HIDE_LAUNCHER`.

### Jebakan 4: keanehan input yang merusak otomasi

- Pemutar berbasis TVBox (mis. 影视仓 `com.tencent.qqlivehuawei`) telan `KEYCODE_HOME` / `KEYCODE_BACK` disuntik. Home remote fisik masih jalan.
- Home bawaan abaikan `input tap` dan hanya jawab `KEYCODE_DPAD_*`. Klik berbasis screenshot gagal: bounds uiautomator 1280x720 tapi screencap diskala.

## Harus tetap aktif

| Package | Why |
| :--- | :--- |
| `com.tianci.de` | About / device info (ADB path) |
| `com.skyworthdigital.stb.dataprovider` | About / device info (ADB path) |
| `com.coocaa.sky.ccapi` | Core API; About crashed after disabling it |
| `com.tianci.movieplatform` | Stock launcher, boot crash if disabled |
| `com.coocaa.factory` | Factory menu, holds the ADB toggle |
| `com.tianci.localmedia` | USB APK install |
| `com.skyworth.tv` | Factory menu, picture settings, accessibility |
| `com.coocaa.tvmanager` | TV management |
| `com.coocaa.guide`, `com.coocaa.miracast` | System guide and casting |

## Diverifikasi aman dinonaktifkan (16)

`com.tianci.ad`, `com.tianci.appstore`, `com.tianci.push`, `com.tianci.user`, `com.tianci.ipc`, `com.tianci.autoinstaller`, `com.baidu.oases`, `com.coocaa.mall`, `com.coocaa.activecenter`, `com.coocaa.app_browser`, `com.coocaa.messageprovider`, `com.coocaa.tvpitv`, `com.coocaa.theme.child`, `com.icoolme.tv.weather`, `com.skyworth.blockchain`, `com.skyworth.smarthome_tv`.

Enam pertama (`ad`, `push`, `appstore`, `user`, `ipc`, `autoinstaller`) sudah nonaktif dari pabrik di unit ini, risiko terendah.

## Laporan terkait: Coocaa 8R145 / K3 Pro (#417)

Keluarga vendor sama (`com.tianci.*` / `com.coocaa.*`), firmware berbeda: Realtek RTD2885N, Android 11, board `8R145_P3DM`. Catatan dan daftar **tidak** transfer ke TV ini, dan sebaliknya.

| | 8R145 (#417, Android 11) | 8R40_K5C (panduan ini, Android 8.0) |
| :--- | :--- | :--- |
| `cmd package set-home-activity` | accepted | refused: `cannot be home on user 0` |
| Package declaring `android.intent.category.HOME` | the Coocaa launcher | none at all |
| Disabling `com.tianci.movieplatform` | boot hang, factory reset | black screen at boot, factory reset |
| `com.coocaa.guide` | disabled in the #417 list | **must stay enabled** here |
| About page / ADB lockout (Trap 1) | not reported | the main hazard on this firmware |

Paket muncul di kedua laporan nonaktif tanpa masalah: `com.tianci.ad`, `com.tianci.push`, `com.tianci.autoinstaller`, `com.coocaa.app_browser`, `com.coocaa.messageprovider`.

Jangan salin daftar mana pun ke TV lain. Paket hanya di daftar #417 (mis. `com.skyworth.aisense`, `com.coocaa.sysreport`, `com.coocaa.tvpi`) tidak diuji di 8R40_K5C dan tidak ada di sini.

## Pertanyaan terbuka (dari #692)

1. Bisa launcher pihak ketiga persist lewat reboot tanpa root (`HIDE_LAUNCHER`, broadcast `com.skyworth.broadcast.homepage.switch`, hotel mode `persist.sky.hotel_switch`)? Belum diuji.
2. Root di RTD285x kernel 4.4.3: PoC CVE-2019-2215 arm64 butuh mmap 4 GB; port arm32 butuh offset simbol kernel dari boot image.
