# Kartu modul, spec 1

Aplikasi Android TV yang terpasang bisa mendeklarasikan kartu untuk launcher VibeYourTV. Kartu adalah ubin aplikasi dengan
label pilihannya, keterangan opsional, dan warna cahaya opsional, diletakkan di depan
kisi alfabet. Launcher menampilkan kartu hanya setelah pemilik TV menjawab Show pada prompt, dan
mengingat jawaban itu untuk kunci penandatangan aplikasi.

Dokumen ini adalah kontrak antara modul (aplikasinya) dan launcher. Cara membuatnya untuk
pembuat modul ada di [`docs/modules.md`](../modules.md).

## Deklarasi

Di `AndroidManifest.xml` modul, pada aktivitas yang sudah punya filter `LEANBACK_LAUNCHER`
(yang ditampilkan launcher sebagai ubin):

```xml
<activity android:name=".LibraryActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="com.gathin.tvhome.category.DOCK_CARD" />
    </intent-filter>
    <meta-data android:name="com.gathin.tvhome.card" android:resource="@xml/module_card" />
</activity>
```

`res/xml/module_card.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<card spec="1" label="Retro" caption="NES games" accent="#E8A33D" />
```

Aturan deklarasi:

- Filter kartu dan `meta-data` berada di aktivitas yang juga punya `MAIN` +
  `LEANBACK_LAUNCHER`, `exported`, dan aktif. Kartu di aktivitas lain dibuang.
- `meta-data` harus memakai `android:resource` yang menunjuk ke resource XML. `android:value` tidak
  dibaca.
- Satu kartu per paket. Kalau sebuah paket mendeklarasikan lebih dari satu, hanya yang pertama dalam daftar sistem yang
  dibaca.
- Elemen akar adalah `<card>` dengan atribut saja, tanpa namespace XML. Elemen anak diabaikan.
  Atribut yang tidak dikenal diabaikan, sampai 16 atribut seluruhnya.

## Field

Nilainya teks literal yang ditulis di berkas XML. Referensi (`@string/name`, `?attr/name`)
tidak dipecahkan di spec 1 dan membuat kartu tidak valid, jadi kartu tidak bisa dilokalkan di spec 1.

| Atribut | Wajib | Aturan |
| :- | :- | :- |
| `spec` | ya | Bilangan bulat desimal. Launcher ini membaca spec `1`. Nilai lain: kartu diabaikan, dengan satu baris log. |
| `label` | ya | 1 sampai 24 karakter dari `A-Z a-z 0-9` spasi `. & + ' -`, tanpa spasi di awal atau akhir, sedikitnya satu huruf atau angka. Lihat nama cadangan di bawah. |
| `caption` | tidak | Sampai 32 karakter dari himpunan yang sama. Tampil setelah label saat kartu fokus di kisi. Baris nama di bawah ubin satu baris; label dan caption bersama-sama menampilkan sekitar 26 karakter di kisi 4 kolom, jadi caption cukup beberapa kata. |
| `accent` | tidak | `#RRGGBB`. Warna cahaya ambient saat kartu fokus di dock. |

Kompiler resource menyimpan sebagian nilai atribut dalam bentuk bertipe; launcher menerima bentuk
yang dikembalikannya untuk nilai di atas (`spec="1"` sebagai integer, `accent="#E8A33D"` sebagai warna,
yang kembali sebagai `#ffe8a33d`). Biarkan label dan caption sebagai kata: nilai yang terbaca sebagai
angka dengan satuan atau sebagai warna disimpan dalam bentuk bertipe itu dan tidak akan cocok dengan yang kamu tulis.

### Reserved names

Perbandingan memakai bentuk lipatan label: huruf kecil, dengan segala sesuatu selain huruf ASCII
dan angka dihapus. "Live TV", "live-tv", dan "LIVETV" adalah nama yang sama.

Sebuah label ditolak kalau:

- berisi `verified` atau `official` di mana pun: kata yang menyiratkan endorsement dicadangkan, dan
  tidak ada yang dideklarasikan modul yang pernah ditampilkan sebagai didukung (dua kata yang sama ditolak di
  caption);
- sama dengan salah satu nama yang dipakai UI launcher sendiri, dalam bahasa Inggris atau bahasa TV: yang
  dikatakan pil dan dialognya (`Home`, `Settings`, `Inputs`, `Wi-Fi`, `Live TV`, `App library`,
  `Choose default home`), nama screensaver jam di pemilih screensaver TV (`Clock`),
  `Default home` kata per kata, dan dua nama proyek `cleanyourtv` dan `tv-vibehack`;
