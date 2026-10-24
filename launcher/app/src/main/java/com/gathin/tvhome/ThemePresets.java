package com.gathin.tvhome;

import android.content.SharedPreferences;
import android.view.KeyEvent;

/**
 * Built-in Home looks the on-TV picker can apply. The choice is a preference, so a pushed
 * {@code config.json} stays on disk and comes back as {@link #KUSTOM}. Presets never set
 * {@code greeting_name} or {@code theme_picker}.
 *
 * <p>Each row is columns, dock opacity, wallpaper dim, flags, background (0 leaves the default),
 * and a season index. Packed this way so the table stays a few hundred bytes.
 */
final class ThemePresets {
    static final String PRESET_KEY = "theme_preset";
    static final String TEEN_TICKER_KEY = "teen_ticker";
    static final String SEASON_MOTION_KEY = "season_motion";
    static final String PIN_KEY = "kids_pin";

    static final String SAYA = "saya";
    static final String ORANG_TUA = "orang_tua";
    static final String ANAK = "anak";
    static final String REMAJA = "remaja";
    static final String SALJU = "salju";
    static final String GUGUR = "gugur";
    static final String DINGIN = "dingin";
    static final String SEMI = "semi";
    static final String KUSTOM = "kustom";

    static final String SNOW = "snow";
    static final String LEAVES = "leaves";
    static final String FROST = "frost";
    static final String PETALS = "petals";

    static final String[] BUILTIN = {SAYA, ORANG_TUA, ANAK, REMAJA, SALJU, GUGUR, DINGIN, SEMI};

    private static final String[] SEASONS = {"", SNOW, LEAVES, FROST, PETALS};

    private static final int WALL = 1;
    private static final int GLOW_SET = 1 << 1;
    private static final int GLOW = 1 << 2;
    private static final int AMBIENT = 1 << 3;
    private static final int TICKER = 1 << 4;
    private static final int TICKER_OPT = 1 << 5;
    private static final int LIB_SET = 1 << 6;
    private static final int LIB = 1 << 7;
    private static final int LARGE = 1 << 8;
    private static final int NAMES = 1 << 9;
    private static final int DOCK_TITLE = 1 << 10;
    private static final int GLASS = 1 << 11;
    private static final int ICON_ONLY = 1 << 12;
    private static final int HIDE_SETTINGS = 1 << 13;
    private static final int HIDE_INPUTS = 1 << 14;
    private static final int LIGHT = 1 << 15;
    private static final int LIB_BUTTON = 1 << 16;
    private static final int SKY0 = 1 << 17;
    private static final int ROWS0 = 1 << 18;

    /** columns, dock opacity, wallpaper dim, flags, background, season index. */
    private static final int[][] ROW = {
            {5, 70, 0, WALL | GLOW_SET | GLOW | AMBIENT | TICKER, 0, 0},
            {3, 80, 18, WALL | AMBIENT | TICKER | LIB_SET | LIB | LIB_BUTTON | LARGE | NAMES | DOCK_TITLE,
                    0xFF1A140F, 0},
            {3, 100, 0, GLOW_SET | LIB_SET | LARGE | NAMES | DOCK_TITLE | HIDE_SETTINGS | HIDE_INPUTS
                    | SKY0 | ROWS0, 0xFF1565C0, 0},
            {5, 70, 45, WALL | GLOW_SET | GLOW | AMBIENT | TICKER_OPT | GLASS | ICON_ONLY, 0xFF0E0F14, 0},
            {5, 62, 0, WALL | GLOW_SET | LIGHT | GLASS | ICON_ONLY | SKY0, 0xFFD6E6F2, 1},
            {5, 75, 28, WALL | GLOW_SET | GLOW | AMBIENT | SKY0, 0xFF3C2415, 2},
            {5, 55, 22, WALL | AMBIENT | GLASS | ICON_ONLY | SKY0, 0xFF0E2230, 3},
            {5, 60, 0, WALL | GLOW_SET | LIGHT | GLASS | ICON_ONLY | SKY0, 0xFFF3D6E4, 4},
    };

    private ThemePresets() {}

    static int index(String id) {
        if (id == null) return -1;
        for (int i = 0; i < BUILTIN.length; i++) if (BUILTIN[i].equals(id)) return i;
        return -1;
    }

    static boolean isBuiltin(String id) {
        return index(id) >= 0;
    }

