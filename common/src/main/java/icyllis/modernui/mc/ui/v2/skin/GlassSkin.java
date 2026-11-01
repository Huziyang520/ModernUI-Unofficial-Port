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
 * Frosted glass: translucent panels over an accent tinted gradient, 20dp radii, a 1dp
 * highlight along the top edge and a dark line at the bottom to fake depth.
 */
public final class GlassSkin extends BaseSkin {

    public GlassSkin(@NonNull TgTheme theme) {
        super(tokens(theme));
    }

    @NonNull
    private static SkinTokens tokens(@NonNull TgTheme theme) {
        var t = SkinTokens.defaults(theme);
        t.bg = theme.surface;
        t.panel = TgTheme.alpha(theme.surfaceHigh, 0.55f);
        t.panelAlt = TgTheme.alpha(theme.surfaceHigh, 0.38f);
        t.line = TgTheme.alpha(0xFFFFFFFF, 0.14f);
        t.text = theme.onSurface;
        t.muted = TgTheme.alpha(theme.onSurface, 0.62f);
        t.accent = theme.accent;
        t.onAccent = theme.onAccent;
        t.accentSoft = TgTheme.alpha(theme.accent, 0.20f);

        t.radiusPanel = 20f;
        t.radiusControl = 12f;
        t.rowHeight = 58f;
        t.padH = 18f;
        t.padV = 9f;
        t.lineWidth = 1f;
        t.sectionGap = 22f;

        t.titleSize = 15f;
        t.descSize = 11f;
        t.sectionSize = 12f;
        t.valueSize = 12f;

        t.motionMs = 240L;
        t.ease = SkinTokens.TgEase.OUT_CUBIC;
        t.glow = true;
        return t;
    }

    @NonNull
    @Override
    public String id() {
        return SkinRegistry.GLASS;
    }

    @Override
    public void drawBackdrop(@NonNull Canvas canvas, int width, int height) {
        canvas.drawColor(mTokens.bg);
        // a soft accent wash from the top left corner
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setShader(new icyllis.modernui.graphics.LinearGradient(
                0, 0, width, height,
                TgTheme.alpha(mTokens.accent, 0.16f), 0x00000000,
                icyllis.modernui.graphics.Shader.TileMode.CLAMP, null));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
        paint.recycle();
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float rad = dp(radius);
        // outer accent halo, then the frosted body, then the inner white hairline
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(2f));
        paint.setColor(TgTheme.alpha(mTokens.accent, 0.10f));
        canvas.drawRoundRect(l, t, r, b, rad + dp(1f), paint);

        paint.setStyle(Paint.FILL);
        paint.setColor(mTokens.panel);
        canvas.drawRoundRect(l, t, r, b, rad, paint);

        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.10f));
        canvas.drawRoundRect(l + dp(0.5f), t + dp(0.5f), r - dp(0.5f), b - dp(0.5f), rad, paint);

        // top highlight fading towards the right, bottom shadow line
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.18f));
        canvas.drawLine(l + rad * 0.6f, t + dp(0.5f), l + (r - l) * 0.55f, t + dp(0.5f), paint);
        paint.setColor(TgTheme.alpha(0xFF000000, 0.14f));
        canvas.drawLine(l + rad * 0.6f, b - dp(0.5f), r - rad * 0.6f, b - dp(0.5f), paint);
        paint.recycle();
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.07f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(x1, y, x2, y, paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, empty ? 0.05f : 0.12f));
        paint.setStrokeWidth(Math.max(1f, dp(1f)));
        canvas.drawLine(l, y, r, y, paint);
        paint.recycle();
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.12f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        if (progress > 0.02f) {
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.85f * progress));
            canvas.drawRoundRect(l, t, r, b, radius, paint);
        }
        float inset = dp(3f);
        float knob = (b - t) - inset * 2f;
        float x = l + inset + ((r - l) - inset * 2f - knob) * progress;
        float cx = x + knob * 0.5f;
        float cy = (t + b) * 0.5f;
        // white sphere with a specular arc
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.24f));
        canvas.drawCircle(cx, cy, knob * 0.62f, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, knob * 0.42f, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.9f));
        canvas.drawCircle(cx, cy - knob * 0.10f, knob * 0.28f, paint);
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float cy = (t + b) * 0.5f;
        float thickness = dp(4f);
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.12f));
        canvas.drawRoundRect(l, cy - thickness * 0.5f, r, cy + thickness * 0.5f,
                thickness * 0.5f, paint);

        float x = l + (r - l) * fraction;
        if (x > l) {
            paint.setShader(new icyllis.modernui.graphics.LinearGradient(l, 0, Math.max(x, l + 1f), 0,
                    TgTheme.alpha(mTokens.accent, 0.75f), mTokens.accent,
                    icyllis.modernui.graphics.Shader.TileMode.CLAMP, null));
            canvas.drawRoundRect(l, cy - thickness * 0.5f, x, cy + thickness * 0.5f,
                    thickness * 0.5f, paint);
            paint.setShader(null);
        }
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.20f));
        canvas.drawCircle(x, cy, dp(dragging ? 9f : 7f), paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(x, cy, dp(dragging ? 7f : 5.5f), paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        paint.setColor(active ? mTokens.accent
                : TgTheme.alpha(0xFFFFFFFF, pressed ? 0.16f : 0.08f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = dp(mTokens.radiusControl);
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, focused ? 0.12f : 0.07f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(focused ? 1.6f : 1f));
        paint.setColor(focused ? mTokens.accent : TgTheme.alpha(0xFFFFFFFF, 0.14f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.recycle();
    }

    @Override
    public void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean pressed, boolean enabled) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        paint.setColor(enabled
                ? TgTheme.alpha(mTokens.accent, pressed ? 1f : 0.85f)
                : TgTheme.alpha(0xFFFFFFFF, 0.08f));
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.22f));
        canvas.drawRoundRect(l + dp(0.5f), t + dp(0.5f), r - dp(0.5f), b - dp(0.5f), radius, paint);
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        if (active) {
            paint.setColor(TgTheme.alpha(mTokens.accent, 0.20f));
            canvas.drawRoundRect(l, t, r, b, dp(mTokens.radiusControl), paint);
            paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.35f));
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(dp(1f));
            canvas.drawRoundRect(l, t, r, b, dp(mTokens.radiusControl), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(0xFFFFFFFF, 0.35f));
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
        paint.recycle();
    }
}
