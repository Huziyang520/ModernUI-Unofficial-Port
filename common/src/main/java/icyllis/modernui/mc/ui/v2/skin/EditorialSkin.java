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
 * Editorial print: warm off white paper, near black ink, serif display headings and
 * inset hairline rules. The accent is used as ink red.
 */
public final class EditorialSkin extends BaseSkin {

    public EditorialSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        t.bg = 0xFFF6F2E9;
        t.panel = 0xFFFFFDF7;
        t.panelAlt = 0xFFF1ECE0;
        t.line = TgTheme.alpha(0xFF1A1A1A, 0.16f);
        t.text = 0xFF141414;
        t.muted = 0xFF5C5751;
        // the player accent becomes the ink colour
        t.accent = theme.accent;
        t.onAccent = TgTheme.contrasting(theme.accent);
        t.accentSoft = TgTheme.alpha(theme.accent, 0.10f);

        t.radiusPanel = 0f;
        t.radiusControl = 0f;
        t.rowHeight = 58f;
        t.padH = 24f;
        t.padV = 10f;
        t.lineWidth = 1f;
        t.sectionGap = 28f;

        t.titleSize = 14f;
        t.descSize = 11f;
        t.sectionSize = 20f;
        t.valueSize = 12f;

        t.monoNumerals = true;
        t.motionMs = 160L;
        t.ease = SkinTokens.TgEase.OUT_CUBIC;
        t.titleTypeface = TypefaceOf.serif();
        t.monoTypeface = TypefaceOf.mono();
        return t;
    }

    @NonNull
    @Override
    public String id() {
        return SkinRegistry.EDITORIAL;
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.panel);
        canvas.drawRect(l, t, r, b, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        paint.setColor(mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        // magazine rules are inset, not full bleed
        canvas.drawLine(x1 + dp(24f), y, x2, y, paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(mTokens.text, empty ? 0.10f : 0.85f));
        paint.setStrokeWidth(Math.max(1f, dp(3f)));
        canvas.drawLine(l, y, r, y, paint);
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        // the widget draws the words; the active one gets an ink underline
        if (progress <= 0.02f) {
            return;
        }
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(mTokens.accent, progress));
        paint.setStrokeWidth(Math.max(1f, dp(3f)));
        canvas.drawLine(l, b - dp(1f), r, b - dp(1f), paint);
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float cy = (t + b) * 0.5f;
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(l, cy, r, cy, paint);
        float x = l + (r - l) * fraction;
        paint.setColor(mTokens.accent);
        paint.setStrokeWidth(Math.max(2f, dp(2f)));
        if (x > l) {
            canvas.drawLine(l, cy, x, cy, paint);
        }
        paint.setStyle(Paint.FILL);
        canvas.drawCircle(x, cy, dp(dragging ? 4.5f : 3.5f), paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        if (!active) {
            return;
        }
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.accent);
        paint.setStrokeWidth(Math.max(1f, dp(3f)));
        canvas.drawLine(l, b - dp(1.5f), r, b - dp(1.5f), paint);
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(focused ? mTokens.accent : mTokens.line);
        paint.setStrokeWidth(Math.max(1f, dp(focused ? 2f : 1f)));
        canvas.drawLine(l, b - dp(0.5f), r, b - dp(0.5f), paint);
        paint.recycle();
    }

    @Override
    public void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean pressed, boolean enabled) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        paint.setColor(TgTheme.alpha(mTokens.accent, enabled ? (pressed ? 1f : 0.7f) : 0.25f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        if (active) {
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.10f));
            canvas.drawRect(l, t, r, b, paint);
            paint.setColor(mTokens.accent);
            paint.setStrokeWidth(Math.max(1f, dp(3f)));
            canvas.drawLine(l + dp(1f), t + dp(4f), l + dp(1f), b - dp(4f), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        paint.setColor(mTokens.line);
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }
}
