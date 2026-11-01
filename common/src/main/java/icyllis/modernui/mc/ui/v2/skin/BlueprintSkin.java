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
 * Engineering blueprint: deep navy paper, a two level drafting grid, thin linework,
 * registration marks, dashed separators and dimension lines.
 */
public final class BlueprintSkin extends BaseSkin {

    public BlueprintSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        t.bg = 0xFF0B1E3A;
        t.panel = 0xFF0E2547;
        t.panelAlt = 0xFF0C2040;
        t.line = TgTheme.alpha(0xFFBFD8FF, 0.40f);
        t.text = 0xFFE6F0FF;
        t.muted = 0xFF8FB0DA;
        t.accent = theme.accent;
        t.onAccent = TgTheme.contrasting(t.accent);
        t.accentSoft = TgTheme.alpha(t.accent, 0.14f);

        t.radiusPanel = 0f;
        t.radiusControl = 0f;
        t.rowHeight = 52f;
        t.padH = 16f;
        t.padV = 8f;
        t.lineWidth = 1f;
        t.sectionGap = 18f;

        t.titleSize = 12f;
        t.descSize = 10f;
        t.sectionSize = 13f;
        t.valueSize = 12f;

        t.upperCaseSections = true;
        t.upperCaseRows = true;
        t.monoNumerals = true;
        t.texture = true;
        t.cornerMarks = true;
        t.dashedDivider = true;
        t.motionMs = 120L;
        t.ease = SkinTokens.TgEase.LINEAR;
        t.typeface = TypefaceOf.mono();
        t.monoTypeface = t.typeface;
        return t;
    }

    @NonNull
    @Override
    public String id() {
        return SkinRegistry.BLUEPRINT;
    }

    @Override
    public void drawBackdrop(@NonNull Canvas canvas, int width, int height) {
        canvas.drawColor(mTokens.bg);
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));

        paint.setColor(TgTheme.alpha(0xFFBFD8FF, 0.06f));
        float fine = dp(8f);
        for (float x = 0; x < width; x += fine) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        for (float y = 0; y < height; y += fine) {
            canvas.drawLine(0, y, width, y, paint);
        }

        paint.setColor(TgTheme.alpha(0xFFBFD8FF, 0.12f));
        float coarse = dp(32f);
        for (float x = 0; x < width; x += coarse) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        for (float y = 0; y < height; y += coarse) {
            canvas.drawLine(0, y, width, y, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(mTokens.panel, 0.90f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(mTokens.line);
        canvas.drawRect(l, t, r, b, paint);

        // registration marks, 10dp legs
        float arm = dp(10f);
        paint.setColor(TgTheme.alpha(0xFFBFD8FF, 0.65f));
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
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        // 3dp dash then 3dp gap
        float dash = dp(3f);
        for (float x = x1; x < x2; x += dash * 2f) {
            canvas.drawLine(x, y, Math.min(x + dash, x2), y, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(0xFFBFD8FF, empty ? 0.20f : 0.55f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(l, y, r, y, paint);
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float cy = (t + b) * 0.5f;
        float node = dp(6f);
        float x0 = l + node;
        float x1 = r - node;
        // two circuit nodes joined by a wire
        paint.setColor(TgTheme.mix(mTokens.line, mTokens.accent, progress));
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(x0, cy, x1, cy, paint);

        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1.4f));
        paint.setColor(TgTheme.mix(mTokens.muted, mTokens.accent, 1f - progress));
        canvas.drawCircle(x0, cy, node, paint);
        paint.setColor(TgTheme.mix(mTokens.muted, mTokens.accent, progress));
        canvas.drawCircle(x1, cy, node, paint);

        // the active node fills in
        paint.setStyle(Paint.FILL);
        if (progress > 0.5f) {
            canvas.drawCircle(x1, cy, node * 0.55f, paint);
        } else {
            paint.setColor(mTokens.muted);
            canvas.drawCircle(x0, cy, node * 0.55f, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float cy = (t + b) * 0.5f;
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(l, cy, r, cy, paint);

        // hollow nodes every 25%
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        for (int i = 0; i <= 4; i++) {
            float x = l + (r - l) * i / 4f;
            paint.setColor(i / 4f <= fraction ? mTokens.accent : mTokens.muted);
            canvas.drawCircle(x, cy, dp(2.5f), paint);
        }

        float x = l + (r - l) * fraction;
        paint.setColor(mTokens.accent);
        paint.setStrokeWidth(dp(1.2f));
        canvas.drawLine(x, cy - dp(6f), x, cy - dp(2f), paint);
        canvas.drawLine(x, cy - dp(2f), x - dp(3f), cy - dp(7f), paint);
        canvas.drawLine(x, cy - dp(2f), x + dp(3f), cy - dp(7f), paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(active ? 2f : 1f));
        paint.setColor(active ? mTokens.accent : mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        if (active) {
            // dimension line under the selected segment
            paint.setStrokeWidth(dp(1f));
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.7f));
            canvas.drawLine(l, b + dp(2f), r, b + dp(2f), paint);
            canvas.drawLine(l, b + dp(0.5f), l, b + dp(3.5f), paint);
            canvas.drawLine(r, b + dp(0.5f), r, b + dp(3.5f), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(mTokens.panel, 0.6f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(focused ? 1.6f : 1f));
        paint.setColor(focused ? mTokens.accent : mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        // corner ticks
        float arm = dp(4f);
        paint.setStrokeWidth(dp(1f));
        canvas.drawLine(l, t, l + arm, t, paint);
        canvas.drawLine(l, t, l, t + arm, paint);
        canvas.drawLine(r, b, r - arm, b, paint);
        canvas.drawLine(r, b, r, b - arm, paint);
        paint.recycle();
    }

    @Override
    public void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean pressed, boolean enabled) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        if (pressed) {
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.16f));
            canvas.drawRect(l, t, r, b, paint);
        }
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
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.16f));
            canvas.drawRect(l, t, r, b, paint);
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(dp(1f));
            paint.setColor(mTokens.accent);
            canvas.drawRect(l, t, r, b, paint);
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
