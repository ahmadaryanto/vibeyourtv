# tools/relay.py

Meneruskan port di komputer ini ke port ADB TV. Pakai saat asisten coding menjalankan perintah di sandbox yang bisa reach komputer ini tapi bukan perangkat di jaringan lokal. Claude Code di macOS sering begini: privasi Local Network memblokir sandbox, jadi `tools/tv.py` timeout atau "No route to host" padahal TV merespons dari terminal biasa.

```bash
# Di terminal biasa (Terminal, iTerm, VS Code), biarkan jalan:
tools/venv/bin/python -I tools/relay.py 192.168.1.50

# Lalu, di mana pun asisten menjalankan tools:
export TV_HOST=127.0.0.1 TV_PORT=15555
tools/venv/bin/python -I tools/tv.py --probe
```

- **Opsi:** `--tv-port` kalau port ADB TV bukan 5555; `--port` untuk port lokal lain (satu relay per TV, masing-masing port sendiri). Alamat TV default dari `$TV_HOST`.
- **Tool mencetak `TV_HOST`/`TV_PORT` yang dipakai**, plus satu baris setiap kali TV tidak bisa dijangkau.
- **Hentikan dengan Ctrl-C** kalau sudah selesai.

## Keamanan

- **Hanya listen di `127.0.0.1`**, tidak di jaringan, dan tidak ada opsi mengubahnya. Perangkat lain tidak bisa memakainya untuk reach TV kamu.
- **Program di komputer ini bisa reach port ADB TV selama relay jalan.** ADB tetap butuh kunci yang TV sudah terima ("Allow USB debugging?"), tapi hentikan relay kalau sudah selesai, seperti menutup ADB jaringan.

## Kalau gagal

- **"cannot reach 192.168.1.50:5555"**, dari relay: TV mati atau standby (beberapa TV hilang dari jaringan), di jaringan lain, atau network debugging dimatikan. Tools lalu melihat connection reset by peer.
- **"cannot listen on 127.0.0.1:15555"**: relay sudah jalan di port itu. Pakai yang ada, atau pilih `--port` lain.
- **"that is the relay itself"**: alamat TV diberikan sebagai `127.0.0.1` dengan port relay sendiri. Berikan IP TV yang sebenarnya.
