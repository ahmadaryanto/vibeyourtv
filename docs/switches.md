# Switches on a TV

Setiap key yang bisa diatur theme (tabel Keys di [`themes/README.md`](../themes/README.md)) dapat
screenshot yang menunjukkan efeknya di TV sungguhan. Tabel ini tempat daftarnya: repo VibeYourTV dan
[cleanyour.tv/addons#switches](https://cleanyour.tv/addons#switches) sama-sama menampilkannya dari sini.

## Adding a key

Key baru butuh screenshot. Taruh di `docs/switches/<key>.jpg` (screenshot yang sudah ada di
repo dan memang menunjukkan key itu juga dihitung) lalu tambah satu baris di bawah:

- Dari TV sungguhan, dengan key-nya nyala, sebagai JPEG atau PNG, 16:9 (1280×720 sudah cukup), di bawah 300 KB.
- Tunjukkan bagian yang berubah dan kelihatan. Kalau key memetakan tombol remote, tunjukkan
  layar yang dibuka tombol itu; kalau yang berubah cuma perilakunya, tunjukkan efek setelannya, misalnya
  panel, menu, atau aplikasi yang muncul.
- Blur yang pribadi: nama Wi-Fi, orang, alamat.

`tests/test_switches.py` memeriksa setiap key punya baris di sini, gambarnya ada, dan ukurannya
sesuai aturan. Key dari sebelum aturan ini ada di daftar yang masih ditunggu; screenshot untuk salah satunya selalu diterima.

## Screenshots

| Key | Screenshot |
|---|---|
| `columns` | <img src="../themes/six-columns/screenshot.jpg" width="320" alt="columns on a TV"> |
| `sky_intensity` | <img src="sky-sunset.jpg" width="320" alt="sky_intensity on a TV"> |
| `wallpaper_rotation_minutes` | <img src="../themes/scenes/screenshot.jpg" width="320" alt="wallpaper_rotation_minutes on a TV"> |
| `festival` | <img src="../themes/dussehra/screenshot.jpg" width="320" alt="festival on a TV"> |
| `settings_key_panel` | <img src="quick-settings-panel.jpg" width="320" alt="settings_key_panel on a TV"> |
| `home_idle_clock_minutes` | <img src="clock-screensaver.png" width="320" alt="home_idle_clock_minutes on a TV"> |
| `gamepad_passthrough` | <img src="switches/gamepad_passthrough.jpg" width="320" alt="gamepad_passthrough on a TV"> |
