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
 * Retro CRT terminal: phosphor green on near black, scanlines, a rounded tube frame and
 * character cell controls. The accent only highlights; the body stays phosphor green.
 */
public final class TerminalSkin extends BaseSkin {

    private static final int PHOSPHOR = 0xFF33FF66;

    public TerminalSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        t.bg = 0xFF050805;
        t.panel = 0xFF08120B;
        t.panelAlt = 0xFF060D08;
        t.line = 0xFF1F4D2A;
        t.text = PHOSPHOR;
        t.muted = 0xFF22A347;
        t.accent = theme.accent;
        t.onAccent = TgTheme.contrasting(theme.accent);
        t.accentSoft = TgTheme.alpha(theme.accent, 0.18f);
        t.danger = 0xFFFF5555;

        t.radiusPanel = 0f;
        t.radiusControl = 0f;
        t.rowHeight = 46f;
        t.padH = 14f;
        t.padV = 6f;
        t.lineWidth = 1f;
        t.sectionGap = 14f;

        t.titleSize = 13f;
        t.descSize = 10.5f;
        t.sectionSize = 12f;
        t.valueSize = 12f;

        t.upperCaseSections = true;
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
        return SkinRegistry.TERMINAL;
    }

    @Override
    public void drawBackdrop(@NonNull Canvas canvas, int width, int height) {
        // no base fill: the blur layer owns the backdrop colour
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        // scanline every 2dp
        paint.setColor(TgTheme.alpha(0xFF000000, 0.30f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        for (float y = 0; y < height; y += dp(2f)) {
            canvas.drawLine(0, y, width, y, paint);
        }
        // rounded tube corners; drawArc takes a bounding box plus a start angle
        paint.setColor(TgTheme.alpha(PHOSPHOR, 0.22f));
        float r = dp(14f);
        float m = dp(2f);
        float w = width - m;
        float h = height - m;
        canvas.drawArc(m, m, m + r * 2f, m + r * 2f, 180f, paint);
        canvas.drawArc(w - r * 2f, m, w, m + r * 2f, 270f, paint);
        canvas.drawArc(w - r * 2f, h - r * 2f, w, h, 0f, paint);
        canvas.drawArc(m, h - r * 2f, m + r * 2f, h, 90f, paint);
        paint.recycle();
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(mTokens.panelSurface());
        canvas.drawRect(l, t, r, b, paint);
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(dp(1f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(PHOSPHOR, 0.18f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(x1, y, x2, y, paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(PHOSPHOR, empty ? 0.12f : 0.30f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        // a dashed rule reads as a terminal separator
        float dash = dp(4f);
        for (float x = l; x < r; x += dash * 2f) {
            canvas.drawLine(x, y, Math.min(x + dash, r), y, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        // [x] / [ ] is drawn as text by the widget; here only the cell background
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(checked ? TgTheme.alpha(mTokens.accent, 0.18f)
                : TgTheme.alpha(PHOSPHOR, 0.06f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setColor(checked ? TgTheme.alpha(mTokens.accent, 0.8f) : TgTheme.alpha(PHOSPHOR, 0.25f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        float cy = (t + b) * 0.5f;
        float h = dp(8f);
        // ten cells; filled ones use a bright phosphor block
        int cells = 10;
        float cell = (r - l) / cells;
        int filled = Math.round(cells * fraction);
        for (int i = 0; i < cells; i++) {
            float x = l + i * cell;
            int color;
            if (i < filled) {
                color = i < cells * 0.5f ? TgTheme.alpha(PHOSPHOR, 0.85f) : TgTheme.alpha(mTokens.accent, 0.85f);
            } else {
                color = TgTheme.alpha(PHOSPHOR, 0.12f);
            }
            paint.setColor(color);
            canvas.drawRect(x + dp(1f), cy - h * 0.5f, x + cell - dp(1f), cy + h * 0.5f, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(active ? TgTheme.alpha(mTokens.accent, 0.85f)
                : TgTheme.alpha(PHOSPHOR, pressed ? 0.16f : 0.05f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setColor(active ? TgTheme.alpha(mTokens.accent, 1f) : TgTheme.alpha(PHOSPHOR, 0.25f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(PHOSPHOR, 0.05f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setColor(focused ? TgTheme.alpha(mTokens.accent, 0.9f) : TgTheme.alpha(PHOSPHOR, 0.30f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean pressed, boolean enabled) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(enabled ? TgTheme.alpha(mTokens.accent, pressed ? 0.30f : 0.14f)
                : TgTheme.alpha(PHOSPHOR, 0.05f));
        canvas.drawRect(l, t, r, b, paint);
        paint.setColor(enabled ? TgTheme.alpha(mTokens.accent, 0.85f) : TgTheme.alpha(PHOSPHOR, 0.20f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        if (active) {
            paint.setColor(TgTheme.alpha(PHOSPHOR, 0.14f));
            canvas.drawRect(l, t, r, b, paint);
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.9f));
            canvas.drawRect(l, t, l + Math.max(2f, dp(2f)), b, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(false);
        paint.setColor(TgTheme.alpha(PHOSPHOR, 0.35f));
        paint.setStrokeWidth(dp(1f));
        canvas.drawRect(l, t, r, b, paint);
        paint.recycle();
    }
}
