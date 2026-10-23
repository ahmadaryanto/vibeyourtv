<!-- Terima kasih. Satu atau dua kalimat tentang apa yang berubah dan TV tempat ini dicoba sudah cukup. -->

**Dicoba di:** <!-- model, versi Android -->

### Kalau ini menambah atau mengubah daftar TV
- [ ] `examples/<brand>-<model>/debloat.txt`, header dengan model, versi Android, dan build firmware
- [ ] Hanya paket yang benar-benar saya nonaktifkan dan pakai sehari-hari; apa pun yang harus saya aktifkan lagi dicatat
- [ ] Satu baris di `examples/README.md` dan satu di tabel TV README
- [ ] Tidak ada yang di daftar ini yang meng-uninstall

### Kalau ini menyentuh launcher
- [ ] Dicek di TV sungguhan, screenshot dilampirkan
- [ ] D-pad dicek dari setiap tepi (Up, Down, Left, Right)
- [ ] Tanpa jaringan, tanpa layanan latar, tanpa izin baru
- [ ] String baru di `values/strings.xml` dan folder `values-*` yang lain
- [ ] Apa pun yang khusus satu TV ada di `config.xml` atau tema, bukan di kode

### Kalau ini menambah atau mengubah tema
- [ ] `themes/<name>/config.json` dan `themes/<name>/screenshot.jpg`: Home di TV sungguhan dengan tema itu, JPEG 1280×720 di bawah 300 KB
- [ ] Satu baris di `themes/README.md` yang menampilkan screenshotnya

### Kalau ini menambah kunci tema (sebuah sakelar)
- [ ] Screenshot kunci itu di TV sungguhan dan barisnya di `docs/switches.md`

### Kalau ini menambah add-on
- [ ] `addons/<name>/screenshot.jpg`: add-on di TV sungguhan, JPEG 16:9 di bawah 300 KB, tampil di barisnya di `addons/README.md`
