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
 * Ultra minimal monochrome: hairline rules, zero radius, enormous whitespace and
 * typographic controls. Hierarchy comes only from type size and line work.
 */
public final class MonoSkin extends BaseSkin {

    public MonoSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        boolean dark = theme.dark;
        t.bg = dark ? 0xFF0A0A0A : 0xFFFAFAFA;
        t.panel = 0x00000000;
        t.panelAlt = dark ? 0xFF121212 : 0xFFF0F0F0;
        t.text = dark ? 0xFFF5F5F5 : 0xFF0A0A0A;
        t.muted = TgTheme.alpha(t.text, 0.55f);
        t.line = TgTheme.alpha(t.text, 0.12f);
        // no colour at all: the accent is pure contrast
        t.accent = dark ? 0xFFFFFFFF : 0xFF000000;
        t.onAccent = dark ? 0xFF0A0A0A : 0xFFFFFFFF;
        t.accentSoft = TgTheme.alpha(t.text, 0.08f);

        t.radiusPanel = 0f;
        t.radiusControl = 0f;
        t.rowHeight = 64f;
        t.padH = 28f;
        t.padV = 10f;
        t.lineWidth = 1f;
        t.sectionGap = 40f;

        t.titleSize = 14f;
        t.descSize = 11f;
        t.sectionSize = 26f;
        t.valueSize = 12f;

        t.upperCaseSections = false;
        t.monoNumerals = true;
        t.motionMs = 120L;
        t.ease = SkinTokens.TgEase.LINEAR;
        t.monoTypeface = TypefaceOf.mono();
        return t;
    }

    @NonNull
    @Override
    public String id() {
        return SkinRegistry.MONO;
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        // panels are not boxed in this style; only a rule marks the top edge
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(l, t, r, t, paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(mTokens.text, empty ? 0.10f : 0.85f));
        paint.setStrokeWidth(Math.max(1f, dp(2f)));
        canvas.drawLine(l, y, r, y, paint);
        paint.recycle();
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(x1, y, x2, y, paint);
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        // the widget draws [ ON ]/[ OFF ] text; here only the inverse pill behind it
        if (progress <= 0.01f) {
            return;
        }
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(mTokens.text, 0.90f * progress));
        canvas.drawRect(l, t, r, b, paint);
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
        paint.setColor(mTokens.text);
        paint.setStrokeWidth(Math.max(2f, dp(3f)));
        if (x > l) {
            canvas.drawLine(l, cy, x, cy, paint);
        }
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(x, cy - dp(5f), x, cy + dp(5f), paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        // underline tabs: only the active item gets a 2dp rule beneath it
        if (!active) {
            return;
        }
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.text);
        paint.setStrokeWidth(Math.max(1f, dp(2f)));
        canvas.drawLine(l, b - dp(1f), r, b - dp(1f), paint);
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(focused ? mTokens.text : mTokens.line);
        paint.setStrokeWidth(Math.max(1f, dp(focused ? 1.6f : 1f)));
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
        paint.setColor(TgTheme.alpha(mTokens.text, enabled ? (pressed ? 1f : 0.6f) : 0.2f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        if (active) {
            paint.setColor(mTokens.text);
            paint.setStrokeWidth(Math.max(1f, dp(2f)));
            canvas.drawLine(l + dp(1f), t + dp(6f), l + dp(1f), b - dp(6f), paint);
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
