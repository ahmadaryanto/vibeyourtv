package com.gathin.tvhome;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Remote picker for the built-in presets. A centred sheet, cards in a row, OK applies. Choosing one
 * does not write config.json; that file stays available as Kustom.
 */
final class ThemePicker {
    private ThemePicker() {}

    static void open(Activity activity) {
        float density = activity.getResources().getDisplayMetrics().density;
        SharedPreferences prefs = activity.getSharedPreferences(Config.SETTINGS_NAME, Activity.MODE_PRIVATE);
        boolean customFile = customFile(activity);
        String active = ThemePresets.activeId(prefs.getString(ThemePresets.PRESET_KEY, ""), customFile);

        LinearLayout sheet = column(activity);
        sheet.setPadding(dp(density, 28), dp(density, 22), dp(density, 28), dp(density, 18));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF01C1C1E);
        bg.setCornerRadius(dp(density, 28));
        sheet.setBackground(bg);
        sheet.setClipChildren(false);

        TextView title = label(activity, activity.getString(R.string.theme_picker_title), 22, 0xFFFFFFFF);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        sheet.addView(title);

        TextView detail = label(activity, "", 14, 0xB3FFFFFF);
        detail.setMinLines(2);
        detail.setPadding(0, dp(density, 6), 0, dp(density, 12));
        sheet.addView(detail);

