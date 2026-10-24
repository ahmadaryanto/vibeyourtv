---
name: vibeyourtv-setup
description: >-
  Set up an Android TV or Google TV from a PC over Wi-Fi with adb, with no
  phone app. Use when the user wants Developer options, wireless debugging,
  a package-list backup, debloating with pm disable-user, an optional
  confirmed uninstall to free storage, the VibeYourTV launcher as Home, or a
  theme (config.json, wallpaper, greeting_name, crypto_ticker). Always
  confirm before a destructive step and keep every change reversible.
---

# VibeYourTV setup over Wi-Fi

Guide a person through setting up their own Android TV or Google TV from a computer on the same Wi-Fi. Talk to them in the language they use. Use placeholder addresses such as `TV_IP` and `192.168.1.50`. Never ask for, store, or write down a real name, photo, serial, MAC, Android ID, Wi-Fi name, account email, or ADB key in the repo or in a commit.

The launcher package stays `com.gathin.tvhome`. Do not rename it. Prefer the scripts in this repo (`tools/`, `launcher/build-and-install.sh`) over inventing new ones.

## Safety

- Disable with `pm disable-user --user 0`, one package per call. That is reversible with `pm enable --user 0`.
- Uninstall only after the person names the apps and confirms. Say clearly that uninstall is not undone by the restore script. Prefer disable.
- Never disable Android core packages: `com.android.systemui`, `com.android.settings`, `com.android.tv.settings`, `com.android.shell`, `com.android.phone`, `android`, the permission controller, the package installer, `com.android.providers.*` (except calendar, contacts, user dictionary, and blocked numbers), `com.google.android.ext.services`, module metadata, and `com.google.android.overlay.modules*`.
- Leave the TV app, HDMI and tuner input services, the input menu, remote and Bluetooth pairing, the on-screen keyboard, Play Store, and Play Services enabled.
- Never start a factory or test package (`com.cvte.tv.hkcfac`, `mediatek.factorymenu.ui`, and similar).
- On a CVTE board (any `com.cvte.*` package), leave `com.cvte.tv.launcher.*` enabled unless a matching `examples/` list for that exact board disables it.
- Do not disable the stock launcher until the new launcher is installed and `dumpsys activity activities` shows it as the focused Home.
- Confirm before every destructive step. Stop if `pm disable-user` is refused for every package, a sideload fails verification, or the TV's SDK is below 26.

## 1. On the TV

Walk them through the remote, one step at a time:

1. Settings → Device Preferences → About.
2. Press OK on **Build** seven times until Developer options unlock.
3. Back, then Developer options.
4. Turn on **USB debugging**.
5. If there is a **Network debugging** or **ADB debugging** / wireless debugging toggle, turn that on too. Note the IP and port the TV shows. The usual port is `5555`.
6. Leave the TV awake.

## 2. On the computer

Install Android platform-tools (the `adb` binary), not a phone app:

- Linux: the distro package `android-tools-adb` or the official platform-tools zip, then `adb version`.
- macOS: `brew install android-platform-tools`, or the official zip.
- Windows: the official platform-tools zip, and use `adb.exe` from that folder.

The computer and the TV must be on the same LAN.

```bash
adb connect TV_IP:5555
adb devices
```

Accept **Allow USB debugging?** on the TV. `adb devices` must show `device`, not `unauthorized` or `offline`.

## 3. Back up first

```bash
mkdir -p backup
adb shell pm list packages > backup/packages.txt
adb shell pm list packages -d > backup/disabled.txt
adb shell getprop > backup/getprop.txt
adb shell settings list system > backup/settings-system.txt
adb shell settings list secure > backup/settings-secure.txt
adb shell settings list global > backup/settings-global.txt
```

Keep that folder. Do not commit it.

## 4. Debloat

List third-party and obvious bloat (`pm list packages -3`, plus known ad, telemetry, and recommendation packages). Show the person a checklist of streaming apps, the voice assistant, Chromecast, Miracast, and the firmware updater. Disable only what they do not tick.

```bash
adb shell pm disable-user --user 0 PACKAGE
```

One package per call. After each small batch, ask them to look at the TV. To undo one package: `adb shell pm enable --user 0 PACKAGE`. A full undo is `tools/restore.sh` when the repo's disable script was used.

### Optional uninstall

