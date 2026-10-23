# tools/flamechart.py

Gambar PNG flame chart sebelum/sesudah dari trace `tools/trace.py`: satu jalur per thread, kedua panel pakai skala waktu yang sama, pekerjaan yang kamu ubah ditandai magenta plus totalnya. Pakai ini untuk menunjukkan *dari mana* perbedaan di `tools/trace_report.py` berasal; lampirkan tabel laporan di sampingnya.

```bash
tools/venv/bin/pip install -r tools/requirements-perf.txt     # adds Pillow
tools/venv/bin/python tools/flamechart.py traces/before traces/after \
    --highlight '^(activityResume|RV OnBindView)$' \
    --title 'Back to Home: Home press to first frame' \
    --subtitle 'TCL C755, Google TV 14 · magenta: onResume and tile binding' \
    --out chart.png
```

| Opsi | Default | Catatan |
|---|---|---|
| `TRACE ...` | | Sebelum, lalu sesudah (maks. 4). Folder = run median, menurut yang pertama dari waktu ke frame pertama, median frame time, dan median draw RenderThread yang ada di setiap run; panel menyebut run dan ukurannya. Folder tanpa ketiganya di setiap run ditolak. |
| `--highlight REGEX` | `^(activityResume\|RV OnBindView)$` | Nama slice yang digambar magenta dan dijumlahkan. Anak dari yang cocok dihitung sekali. |
| `--window` | `first-frame` | `first-frame`: dari launcher start sampai frame pertama digambar. `start:MS`: rentang tetap dari awal. `all`: seluruh rekaman (selalu untuk `nav`). |
| `--threads` | `main,RenderThread` | Jalur, menurut nama thread di proses launcher. |

Perubahan Home biasanya butuh dua chart:
- **Saat start:** default, dengan `--highlight` di slice yang kamu hilangkan.
- **Frame setelahnya:** `--window start:1500 --threads RenderThread --highlight '^DrawFrame'`.

## Aturan

- **Pilih `--highlight` sebelum melihat hasil.** Sebut slice yang harus hilang karena perubahanmu, baru gambar. Regex yang disesuaikan setelah melihat apa yang mengecil tidak membuktikan apa-apa.
- **Kirim folder, bukan satu run.** Tool memilih run median. Kalau kirim trace tunggal, jelaskan kenapa.
- **Magenta hanya waktu main dan render thread.** Pekerjaan pindah ke thread lain atau GPU belum tentu hilang: tambahkan thread itu sebagai jalur, atau pakai angka GPU dari `nav`.

Metode before/after lengkap untuk PR ada di [`docs/workflows/perf-ab.md`](../docs/workflows/perf-ab.md).
