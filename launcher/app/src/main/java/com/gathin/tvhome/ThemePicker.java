package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Remote list of the built-in presets. OK applies one and leaves config.json on disk as Kustom.
 * A list, not a hand-built card sheet: the sheet did not fit the release size gate.
 */
final class ThemePicker implements DialogInterface.OnClickListener, DialogInterface.OnKeyListener {
    private static final int LEAVE = 1;
    private static final int CURRENT = 2;
    private static final int NEW = 3;
    private static final int AGAIN = 4;
    private static final String NAMES =
            "Saya · foto, 5 kolom, BTC\n"
                    + "Orang Tua · besar, foto, BTC\n"
                    + "Anak · ubin besar, tanpa pengaturan\n"
                    + "Remaja · gelap, glow\n"
                    + "Salju · putih biru, salju\n"
                    + "Musim Gugur · oranye, daun\n"
                    + "Musim Dingin · biru es\n"
                    + "Musim Semi · pastel, kelopak\n"
                    + "Kustom · config.json";

    private final Activity activity;
    private final SharedPreferences prefs;
    private final String[] name;
    private final String active;
    private String[] ids = new String[0];
    private AlertDialog pinDialog;
    private TextView dots;
    private final int[] digits = new int[4];
    private int cursor;
    private int mode;
    private String pending = "";
    private String target = "";

    private ThemePicker(Activity activity) {
        this.activity = activity;
        prefs = activity.getSharedPreferences(Config.SETTINGS_NAME, Activity.MODE_PRIVATE);
        name = NAMES.split("\n");
        active = ThemePresets.activeId(prefs.getString(ThemePresets.PRESET_KEY, ""), custom(activity));
    }

    static void open(Activity activity) {
        new ThemePicker(activity).show();
    }

