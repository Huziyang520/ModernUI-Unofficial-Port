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
import icyllis.modernui.text.Typeface;

/**
 * Industrial control panel: graphite plates, hairline steel borders, amber readouts,
 * monospaced numerals, corner brackets and calibrated scales.
 */
public final class IndustrialSkin extends BaseSkin {

    public IndustrialSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        // the plate colour is fixed; the accent is only a highlight and a pointer here
        t.bg = 0xFF0E1116;
        t.panel = 0xFF171C23;
        t.panelAlt = 0xFF10151B;
        t.line = 0xFF2C3642;
        t.text = 0xFFD7DEE6;
        t.muted = 0xFF7E8B99;
        t.danger = 0xFFE5484D;
        t.accent = theme.dark ? theme.accent : 0xFFFFB020;
        t.onAccent = TgTheme.contrasting(t.accent);
        t.accentSoft = TgTheme.alpha(t.accent, 0.22f);

        t.radiusPanel = 2f;
        t.radiusControl = 2f;
        t.rowHeight = 56f;
        t.padH = 16f;
        t.padV = 8f;
        t.lineWidth = 1f;
        t.sectionGap = 18f;

        t.titleSize = 13f;
        t.descSize = 10.5f;
        t.sectionSize = 11f;
        t.valueSize = 12f;

        t.upperCaseSections = true;
        t.monoNumerals = true;
        t.cornerMarks = true;
        t.motionMs = 90L;
        t.ease = SkinTokens.TgEase.LINEAR;
        t.typeface = mono();
        t.monoTypeface = t.typeface;
        return t;
    }

    private static Typeface mono() {
        var font = Typeface.getSystemFont("Consolas");
        return font != null ? font : Typeface.MONOSPACED;
    }

    @NonNull
    @Override
    public String id() {
        return SkinRegistry.INDUSTRIAL;
    }

    @Override
    public void drawBackdrop(@NonNull Canvas canvas, int width, int height) {
        // no base fill: the blur layer owns the backdrop colour
        // faint vertical seams keep the plate from reading as a flat void
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(mTokens.line, 0.35f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        for (float x = 0; x < width; x += dp(96f)) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(mTokens.panelSurface());
        canvas.drawRect(l, t, r, b, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(mTokens.line);
        canvas.drawRect(l, t, r, b, paint);

        // L shaped brackets in every corner
        float arm = dp(7f);
        paint.setColor(mTokens.accent);
        paint.setStrokeWidth(dp(1.4f));
        canvas.drawLine(l, t, l + arm, t, paint);
        canvas.drawLine(l, t, l, t + arm, paint);
        canvas.drawLine(r, t, r - arm, t, paint);
        canvas.drawLine(r, t, r, t + arm, paint);
        canvas.drawLine(l, b, l + arm, b, paint);
        canvas.drawLine(l, b, l, b - arm, paint);
        canvas.drawLine(r, b, r - arm, b, paint);
        canvas.drawLine(r, b, r, b - arm, paint);
        paint.recycle();
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(x1, y, x2, y, paint);
        // tick marks at both ends read as a calibrated scale
        paint.setColor(TgTheme.alpha(mTokens.muted, 0.65f));
        canvas.drawLine(x1, y - dp(3f), x1, y + dp(3f), paint);
        canvas.drawLine(x2, y - dp(3f), x2, y + dp(3f), paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(mTokens.line, empty ? 0.5f : 1f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(l, y, r, y, paint);
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float inset = (b - t) * 0.22f;
        float frameT = t + inset;
        float frameB = b - inset;
        paint.setColor(TgTheme.alpha(mTokens.text, 0.10f));
        canvas.drawRect(l, frameT, r, frameB, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.mix(mTokens.line, mTokens.accent, progress));
        canvas.drawRect(l, frameT, r, frameB, paint);

        float knobW = (frameB - frameT) * 0.86f;
        float travel = (r - l) - knobW - dp(2f) * 2f;
        float x = l + dp(2f) + travel * progress;
        paint.setStyle(Paint.FILL);
        paint.setColor(TgTheme.mix(mTokens.muted, mTokens.accent, progress));
        canvas.drawRect(x, frameT + dp(2f), x + knobW, frameB - dp(2f), paint);
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float cy = (t + b) * 0.5f;
        paint.setColor(TgTheme.alpha(mTokens.text, 0.14f));
        paint.setStrokeWidth(dp(2f));
        canvas.drawLine(l, cy, r, cy, paint);

        // ten calibration ticks along the travel
        paint.setColor(TgTheme.alpha(mTokens.muted, 0.55f));
        paint.setStrokeWidth(dp(1f));
        for (int i = 0; i <= 10; i++) {
            float x = l + (r - l) * i / 10f;
            float len = i % 5 == 0 ? dp(4f) : dp(2.5f);
            canvas.drawLine(x, cy - len, x, cy + len, paint);
        }

        float x = l + (r - l) * fraction;
        paint.setColor(mTokens.accent);
        paint.setStrokeWidth(dp(2.2f));
        canvas.drawLine(x, cy - dp(6f), x, cy + dp(6f), paint);
        // pointer triangle above the bar
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(x, cy - dp(7f), x - dp(3f), cy - dp(12f), paint);
        canvas.drawLine(x, cy - dp(7f), x + dp(3f), cy - dp(12f), paint);
        canvas.drawLine(x - dp(3f), cy - dp(12f), x + dp(3f), cy - dp(12f), paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        if (active) {
            paint.setColor(mTokens.accent);
            canvas.drawRect(l, t, r, b, paint);
        } else if (pressed) {
            paint.setColor(TgTheme.alpha(mTokens.text, 0.10f));
            canvas.drawRect(l, t, r, b, paint);
        }
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(active ? mTokens.accent : mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(mTokens.text, 0.05f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(focused ? 1.5f : 1f));
        paint.setColor(focused ? mTokens.accent : mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        // clipped corner hint
        paint.setColor(TgTheme.alpha(mTokens.muted, 0.5f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(r - dp(6f), b, r, b - dp(6f), paint);
        paint.recycle();
    }

    @Override
    public void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean pressed, boolean enabled) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(enabled ? TgTheme.alpha(mTokens.accent, pressed ? 0.30f : 0.16f)
                : TgTheme.alpha(mTokens.text, 0.06f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(enabled ? mTokens.accent : mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        if (active) {
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.18f));
            canvas.drawRect(l, t, r, b, paint);
            paint.setColor(mTokens.accent);
            canvas.drawRect(l, t, l + Math.max(2f, dp(3f)), b, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }
}
