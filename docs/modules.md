# Membuat modul

Modul adalah aplikasi Android TV biasa yang mendeklarasikan kartu untuk launcher VibeYourTV. Repomu, lisensimu,
kunci penandatanganmu, dirilis sebagai APK di mana pun kamu mau, misalnya di halaman GitHub
Releases dengan SHA-256-nya. Pemilik TV men-sideload-nya seperti aplikasi lain. Launcher menemukan
kartu itu dan bertanya sekali; kalau mereka menjawab Show, aplikasimu tampil sebagai kartu sejak itu:
labelmu, keterangan opsional, dan warnamu di cahaya ambient, diletakkan di depan kisi
alfabet.

Kontraknya [`docs/specs/card.md`](specs/card.md). Halaman ini versi singkatnya.

## 1. Deklarasikan kartu

Pada aktivitas yang sudah punya filter `LEANBACK_LAUNCHER`, tambah filter kedua dan entri
`meta-data`:

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

Lalu buat `res/xml/module_card.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<card spec="1" label="Retro" caption="NES games" accent="#E8A33D" />
```

- `label`: 1 sampai 24 karakter, huruf, angka, spasi, dan `. & + ' -`. Buat pendek; ini
  nama di ubin dan di rak atas. Kata seperti "verified" atau "official", nama UI milik launcher,
  dan nama input seperti HDMI ditolak, begitu juga kartu baru yang labelnya (atau label
  aplikasinya) sama dengan nama aplikasi lain yang terpasang di TV (daftar lengkapnya ada di spec). Mengganti
  label di pembaruan membuat pemilik memutuskan lagi, jadi pilih sekali.
- `caption`: opsional, sampai 32 karakter, tampil setelah label saat kartu fokus. Kata
  endorsement yang ditolak di label juga ditolak di sini. Baris
  nama di bawah ubin satu baris; label dan caption bersama-sama menampilkan sekitar 26 karakter di
  kisi 4 kolom, jadi caption cukup beberapa kata.
- `accent`: opsional `#RRGGBB`, warna cahaya di belakang kartu saat fokus di dock.
- Tulis nilainya sebagai teks literal. Referensi `@string` tidak dipecahkan di spec 1.

Kartu harus berada di aktivitas yang sama dengan filter `LEANBACK_LAUNCHER`-mu, dan aktivitas itu harus
exported dan aktif. Kalau tidak, kartu dibuang.

## 2. Buat tetap ringan

Launcher VibeYourTV menyasar TV dengan RAM 1 GB, dan pemilik mempercayai apa yang mereka taruh di layar utama.

- Jangan pakai layanan latar kecuali fiturnya memang butuh, dan sebutkan di README.
- Tanpa telemetri kecuali opt-in. Tanpa iklan.
- Jangan ikutkan konten yang tidak hakmu untuk disebarluaskan.
- Pakai karyamu sendiri. Jangan pakai logo perusahaan lain sebagai banner atau ikon.
- Jelaskan setiap izin yang kamu minta di README.

## 3. Uji di TV

Pasang lewat ADB, tekan Home, dan prompt muncul:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell input keyevent KEYCODE_HOME
```

(`tools/tv.py` di repo ini jalan di tempat biner `adb` diblokir; lihat README.)

Pemeriksaan yang berguna:

```bash
# Apakah filter kartu dan meta-data mendarat di aktivitas yang benar?
adb shell pm dump io.github.udhaykumarbala.retro | grep -B2 -A6 DOCK_CARD

# Kenapa kartu ditolak? Satu baris per alasan.
adb logcat -s TvHomeCards
```

Lalu: jawab Keep hidden dan cek aplikasi tetap punya ubin biasa; tahan OK di Settings, buka daftar
Module cards, setel ke Shown, dan cek kartu menggantikan ubin; tekan OK pada kartu
dan cek aplikasimu terbuka; copot pemasangan dan cek kartunya hilang. Ambil screenshot kartu
yang fokus di kisi untuk README-mu.

## 4. Rilis dan simpan kuncimu

Jawaban pemilik terikat ke sertifikat penandatanganmu. Publikasikan digest sertifikat di setiap
rilis supaya orang bisa memeriksa yang mereka pasang:

```bash
apksigner verify -print-certs app-release.apk | grep 'SHA-256'
```

(`apksigner` menerima opsi dengan satu tanda hubung maupun dua.)

Baris "Signer #1 certificate SHA-256 digest" adalah yang dihitung launcher di TV. Tanda tangani setiap
pembaruan dengan kunci yang sama: kunci berbeda membuat setiap TV bertanya lagi, dan kartu tetap tersembunyi
sampai pemilik menjawab. Tanda tangani dengan satu sertifikat; launcher menolak APK dengan beberapa penandatangan.
Jangan taruh keystore di repo.

Publikasikan juga SHA-256 APK itu sendiri di sebelahnya.

## Modul acuan

Retro, pustaka NES untuk TV, mendeklarasikan kartu persis seperti di atas:
<https://github.com/udhaykumarbala/tv-vibehack-retro>.

Launcher yang ingin baris Lanjutkan menonton dari aplikasi lain bisa membacanya, lihat
[`docs/watch-next.md`](watch-next.md).
