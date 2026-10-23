# Prayer Times

Lima waktu sholat hari ini dan sunrise untuk satu kota, full screen di TV, sholat berikutnya disorot dengan
countdown, plus tanggal Gregorian dan Hijriah hari ini. Setiap sholat punya nama Arab dan Inggris. Untuk
launcher [VibeYourTV](../../README.md) add-on mendeklarasikan [module card](../../docs/modules.md); setelah
jawab **Show card**, tile-nya "Prayer Times" dengan glow hijau; di home Android TV lain app biasa.

![The prayer times page](https://raw.githubusercontent.com/majnioui/VibeYourTV-contrib/pr-assets/addon-prayer-times/prayer-times.png)

- **Data** dari [Aladhan](https://aladhan.com/), tanpa akun atau key.
- **Kota dan metode perhitungan** di-set sebelum build, di `app/src/main/res/values/config.xml`:
  `latitude`, `longitude`, `place` (nama yang ditampilkan) dan `method`, [nomor metode](https://aladhan.com/calculation-methods) Aladhan (2 ISNA, 3 Muslim World League, 4 Umm al-Qura, 5 Egyptian, 13 Turkey, 21 Morocco...). Default Casablanca metode Morocco.
- Membaca Aladhan saat halaman dibuka dan lagi setelah tengah malam. Countdown update tiap menit.
  Offline, waktu terakhir tetap di layar.
- Format 24 jam, teks Inggris.

## Permissions (izin)

| Permission | Alasan |
|---|---|
| `INTERNET` | Aladhan: waktu hari ini untuk koordinat di config. |

Itu saja. Yang dikirim: tanggal hari ini, latitude/longitude dari `config.xml`, nomor metode. Tanpa
akun, telemetri, iklan, layanan background, permission lokasi. Backup mati.

## Build dan pasang

JDK 17 dan Android SDK platform 34 dengan build-tools 34.0.0, seperti launcher:

```bash
cd addons/prayer-times && ./gradlew assembleRelease
TV_HOST=<tv ip> ../../tools/venv/bin/python -I ../../tools/tv.py --push app/build/outputs/apk/release/app-release.apk /data/local/tmp/addon.apk
TV_HOST=<tv ip> ../../tools/venv/bin/python -I ../../tools/tv.py 'pm install -r /data/local/tmp/addon.apk; rm -f /data/local/tmp/addon.apk'
```

Tekan Home: launcher tanya sekali apakah card ditampilkan. Ubah jawaban nanti dengan hold OK di
Settings, lalu Module cards.

Release build ditandatangani debug key agar pasang lewat ADB seperti launcher. Tanda tangani build sendiri
dengan key sendiri jika dibagikan; launcher pin jawaban Show ke signing key.

## Credits

Ikon sholat: [Lucide](https://lucide.dev) (ISC) dan [Feather](https://feathericons.com) (MIT). Font: Outfit
(SIL Open Font License, `FONT-LICENSE-OFL.txt`).

## Diuji di

Xiaomi TV (MiTV-MOOR2), Google TV 11, RTM5.220609.003: halaman, countdown, prompt card, card di dock launcher.

## Maintainer

[@majnioui](https://github.com/majnioui)