    private void show() {
        boolean custom = custom(activity);
        int presets = ThemePresets.BUILTIN.length + (custom ? 1 : 0);
        ids = new String[presets + 3];
        String[] labels = new String[ids.length];
        int checked = -1;
        for (int i = 0; i < ThemePresets.BUILTIN.length; i++) {
            ids[i] = ThemePresets.BUILTIN[i];
            labels[i] = name[i];
            if (ids[i].equals(active)) checked = i;
        }
        int n = ThemePresets.BUILTIN.length;
        if (custom) {
            ids[n] = ThemePresets.KUSTOM;
            labels[n] = name[8];
            if (ThemePresets.KUSTOM.equals(active)) checked = n;
            n++;
        }
        ids[n] = "ticker";
        labels[n++] = prefs.getBoolean(ThemePresets.TEEN_TICKER_KEY, false)
                ? "Ticker BTC: Nyala" : "Ticker BTC: Mati";
        ids[n] = "motion";
        labels[n++] = prefs.getBoolean(ThemePresets.SEASON_MOTION_KEY, true)
                ? "Gerak musim: Nyala" : "Gerak musim: Mati";
        ids[n] = "pin";
        labels[n] = ThemePresets.pinSet(stored()) ? "PIN anak: aktif" : "PIN anak: belum diatur";
        new AlertDialog.Builder(activity).setTitle("Tema").setSingleChoiceItems(labels, checked, this).show();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which < 0 || which >= ids.length) return;
        String id = ids[which];
        dialog.dismiss();
        if ("ticker".equals(id)) {
            boolean on = !prefs.getBoolean(ThemePresets.TEEN_TICKER_KEY, false);
            prefs.edit().putBoolean(ThemePresets.TEEN_TICKER_KEY, on).commit();
            if (ThemePresets.REMAJA.equals(active)) reload();
            else show();
            return;
        }
        if ("motion".equals(id)) {
            boolean on = !prefs.getBoolean(ThemePresets.SEASON_MOTION_KEY, true);
            prefs.edit().putBoolean(ThemePresets.SEASON_MOTION_KEY, on).commit();
            if (ThemePresets.hasSeason(active)) reload();
            else show();
            return;
        }
        if ("pin".equals(id)) {
            mode = ThemePresets.pinSet(stored()) ? CURRENT : NEW;
            showPin();
            return;
        }
        if (id.equals(active)) return;
        if (ThemePresets.leavingKidsNeedsPin(active, id, ThemePresets.pinSet(stored()))) {
            target = id;
            mode = LEAVE;
            showPin();
            return;
        }
        ThemePresets.savePreset(prefs, id);
        if (!activity.isFinishing()) activity.recreate();
    }

    private void showPin() {
        cursor = 0;
        for (int i = 0; i < 4; i++) digits[i] = 0;
        dots = new TextView(activity);
        dots.setGravity(Gravity.CENTER);
        dots.setTextSize(22);
        dots.setPadding(0, 28, 0, 28);
        paint();
        pinDialog = new AlertDialog.Builder(activity).setTitle(title()).setView(dots).setOnKeyListener(this).show();
        dots.requestFocus();
    }

    public boolean onKey(DialogInterface dialog, int code, KeyEvent event) {
        if (dialog != pinDialog || event.getAction() != KeyEvent.ACTION_DOWN) return false;
        int dig = ThemePresets.remoteDigit(code);
        if (dig >= 0) {
            digits[cursor] = dig;
            if (cursor < 3) cursor++;
            else finish(codeOf());
            paint();
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_LEFT) {
            cursor = cursor == 0 ? 0 : cursor - 1;
            paint();
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (cursor == 3) finish(codeOf());
            else cursor++;
            paint();
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN) {
            int step = code == KeyEvent.KEYCODE_DPAD_UP ? 1 : 9;
            digits[cursor] = (digits[cursor] + step) % 10;
            paint();
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER
                || code == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            finish(codeOf());
            return true;
        }
        if (code == KeyEvent.KEYCODE_DEL && mode == NEW && ThemePresets.pinSet(stored())) {
            ThemePresets.savePin(prefs, "");
            pinDialog.dismiss();
            show();
            return true;
        }
        return false;
    }

    private void finish(String pin) {
        String stored = stored();
        if (mode == LEAVE || mode == CURRENT) {
            if (!ThemePresets.pinOk(stored, pin)) {
                Toast.makeText(activity, "PIN salah", Toast.LENGTH_SHORT).show();
                return;
            }
            pinDialog.dismiss();
            if (mode == LEAVE) {
                ThemePresets.savePreset(prefs, target);
                if (!activity.isFinishing()) activity.recreate();
            } else {
                mode = NEW;
                showPin();
            }
            return;
        }
        if (mode == NEW) {
            pending = pin;
            mode = AGAIN;
            pinDialog.dismiss();
            showPin();
            return;
        }
        if (!pin.equals(pending)) {
            Toast.makeText(activity, "PIN tidak sama", Toast.LENGTH_SHORT).show();
            return;
        }
        ThemePresets.savePin(prefs, pin);
        pinDialog.dismiss();
        show();
    }

    private String codeOf() {
        return "" + digits[0] + digits[1] + digits[2] + digits[3];
    }

    private void paint() {
        if (dots == null) return;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) mask.append(' ');
            mask.append(i == cursor ? "[" + digits[i] + "]" : String.valueOf(digits[i]));
        }
        dots.setText(mask.toString());
    }

    private String title() {
        if (mode == LEAVE) return "PIN untuk keluar dari Anak";
        if (mode == CURRENT) return "PIN saat ini";
        if (mode == NEW) return "PIN baru, 4 angka";
        return "Ulangi PIN";
    }

    private void reload() {
        ThemePresets.bump(prefs);
        if (!activity.isFinishing()) activity.recreate();
    }

    private String stored() {
        return prefs.getString(ThemePresets.PIN_KEY, "");
    }

    private static boolean custom(Activity activity) {
        File dir = Config.dir(activity);
        if (dir == null) return false;
        File file = new File(dir, Config.FILE_NAME);
        return file.isFile() && file.length() > 0;
    }
}
