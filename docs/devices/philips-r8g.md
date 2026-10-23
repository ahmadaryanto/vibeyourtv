# Philips R8G Google TV: mematikan lampu standby

Dicek di Philips R8G buatan MOKA/TPV (perangkat `whitefield`), Google TV 14 / API 34, Realtek `rtd2851f`,
build `UKRE.260524.058`, fingerprint `PHILIPS/PHILIPS/whitefield:14/UKRE.260524.058/16139055:user/release-keys`.
Daftar debloatnya [`examples/philips-r8g-google-tv`](../../examples/philips-r8g-google-tv/debloat.txt).
Ini observasi di satu set, bukan klaim kompatibilitas firmware.

Lampu merah di bawah layar tetap nyala saat TV standby, dan menu TV tidak punya pengaturannya.
Lampu itu dikendalikan layanan vendor Realtek, `com.realtek.power.ILedManager/default`, yang bisa dipanggil shell ADB
dan tidak bisa oleh aplikasi (HAL vendor, jadi tidak ada add-on yang bisa).

## Panggilan

Antarmukanya (dari kelas AIDL `com.realtek.power` di `com.realtek.energymode`) punya panggilan 1,
`setPowerLedConfiguration(int state, int setting)`. State-nya `LedState`:

| state | meaning |
|---|---|
| 0 | `PANEL_ON`: the TV is on |
| 1 | `PANEL_OFF` |
| 2 | `STR`: standby (suspend to RAM) |
| 3 | `LPM`: low-power standby |
| 4 | `BLINKING` |

Setting tidak terdokumentasi. 0 mematikan lampu untuk state itu; 1 diasumsikan lampu normal (nilai lama tidak bisa dibaca: tidak ada getter). Mati saat nyala dan di kedua standby:

```bash
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'for s in 0 2 3; do service call com.realtek.power.ILedManager/default 1 i32 $s i32 0; done'
```

Setiap panggilan menjawab `Result: Parcel(NULL)`. `service call com.realtek.power.ILedManager/default 16777215`
(`getInterfaceVersion`) menjawab `1` adalah cek awal yang aman bahwa layanan ada.

## Apa yang terjadi

Saat TV nyala, lampu sudah mati. Saat tombol power, lampu nyala beberapa detik saat TV mati (urutan itu tidak tercakup state di atas), lalu mati dan tetap mati di standby. Untuk undo, kirim setting 1 alih-alih 0 untuk state yang sama (undo belum dicoba). Belum dicek: apakah bertahan setelah cabut listrik penuh atau update firmware; jika lampu kembali, jalankan baris itu lagi.

Set Realtek lain mungkin punya layanan sama dengan nama sama (`service list | grep -i led`); nomor state di atas dari firmware ini.
