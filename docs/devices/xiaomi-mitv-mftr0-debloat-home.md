# Xiaomi MiTV-MFTR0: debloat dan Home pihak ketiga di HyperOS TV 2 (Android 11)

Pendamping [xiaomi-mitv-mftr0-freeguy.txt](xiaomi-mitv-mftr0-freeguy.txt) (rute
input, intent pengaturan Xiaomi, penemuan aplikasi). Catatan ini mencakup sesi debloat/Home 2026-10-22
di MiTV-MFTR0 (`freeguy`, MediaTek mt9653), **firmware CN
`OS2.0.23.0.RFTMATV` (HyperOS TV 2), Android 11 (SDK 30)** — set CN kedua dari
sesi yang sama dengan [MiTV-ASTR0](xiaomi-mitv-astr0-blueplanet.md) (HyperOS TV 3,
Android 14), dan titik awalnya temuan ASTR0. Semua di bawah dicek per-paket di set ini dulu.

## Screensaver iklan sama, satu generasi lebih awal

`com.xiaomi.mitv.hyper.screensaver` ada di OS2 dengan pengaturan identik
(`screen_saver_timeout=480000`, kartu waktu/cuaca terbuka, AOSP `screensaver_enabled=0`
tidak terkait). Dinonaktifkan tanpa menunggu tangkapan live: paket sama, bentuk pengaturan sama,
keluarga firmware sama dengan bukti ASTR0. Klasik
`com.mitv.screensaver` / `com.xiaomi.mitv.advertise` juga tidak ada di sini;
`boot_ad=0` dari pabrik.

## Home: trampoline allowlist juga jalan di OS2

Urutan sama ASTR0, hasil sama: dengan home identitas allowlist
(`com.ts.launcher`) terinstal dan PatchWall (`com.mitv.tvhome`) nonaktif, HOME
mengarah ke trampoline dan mendarat di launcher pihak ketiga terinstal
(`com.oversea.aslauncher`, Emotn UI), lewat reboot — set boot langsung ke
sana. `cmd package set-home-activity` pada aplikasi biasa sama kosmetik di sini
(melaporkan `Success`, tidak mengubah apa pun). Jadi di set Xiaomi CN sejauh ini:
OS2/Android 11 dan OS3/Android 14 sama-sama menghormati allowlist nama paket, MiTV-MFFU0
(OS3.0.115) termasuk. Seperti daftar gaya ASTRP0: instal
trampoline dulu, nonaktifkan PatchWall kedua; `pm enable com.mitv.tvhome` plus
reboot membatalkannya.

## Beda dari set ASTR0

- Tidak ada preload WPS (`cn.wps.moffice_i18n_TV` absent), tidak ada `com.mitv.overseaservice`,
  tidak ada paket modul AOSP adservices/ondevicepersonalization/federatedcompute
  (Android 11 lebih dulu).
- `com.emotn.browser` (browser Emotn, terpisah dari launchernya) preload di sini dan dinonaktifkan.
- Daftar keep diverifikasi lagi: `com.xiaomi.mitv.smartpanel` panel tombol Menu;
  `com.sohu.inputmethod.sogou.tv` satu-satunya IME; `com.xiaomi.mitv.tvplayer`,
  `com.xiaomi.mitv.settings`, `com.xiaomi.mitv.systemui`, `mitv.service` tidak disentuh.

## Dua catatan operasional

- Konek ADB-over-TCP pertama butuh konfirmasi RSA `允许USB调试` di TV (centang
  "always allow"); jika dialog tidak muncul, `adb kill-server` + reconnect
  memicu handshake lagi. ADB tertutup lagi setiap reboot.
- **Klasifikasikan preload dengan `pm path`, bukan ingatan**: Moonlight pemilik
  (`com.limelight`) ada di draf awal daftar karena paket bernama sama preload pabrik di set lain; `pm path` menunjukkan `/data/app/...` (instal
  user) dan dihapus dari daftar lalu diaktifkan lagi. `pm disable-user`
  menyembunyikan ikon, terasa "terhapus" bagi pemilik; `pm enable` mengembalikannya
  dengan data utuh.
