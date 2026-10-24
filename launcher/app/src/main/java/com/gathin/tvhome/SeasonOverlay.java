package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;

/**
 * A few drifting shapes over the wallpaper: snow, leaves, frost specks or petals. Drawn at about
 * 20 frames a second, with no per-frame allocation and no hardware layer, so a 1 GB TV can leave
 * it on. {@link #stop()} drops the callbacks; the picker's "Gerak musim" switch is what calls it.
 */
final class SeasonOverlay extends View {
    private static final int MAX = 12;
    private static final long FRAME_MS = 50;

    private final float density;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final float[] x = new float[MAX];
    private final float[] phase = new float[MAX];
    private final float[] speed = new float[MAX];
    private final float[] sway = new float[MAX];
    private final float[] size = new float[MAX];
    private final int[] color = new int[MAX];
    private int count;
    private String kind = "";
    private boolean running;
    private long startMs;
    private final Runnable tick = () -> {
        if (running) invalidate();
    };

    SeasonOverlay(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        setWillNotDraw(false);
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void start(String season) {
        String next = ThemePresets.canonicalSeason(season);
        if (next.isEmpty()) {
            stop();
            return;
        }
        if (!next.equals(kind) || count == 0) seed(next);
        kind = next;
        running = true;
        if (startMs == 0) startMs = SystemClock.uptimeMillis();
        setVisibility(VISIBLE);
        removeCallbacks(tick);
        invalidate();
    }

    void stop() {
        running = false;
        removeCallbacks(tick);
        setVisibility(GONE);
    }

    private void seed(String next) {
        Random rnd = new Random(next.hashCode());
        boolean snow = ThemePresets.SNOW.equals(next);
        boolean leaves = ThemePresets.LEAVES.equals(next);
        boolean petals = ThemePresets.PETALS.equals(next);
        count = snow ? 12 : leaves ? 8 : petals ? 10 : 6;
        for (int i = 0; i < count; i++) {
            x[i] = (i + rnd.nextFloat()) / count;
            phase[i] = rnd.nextFloat();
            speed[i] = (snow ? 0.035f : leaves ? 0.028f : petals ? 0.02f : 0.012f) + rnd.nextFloat() * 0.02f;
            sway[i] = 0.4f + rnd.nextFloat() * 0.8f;
            size[i] = snow ? 2.2f + rnd.nextFloat() * 2.2f
                    : leaves ? 5f + rnd.nextFloat() * 3f
                    : petals ? 3.5f + rnd.nextFloat() * 2.5f
                    : 1.4f + rnd.nextFloat();
            color[i] = snow ? (i % 2 == 0 ? 0xCCF4FAFF : 0xAA5C7C99)
                    : leaves ? (i % 2 == 0 ? 0xE0D2691E : 0xD08C4A22)
                    : petals ? (i % 2 == 0 ? 0xE0C45078 : 0xD0E89BB8)
                    : 0x99E7F6FF;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!running || count == 0) return;
        float t = (SystemClock.uptimeMillis() - startMs) / 1000f;
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            if (running) postDelayed(tick, FRAME_MS);
            return;
        }
        boolean leaves = ThemePresets.LEAVES.equals(kind);
        boolean petals = ThemePresets.PETALS.equals(kind);
        for (int i = 0; i < count; i++) {
            float yFrac = (phase[i] + t * speed[i]) % 1f;
            float swayPx = (float) Math.sin(t * sway[i] + phase[i] * 6.28f) * 18f * density;
            float px = x[i] * w + swayPx;
            float py = yFrac * (h + 40f * density) - 20f * density;
            paint.setColor(color[i]);
            float r = size[i] * density;
            if (leaves || petals) {
                canvas.save();
                canvas.rotate((t * 24f + phase[i] * 180f) % 360f, px, py);
                oval.set(px - r, py - r * 0.45f, px + r, py + r * 0.45f);
                canvas.drawOval(oval, paint);
                canvas.restore();
            } else {
                canvas.drawCircle(px, py, r, paint);
            }
        }
        removeCallbacks(tick);
        if (running) postDelayed(tick, FRAME_MS);
    }
}
