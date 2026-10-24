package com.gathin.tvhome;

import android.view.KeyEvent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ThemePresetsTest {
    @Test public void sayaIsTheDescribedHome() {
        String o = ThemePresets.overlayJson(ThemePresets.SAYA, false, true);
        assertEquals("5", value(o, "columns"));
        assertEquals("true", value(o, "app_glow"));
        assertEquals("70", value(o, "dock_opacity"));
        assertEquals("true", value(o, "crypto_ticker"));
        assertEquals("true", value(o, "wallpaper"));
        assertNull(value(o, "greeting_name"));
        assertNull(value(o, "theme_picker"));
    }

    @Test public void parentsKeepThePhotoAndLargerType() {
        String o = ThemePresets.overlayJson(ThemePresets.ORANG_TUA, false, true);
        assertEquals("true", value(o, "app_library"));
        assertEquals("true", value(o, "large_text"));
        assertEquals("true", value(o, "crypto_ticker"));
        assertEquals("true", value(o, "wallpaper"));
        assertEquals("true", value(o, "show_settings_pill"));
        assertTrue(Integer.parseInt(value(o, "columns")) < 5);
    }

    @Test public void kidsHidesSettingsAndInputs() {
        String o = ThemePresets.overlayJson(ThemePresets.ANAK, true, true);
        assertEquals("false", value(o, "show_settings_pill"));
        assertEquals("false", value(o, "show_inputs_pill"));
        assertEquals("false", value(o, "crypto_ticker"));
        assertEquals("false", value(o, "wallpaper"));
        assertEquals("true", value(o, "large_text"));
        assertEquals("3", value(o, "columns"));
    }

    @Test public void teensTickerIsOptional() {
        assertEquals("false", value(ThemePresets.overlayJson(ThemePresets.REMAJA, false, true), "crypto_ticker"));
        assertEquals("true", value(ThemePresets.overlayJson(ThemePresets.REMAJA, true, true), "crypto_ticker"));
        assertEquals("true", value(ThemePresets.overlayJson(ThemePresets.REMAJA, false, true), "app_glow"));
        assertEquals("#0E0F14", value(ThemePresets.overlayJson(ThemePresets.REMAJA, false, true), "background_color"));
    }

    @Test public void seasonsKeepTheWallpaperAndCanStopMoving() {
        assertSeason(ThemePresets.SALJU, ThemePresets.SNOW, true);
        assertSeason(ThemePresets.GUGUR, ThemePresets.LEAVES, true);
        assertSeason(ThemePresets.DINGIN, ThemePresets.FROST, true);
        assertSeason(ThemePresets.SEMI, ThemePresets.PETALS, true);
        assertEquals("false", value(ThemePresets.overlayJson(ThemePresets.SALJU, false, false), "season_motion"));
        assertEquals("true", value(ThemePresets.overlayJson(ThemePresets.SEMI, false, false), "light_chrome"));
        assertEquals("true", value(ThemePresets.overlayJson(ThemePresets.DINGIN, false, true), "dock_glass"));
    }

    @Test public void customFileIsNotABuiltinAndIsNotWrittenByAPreset() {
        assertFalse(ThemePresets.isBuiltin(ThemePresets.KUSTOM));
        assertNull(ThemePresets.overlayJson(ThemePresets.KUSTOM, false, true));
        assertEquals(ThemePresets.KUSTOM, ThemePresets.activeId("", true));
        assertEquals("", ThemePresets.activeId("", false));
        assertEquals(ThemePresets.SAYA, ThemePresets.activeId(ThemePresets.SAYA, true));
    }

    @Test public void aPushedFileWinsOverABuiltinPreset() {
        assertEquals(ThemePresets.KUSTOM, ThemePresets.presetAfterFileChange(ThemePresets.ANAK, true));
        assertEquals(ThemePresets.ANAK, ThemePresets.presetAfterFileChange(ThemePresets.ANAK, false));
        assertEquals(ThemePresets.KUSTOM, ThemePresets.presetAfterFileChange(ThemePresets.KUSTOM, true));
        assertEquals("", ThemePresets.presetAfterFileChange("", true));
    }

    @Test public void leavingKidsNeedsThePinOnlyWhenOneIsSet() {
        assertTrue(ThemePresets.leavingKidsNeedsPin(ThemePresets.ANAK, ThemePresets.SAYA, true));
        assertFalse(ThemePresets.leavingKidsNeedsPin(ThemePresets.ANAK, ThemePresets.SAYA, false));
        assertFalse(ThemePresets.leavingKidsNeedsPin(ThemePresets.ANAK, ThemePresets.ANAK, true));
        assertFalse(ThemePresets.leavingKidsNeedsPin(ThemePresets.SAYA, ThemePresets.ANAK, true));
        assertTrue(ThemePresets.pinOk("1234", "1234"));
        assertFalse(ThemePresets.pinOk("1234", "1235"));
        assertFalse(ThemePresets.pinOk("12", "12"));
        assertFalse(ThemePresets.pinSet(null));
        assertEquals("1234", ThemePresets.normalizePin("12-34"));
    }

    @Test public void remoteDigitsFollowTheNumberKeys() {
        assertEquals(0, ThemePresets.remoteDigit(KeyEvent.KEYCODE_0));
        assertEquals(9, ThemePresets.remoteDigit(KeyEvent.KEYCODE_9));
        assertEquals(5, ThemePresets.remoteDigit(KeyEvent.KEYCODE_NUMPAD_5));
        assertEquals(-1, ThemePresets.remoteDigit(KeyEvent.KEYCODE_DPAD_CENTER));
    }

    @Test public void unknownSeasonNamesAreDropped() {
        assertEquals("", ThemePresets.canonicalSeason("rain"));
        assertEquals(ThemePresets.SNOW, ThemePresets.canonicalSeason(" Snow "));
    }

    private static void assertSeason(String id, String kind, boolean motion) {
        String o = ThemePresets.overlayJson(id, true, motion);
        assertEquals(kind, value(o, "season_overlay"));
        assertEquals(motion ? "true" : "false", value(o, "season_motion"));
        assertEquals("true", value(o, "wallpaper"));
        assertEquals("false", value(o, "crypto_ticker"));
    }

    /** The value of one flat key, or null. Strings come back without quotes. */
    private static String value(String json, String key) {
        String needle = "\"" + key + "\":";
        int at = json.indexOf(needle);
        if (at < 0) return null;
        int start = at + needle.length();
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        }
        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(start, end);
    }
}
