# Capacity Stats

Module card Android TV opsional standalone, di-maintain [lionlev21](https://github.com/lionlev21).
Build dengan JDK 17 dan Android SDK 34: `./gradlew testReleaseUnitTest lintRelease assembleRelease`.
Script gradlew kecil memakai Gradle wrapper repo yang dipin dengan folder ini sebagai proyek;
tanpa JAR wrapper disalin ke add-on dan tanpa sumber launcher. Build tetap di dalam repo.
Pasang APK hasil build lewat ADB atau installer pilihanmu. Tanpa binary atau APK prebuilt di commit.

Launcher menemukan module card statis dan tanya apakah ditampilkan. Buka card = bacaan live di app ini.
Module spec 1 punya caption statis: tidak bisa menampilkan counter live di Home. Tanpa perubahan launcher.
UI Inggris dan Ibrani, arah locale standar untuk bahasa lain. Bacaan refresh tiap sepuluh detik hanya saat
layar ini resumed; tidak ada layanan background setelah keluar.

## Permissions (izin)

Tanpa permission. Tanpa jaringan, telemetri, iklan, atau layanan background. Membaca
/proc/meminfo dan kapasitas filesystem data. RAM tersedia/total dan used/total dilabel terpisah;
"used" = total minus available, bukan RSS proses. Di kernel lama, available diestimasi
MemFree + Buffers + Cached, dibatasi MemTotal. Nilai hilang/tidak valid dihilangkan.

## Validasi

Diuji di **Sony KD-50X85J, Android TV 12**.

6 tes JVM, lintRelease dan release build lulus. Di hardware asli: kedua rasio RAM, storage, arah Ibrani,
pengenalan module-card, worker berhenti saat Back.
