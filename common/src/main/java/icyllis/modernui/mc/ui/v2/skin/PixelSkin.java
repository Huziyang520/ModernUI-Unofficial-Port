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
 * Pixel art: hard edges, 2dp outlines, dithered checkerboard, chunky bevelled blocks.
 * Every size is a multiple of 4dp and nothing is anti-aliased.
 */
public final class PixelSkin extends BaseSkin {

    public PixelSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        t.bg = 0xFF14121A;
        t.panel = 0xFF221E2B;
        t.panelAlt = 0xFF1A1721;
        t.line = 0xFF3B3450;
        t.text = 0xFFF2E9E4;
        t.muted = 0xFF9C90B0;
        t.accent = theme.accent;
        t.onAccent = TgTheme.contrasting(t.accent);
        t.accentSoft = TgTheme.alpha(t.accent, 0.30f);

        t.radiusPanel = 0f;
        t.radiusControl = 0f;
        t.rowHeight = 44f;
        t.padH = 12f;
        t.padV = 8f;
        t.lineWidth = 2f;
        t.sectionGap = 16f;

        t.titleSize = 12f;
        t.descSize = 10f;
        t.sectionSize = 12f;
        t.valueSize = 11f;

        t.upperCaseSections = true;
        t.upperCaseRows = true;
        t.monoNumerals = true;
        t.texture = true;
        t.motionMs = 0L;
        t.ease = SkinTokens.TgEase.LINEAR;
        t.typeface = TypefaceOf.mono();
        t.monoTypeface = t.typeface;
        return t;
    }

    @NonNull
    @Override
    public String id() {
        return SkinRegistry.PIXEL;
    }

    @Override
    public void drawBackdrop(@NonNull Canvas canvas, int width, int height) {
        canvas.drawColor(mTokens.bg);
        // 8dp checkerboard, two shades three percent apart
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float cell = dp(8f);
        int light = TgTheme.shade(mTokens.bg, 1.06f);
        for (float y = 0; y < height; y += cell) {
            boolean odd = ((int) (y / cell) & 1) == 1;
            for (float x = 0; x < width; x += cell) {
                if (odd == (((int) (x / cell) & 1) == 1)) {
                    paint.setColor(light);
                    canvas.drawRect(x, y, Math.min(x + cell, width), Math.min(y + cell, height), paint);
                }
            }
        }
        paint.recycle();
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(mTokens.panel);
        canvas.drawRect(l, t, r, b, paint);
        bevel(canvas, paint, l, t, r, b);
        paint.recycle();
    }

    /** Two tone bevel: light on the top left, dark on the bottom right. */
    private void bevel(@NonNull Canvas canvas, @NonNull Paint paint,
                       float l, float t, float r, float b) {
        float w = Math.max(2f, dp(2f));
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.14f));
        canvas.drawRect(l, t, r, t + w, paint);
        canvas.drawRect(l, t, l + w, b, paint);
        paint.setColor(TgTheme.alpha(0xFF000000, 0.35f));
        canvas.drawRect(l, b - w, r, b, paint);
        canvas.drawRect(r - w, t, r, b, paint);
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(mTokens.line);
        canvas.drawRect(x1, y, x2, y + Math.max(2f, dp(2f)), paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(mTokens.line, empty ? 0.5f : 1f));
        canvas.drawRect(l, y, r, y + Math.max(2f, dp(2f)), paint);
        // three blocks flanking the heading
        paint.setColor(empty ? TgTheme.alpha(mTokens.accent, 0.35f) : mTokens.accent);
        for (int i = 0; i < 3; i++) {
            float bx = l + i * dp(6f);
            canvas.drawRect(bx, y - dp(6f), bx + dp(4f), y - dp(2f), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float box = dp(16f);
        float cy = (t + b) * 0.5f;
        float left = l;
        float gap = dp(4f);
        // two blocks side by side: off then on
        paint.setColor(TgTheme.alpha(mTokens.text, 0.16f));
        canvas.drawRect(left, cy - box * 0.5f, left + box, cy + box * 0.5f, paint);
        paint.setColor(TgTheme.mix(TgTheme.alpha(mTokens.text, 0.10f), mTokens.accent, progress));
        float onX = left + box + gap;
        canvas.drawRect(onX, cy - box * 0.5f, onX + box, cy + box * 0.5f, paint);
        if (checked) {
            paint.setColor(TgTheme.contrasting(mTokens.accent));
            float inner = dp(2f);
            canvas.drawRect(onX + inner, cy - box * 0.5f + inner,
                    onX + box - inner, cy + box * 0.5f - inner, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float cy = (t + b) * 0.5f;
        float cell = dp(6f);
        float gap = dp(2f);
        int cells = Math.max(1, (int) ((r - l) / (cell + gap)));
        int filled = Math.round(cells * fraction);
        for (int i = 0; i < cells; i++) {
            float x = l + i * (cell + gap);
            paint.setColor(i < filled ? mTokens.accent : TgTheme.alpha(mTokens.text, 0.16f));
            canvas.drawRect(x, cy - cell * 0.5f, x + cell, cy + cell * 0.5f, paint);
        }
        // inverted triangle above the head cell
        float headX = l + filled * (cell + gap);
        paint.setColor(mTokens.accent);
        canvas.drawRect(headX, cy - cell * 0.5f - dp(5f), headX + cell, cy - cell * 0.5f - dp(3f), paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(active ? mTokens.accent : TgTheme.alpha(mTokens.text, 0.08f));
        canvas.drawRect(l, t, r, b, paint);
        bevel(canvas, paint, l, t, r, b);
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(mTokens.text, 0.05f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setColor(focused ? mTokens.accent : mTokens.line);
        float w = Math.max(2f, dp(2f));
        canvas.drawRect(l, t, r, t + w, paint);
        canvas.drawRect(l, b - w, r, b, paint);
        canvas.drawRect(l, t, l + w, b, paint);
        canvas.drawRect(r - w, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        if (active) {
            paint.setColor(mTokens.accent);
            canvas.drawRect(l, t, r, b, paint);
            paint.setColor(TgTheme.contrasting(mTokens.accent));
            canvas.drawRect(l + dp(3f), t + dp(3f), r - dp(3f), b - dp(3f), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(mTokens.line);
        float w = Math.max(2f, dp(2f));
        canvas.drawRect(l, t, r, t + w, paint);
        canvas.drawRect(l, b - w, r, b, paint);
        canvas.drawRect(l, t, l + w, b, paint);
        canvas.drawRect(r - w, t, r, b, paint);
        paint.recycle();
    }
}
