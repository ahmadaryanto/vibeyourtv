package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.View;

/**
 * A few drifting shapes over the wallpaper. About 20 frames a second, no per-frame allocation
 * and no hardware layer. {@link #stop()} drops the callbacks.
 */
final class SeasonOverlay extends View implements Runnable {
    private static final int MAX = 12;
    private static final long FRAME_MS = 50;

    private final float density;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] x = new float[MAX];
    private final float[] phase = new float[MAX];
    private final float[] speed = new float[MAX];
    private final float[] size = new float[MAX];
    private final int[] color = new int[MAX];
    private int count;
    private int kind = -1;
    private boolean running;
    private long startMs;

    SeasonOverlay(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        setWillNotDraw(false);
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void start(String season) {
        int next = seasonIndex(season);
        if (next < 0) {
            stop();
            return;
        }
        if (next != kind) seed(next);
        kind = next;
        running = true;
        if (startMs == 0) startMs = SystemClock.uptimeMillis();
        setVisibility(VISIBLE);
        removeCallbacks(this);
        invalidate();
    }

    void stop() {
        running = false;
        removeCallbacks(this);
        setVisibility(GONE);
    }

    public void run() {
        if (running) invalidate();
    }

    private static int seasonIndex(String season) {
        String name = ThemePresets.canonicalSeason(season);
        if (ThemePresets.SNOW.equals(name)) return 0;
        if (ThemePresets.LEAVES.equals(name)) return 1;
        if (ThemePresets.FROST.equals(name)) return 2;
        if (ThemePresets.PETALS.equals(name)) return 3;
        return -1;
    }

    private void seed(int next) {
        int h = next * 997;
        count = next == 0 ? 12 : next == 1 ? 8 : next == 3 ? 10 : 6;
        for (int i = 0; i < count; i++) {
            h = h * 1103515245 + 12345;
            float u = ((h >>> 8) & 255) / 255f;
            x[i] = (i + u) / count;
            phase[i] = ((h >>> 16) & 255) / 255f;
            speed[i] = (next == 0 ? 0.04f : next == 1 ? 0.03f : 0.02f) + u * 0.02f;
            size[i] = (next == 2 ? 1.6f : next == 0 ? 2.4f : 4f) + u * 2f;
            color[i] = next == 1 ? (i % 2 == 0 ? 0xE0D2691E : 0xD08C4A22)
                    : next == 3 ? (i % 2 == 0 ? 0xE0C45078 : 0xD0E89BB8)
                    : next == 0 ? (i % 2 == 0 ? 0xCCF4FAFF : 0xAA5C7C99)
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
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            postDelayed(this, FRAME_MS);
            return;
        }
        float t = (SystemClock.uptimeMillis() - startMs) / 1000f;
        boolean oval = kind == 1 || kind == 3;
        for (int i = 0; i < count; i++) {
            float px = x[i] * w + (float) Math.sin(t * 0.8f + phase[i] * 6.2f) * 18f * density;
            float py = ((phase[i] + t * speed[i]) % 1f) * (h + 40f * density) - 20f * density;
            paint.setColor(color[i]);
            float r = size[i] * density;
            if (oval) {
                canvas.save();
                canvas.rotate((t * 24f + phase[i] * 180f) % 360f, px, py);
                canvas.drawOval(px - r, py - r * 0.45f, px + r, py + r * 0.45f, paint);
                canvas.restore();
            } else {
                canvas.drawCircle(px, py, r, paint);
            }
        }
        removeCallbacks(this);
        postDelayed(this, FRAME_MS);
    }
}
