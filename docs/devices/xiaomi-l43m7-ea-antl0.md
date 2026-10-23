# Xiaomi L43M7-EA (MiTV-ANTL0): iklan startup mati dengan satu pengaturan

Di TV ini `settings put system boot_ad_switch 0` menghilangkan iklan startup 15 detik, dicek setelah restart penuh. Launcher bawaan tetap; tidak ada yang dinonaktifkan atau diinstal.

| Retail model | Internal model | Android | Firmware | Stock launcher |
| --- | --- | --- | --- | --- |
| Xiaomi L43M7-EA, 43" | MiTV-ANTL0 / starwars | 5.1.1 (SDK 22) | 23.2.4.2225 | `com.mitv.tvhome` 3.38.1017 |

Launcher membaca pengaturan itu, jadi versinya sama pentingnya dengan firmware. Di konfigurasi ini metode lama `persist.bootvideo.bootanim false` dari [daftar ANTL0](../../examples/redmi-mitv-antl0/debloat.txt) bertahan setelah restart tetapi tidak menghilangkan iklan.

```sh
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'settings get system boot_ad_switch'   # save it; null here
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'settings put system boot_ad_switch 0'
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'settings delete system boot_ad_switch' # undo, if it was null
```

Jika kunci sudah punya nilai sebelumnya, kembalikan nilai itu alih-alih menghapusnya. Logo Xiaomi ~10 detik masih tampil. ADB jaringan tertutup setelah restart; nyalakan-matikan ADB debugging membawanya kembali.

Batas: launcher menganggap default kunci 1 dan layar izin iklannya (`AuthManageActivity`) bisa mengembalikannya ke 1, jadi mengubah kontrol itu bisa membatalkan langkah ini. Timer tujuh hari terpisah memakai kunci Global `mitv.settings.advertise.*`, bukan yang ini. Pembaruan launcher dan firmware, reset pabrik, dan launcher pengganti belum diuji.
