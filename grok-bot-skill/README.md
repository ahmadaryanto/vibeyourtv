# Skill Grok Bot untuk VibeYourTV

Skill ini mengajari asisten AI cara menyiapkan Android TV atau Google TV dari komputer, lewat Wi-Fi dan `adb`. Tidak perlu aplikasi di ponsel.

Yang tercakup: menghidupkan opsi pengembang dan debugging nirkabel, memasang platform-tools, `adb connect`, mencadangkan daftar paket, menonaktifkan bloat dengan `pm disable-user --user 0` sambil menyisakan aplikasi streaming yang kamu pilih, menghapus aplikasi hanya setelah konfirmasi, memeriksa pemilik Wi-Fi sebelum mematikan setupwraith, membangun dan memasang peluncur lalu menjadikannya Beranda, serta menerapkan tema (`config.json`, `wallpaper.jpg`, `theme_revision`).

## Cara menambahkannya ke Grok Bot

1. Salin folder `grok-bot-skill` ini ke folder skill bot, dengan nama yang sama seperti di frontmatter:
   `vibeyourtv-setup/SKILL.md`
2. Pastikan `SKILL.md` tetap punya blok YAML di atas dengan `name: vibeyourtv-setup` dan `description`.
3. Mulai obrolan baru supaya bot membaca skill itu.
4. Minta dengan kalimat biasa, misalnya: "Siapkan TV Android saya dari PC lewat Wi-Fi, tanpa aplikasi di ponsel."

Bot akan menanyakan IP TV (`TV_IP`), aplikasi yang ingin kamu simpan, dan konfirmasi sebelum langkah yang sulit dikembalikan. Jangan tempel nama, foto, atau alamat asli ke dalam repo.
