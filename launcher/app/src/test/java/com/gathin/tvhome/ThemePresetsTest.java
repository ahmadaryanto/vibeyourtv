package com.gathin.tvhome;

import android.view.KeyEvent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ThemePresetsTest {
    @Test public void sayaIsTheDescribedHome() {
        assertEquals(5, ThemePresets.columns(ThemePresets.SAYA));
        assertEquals(1, ThemePresets.glow(ThemePresets.SAYA));
        assertEquals(70, ThemePresets.opacity(ThemePresets.SAYA));
        assertTrue(ThemePresets.ticker(ThemePresets.SAYA, false));
        assertTrue(ThemePresets.wallpaper(ThemePresets.SAYA));
        // No greeting name and no background of its own: the photo and the time greeting stay.
        assertEquals(0, ThemePresets.background(ThemePresets.SAYA));
        assertFalse(ThemePresets.light(ThemePresets.SAYA));
    }

    @Test public void parentsKeepThePhotoAndLargerType() {
        assertEquals(1, ThemePresets.library(ThemePresets.ORANG_TUA));
        assertTrue(ThemePresets.large(ThemePresets.ORANG_TUA));
        assertTrue(ThemePresets.ticker(ThemePresets.ORANG_TUA, false));
        assertTrue(ThemePresets.wallpaper(ThemePresets.ORANG_TUA));
        assertTrue(ThemePresets.settingsPill(ThemePresets.ORANG_TUA));
        assertTrue(ThemePresets.columns(ThemePresets.ORANG_TUA) < 5);
    }

    @Test public void kidsHidesSettingsAndInputs() {
        assertFalse(ThemePresets.settingsPill(ThemePresets.ANAK));
        assertFalse(ThemePresets.inputsPill(ThemePresets.ANAK));
        assertFalse(ThemePresets.ticker(ThemePresets.ANAK, true));
        assertFalse(ThemePresets.wallpaper(ThemePresets.ANAK));
        assertTrue(ThemePresets.large(ThemePresets.ANAK));
        assertEquals(3, ThemePresets.columns(ThemePresets.ANAK));
        assertTrue(ThemePresets.rowsZero(ThemePresets.ANAK));
        assertEquals(0, ThemePresets.glow(ThemePresets.ANAK));
    }

    @Test public void teensTickerIsOptional() {
        assertFalse(ThemePresets.ticker(ThemePresets.REMAJA, false));
        assertTrue(ThemePresets.ticker(ThemePresets.REMAJA, true));
        assertEquals(1, ThemePresets.glow(ThemePresets.REMAJA));
        assertEquals(0xFF0E0F14, ThemePresets.background(ThemePresets.REMAJA));
    }

    @Test public void seasonsKeepTheWallpaperAndCanStopMoving() {
        assertSeason(ThemePresets.SALJU, ThemePresets.SNOW);
        assertSeason(ThemePresets.GUGUR, ThemePresets.LEAVES);
        assertSeason(ThemePresets.DINGIN, ThemePresets.FROST);
        assertSeason(ThemePresets.SEMI, ThemePresets.PETALS);
        assertFalse(ThemePresets.motion(ThemePresets.SALJU, false));
        assertTrue(ThemePresets.motion(ThemePresets.SALJU, true));
        assertFalse(ThemePresets.motion(ThemePresets.SAYA, true));
        assertTrue(ThemePresets.light(ThemePresets.SEMI));
        assertTrue(ThemePresets.glass(ThemePresets.DINGIN));
        assertTrue(ThemePresets.light(ThemePresets.SALJU));
    }

    @Test public void customFileIsNotABuiltinAndIsNotWrittenByAPreset() {
        assertFalse(ThemePresets.isBuiltin(ThemePresets.KUSTOM));
        assertEquals(-1, ThemePresets.glow(ThemePresets.KUSTOM));
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

    @Test public void overlayJsonMatchesTheTable() {
        String saya = ThemePresets.overlayJson(ThemePresets.SAYA, false, true);
        assertEquals("5", value(saya, "columns"));
        assertEquals("true", value(saya, "crypto_ticker"));
        assertEquals("true", value(saya, "wallpaper"));
        assertEquals(null, value(saya, "greeting_name"));
        String kids = ThemePresets.overlayJson(ThemePresets.ANAK, true, true);
        assertEquals("false", value(kids, "show_settings_pill"));
        assertEquals("false", value(kids, "wallpaper"));
        assertEquals("0", value(kids, "rows_below_dock"));
        String teen = ThemePresets.overlayJson(ThemePresets.REMAJA, true, true);
        assertEquals("true", value(teen, "crypto_ticker"));
        assertEquals("#0E0F14", value(teen, "background_color"));
        String snow = ThemePresets.overlayJson(ThemePresets.SALJU, false, false);
        assertEquals("snow", value(snow, "season_overlay"));
        assertEquals("false", value(snow, "season_motion"));
        assertEquals("true", value(snow, "light_chrome"));
        assertNull(ThemePresets.overlayJson(ThemePresets.KUSTOM, false, true));
    }

    @Test public void unknownSeasonNamesAreDropped() {
        assertEquals("", ThemePresets.canonicalSeason("rain"));
        assertEquals(ThemePresets.SNOW, ThemePresets.canonicalSeason(" Snow "));
    }

    private static String value(String json, String key) {
        String needle = "\"" + key + "\":";
        int at = json.lastIndexOf(needle);
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

    private static void assertSeason(String id, String kind) {
        assertEquals(kind, ThemePresets.season(id));
        assertTrue(ThemePresets.wallpaper(id));
        assertFalse(ThemePresets.ticker(id, true));
        assertTrue(ThemePresets.skyOff(id));
    }
}
