/*
 * Modern UI.
 * Copyright (C) 2026 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc.ui.v2.skin;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.mc.ui.v2.TgTheme;

/**
 * Cyberpunk neon: near black indigo, cyan and magenta tube light. Glow is faked with
 * stacked strokes of decreasing opacity, plus a faint grid and CRT scanlines.
 */
public final class NeonSkin extends BaseSkin {

    private static final int MAGENTA = 0xFFF472B6;

    public NeonSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        t.bg = 0xFF070613;
        t.panel = 0xD90E0B1E;
        t.panelAlt = 0xFF0B0919;
        t.line = TgTheme.alpha(0xFF22D3EE, 0.35f);
        t.text = 0xFFEAF7FF;
        t.muted = 0xFF8EA0C0;
        t.accent = theme.accent;
        t.onAccent = TgTheme.contrasting(t.accent);
        t.accentSoft = TgTheme.alpha(t.accent, 0.16f);
        t.danger = 0xFFFF2D78;

        t.radiusPanel = 12f;
        t.radiusControl = 999f;
        t.rowHeight = 54f;
        t.padH = 16f;
        t.padV = 8f;
        t.lineWidth = 1f;
        t.sectionGap = 20f;

        t.titleSize = 13f;
        t.descSize = 10.5f;
        t.sectionSize = 12f;
        t.valueSize = 11.5f;

        t.upperCaseSections = true;
        t.monoNumerals = true;
        t.texture = true;
        t.glow = true;
        t.glowLayers = 3;
        t.motionMs = 180L;
        t.ease = SkinTokens.TgEase.OUT_CUBIC;
        t.monoTypeface = TypefaceOf.mono();
        return t;
    }

    @NonNull
    @Override
    public String id() {
        return SkinRegistry.NEON;
    }

    @Override
    public void drawBackdrop(@NonNull Canvas canvas, int width, int height) {
        canvas.drawColor(mTokens.bg);
        var paint = Paint.obtain();
        paint.setAntiAlias(true);

        // perspective grid: verticals every 48dp, horizontals every 32dp, 3% alpha
        paint.setColor(TgTheme.alpha(0xFF22D3EE, 0.045f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        for (float x = 0; x < width; x += dp(48f)) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        for (float y = 0; y < height; y += dp(32f)) {
            canvas.drawLine(0, y, width, y, paint);
        }

        // scanlines every 3dp at 4% alpha
        paint.setColor(TgTheme.alpha(0xFF000000, 0.20f));
        for (float y = 0; y < height; y += dp(3f)) {
            canvas.drawLine(0, y, width, y, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.panel);
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);

        glowStroke(canvas, paint, l, t, r, b, radius, mTokens.accent);
        paint.setStyle(Paint.FILL);
        // luminous bar along the bottom edge
        paint.setColor(TgTheme.alpha(mTokens.accent, 0.85f));
        canvas.drawRoundRect(l + dp(10f), b - dp(3f), r - dp(10f), b - dp(1f), dp(1f), paint);
        paint.recycle();
    }

    /** Three stacked strokes: wide and faint, then narrow and bright. */
    private void glowStroke(@NonNull Canvas canvas, @NonNull Paint paint,
                            float l, float t, float r, float b, float radius, int color) {
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(3f));
        paint.setColor(TgTheme.alpha(color, 0.08f));
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
        paint.setStrokeWidth(dp(2f));
        paint.setColor(TgTheme.alpha(color, 0.18f));
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(color, 0.90f));
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(mTokens.accent, 0.14f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(x1, y, x2, y, paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dp(1f));
        float mid = (l + r) * 0.5f;
        paint.setColor(TgTheme.alpha(mTokens.accent, empty ? 0.10f : 0.55f));
        canvas.drawLine(l, y, mid, y, paint);
        paint.setColor(TgTheme.alpha(MAGENTA, empty ? 0.10f : 0.55f));
        canvas.drawLine(mid, y, r, y, paint);
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        paint.setColor(TgTheme.alpha(mTokens.text, 0.10f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);

        if (progress > 0.02f) {
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(dp(3f));
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.10f * progress));
            canvas.drawRoundRect(l, t, r, b, radius, paint);
            paint.setStrokeWidth(dp(1.4f));
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.95f * progress));
            canvas.drawRoundRect(l, t, r, b, radius, paint);
            paint.setStyle(Paint.FILL);
        }

        float inset = dp(3f);
        float knob = (b - t) - inset * 2f;
        float x = l + inset + ((r - l) - inset * 2f - knob) * progress;
        // the knob glows: halo then core
        paint.setColor(TgTheme.alpha(mTokens.accent, 0.30f * Math.max(progress, 0.15f)));
        canvas.drawCircle(x + knob * 0.5f, (t + b) * 0.5f, knob * 0.72f, paint);
        paint.setColor(TgTheme.mix(TgTheme.alpha(mTokens.text, 0.55f), mTokens.accent, progress));
        canvas.drawCircle(x + knob * 0.5f, (t + b) * 0.5f, knob * 0.42f, paint);
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float cy = (t + b) * 0.5f;
        float thickness = dp(3f);
        paint.setColor(TgTheme.alpha(mTokens.text, 0.14f));
        canvas.drawRoundRect(l, cy - thickness * 0.5f, r, cy + thickness * 0.5f,
                thickness * 0.5f, paint);

        float x = l + (r - l) * fraction;
        if (x > l) {
            paint.setShader(new icyllis.modernui.graphics.LinearGradient(l, 0, Math.max(x, l + 1f), 0,
                    mTokens.accent, MAGENTA,
                    icyllis.modernui.graphics.Shader.TileMode.CLAMP, null));
            canvas.drawRoundRect(l, cy - thickness * 0.5f, x, cy + thickness * 0.5f,
                    thickness * 0.5f, paint);
            paint.setShader(null);
        }
        // crosshair plus glowing core
        paint.setColor(TgTheme.alpha(mTokens.accent, 0.28f));
        canvas.drawCircle(x, cy, dp(dragging ? 10f : 8f), paint);
        paint.setColor(mTokens.onAccent);
        canvas.drawCircle(x, cy, dp(6f), paint);
        paint.setColor(mTokens.accent);
        canvas.drawCircle(x, cy, dp(3.4f), paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        if (active) {
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.22f));
            canvas.drawRoundRect(l, t, r, b, radius, paint);
        }
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(active ? 1.6f : 1f));
        paint.setColor(active ? mTokens.accent
                : TgTheme.alpha(mTokens.text, pressed ? 0.30f : 0.14f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        paint.setColor(TgTheme.alpha(mTokens.text, 0.05f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        if (focused) {
            glowStroke(canvas, paint, l, t, r, b, radius, mTokens.accent);
        } else {
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(dp(1f));
            paint.setColor(TgTheme.alpha(mTokens.text, 0.16f));
            canvas.drawRoundRect(l, t, r, b, radius, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean pressed, boolean enabled) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        paint.setColor(TgTheme.alpha(enabled ? mTokens.accent : mTokens.text, pressed ? 0.22f : 0.12f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(enabled ? mTokens.accent : TgTheme.alpha(mTokens.text, 0.20f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        if (active) {
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.16f));
            canvas.drawRoundRect(l, t, r, b, dp(mTokens.radiusControl), paint);
            paint.setColor(mTokens.accent);
            canvas.drawRoundRect(l, t + (b - t) * 0.28f, l + Math.max(2f, dp(2f)),
                    b - (b - t) * 0.28f, dp(1f), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(mTokens.accent, 0.75f));
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
        paint.recycle();
    }
}
