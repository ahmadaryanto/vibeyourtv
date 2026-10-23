# TCL 55P71B Pro (G09, Google TV 12): helper tombol terikat merugikan auto-repeat remote

Laporan pemilik: [#763](https://github.com/Gathin23/cleanyourtv/issues/763). Satu unit, satu sesi: TCL
55P71B Pro "Smart TV Pro", perangkat `G09`, Android 12 (Google TV), firmware `STT2.230203.001.AS23`,
Amlogic.

## Gejala

Tahan volume naik atau turun dan volume bergerak satu langkah, di layar home dan di dalam aplikasi. Menekan
tombol sebanyak itu memberi langkah sebanyak itu. Remote belum tentu masalah dan debloat juga bukan:
remote yang sama naik normal lagi begitu tidak ada layanan aksesibilitas filter tombol terikat, dan ke mana repeat
pergi satu `getevent -lt` saja — jika kernel masih mencetaknya saat tombol ditekan, repeat dibuang di atas driver.

## Bisect

Pemilik mengaktifkan dua helper opsional launcher satu per satu lalu bersama:

| `enabled_accessibility_services` | holding volume up/down |
| --- | --- |
| both `HomeKeyService` and `InputKeyService` | one step per press |
| `HomeKeyService` alone | one step per press |
| `InputKeyService` alone | one step per press |
| `""`, with `accessibility_enabled 0` | ramps continuously |

All or nothing. This is not one helper mishandling `VOLUME_UP`: the dump while broken lists both as
`Service[label=Home button, capabilities=8]` and `Service[label=Home, capabilities=8]`, and capability
8 is `CAPABILITY_CAN_FILTER_KEY_EVENTS`. Any bound service that asks to filter key events puts this
firmware's input path in a mode where the first press arrives and the repeats that follow are dropped.

Baca state tanpa mengubah apa pun:

```sh
tools/tv.py 'settings get secure enabled_accessibility_services'
tools/tv.py 'settings get secure accessibility_enabled'
tools/tv.py 'dumpsys accessibility'
```

Untuk mengembalikan repeat untuk uji, dan helper kembali setelahnya:

```sh
# keep the string the first read printed; the empty list also unbinds TalkBack and any other service
tools/tv.py "settings put secure enabled_accessibility_services ''; settings put secure accessibility_enabled 0"
tools/tv.py "settings put secure enabled_accessibility_services com.gathin.tvhome/com.gathin.tvhome.HomeKeyService:com.gathin.tvhome/com.gathin.tvhome.InputKeyService; settings put secure accessibility_enabled 1"
```

Baris kedua dua helper launcher, bukan "nilainya": write back what the read printed. `theme.sh` sudah menaruh ulang daftar setelah push tema, `am force-stop com.gathin.tvhome` menjatuhkannya sendiri
(lihat [TROUBLESHOOTING](../TROUBLESHOOTING.md)), dan menjalankan ulang `launcher/build-and-install.sh`
tanpa `INPUT_KEY_SERVICE=1`/`HOME_KEY_SERVICE=1` membiarkan helper mati.

## The launcher is not the one eating the repeats

Kedua helper return `false` for keys they do not map, so a volume key never reaches launcher code and
is not consumed there: `InputKeyService.onKeyEvent` passes anything that is not in `app_keys`,
`input_key_keycodes`, `KEYCODE_TV` or `KEYCODE_TV_INPUT`, and `HomeKeyService` passes everything except
`KEYCODE_ALL_APPS` and the settings keycodes/scancodes. The repeat is lost before the app sees the
event. What the report does not contain is an `onKeyEvent` line for a held volume key, so "the
platform's filter drops the repeat" is the reporter's reading of the bisect, not a captured log. A
future report that logs the repeat event arriving at a helper and being passed through would move the
blame back into the launcher.

## Apa yang helper beli, dan biayanya

Tombol yang helper ada untuk di remote ini:

| Button | What arrives | Helper |
| --- | --- | --- |
| Gear (TV settings) | `KEYCODE_NOTIFICATION` 83, scancode 561 | `settings_key_keycodes` / `settings_key_scancodes`, `HOME_KEY_SERVICE=1` |
| Netflix | vendor keycode 4062, scancode 472 | `app_keys`, `INPUT_KEY_SERVICE=1` |
| Prime Video | vendor keycode 4099 | `app_keys` |
| YouTube | vendor keycode 4065 | `app_keys` |
| Input | `KEYCODE_TV` / `KEYCODE_TV_INPUT` | `InputKeyService`, and TCL draws that panel itself on this set |

Vendor keycodes reach nothing on their own: `com.tcl.ttvs`'s `KeyDownBehaviorReceiver` logs
`keyString=4062 keyCode=4062 type=IR` and acts on nothing. These buttons were answered by the stock
home, and the [G09 example list](../../examples/tcl-g09-google-tv/debloat.txt) disables launcherx, so
after the cleanup only a helper can answer them. That is where the trade comes from: on this firmware
the buttons and the volume ramp cannot both be had, because both need the same filter capability.

## The same trade on a Xiaomi MediaTek set

Pemilik kedua melaporkan [#944](https://github.com/Gathin23/cleanyourtv/issues/944): Xiaomi
MiTV-MOSR4 (`irobot`, MediaTek), Google TV / Android 11 (SDK 30), firmware `RTM5.220609.003.1265`.
Holding volume up with `InputKeyService` and `HomeKeyService` bound moves it one step, and
`InputKeyService` alone is still one step; `dumpsys accessibility` lists the bound service with
`capabilities=8`. Same symptom, same capability, second vendor and second Android version.

Dua hal beda dari bisect di atas, keduanya perlu dipertahankan. The empty-list control was not run:
di set ini mengosongkan daftar melepas tombol yang helper dipasang — dengan launcherx
nonaktif, tombol Input, Settings, YouTube dan Netflix hanya sampai
`com.xiaomi.android.tvsetup.partnercustomizer/.GlobalKeyReceiver`, yang memulai aktivitas launcherx dan
crash (catatan [MOSR4](xiaomi-mitv-mosr4.md) punya bagian itu, dan baris instalnya pass kedua helper).
So "the ramp returns with no service bound" is still measured on the G09 alone. And the build string in
the issue is not the one that note carries (`RTM5.220609.003/1585`, written on another unit of the same
model), so treat the two as separate units until both fingerprints are read: the model, the firmware
family and the behaviour are what they agree on.

Itu cukup melepas mekanisme dari "firmware ini": tombol volume dapat pertanyaan awal dari window policy di Android 11 dan 12, di dua vendor, saat layanan terikat memegang
`CAPABILITY_CAN_FILTER_KEY_EVENTS`. Trade dan pilihan di bawah sama di kedua set. Saran reporter sendiri — helper yang mensintesis repeat dengan `adjustSuggestedStreamVolume` saat
tombol volume ditekan — kode launcher, dan switch off-by-default paling banget; is not in this note.

## Choices

- **Keep the repeat and skip the helpers.** Do not pass `INPUT_KEY_SERVICE=1` or `HOME_KEY_SERVICE=1`
  when installing. The price is the gear button and the remote's app buttons: see the
  [G09 example](../../examples/tcl-g09-google-tv/README.md), whose install line passes
  `INPUT_KEY_SERVICE=1` for exactly those buttons, so a set that behaves like this one pays for them
  with the volume ramp. The Input button keeps working there without any helper.
- **Keep the buttons and accept stepwise volume.** The volume keys still change the volume, one step
  per press; the launcher cannot make the platform ramp it again. Setting volume from a phone app or
  through an AVR/soundbar remote was not compared on this set.
- **Keep launcherx enabled and bind no helper.** The stock home answers those buttons, so the repeat
  survives. This is the only configuration that has both, and the price is the stock home screen the
  cleanup was replacing.

It is a toggle, not a fix: two one-line `settings` writes switch between the two behaviours whenever
you want, but never both at once.

## Limits

One unit, one firmware build (`STT2.230203.001.AS23`), one session. Only volume up/down was held:
channel up/down, a held D-pad (seek) and the media keys were not tried, so "auto-repeat" here means the
key that was measured. Whether a filter service that is not the launcher's (TalkBack, a remote app)
does the same is untested; the capability in the dump is the only common factor seen. No cold boot, no
second remote. The two other G09 units in this repo (UK `G09_4K_GB` AS24 in Brazil, and
`G09_4K_GB_32BIT` AS10) never looked at this, so the claim belongs to the build, not to "G09". No
launcher-side fix is offered, and none should be expected: an app can decline the key - these two
already do - but cannot hand a repeat that the platform dropped back to the input stack, and putting
`VOLUME_UP` in `app_keys` would open an app rather than ramp the volume.