Only to free storage, only for packages the person named, only after a second confirmation. Disabled packages do not free as much space as removed ones, and uninstall cannot be restored from the disable list.

```bash
adb shell pm uninstall --user 0 PACKAGE
```

Skip system packages the TV needs to boot. If uninstall is refused, leave the package disabled.

### setupwraith and Wi-Fi

Before disabling `com.google.android.tungsten.setupwraith` (or any setup wizard that owns Wi-Fi), run:

```bash
adb shell dumpsys wifi | grep -o 'cname=[^ ]*'
```

If that wizard owns the saved Wi-Fi network, disabling it drops the TV off Wi-Fi and drops ADB. Do not disable it in that case unless the person is on Ethernet and agrees. `tools/disable.sh` enforces the same check.

## 5. Launcher as Home

From this repo, with JDK 17:

```bash
TV_HOST=TV_IP launcher/build-and-install.sh
```

Or install an APK already built from this repo:

```bash
adb connect TV_IP:5555
adb install -r launcher/app/build/outputs/apk/release/app-release.apk
adb shell cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity
```

Confirm Home:

```bash
adb shell dumpsys activity activities | grep mFocusedApp
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

The focused app must be `com.gathin.tvhome`. Reboot once and check that Home and ADB both come back before disabling anything else. Do not enable a remote-key helper in the same pass as the first Home handover.

Disable the stock launcher only after that confirmation, still with `pm disable-user --user 0`.

## 6. Theme, wallpaper, greeting, crypto ticker

Prefer `TV_HOST=TV_IP tools/theme.sh themes/<name>`. That script writes `config.json`, `wallpaper.jpg`, and `theme_revision`, then restarts Home.

To push files by hand, stage them and write through the theme provider. The provider creates the file as the launcher, which matters on Android 11 and later:

```bash
adb push config.json /data/local/tmp/config.json
adb push wallpaper.jpg /data/local/tmp/wallpaper.jpg
adb shell 'content write --uri content://com.gathin.tvhome.theme/config.json < /data/local/tmp/config.json'
adb shell 'content write --uri content://com.gathin.tvhome.theme/wallpaper.jpg < /data/local/tmp/wallpaper.jpg'
adb shell 'printf %s REVISION > /data/local/tmp/theme_revision'
adb shell 'content write --uri content://com.gathin.tvhome.theme/theme_revision < /data/local/tmp/theme_revision'
```

If `content write` prints `Unsupported operation`, copy into the launcher's files directory instead:

```bash
adb shell cp /data/local/tmp/config.json /sdcard/Android/data/com.gathin.tvhome/files/config.json
adb shell cp /data/local/tmp/wallpaper.jpg /sdcard/Android/data/com.gathin.tvhome/files/wallpaper.jpg
adb shell cp /data/local/tmp/theme_revision /sdcard/Android/data/com.gathin.tvhome/files/theme_revision
```

`config.json` keys stay English. Useful ones:

- `greeting_name`: a name after the time-of-day greeting. Ask what they want shown. Do not invent a name.
- `crypto_ticker`: `true` shows a live BTC price under the date (CoinGecko, every 5 minutes while Home is resumed). Default is false.
- `wallpaper`: `true` to show `wallpaper.jpg`.

For a photo wallpaper, ask for a 16:9 image (1920×1080 is enough). Faces should sit in the middle third, with space at the edges, because the TV crops the sides.

Restart Home afterwards:

```bash
adb shell am force-stop com.gathin.tvhome
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
```

Undo a theme with `tools/theme.sh --reset`, or `content delete --uri content://com.gathin.tvhome.theme/config.json` and the same for `wallpaper.jpg`.

If `theme_picker` is true (`config.xml`, or `"theme_picker": true` in `config.json`), the person can switch looks on the TV with the round Tema button or the Menu key. The built-in presets are Saya, Orang Tua, Anak, Remaja, Salju, Musim Gugur, Musim Dingin, and Musim Semi. A pushed `config.json` is not overwritten by those presets; it shows up in the picker as Kustom, and a new push selects Kustom again. Seasonal motion (snow, leaves, frost, petals) stays off the wallpaper file and can be turned off in the picker. The Anak preset can ask for a 4-digit PIN before leaving; do not invent a PIN.

## 7. Close ADB when finished

After the person says they are done: turn USB debugging and network debugging off on the TV, revoke USB debugging authorisations, and reboot. Do not leave port 5555 open.
