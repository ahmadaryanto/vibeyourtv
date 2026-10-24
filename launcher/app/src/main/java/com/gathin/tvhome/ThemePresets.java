package com.gathin.tvhome;

import android.content.SharedPreferences;
import android.view.KeyEvent;

/**
 * Built-in Home looks the on-TV picker can apply. Each one is a config.json overlay: keys that are
 * absent stay at config.xml. The picker's choice is a preference, so a pushed {@code config.json}
 * is left on disk and comes back as {@link #KUSTOM}.
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

    private ThemePresets() {}

    static boolean isBuiltin(String id) {
        if (id == null) return false;
        for (String preset : BUILTIN) if (preset.equals(id)) return true;
        return false;
    }

    static boolean hasSeason(String id) {
        return SALJU.equals(id) || GUGUR.equals(id) || DINGIN.equals(id) || SEMI.equals(id);
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

    /** A remote number key, or -1. Keycodes stay here so the pad and the tests share one map. */
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

    /** Swatch behind the picker's label. Light seasons use a light chip; the sheet stays dark. */
    static int swatch(String id) {
        if (SAYA.equals(id)) return 0xFF2A2C31;
        if (ORANG_TUA.equals(id)) return 0xFF1A140F;
        if (ANAK.equals(id)) return 0xFF1565C0;
        if (REMAJA.equals(id)) return 0xFF0E0F14;
        if (SALJU.equals(id)) return 0xFFD6E6F2;
        if (GUGUR.equals(id)) return 0xFF3C2415;
        if (DINGIN.equals(id)) return 0xFF0E2230;
        if (SEMI.equals(id)) return 0xFFF3D6E4;
        return 0xFF3A3A3C;
    }

    static boolean lightSwatch(String id) {
        return SALJU.equals(id) || SEMI.equals(id);
    }

    /**
     * Overlay JSON for a built-in preset, or null when {@code id} is not one. {@code teenTicker} is
     * the Remaja card's optional BTC line. {@code seasonMotion} is the picker's "Gerak musim" switch.
     * Built as text: unit tests have no real {@code JSONObject}.
     */
    static String overlayJson(String id, boolean teenTicker, boolean seasonMotion) {
        if (!isBuiltin(id)) return null;
        Json o = base();
        if (SAYA.equals(id)) {
            o.num("columns", 5);
            o.bool("app_glow", true);
            o.bool("ambient_glow", true);
            o.num("dock_opacity", 70);
            o.bool("crypto_ticker", true);
            o.bool("wallpaper", true);
            o.num("wallpaper_dim", 0);
        } else if (ORANG_TUA.equals(id)) {
            o.num("columns", 3);
            o.bool("app_library", true);
            o.bool("library_button", true);
            o.bool("crypto_ticker", true);
            o.bool("wallpaper", true);
            o.num("wallpaper_dim", 18);
            o.bool("always_show_names", true);
            o.bool("large_text", true);
            o.bool("dock_title", true);
            o.num("dock_opacity", 80);
            o.bool("ambient_glow", true);
            o.str("background_color", "#1A140F");
        } else if (ANAK.equals(id)) {
            o.num("columns", 3);
            o.bool("crypto_ticker", false);
            o.bool("wallpaper", false);
            o.str("background_color", "#1565C0");
            o.bool("app_glow", false);
            o.bool("ambient_glow", false);
            o.num("dock_opacity", 100);
            o.bool("show_settings_pill", false);
            o.bool("show_inputs_pill", false);
            o.bool("always_show_names", true);
            o.bool("large_text", true);
            o.bool("dock_title", true);
            o.num("rows_below_dock", 0);
            o.bool("app_library", false);
            o.num("sky_intensity", 0);
        } else if (REMAJA.equals(id)) {
            o.num("columns", 5);
            o.str("background_color", "#0E0F14");
            o.bool("app_glow", true);
            o.bool("ambient_glow", true);
            o.bool("dock_glass", true);
            o.num("dock_opacity", 70);
            o.bool("crypto_ticker", teenTicker);
            o.bool("wallpaper", true);
            o.num("wallpaper_dim", 45);
            o.bool("icon_only_buttons", true);
        } else if (SALJU.equals(id)) {
            season(o, SNOW, seasonMotion);
            o.num("columns", 5);
            o.str("background_color", "#D6E6F2");
            o.bool("light_chrome", true);
            o.bool("dock_glass", true);
            o.num("dock_opacity", 62);
            o.bool("wallpaper", true);
            o.num("wallpaper_dim", 0);
            o.bool("icon_only_buttons", true);
            o.bool("app_glow", false);
            o.bool("ambient_glow", false);
        } else if (GUGUR.equals(id)) {
            season(o, LEAVES, seasonMotion);
            o.num("columns", 5);
            o.str("background_color", "#3C2415");
            o.bool("wallpaper", true);
            o.num("wallpaper_dim", 28);
            o.num("dock_opacity", 75);
            o.bool("app_glow", true);
            o.bool("ambient_glow", true);
        } else if (DINGIN.equals(id)) {
            season(o, FROST, seasonMotion);
            o.num("columns", 5);
            o.str("background_color", "#0E2230");
            o.bool("dock_glass", true);
            o.num("dock_opacity", 55);
            o.bool("wallpaper", true);
            o.num("wallpaper_dim", 22);
            o.bool("ambient_glow", true);
            o.bool("icon_only_buttons", true);
        } else if (SEMI.equals(id)) {
            season(o, PETALS, seasonMotion);
            o.num("columns", 5);
            o.str("background_color", "#F3D6E4");
            o.bool("light_chrome", true);
            o.bool("dock_glass", true);
            o.num("dock_opacity", 60);
            o.bool("wallpaper", true);
            o.num("wallpaper_dim", 0);
            o.bool("icon_only_buttons", true);
            o.bool("app_glow", false);
            o.bool("ambient_glow", false);
        }
        return o.end();
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

    private static Json base() {
        Json o = new Json();
        o.bool("show_settings_pill", true);
        o.bool("show_inputs_pill", true);
        o.bool("show_network_pill", true);
        o.str("season_overlay", "");
        o.bool("season_motion", false);
        o.bool("large_text", false);
        o.bool("light_chrome", false);
        o.bool("crypto_ticker", false);
        return o;
    }

    private static void season(Json o, String kind, boolean motion) {
        o.str("season_overlay", kind);
        o.bool("season_motion", motion);
        o.bool("crypto_ticker", false);
        o.num("sky_intensity", 0);
    }

    /** A flat JSON object. Keys here are fixed names; values are booleans, ints, or plain strings. */
    private static final class Json {
        private final StringBuilder out = new StringBuilder("{");
        private boolean any;

        void bool(String key, boolean value) {
            raw(key, value ? "true" : "false");
        }

        void num(String key, int value) {
            raw(key, Integer.toString(value));
        }

        void str(String key, String value) {
            raw(key, "\"" + value + "\"");
        }

        private void raw(String key, String value) {
            String needle = "\"" + key + "\":";
            int at = out.indexOf(needle);
            if (at >= 0) {
                int start = at + needle.length();
                int end = start;
                if (end < out.length() && out.charAt(end) == '"') {
                    end = out.indexOf("\"", end + 1);
                    end = end < 0 ? out.length() : end + 1;
                } else {
                    while (end < out.length() && out.charAt(end) != ',' && out.charAt(end) != '}') end++;
                }
                out.replace(start, end, value);
                return;
            }
            if (any) out.append(',');
            any = true;
            out.append('"').append(key).append("\":").append(value);
        }

        String end() {
            return out.append('}').toString();
        }
    }
}