- menamai input yang ditampilkan menu Input: `HDMI` dan apa pun yang diawali itu, `AV`;
- selagi prompt jatuh tempo (kartu baru, atau yang kunci penandatangan atau labelnya berubah): sama dengan label
  aplikasi lain yang terpasang di TV ini (nama di layar utama), atau label aplikasi modul itu sendiri sama dengan
  label aplikasi terpasang lain. Label aplikasi milik modul sendiri tidak masalah: kartu bernama "Retro" dari
  aplikasi bernama "Retro" adalah kasus biasa. Setelah pemilik menjawab, nama itu tetap pada kartu itu:
  aplikasi yang dipasang kemudian dengan label yang sama tidak menggesernya. Jawaban itu untuk nama itu:
  kartu yang mengubah labelnya pada pembaruan menjadi tertunda lagi dan melewati pemeriksaan ini dari awal;
- dua kartu berbagi label: yang sudah diputuskan pemilik mempertahankan nama itu (keputusan
  tertua yang menang), yang lain dibuang; kalau keduanya belum punya keputusan, keduanya dibuang dan tidak ada yang
  ditanya tentang nama yang diperebutkan.

## Limits

Launcher tidak pernah percaya masukan modul kecil atau terbentuk baik. Semuanya berbatas dan
pelanggaran apa pun membuang kartu yang satu itu, dengan satu baris log dan tanpa efek lain.

| Apa | Batas |
| :- | :- |
| Kartu yang dibaca per penyegaran daftar paket | 32, sisanya diabaikan |
| Kartu yang ditampilkan sebagai ubin | 8, menurut label; sisanya dicantumkan sebagai melewati batas di daftar Module cards dan tetap memakai ubin biasa |
| Keputusan yang diingat di `cards.json` | 128, yang tertua dibuang di luar itu |
| Peristiwa XML yang dijelajah per kartu | 64 |
| Kedalaman sarang XML | 2 (`<card>` dan anak langsungnya) |
| Atribut pada `<card>` | 16 |
| Panjang label dan caption | 24 dan 32 karakter, diperiksa sebelum yang lain melihat nilainya |
| Penandatangan | tepat satu sertifikat penandatangan; paket dengan beberapa ditolak |

## Perilaku launcher

1. Penemuan. Setiap kali layar utama tampil di depan, launcher menanyakan package manager
   untuk aktivitas dengan `MAIN` + `com.gathin.tvhome.category.DOCK_CARD` (query package manager,
   tanpa polling, tanpa layanan, tanpa jaringan). Untuk masing-masing ia memeriksa aktivitas, membaca XML lewat package
   manager, menerapkan aturan di atas, dan menghitung digest penandatangan. Setiap kartu dibaca di dalam
   batas kesalahannya sendiri.
