# TCL BeyondTV AP08: laporan setup awal

Dicek di TV sungguhan satu sesi 2026-10-21, termasuk restart dan cek pemilik dengan remote fisik dan casting ponsel. Ini **bukan daftar debloat teruji beberapa hari**. Perubahan paket di bawah mencatat pilihan pemilik ini, bukan daftar untuk diterapkan apa adanya. Model ritel tidak ditetapkan.

## Pengenal perangkat

| Field | Observed value |
|---|---|
| Model / device / product | `BeyondTV` |
| Board | `RTD2851` |
| Android / API | Android TV 9 / 28 |
| Build display | `PPR1.180610.011 release-keys` |
| Build incremental | `AP08` |
| Security patch | `2021-01-05` |
| Reported memory | `2097152 kB` |

Melengkapi [contoh BeyondTV](../../examples/tcl-beyondtv/debloat.txt). Jangan campur AP08 dengan [contoh RT2851 AP06](../../examples/tcl-beyondtv-rt2851/debloat.txt), patch keamanan berbeda. Uji memakai launcher lokal build melaporkan versi 2.5 / versionCode 6; tidak memvalidasi setiap revisi upstream later.

## Konfigurasi launcher dan input

Konfigurasi launcher lokal memakai nilai ini di `launcher/app/src/main/res/values/config.xml`; default bersama tidak perlu diubah:

```xml
<string-array name="dock_apps">
    <item>com.google.android.youtube.tv</item>
    <item>com.netflix.ninja</item>
    <item>com.amazon.amazonvideo.livingroom</item>
    <item>com.android.vending</item>
</string-array>
<string-array name="hidden_apps">
    <item>com.tcl.tv</item>
</string-array>
<string name="live_tv_package" translatable="false">com.tcl.tv</string>
```

Ganti entri resource yang sesuai alih-alih menambah nama duplikat. Biarkan `com.tcl.tv` dan `com.tcl.tvinput` aktif: menyembunyikan tile TV hanya pilihan presentasi launcher. Layanan input di unit ini `com.tcl.tvinput/.TDTVInputService` dan `com.tcl.tvinput/.passthroughinput.TvPassThroughService`.

Aktivitas HOME asli `com.google.android.tvlauncher/.MainActivity`. Pengganti diinstal dan diluncurkan sebelum nonaktifkan HOME bawaan, lalu diset HOME dan dicek dengan tombol Home. `com.gathin.tvhome/.MainActivity` aktivitas resumed, termasuk setelah restart. `com.google.android.tungsten.setupwraith` terinstal tetapi tidak mengiklankan HOME di unit ini dan tetap aktif. Aktivitas kustomisasi tombol TCL dan fallback HOME Settings Android juga tetap aktif. Tidak perlu helper aksesibilitas atau remap scancode remote.

## Perubahan diamati sesi ini

Semua perubahan memakai disable paket user-0 reversible, bukan uninstall. Ada 19 paket nonaktif sebelum sesi dan 30 setelahnya. Sebelas penambahan ini tetap nonaktif setelah restart, tanpa perubahan tak terduga pada set disabled:

| Package | Session choice / constraint |
|---|---|
| `com.tcl.bi` | TCL analytics |
| `com.google.android.feedback` | Feedback reporting |
| `com.google.android.tv.bugreportsender` | TV bug reporting |
| `com.tcl.tv.tclhome_passive` | Passive TCL Home promotion |
| `com.tcl.esticker` | Retail stickers |
| `com.google.android.tvrecommendations` | Stock-home recommendations |
| `com.google.android.leanbacklauncher.recommendations` | Stock-home recommendations |
| `com.google.android.tvlauncher` | Only after replacement HOME was installed and working |
| `com.tcl.tvweishi` | Cleaner; disabled after installation because it also exposes an installer service |
| `com.tcl.appmarket2` | Duplicate store; disabled after installation because it also exposes an installer service |
| `com.tcl.versionUpdateApp` | TV firmware updater, disabled at the owner's explicit request; not a default recommendation |

Updater firmware remote tetap aktif. Tidak ada paket nonaktif baru perlu diaktifkan saat sesi; perilaku jangka panjang tidak diketahui. Catat state paket asli sebelum mengubah dan restore saat undo. Aktifkan HOME bawaan sebelum hapus launcher pengganti.

YouTube, Netflix, Prime Video, Play Store, ZEE5, Google Play Movies & TV, YouTube Music dan TCL Channel tetap aktif. Chromecast, asisten suara, Miracast dan T-Cast tetap aktif. Pemilik ini **tidak** menerapkan disable Play Movies atau YouTube Music dari contoh yang ada.

Paket ambigu atau terkait input dipertahankan, termasuk `com.tcl.partnercustomizer` (aktivitas T-key), `com.tcl.messagebox`, `com.tcl.usercenter`, `com.tcl.showmode`, `com.tcl.framework.custom` dan `com.tcl.keycustomfunctionservice`. Tidak ada kesimpulan tentang aman dihapus dari laporan ini.

## Verifikasi dan batas

- Menu Inputs menampilkan HDMI 1, HDMI 2, AV dan Live TV. Pilih HDMI 1 buka `com.tcl.tv/.TVActivity` dan layar no-signal; Home kembali ke launcher. **Gambar/suara HDMI dengan sumber terhubung dan reception tuner tidak diuji.**
- Pemilik konfirmasi semua tombol remote fisik yang ada jalan setelah restart, mencakup Home, Input/Source, Settings, volume, suara/mikrofon dan pintasan aplikasi. Cek pemilik, bukan trace event remote lengkap.
- Atas permintaan pemilik, Android Private DNS diset mode hostname `dns.adguard-dns.com`. Validasi konektivitas lulus dan pemilik konfirmasi video dan suara di uji streaming. Tidak membuktikan pemutaran di setiap aplikasi dipertahankan atau blok iklan YouTube. Restore pengaturan Private DNS sebelumnya untuk undo.
- Pemilik konfirmasi TV muncul untuk casting ponsel dan memutar video dengan suara setelah restart. Miracast dan T-Cast dipertahankan tetapi tidak diuji terpisah.
- Buffer crash pasca-restart kosong saat inspeksi. Siklus standby, pemakaian lama, update firmware dan fungsi setiap aplikasi tidak diuji.
- Akhirnya pemilik cabut otorisasi USB-debugging, matikan debugging dan restart. Percobaan TCP ke port 5555 **connection refused**. Network debugging tidak dibiarkan aktif.

Dump perangkat pribadi, alamat jaringan, nama Wi-Fi, screenshot berisi pengenal jaringan, kunci signing dan alat build unduhan sengaja tidak ada di laporan ini.
