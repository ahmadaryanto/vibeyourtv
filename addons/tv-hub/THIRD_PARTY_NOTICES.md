# Pemberitahuan pihak ketiga

TV Hub berlisensi MIT. APK menyertakan library berikut, semua di bawah
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0):

| Library | Version | Copyright |
| :- | :- | :- |
| AndroidX Media3: exoplayer, exoplayer-hls, ui (and their common, datasource, extractor, container, decoder modules) | 1.4.1 | The Android Open Source Project |
| AndroidX RecyclerView | 1.3.2 | The Android Open Source Project |
| AndroidX Core, Annotation, Collection, Lifecycle (pulled in by the above) | various | The Android Open Source Project |
| Kotlin standard library | 2.0.21 | JetBrains s.r.o. and Kotlin contributors |
| kotlinx.coroutines | 1.8.1 | JetBrains s.r.o. and Kotlin contributors |

Dependensi hanya untuk tes, tidak dikirim: JUnit 4 (EPL 1.0), kXML 2 (MIT-style), org.json (Public Domain / JSON licence).

Pemberitahuan yang sama ada di app di Settings > About and licences (`app/src/main/assets/licenses.txt`).

## Yang tidak disertakan

- Tidak ada kode dari [OpenTV](https://github.com/opentvproject/opentv) (GPL-3.0),
  [Dispatcharr](https://github.com/Dispatcharr/Dispatcharr) (AGPL-3.0) atau
  [Kodi](https://github.com/xbmc/xbmc) (GPL-2.0-or-later). Mereka dibaca untuk perilaku dan
  publikasi; player, parser, dan panduan TV Hub ditulis independen di atas Media3.
  TV Hub berbicara ke Dispatcharr hanya lewat alamat `/output/m3u` dan `/output/epg`
  yang didokumentasikan, jadi AGPL tidak menjangkau app ini.
- Tidak ada playlist, saluran, logo, atau kredensial. Fixture demo di `fixtures/` dibuat
  `tools/make_fixtures.py`: listing fiktif, logo warna solid dari script, dan satu
  stream uji publik, Big Buck Bunny (c) Blender Foundation, [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/).
- Banner dan ikon dari `tools/make_art.py`; wordmark memakai Outfit (SIL Open
  Font License 1.1). Tidak ada file font di APK.

Pluto TV, Tubi, Plex, Kodi, HDHomeRun, Dispatcharr, Fire TV dan Google TV merek pemiliknya,
disebut hanya untuk menjelaskan apa yang TV Hub bisa buka atau baca. TV Hub tidak afiliasi dengan mereka atau VibeYourTV.
