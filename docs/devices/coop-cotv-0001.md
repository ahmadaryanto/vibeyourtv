# COOP COTV-0001 (Realtek rtd298x, Android 4.4.2, Argentina)

Laporan untuk set era KitKat dijual Cooperativa RENACER (Ushuaia) sebagai "TV LED HD SMART". Sumber:
issue [#795](https://github.com/Gathin23/cleanyourtv/issues/795). Laporan pertama berakhir penyebab
tidak diketahui dan set dianggap write-off; reporter yang sama lalu mengisolasi penyebab, memulihkan TV dan
menerapkan daftar lagi per batch. Bagian kedua itulah mengapa catatan ini ada — bullet gabungan di
`docs/TROUBLESHOOTING.md` pernah bilang set macet begini tidak bisa dipulihkan.

> **Peringatan:** `com.haiersoft.launcherproject2995hk` satu-satunya aplikasi Home di build ini, dan launcher
> repo VibeYourTV butuh Android 8.0 (minSdk 26) sementara set ini SDK 19, jadi tidak ada pengganti. Nonaktifkan
> dan setiap boot berhenti di `INICIALIZANDO`. `com.tsb.tv` terlihat tidak terkait dan dependensi launcher itu:
> menonaktifkannya membuat launcher crash-loop.

## Perangkat

| Field | Value |
| :--- | :--- |
| Retail model | COOP COTV-0001 ("TV LED HD SMART"), serial 001909 |
| Product / board | `rtd298x_tv001` |
| SoC | Realtek `rtd298x` |
| Kernel | 3.7.2-298x |
| Android / SDK | 4.4.2 / 19 |
| Build | `rtd298x_tv001-eng 4.4.2 KOT49H eng.yeh.20180522.224300 test-keys` (eng build, test-keys) |
| Bootloader | 0000.0000.0002 |
| SELinux | Enforcing |
| Packages | 58 |
| Ports | 2× USB-A host, Ethernet, Wi-Fi; no visible recovery button or service port |
| Developer options | USB debugging only (KitKat has no network-ADB toggle) |

## Cara set dijangkau

- Wi-Fi (192.168.18.3): port 5555 tertutup, `nmap -Pn -p 1-65535` tidak menemukan port TCP terbuka sama sekali.
- Ethernet (192.168.18.32): 5555 terbuka, dan itu satu-satunya jalan masuk. `tools/tv.py` dipakai karena tidak ada
  binary `adb` di mesin reporter.
- Koneksi pertama memunculkan `ConnectionResetError`; retry beberapa detik kemudian berhasil. Set
  tampaknya menerima satu sesi ADB sekaligus, jadi connect gagal bukan akhir.

## Apa yang salah

17 paket dinonaktifkan dengan `pm disable-user --user 0`, masing-masing echo `new state: disabled-user`,
dan `pm list packages -d` membaca kembali ke-17. Launcher pabrik
`com.haiersoft.launcherproject2995hk` termasuk di antaranya dan tidak ada pengganti terinstal, karena
launcher repo tidak bisa jalan di bawah Android 8.0. Setelah power cycle set diam di robot Android hijau
dengan `INICIALIZANDO` 20+ menit, dan tetap di sana lewat hard power cut dan off/on lagi dari
remote.

## Penyebab, terisolasi

Menonaktifkan launcher sendiri mereproduksi hang; mengaktifkan lagi dan reboot membawa set kembali.
Disabling `com.haiersoft.launcherproject2995hk` alone reproduces the hang; re-enabling it and
rebooting brings the set back. The hard power cut during the first boot was **not** the cause, and
neither was the batch size: 15 of the same 17 packages are disabled today on a working set.

## Jebakan kedua: `com.tsb.tv`

Menonaktifkan paket itu membuat launcher crash-loop dengan "Se ha detenido la aplicación
lancherProject", dan mengaktifkan lagi menghapus dialog. Tidak ada di daftar paket yang bilang paket
vendor yang tampak tidak terkait adalah yang dijalankan launcher, argumen untuk menerapkan
daftar per batch dengan reboot dan lihat layar di antaranya, bukan untuk nama paket tertentu.
Disabling `com.tsb.tv` makes the launcher crash in a loop with "Se ha detenido la aplicación
lancherProject", and re-enabling it clears the dialog.

## Pemulihan dari `INICIALIZANDO`

- **Layar macet masih bisa jawab ADB.** The stuck screen can still answer ADB. Setelah set dianggap tidak terjangkau, ping dan
  port 5555 menjawab lagi di alamat Ethernet sementara layar masih `INICIALIZANDO`.
  Reporter curiga kabel Ethernet longgar saat cek sebelumnya dan tidak bisa konfirmasi,
  tapi cek link dan retry sebelum simpulkan set KitKat hilang.
- Aktifkan kembali setiap paket dari backup sebelum perubahan, konfirmasi daftar disabled kosong, lalu reboot:

  ```sh
  tools/tv.py 'pm list packages -d'
  tools/tv.py 'pm enable com.haiersoft.launcherproject2995hk'
  # ...once per package that was disabled, or once per package in the backup
  tools/tv.py 'pm list packages -d'      # must print nothing
  tools/tv.py --reboot                   # returns once ADB accepts it; wait for the TV to come back
  ```

  Set ini menampilkan `INICIALIZANDO` sekali lagi saat reboot lalu boot normal. Boots take 33–73 s.
- Keyboard USB atau port service tidak tersedia di papan ini, dan tidak ada firmware publik, jadi
  `tools/restore.sh` hanya terjangkau saat ADB menjawab — itulah mengapa dua menit
  cek ulang link Ethernet penting.

## Daftar yang jalan

15 dari 17 asli, empat grup dengan reboot dan cek layar setelah masing-masing, dan
dua terakhir dicoba sendiri setelah set stabil:

| Group | Packages | Result |
| :--- | :--- | :--- |
| 1 | `com.android.galaxy4`, `com.android.noisefield`, `com.android.dreams.phototable`, `com.android.dreams.basic`, `com.android.soundrecorder`, `com.android.speechrecorder` | boots |
| 2 | `com.realtek.camera`, `com.rtk.android.miracast`, `com.svox.pico` | boots |
| 3 | `com.adups.fota`, `com.adups.fota.sysoper`, `cn.haiqin.realtek.other.vstoresubclient` | boots, menu and media playback work |
| 4 | `com.cvte.taskmanager` | boots |
| later | `com.rtk.mediabrowser`, `com.hikeen.xymenu` | boots; media playback and the TV menu still work |

**Must stay enabled:** `com.haiersoft.launcherproject2995hk` (the only Home app) and `com.tsb.tv`
(what that launcher runs on). Neither has a replacement on SDK 19.

Ide awal di laporan — lewati `com.adups.*` di build lama — ditarik reporter:
kedua paket adups nonaktif bersih dan set boot, jadi jebakannya paket Home, bukan
updater.

## Limits

- Satu unit, satu build eng/test-keys, satu malam. Grup dicek dengan reboot dan lihat
  menu dan pemutaran media, bukan paket per paket; hanya launcher dan `com.tsb.tv` yang
  diisolasi sendiri.
- No cold boot after the final list, and no second power cycle, so a delayed failure is still
  possible.
- Tidak ada di sini yang bilang build `rtd298x` lain sama, dan tidak ada yang diuji tentang
  paket `com.realtek.*` di luar dua di atas.
- `com.rtk.engmenu` sengaja tidak disentuh.
- Bagian launcher mustahil di set ini: minSdk 26 vs SDK 19. Menurunkan minSdk perubahan sumber,
  dan APK dibangun di tempat lain bukan yang repo VibeYourTV verifikasi.

## Apa yang dilakukan berbeda

- Biarkan launcher bawaan. `tools/disable.sh` lewati paket yang punya Home saat tidak ada apl
  terinstal lain yang jawab HOME, dan di Android 6 ke bawah minta `dumpsys activity` saat `cmd package`
  tidak ada (lihat `docs/TROUBLESHOOTING.md`). Di set ini cek itu harus menyebut
  `com.haiersoft.launcherproject2995hk` lewat `dumpsys activity`; reporter nonaktifkan paket
  manual, jadi tidak pernah jalan, dan apakah itu menyebut launcher ini di sini is unverified.
- Jaga jalur ADB hidup: Ethernet, karena firmware ini tidak punya toggle network-debugging, dan satu
  sesi sekaligus.
- Batch dengan reboot di antaranya, bukan satu daftar panjang: begitu `com.tsb.tv` ketahuan.
