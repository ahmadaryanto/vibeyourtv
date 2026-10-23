# Membaca Lanjutkan menonton

Sebuah launcher bisa menampilkan baris yang dipublikasikan aplikasi lain ke Lanjutkan menonton (Watch Next)
sistem dan kanal pratinjau. Semuanya ada di penyedia TV, di belakang `TvContract.WatchNextPrograms` dan
`TvContract.PreviewPrograms`.

- **Izin:** `android.permission.READ_TV_LISTINGS`, izin runtime. Minta lewat
  dialog biasa, atau berikan dari shell: `pm grant <package> android.permission.READ_TV_LISTINGS`.
- **Tidak diperlukan:** `com.android.providers.tv.permission.ACCESS_ALL_EPG_DATA` bersifat
  `signature|privileged`, jadi aplikasi yang di-sideload tidak bisa memegangnya. Dengan hanya `READ_TV_LISTINGS`, query mengembalikan
  baris milik pemanggil sendiri plus setiap baris yang ditandai `searchable`.
- **Query tanpa selection:** pemanggil tanpa `ACCESS_ALL_EPG_DATA` mendapat `SecurityException`
  kalau mengirim selection. Baca seluruh tabel, lalu saring di kode.
- **Sebagian aplikasi tidak memublikasikan apa pun.** Baris hanya ada kalau aplikasinya menulis dan menandai
  searchable, dan sebagian aplikasi tidak melakukan keduanya, jadi query di TV yang dituju sebelum mengandalkan suatu aplikasi.
  Query dari aplikasi yang memegang `READ_TV_LISTINGS`, bukan dari shell: shell tidak punya izin itu, jadi
  `content query --uri content://android.media.tv/watch_next_program` (atau `channel`,
  `preview_program`) mencetak `No result found.` dan tidak bisa membedakan apakah ada aplikasi yang memublikasikan baris.
- **Terlihat di Chromecast with Google TV 4K** (Google TV 14, `UTTC.250917.004`): build uji
  yang memegang `READ_TV_LISTINGS` dan query tanpa selection melihat 6 kanal dan 51 program
  pratinjau, semuanya dari YouTube dan Spotify, dan tidak ada baris Watch Next. URI poster berupa `https://` (satu
  poster Spotify selain itu, `android.resource://`), jadi pembaca butuh akses jaringan untuk menampilkan sebagian besar
  poster itu; `intent_uri` berisi URI `intent:`. Plex (`com.plexapp.android` 2026.20.0, yang meminta
  `WRITE_EPG_DATA`) tidak punya kanal, pratinjau, atau baris Watch Next yang terlihat. Tanpa
  `ACCESS_ALL_EPG_DATA` pembaca hanya melihat barisnya sendiri plus baris yang ditandai `searchable`, jadi baris Plex
  yang tidak searchable juga tidak kelihatan: probe ini tidak bisa membedakan "tidak memublikasikan apa pun" dari
  "hanya memublikasikan baris yang tidak searchable".

Sumber: `createSqlParams()` di `packages/providers/TvProvider/src/com/android/providers/tv/TvProvider.java`
(AOSP) memegang logika `READ_TV_LISTINGS` dan `searchable`; kedua izin didefinisikan di
`AndroidManifest.xml` modul itu.