    static boolean hasSeason(String id) {
        return !season(id).isEmpty();
    }

    /** Id the picker should highlight. An empty choice with a pushed file reads as Kustom. */
    static String activeId(String saved, boolean customFile) {
        if (saved == null) saved = "";
        if (isBuiltin(saved) || KUSTOM.equals(saved)) return saved;
        return customFile ? KUSTOM : "";
    }

    /**
     * A new or removed config.json wins over a built-in preset, which would otherwise keep hiding
     * the file. The file itself is never rewritten here.
     */
    static String presetAfterFileChange(String saved, boolean changed) {
        if (saved == null) saved = "";
        if (!changed) return saved;
        return isBuiltin(saved) ? KUSTOM : saved;
    }

    static boolean leavingKidsNeedsPin(String from, String to, boolean pinSet) {
        return ANAK.equals(from) && to != null && !ANAK.equals(to) && pinSet;
    }

    static String normalizePin(String raw) {
        if (raw == null) return "";
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < raw.length() && digits.length() < 4; i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') digits.append(c);
        }
        return digits.toString();
    }

    static boolean pinSet(String stored) {
        return stored != null && stored.length() == 4 && stored.equals(normalizePin(stored));
    }

    static boolean pinOk(String stored, String entered) {
        return pinSet(stored) && stored.equals(normalizePin(entered));
    }

    /** A remote number key, or -1. */
    static int remoteDigit(int keyCode) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            return keyCode - KeyEvent.KEYCODE_0;
        }
        if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
            return keyCode - KeyEvent.KEYCODE_NUMPAD_0;
        }
        return -1;
    }

    static String canonicalSeason(String raw) {
        if (raw == null) return "";
        String name = raw.trim().toLowerCase();
        if (SNOW.equals(name) || LEAVES.equals(name) || FROST.equals(name) || PETALS.equals(name)) return name;
        return "";
    }

    static int columns(String id) { return at(id, 0); }
    static int opacity(String id) { return at(id, 1); }
    static int dim(String id) { return at(id, 2); }

    /** Background colour, or 0 when the preset keeps the config.xml colour. */
    static int background(String id) { return at(id, 4); }

    static String season(String id) {
        int i = index(id);
        if (i < 0) return "";
        return SEASONS[ROW[i][5]];
    }

    static boolean wallpaper(String id) { return bit(id, WALL); }
    static boolean settingsPill(String id) { return index(id) >= 0 && !bit(id, HIDE_SETTINGS); }
    static boolean inputsPill(String id) { return index(id) >= 0 && !bit(id, HIDE_INPUTS); }
    static boolean large(String id) { return bit(id, LARGE); }
    static boolean light(String id) { return bit(id, LIGHT); }
    static boolean glass(String id) { return bit(id, GLASS); }
    static boolean ambient(String id) { return bit(id, AMBIENT); }
    static boolean names(String id) { return bit(id, NAMES); }
    static boolean dockTitle(String id) { return bit(id, DOCK_TITLE); }
    static boolean libButton(String id) { return bit(id, LIB_BUTTON); }
    static boolean iconOnly(String id) { return bit(id, ICON_ONLY); }
    static boolean skyOff(String id) { return bit(id, SKY0); }
    static boolean rowsZero(String id) { return bit(id, ROWS0); }

    /** 1, 0, or -1 when the preset leaves app_glow alone. */
    static int glow(String id) {
        if (!bit(id, GLOW_SET)) return -1;
        return bit(id, GLOW) ? 1 : 0;
    }

    /** 1, 0, or -1 when the preset leaves the app library alone. */
    static int library(String id) {
        if (!bit(id, LIB_SET)) return -1;
        return bit(id, LIB) ? 1 : 0;
    }

    static boolean ticker(String id, boolean teen) {
        if (bit(id, TICKER_OPT)) return teen;
        return bit(id, TICKER);
    }

    /** Seasonal motion follows the picker switch. Other presets stay still. */
    static boolean motion(String id, boolean pref) {
        return hasSeason(id) && pref;
    }

    /** Swatch behind the picker's label. Light seasons use a light chip; the sheet stays dark. */
    static int swatch(String id) {
        int color = background(id);
        if (color != 0) return color;
        return SAYA.equals(id) ? 0xFF2A2C31 : 0xFF3A3A3C;
    }

    /**
     * Overlay JSON for a built-in preset, or null when {@code id} is not one. Built at runtime from
     * {@link #ROW} so the APK stores the table, not eight copies of the same keys.
     */
    static String overlayJson(String id, boolean teen, boolean motion) {
        int i = index(id);
        if (i < 0) return null;
        int[] row = ROW[i];
        int f = row[3];
        StringBuilder s = new StringBuilder(240);
        s.append('{');
        num(s, "columns", row[0]);
        num(s, "dock_opacity", row[1]);
        num(s, "wallpaper_dim", row[2]);
        bool(s, "wallpaper", (f & WALL) != 0);
        bool(s, "show_settings_pill", (f & HIDE_SETTINGS) == 0);
        bool(s, "show_inputs_pill", (f & HIDE_INPUTS) == 0);
        bool(s, "show_network_pill", true);
        bool(s, "large_text", (f & LARGE) != 0);
        bool(s, "light_chrome", (f & LIGHT) != 0);
        bool(s, "ambient_glow", (f & AMBIENT) != 0);
        bool(s, "season_motion", row[5] != 0 && motion);
        str(s, "season_overlay", SEASONS[row[5]]);
        bool(s, "crypto_ticker", (f & TICKER_OPT) != 0 ? teen : (f & TICKER) != 0);
        if ((f & GLOW_SET) != 0) bool(s, "app_glow", (f & GLOW) != 0);
        if ((f & GLASS) != 0) bool(s, "dock_glass", true);
        if ((f & ICON_ONLY) != 0) bool(s, "icon_only_buttons", true);
        if ((f & LIB_SET) != 0) bool(s, "app_library", (f & LIB) != 0);
        if ((f & LIB_BUTTON) != 0) bool(s, "library_button", true);
        if ((f & NAMES) != 0) bool(s, "always_show_names", true);
        if ((f & DOCK_TITLE) != 0) bool(s, "dock_title", true);
        if ((f & SKY0) != 0) num(s, "sky_intensity", 0);
        if ((f & ROWS0) != 0) num(s, "rows_below_dock", 0);
        if (row[4] != 0) str(s, "background_color", hex(row[4]));
        return s.append('}').toString();
    }

    private static void num(StringBuilder s, String key, int value) {
        raw(s, key, Integer.toString(value));
    }

    private static void bool(StringBuilder s, String key, boolean value) {
        raw(s, key, value ? "true" : "false");
    }

    private static void str(StringBuilder s, String key, String value) {
        raw(s, key, "\"" + value + "\"");
    }

    private static void raw(StringBuilder s, String key, String value) {
        if (s.length() > 1) s.append(',');
        s.append('"').append(key).append("\":").append(value);
    }

    private static String hex(int color) {
        char[] out = new char[7];
        out[0] = '#';
        color &= 0xFFFFFF;
        for (int i = 6; i >= 1; i--) {
            int n = color & 0xF;
            out[i] = (char) (n < 10 ? '0' + n : 'A' + n - 10);
            color >>= 4;
        }
        return new String(out);
    }

    /** Drop on-TV column, row, dock and wallpaper overrides so the preset's own values show. */
    static void savePreset(SharedPreferences prefs, String id) {
        prefs.edit()
                .putString(PRESET_KEY, id == null ? "" : id)
                .remove("columns")
                .remove(Config.ROWS_BELOW_DOCK_KEY)
                .remove(Config.DOCK_OPACITY_KEY)
                .remove(Config.WALLPAPER_CHOICE_KEY)
                .putLong(Config.SETTINGS_REVISION_KEY, prefs.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1)
                .commit();
    }

    static void bump(SharedPreferences prefs) {
        prefs.edit().putLong(Config.SETTINGS_REVISION_KEY,
                prefs.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).commit();
    }

    static void savePin(SharedPreferences prefs, String pin) {
        String normalized = normalizePin(pin);
        SharedPreferences.Editor editor = prefs.edit();
        if (normalized.length() == 4) editor.putString(PIN_KEY, normalized);
        else editor.remove(PIN_KEY);
        editor.commit();
    }

    private static int at(String id, int column) {
        int i = index(id);
        return i < 0 ? 0 : ROW[i][column];
    }

    private static boolean bit(String id, int mask) {
        return (at(id, 3) & mask) != 0;
    }
}