2. Digest penandatangan. SHA-256 sertifikat penandatangan berkode DER dari satu-satunya penandatangan paket,
   seperti yang dicetak `apksigner verify` dengan opsi `print-certs` ("Signer #1 certificate SHA-256
   digest"). Di Android 9 dan yang lebih baru launcher membaca penandatangan saat ini; di Android 8 ia membaca
   tanda tangan paket. Paket yang ditandatangani dengan lebih dari satu sertifikat ditolak.
3. Catatan keputusan. Keputusan pemilik disimpan per paket di penyimpanan pribadi launcher
   (`files/cards.json`, format versi 2): paket, digest penandatangan, label terlipat kartu, keputusan
   (`show` atau `hide`), pertama terlihat, diputuskan pada. Berkas ditulis secara atomik dan divalidasi ketat saat dibaca; berkas yang tidak
   lolos validasi dipindah ke `cards.json.bad` dan launcher lanjut seolah belum ada keputusan.
   Catatan tidak pernah meninggalkan TV (cadangan dimatikan untuk launcher).
4. Prompt. Kartu tanpa catatan, atau yang digest penandatangan atau label terlipatnya berbeda dari catatannya,
   tidak ditampilkan. Setelah
   kisi tertata dan layar utama punya fokus, tanpa menu terbuka, launcher menampilkan satu
   prompt. Ia menyebut aplikasi dengan labelnya sendiri (karakter kendali dan pemformatan dihapus, paling banyak 40
   karakter) dan paket, serta kunci dengan dua belas karakter heksa pertama digest; kalau
   label kartu berbeda dari label aplikasi, keduanya disebut.
   Tombolnya "Keep hidden" (fokus lebih dulu) dan "Show card"; Back berarti Keep hidden. Prompt
   juga menjelaskan cara mengubah jawaban sesudahnya. Kiri dan Kanan berpindah antar tombol; Atas dan Bawah
   tidak melakukan apa-apa. Setelah jawaban, kartu berikutnya yang prompt-nya tertunda menyusul langsung.
5. Perubahan penandatangan. Paket yang digest penandatangannya berbeda dari catatannya diperlakukan sebagai baru: kartu
   tidak ditampilkan, dan prompt mengatakan aplikasi diperbarui dengan kunci penandatangan yang berbeda. Catatan
   diganti hanya oleh jawaban pemilik, tidak pernah oleh deteksi saja.
   Perubahan label. Kartu yang label terlipatnya berbeda dari catatannya diperlakukan sama: tidak
   ditampilkan, dan prompt mengatakan kartu mengubah namanya. Keputusan adalah untuk satu nama di bawah satu kunci.
6. Penempatan. Kartu yang ditampilkan menggantikan setiap ubin paketnya: ubin biasa tidak ditampilkan
   di sebelahnya. Paket di `hidden_apps` (`config.xml`) tidak menampilkan ubin maupun kartu. Paket di
   `dock_apps` mendapat kartunya di slot dock itu. Kartu lain diurutkan tepat setelah aplikasi dock yang dikonfigurasi
   dan sebelum kisi alfabet, menurut label. Pada konfigurasi bawaan, empat aplikasi dock dan empat
   kolom, itu ubin kisi pertama. Dengan `dock_apps` lebih sedikit daripada kolom, slot dock yang kosong mengambil
   aplikasi kisi pertama (`fill_dock`, nyala secara bawaan), jadi kartu yang ditampilkan mengisi slot dock yang kosong, karena
   dock lalu hanya baris pertama; dengan `fill_dock` false slot kosong tetap kosong dan
   kartu yang ditampilkan adalah ubin kisi pertama. Di dock, rak atas menampilkan label dan cahaya memakai
   accent; di kisi, baris nama di bawah ubin yang fokus menampilkan label dan caption,
   sejauh muat dalam satu baris. Kartu memakai banner (atau ikon) milik aplikasi untuk gambarnya.
7. Sembunyi. Kartu yang disembunyikan meninggalkan ubin biasa aplikasi persis seperti sebelumnya: menyembunyikan kartu tidak pernah
   menghapus aplikasi, dan menampilkannya tidak pernah memasang apa pun. Kartu disembunyikan atau ditampilkan lagi dari
   daftar Module cards: tahan OK pada pil Settings. Daftar menampilkan setiap kartu yang ditemukan dengan
   label aplikasi, paket, digest penandatangan penuh, dan status; OK mengalihkan Shown dan Hidden.
8. Copot pemasangan. Menghapus modul menghapus kartu. Catatannya tetap, jadi pemasangan ulang dengan
   kunci penandatangan yang sama tidak butuh prompt baru.
9. Peluncuran. Kartu meluncurkan aktivitas yang mendeklarasikannya dengan komponen eksplisit, tanpa extra dan tanpa data,
   dengan cara yang sama setiap ubin diluncurkan. Tidak ada field kartu yang bisa menamai intent, URI, kelas, atau
   paket. Menahan OK pada kartu membuka halaman Info aplikasi, seperti ubin mana pun.
10. Sakelar mati. `module_cards` di `config.xml` atau overlay theme `config.json` (bawaan `true`). Kalau `false` launcher tidak pernah mencari
    kartu, tidak pernah bertanya, dan daftar Module cards hanya mengatakan tidak ada yang terpasang.

## Keamanan

- Launcher tidak pernah memasang, mengunduh, mengaktifkan, atau menonaktifkan apa pun, tidak mengikat layanan, tidak memuat
  kode, view, atau resource dari modul di luar satu berkas XML itu serta label dan gambar yang sudah
  dipakai setiap ubin, dan tidak punya izin jaringan.
- Tidak ada yang ditampilkan sampai pemilik mengatakannya, dan jawaban terikat ke kunci penandatangan. Pemilik
  menilai nama paket dan label aplikasi; launcher menilai digest.
- Kartu hanya bisa membuat aktivitasnya sendiri tampil berbeda; ia tidak bisa memulai yang lain. Meluncurkannya
  sama dengan meluncurkan ubin biasa aplikasi.
- Aturan label hanya ASCII supaya huruf yang mirip tidak bisa meniru nama lain, dan label
  yang cocok dengan aplikasi terpasang lain atau UI launcher sendiri ditolak.
- Kode kartu tidak pernah bisa menjatuhkan layar utama: kalau ada bagian yang gagal di sebuah TV, kartu dimatikan
  untuk sesi itu dengan satu baris log dan Home berperilaku seperti `module_cards` false.
- Risiko sisa: aplikasi yang memegang izin "draw over other apps" bisa menggambar di atas teks
  prompt. Ia tidak bisa menekan tombol prompt dari remote. Prompt hanya ditampilkan selagi layar
  utama punya fokus jendela dan ditutup saat layar utama meninggalkan depan.

## Penolakan

Setiap kartu yang ditolak menghasilkan satu baris di logcat di bawah tag `TvHomeCards`, dengan paket dan
alasannya, misalnya:

```
W TvHomeCards: io.example.spoof: label 'Retro' is the name of another installed app
W TvHomeCards: io.example.deep: XML nested deeper than 2
W TvHomeCards: io.example.big: label is longer than 24 characters
```

Baca dengan `adb logcat -s TvHomeCards` (atau `tools/tv.py 'logcat -d -s TvHomeCards'`). Kartu yang ditolak
tidak punya efek lain: aplikasi tetap memakai ubin biasanya.
