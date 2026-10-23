# Hisense 65D62QH (China, JuUI 9.0.10.845)

**Debloat saja.** Daftar ini membiarkan stock launcher (`com.jamdeo.tv.vod`, 聚好看) aktif dan
tidak mengganti Home; setiap package di bawah dinonaktifkan dengan `pm disable-user`, tidak di-uninstall.

| Field | Value |
|---|---|
| Retail model | Hisense 65D62QH, 65", pasar China |
| UI | JuUI 9.0.10.845 |
| Android model / device | `VIDAA_TV` / `MT9653` |
| Android / build | 11 (API 30) / `RP1A.200720.011` |
| GMS | none (no Play Store, no Google account) |

Platform dan build sama dengan `../vidda-mt9653/` dan `../hisense-e5q-pro/`; daftar ini diturunkan dari
mereka lalu dicek ulang per package terhadap `pm list packages` set ini
(131 packages, nothing disabled at the factory).

- `debloat.txt` — lima batch, masing-masing diikuti cold reboot, dengan panel input ganti HDMI,
  remote, Wi-Fi, pairing Bluetooth, dan IME dicek sebelum batch berikutnya. 26 packages
  tetap. `com.android.calendar`, `.deskclock` dan `.music` di-enable lagi firmware setiap
  boot, jadi dicatat di daftar alih-alih dinonaktifkan berulang.
- `config.json` — panel sumber vendor (`dumpsys tv_input` kosong di firmware ini, jadi tidak ada
  input untuk pill Inputs launcher), Live TV, dan tile yang harus off Home.

Diterapkan 2026-10-22 dan dipakai sehari-hari sejak itu, selalu dengan stock Home: delapan cold
boot total antara batch dan setelahnya, lalu sehari menonton biasa. Boot normal
setiap kali, tanpa crash; ganti HDMI, casting, remote, dan IME masih jalan.

```bash
TV_HOST=<tv ip> tools/disable.sh examples/hisense-65d62qh/debloat.txt
TV_HOST=<tv ip> tools/restore.sh examples/hisense-65d62qh/debloat.txt
TV_HOST=<tv ip> tools/theme.sh examples/hisense-65d62qh
```

`com.jamdeo.tv.vod` **sengaja tidak** ada di daftar: itu stock Home, dan jika dinonaktifkan
firmware ini tidak meluncurkan Home sama sekali saat boot dan tombol Home remote berhenti merespons. Tampilan dan
langkah mengganti Home di set ini ada di
[`docs/devices/hisense-65d62qh.md`](../../docs/devices/hisense-65d62qh.md) untuk siapa pun yang
mempertimbangkan swap launcher di TV sendiri.