        HorizontalScrollView scroll = new HorizontalScrollView(activity);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFocusable(false);
        scroll.setClipToPadding(false);
        scroll.setClipChildren(false);
        scroll.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setClipChildren(false);
        scroll.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sheet.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView ticker = footer(activity, density, tickerText(activity, prefs));
        TextView motion = footer(activity, density, motionText(activity, prefs));
        TextView pin = footer(activity, density, pinText(activity, prefs));
        ticker.setVisibility(ThemePresets.REMAJA.equals(active) ? View.VISIBLE : View.GONE);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int screen = activity.getResources().getDisplayMetrics().widthPixels;
            window.setLayout(Math.min(screen - dp(density, 48), dp(density, 1080)),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.55f);
        }

        String[] ids = ids(customFile);
        View[] cards = new View[ids.length];
        View selected = null;
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            View card = card(activity, density, id, id.equals(active));
            cards[i] = card;
            if (id.equals(active)) selected = card;
            card.setOnFocusChangeListener((v, focused) -> {
                v.setScaleX(focused ? 1.06f : 1f);
                v.setScaleY(focused ? 1.06f : 1f);
                if (!focused) return;
                detail.setText(hint(activity, id));
                boolean teen = ThemePresets.REMAJA.equals(id);
                ticker.setVisibility(teen ? View.VISIBLE : View.GONE);
                int below = teen ? ticker.getId() : motion.getId();
                for (View other : cards) if (other != null) other.setNextFocusDownId(below);
                motion.setNextFocusUpId(teen ? ticker.getId() : v.getId());
                v.post(() -> scroll.smoothScrollTo(Math.max(0, v.getLeft() - dp(density, 24)), 0));
            });
            card.setOnClickListener(v -> choose(activity, dialog, prefs, active, id));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(density, 116), dp(density, 132));
            lp.setMarginEnd(dp(density, 12));
            row.addView(card, lp);
        }
        wire(cards, ticker, motion, pin);
        if (selected == null && cards.length > 0) selected = cards[0];
        if (selected != null) detail.setText(hint(activity, tag(selected)));

        ticker.setOnClickListener(v -> {
            boolean on = !prefs.getBoolean(ThemePresets.TEEN_TICKER_KEY, false);
            prefs.edit().putBoolean(ThemePresets.TEEN_TICKER_KEY, on).commit();
            ticker.setText(tickerText(activity, prefs));
            if (ThemePresets.REMAJA.equals(active)) rebuild(activity, dialog, prefs);
        });
        motion.setOnClickListener(v -> {
            boolean on = !prefs.getBoolean(ThemePresets.SEASON_MOTION_KEY, true);
            prefs.edit().putBoolean(ThemePresets.SEASON_MOTION_KEY, on).commit();
            motion.setText(motionText(activity, prefs));
            if (ThemePresets.hasSeason(active)) rebuild(activity, dialog, prefs);
        });
        pin.setOnClickListener(v -> editPin(activity, prefs, pin));

        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(density, 48));
        gap.topMargin = dp(density, 14);
        sheet.addView(ticker, gap);
        LinearLayout.LayoutParams next = new LinearLayout.LayoutParams(gap);
        next.topMargin = dp(density, 8);
        sheet.addView(motion, next);
        sheet.addView(pin, new LinearLayout.LayoutParams(next));

        dialog.setOnShowListener(d -> {
            View focus = null;
            for (View card : cards) if (tag(card).equals(active)) focus = card;
            if (focus == null && cards.length > 0) focus = cards[0];
            if (focus != null) focus.requestFocus();
        });
        dialog.show();
    }

    private static void choose(Activity activity, Dialog dialog, SharedPreferences prefs,
                               String active, String id) {
        if (id.equals(active)) {
            dialog.dismiss();
            return;
        }
        String stored = prefs.getString(ThemePresets.PIN_KEY, "");
        if (ThemePresets.leavingKidsNeedsPin(active, id, ThemePresets.pinSet(stored))) {
            askPin(activity, R.string.theme_pin_enter, false, pin -> {
                if (!ThemePresets.pinOk(stored, pin)) {
                    Toast.makeText(activity, R.string.theme_pin_wrong, Toast.LENGTH_SHORT).show();
                    return false;
                }
                ThemePresets.savePreset(prefs, id);
                dialog.dismiss();
                activity.getWindow().getDecorView().post(() -> {
                    if (!activity.isFinishing()) activity.recreate();
                });
                return true;
            });
            return;
        }
        ThemePresets.savePreset(prefs, id);
        dialog.dismiss();
        if (!activity.isFinishing()) activity.recreate();
    }

    private static void rebuild(Activity activity, Dialog dialog, SharedPreferences prefs) {
        ThemePresets.bump(prefs);
        dialog.dismiss();
        if (!activity.isFinishing()) activity.recreate();
    }

    private static void editPin(Activity activity, SharedPreferences prefs, TextView pinRow) {
        String stored = prefs.getString(ThemePresets.PIN_KEY, "");
        if (ThemePresets.pinSet(stored)) {
            askPin(activity, R.string.theme_pin_current, false, pin -> {
                if (!ThemePresets.pinOk(stored, pin)) {
                    Toast.makeText(activity, R.string.theme_pin_wrong, Toast.LENGTH_SHORT).show();
                    return false;
                }
                askNewPin(activity, prefs, pinRow, true);
                return true;
            });
        } else {
            askNewPin(activity, prefs, pinRow, false);
        }
    }

    private static void askNewPin(Activity activity, SharedPreferences prefs, TextView pinRow, boolean canClear) {
        askPin(activity, R.string.theme_pin_new, canClear, first -> {
            if (first.isEmpty()) {
                ThemePresets.savePin(prefs, "");
                pinRow.setText(pinText(activity, prefs));
                Toast.makeText(activity, R.string.theme_pin_cleared, Toast.LENGTH_SHORT).show();
                return true;
            }
            askPin(activity, R.string.theme_pin_again, false, second -> {
                if (!first.equals(second)) {
                    Toast.makeText(activity, R.string.theme_pin_mismatch, Toast.LENGTH_SHORT).show();
                    return false;
                }
                ThemePresets.savePin(prefs, first);
                pinRow.setText(pinText(activity, prefs));
                Toast.makeText(activity, R.string.theme_pin_saved, Toast.LENGTH_SHORT).show();
                return true;
            });
            return true;
        });
    }

    /** {@code accept} returns false to keep the pad open (the entry is cleared). Empty means "clear". */
    private interface PinCheck {
        boolean accept(String pin);
    }

    private static void askPin(Activity activity, int titleRes, boolean canClear, PinCheck check) {
        float density = activity.getResources().getDisplayMetrics().density;
        LinearLayout sheet = column(activity);
        sheet.setPadding(dp(density, 22), dp(density, 18), dp(density, 22), dp(density, 16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF01C1C1E);
        bg.setCornerRadius(dp(density, 24));
        sheet.setBackground(bg);

        TextView title = label(activity, activity.getString(titleRes), 18, 0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        sheet.addView(title);
        TextView dots = label(activity, "○ ○ ○ ○", 22, 0xFFFFFFFF);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, dp(density, 10), 0, dp(density, 12));
        sheet.addView(dots);

        StringBuilder entry = new StringBuilder();
        Runnable refresh = () -> {
            StringBuilder mask = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if (i > 0) mask.append(' ');
                mask.append(i < entry.length() ? '●' : '○');
            }
            dots.setText(mask.toString());
        };

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(dp(density, 360), ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.45f);
        }

        Runnable submit = () -> {
            if (entry.length() < 4) return;
            String pin = entry.toString();
            if (check.accept(pin)) dialog.dismiss();
            else {
                entry.setLength(0);
                refresh.run();
            }
        };
        View.OnClickListener digit = v -> {
            if (entry.length() >= 4) return;
            entry.append(v.getTag());
            refresh.run();
            if (entry.length() == 4) submit.run();
        };
        TextView[] keys = new TextView[12];
        String[] faces = {"1", "2", "3", "4", "5", "6", "7", "8", "9",
                activity.getString(R.string.theme_pin_delete), "0", activity.getString(R.string.theme_pin_cancel)};
        LinearLayout grid = column(activity);
        for (int r = 0; r < 4; r++) {
            LinearLayout line = new LinearLayout(activity);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER);
            for (int c = 0; c < 3; c++) {
                int i = r * 3 + c;
                TextView key = footer(activity, density, faces[i]);
                key.setGravity(Gravity.CENTER);
                keys[i] = key;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(density, 46), 1f);
                lp.setMarginEnd(c == 2 ? 0 : dp(density, 8));
                line.addView(key, lp);
                if (i < 9) {
                    key.setTag(String.valueOf(i + 1));
                    key.setOnClickListener(digit);
                } else if (i == 10) {
                    key.setTag("0");
                    key.setOnClickListener(digit);
                } else if (i == 9) {
                    key.setOnClickListener(v -> {
                        if (entry.length() > 0) entry.setLength(entry.length() - 1);
                        refresh.run();
                    });
                } else {
                    key.setOnClickListener(v -> dialog.dismiss());
                }
            }
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(density, 8);
            grid.addView(line, rowLp);
        }
        for (int i = 0; i < keys.length; i++) {
            int col = i % 3;
            int row = i / 3;
            keys[i].setNextFocusLeftId(keys[col == 0 ? i : i - 1].getId());
            keys[i].setNextFocusRightId(keys[col == 2 ? i : i + 1].getId());
            keys[i].setNextFocusUpId(keys[row == 0 ? i : i - 3].getId());
            keys[i].setNextFocusDownId(keys[row == 3 ? i : i + 3].getId());
        }
        sheet.addView(grid);
        if (canClear) {
            TextView clear = footer(activity, density, activity.getString(R.string.theme_pin_clear));
            clear.setGravity(Gravity.CENTER);
            clear.setOnClickListener(v -> {
                if (check.accept("")) dialog.dismiss();
            });
            clear.setNextFocusUpId(keys[10].getId());
            keys[9].setNextFocusDownId(clear.getId());
            keys[10].setNextFocusDownId(clear.getId());
            keys[11].setNextFocusDownId(clear.getId());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(density, 46));
            sheet.addView(clear, lp);
        }

        dialog.setOnKeyListener((d, code, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            int dig = ThemePresets.remoteDigit(code);
            if (dig >= 0) {
                if (entry.length() < 4) entry.append((char) ('0' + dig));
                refresh.run();
                if (entry.length() == 4) submit.run();
                return true;
            }
            if (code == KeyEvent.KEYCODE_DEL) {
                if (entry.length() > 0) entry.setLength(entry.length() - 1);
                refresh.run();
                return true;
            }
            return false;
        });
        dialog.setOnShowListener(d -> keys[0].requestFocus());
        dialog.show();
    }

    private static String[] ids(boolean customFile) {
        if (!customFile) return ThemePresets.BUILTIN;
        String[] ids = new String[ThemePresets.BUILTIN.length + 1];
        System.arraycopy(ThemePresets.BUILTIN, 0, ids, 0, ThemePresets.BUILTIN.length);
        ids[ids.length - 1] = ThemePresets.KUSTOM;
        return ids;
    }

    private static boolean customFile(Activity activity) {
        File dir = Config.dir(activity);
        if (dir == null) return false;
        File file = new File(dir, Config.FILE_NAME);
        return file.isFile() && file.length() > 0;
    }

    private static View card(Activity activity, float density, String id, boolean selected) {
        LinearLayout card = column(activity);
        card.setTag(id);
        card.setFocusable(true);
        card.setClickable(true);
        card.setId(View.generateViewId());
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF2C2C2E);
        bg.setCornerRadius(dp(density, 18));
        if (selected) bg.setStroke(dp(density, 2), 0xFFFFFFFF);
        card.setBackground(bg);
        card.setPadding(dp(density, 8), dp(density, 8), dp(density, 8), dp(density, 8));

        View swatch = new View(activity);
        GradientDrawable chip = new GradientDrawable();
        chip.setColor(ThemePresets.swatch(id));
        chip.setCornerRadius(dp(density, 12));
        swatch.setBackground(chip);
        card.addView(swatch, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(density, 64)));

        TextView name = label(activity, name(activity, id), 14,
                ThemePresets.lightSwatch(id) ? 0xFFFFFFFF : 0xFFFFFFFF);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(density, 8), 0, 0);
        card.addView(name);
        return card;
    }

    private static void wire(View[] cards, TextView ticker, TextView motion, TextView pin) {
        for (int i = 0; i < cards.length; i++) {
            cards[i].setNextFocusLeftId(cards[i == 0 ? 0 : i - 1].getId());
            cards[i].setNextFocusRightId(cards[i == cards.length - 1 ? i : i + 1].getId());
            cards[i].setNextFocusUpId(cards[i].getId());
            cards[i].setNextFocusDownId(ticker.getId());
        }
        ticker.setNextFocusUpId(cards.length > 0 ? cards[0].getId() : ticker.getId());
        ticker.setNextFocusDownId(motion.getId());
        motion.setNextFocusUpId(ticker.getId());
        motion.setNextFocusDownId(pin.getId());
        pin.setNextFocusUpId(motion.getId());
        pin.setNextFocusDownId(pin.getId());
        for (TextView row : new TextView[]{ticker, motion, pin}) {
            row.setNextFocusLeftId(row.getId());
            row.setNextFocusRightId(row.getId());
        }
    }

    private static TextView footer(Activity activity, float density, String text) {
        TextView view = label(activity, text, 16, 0xFFFFFFFF);
        view.setId(View.generateViewId());
        view.setFocusable(true);
        view.setClickable(true);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setMinHeight(dp(density, 48));
        view.setPadding(dp(density, 16), 0, dp(density, 16), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF2C2C2E);
        bg.setCornerRadius(dp(density, 14));
        view.setBackground(bg);
        view.setOnFocusChangeListener((v, focused) -> bg.setStroke(focused ? dp(density, 2) : 0, 0xFFFFFFFF));
        return view;
    }

    private static LinearLayout column(Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private static TextView label(Activity activity, String text, int sp, int color) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color);
        return view;
    }

    private static int dp(float density, int value) {
        return Math.round(value * density);
    }

    private static String tag(View card) {
        Object tag = card.getTag();
        return tag instanceof String ? (String) tag : "";
    }

    private static String name(Activity activity, String id) {
        if (ThemePresets.SAYA.equals(id)) return activity.getString(R.string.theme_saya);
        if (ThemePresets.ORANG_TUA.equals(id)) return activity.getString(R.string.theme_orang_tua);
        if (ThemePresets.ANAK.equals(id)) return activity.getString(R.string.theme_anak);
        if (ThemePresets.REMAJA.equals(id)) return activity.getString(R.string.theme_remaja);
        if (ThemePresets.SALJU.equals(id)) return activity.getString(R.string.theme_salju);
        if (ThemePresets.GUGUR.equals(id)) return activity.getString(R.string.theme_gugur);
        if (ThemePresets.DINGIN.equals(id)) return activity.getString(R.string.theme_dingin);
        if (ThemePresets.SEMI.equals(id)) return activity.getString(R.string.theme_semi);
        return activity.getString(R.string.theme_kustom);
    }

    private static String hint(Activity activity, String id) {
        if (ThemePresets.SAYA.equals(id)) return activity.getString(R.string.theme_saya_hint);
        if (ThemePresets.ORANG_TUA.equals(id)) return activity.getString(R.string.theme_orang_tua_hint);
        if (ThemePresets.ANAK.equals(id)) return activity.getString(R.string.theme_anak_hint);
        if (ThemePresets.REMAJA.equals(id)) return activity.getString(R.string.theme_remaja_hint);
        if (ThemePresets.SALJU.equals(id)) return activity.getString(R.string.theme_salju_hint);
        if (ThemePresets.GUGUR.equals(id)) return activity.getString(R.string.theme_gugur_hint);
        if (ThemePresets.DINGIN.equals(id)) return activity.getString(R.string.theme_dingin_hint);
        if (ThemePresets.SEMI.equals(id)) return activity.getString(R.string.theme_semi_hint);
        return activity.getString(R.string.theme_kustom_hint);
    }

    private static String motionText(Activity activity, SharedPreferences prefs) {
        boolean on = prefs.getBoolean(ThemePresets.SEASON_MOTION_KEY, true);
        return activity.getString(on ? R.string.theme_motion_on : R.string.theme_motion_off);
    }

    private static String tickerText(Activity activity, SharedPreferences prefs) {
        boolean on = prefs.getBoolean(ThemePresets.TEEN_TICKER_KEY, false);
        return activity.getString(on ? R.string.theme_ticker_on : R.string.theme_ticker_off);
    }

    private static String pinText(Activity activity, SharedPreferences prefs) {
        boolean on = ThemePresets.pinSet(prefs.getString(ThemePresets.PIN_KEY, ""));
        return activity.getString(on ? R.string.theme_pin_on : R.string.theme_pin_set);
    }
}
