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
 * Every primitive a skin may ignore. A style overrides the ones its spec calls out and
 * inherits the rest, which keeps a new style down to its real differences.
 */
public abstract class BaseSkin implements Skin {

    protected final SkinTokens mTokens;

    protected BaseSkin(@NonNull SkinTokens tokens) {
        mTokens = tokens;
    }

    @NonNull
    @Override
    public SkinTokens tokens() {
        return mTokens;
    }

    protected float dp(float value) {
        // the tokens are dp; canvases are pixels, so fold in the density here
        return value * mTokens.density;
    }

    @Override
    public void drawBackdrop(@NonNull Canvas canvas, int width, int height) {
        // deliberately empty: the blur layer already painted the player's backdrop colour over
        // the blurred world. Filling here would black out both and override that setting.
    }

    @Override
    public void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.panelSurface());
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
        if (mTokens.lineWidth > 0f) {
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(dp(mTokens.lineWidth));
            paint.setColor(mTokens.line);
            canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawRowBackground(@NonNull Canvas canvas, float l, float t, float r, float b,
                                  boolean first, boolean last, boolean pressed) {
        // rows sit on top of the blurred world, so they need their own surface to stay
        // readable; the panel colour is translucent enough to keep the backdrop visible
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = dp(mTokens.radiusControl);
        paint.setColor(pressed ? TgTheme.alpha(mTokens.accent, 0.16f) : mTokens.panelSurface());
        if (first || last) {
            // only the ends of a group are rounded; the middle joins them into one card
            float topLeft = first ? radius : 0f;
            float topRight = first ? radius : 0f;
            float bottomRight = last ? radius : 0f;
            float bottomLeft = last ? radius : 0f;
            canvas.drawRoundRect(l, t, r, b, topLeft, topRight, bottomRight, bottomLeft, paint);
        } else {
            canvas.drawRect(l, t, r, b, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(mTokens.line);
        paint.setStrokeWidth(Math.max(1f, dp(mTokens.lineWidth)));
        canvas.drawLine(x1, y, x2, y, paint);
        paint.recycle();
    }

    @Override
    public void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty) {
        drawDivider(canvas, l, y, r);
    }

    @Override
    public void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                           float progress, boolean checked, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = (b - t) * 0.5f;
        paint.setColor(TgTheme.mix(TgTheme.alpha(mTokens.text, 0.24f), mTokens.accent, progress));
        canvas.drawRoundRect(l, t, r, b, radius, paint);

        float inset = dp(3f);
        float knob = (b - t) - inset * 2f;
        float x = l + inset + ((r - l) - inset * 2f - knob) * progress;
        paint.setColor(TgTheme.mix(TgTheme.mix(mTokens.panel, mTokens.text, 0.75f), mTokens.onAccent, progress));
        canvas.drawCircle(x + knob * 0.5f, (t + b) * 0.5f, knob * 0.5f, paint);
        paint.recycle();
    }

    @Override
    public void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                                float fraction, boolean dragging) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float cy = (t + b) * 0.5f;
        float thickness = Math.max(dp(2f), (b - t) * 0.18f);
        paint.setColor(TgTheme.alpha(mTokens.text, 0.18f));
        canvas.drawRoundRect(l, cy - thickness * 0.5f, r, cy + thickness * 0.5f, thickness * 0.5f, paint);

        float x = l + (r - l) * fraction;
        paint.setColor(mTokens.accent);
        if (x > l) {
            canvas.drawRoundRect(l, cy - thickness * 0.5f, x, cy + thickness * 0.5f,
                    thickness * 0.5f, paint);
        }
        float knob = dp(dragging ? 8f : 7f);
        paint.setColor(TgTheme.alpha(mTokens.accent, dragging ? 0.34f : 0.20f));
        canvas.drawCircle(x, cy, knob * 2.1f, paint);
        paint.setColor(mTokens.onAccent);
        canvas.drawCircle(x, cy, knob, paint);
        paint.setColor(mTokens.accent);
        canvas.drawCircle(x, cy, knob * 0.62f, paint);
        paint.recycle();
    }

    @Override
    public void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                            int index, int count, boolean active, boolean pressed) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = Math.min((b - t) * 0.5f, dp(mTokens.radiusControl));
        if (active) {
            paint.setColor(mTokens.accent);
            canvas.drawRoundRect(l, t, r, b, radius, paint);
        } else if (pressed) {
            paint.setColor(TgTheme.alpha(mTokens.text, 0.10f));
            canvas.drawRoundRect(l, t, r, b, radius, paint);
        }
        paint.recycle();
    }

    @Override
    public void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(TgTheme.alpha(mTokens.text, 0.06f));
        float radius = dp(mTokens.radiusControl);
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(focused ? 1.6f : 1f));
        paint.setColor(focused ? mTokens.accent : mTokens.line);
        canvas.drawRoundRect(l, t, r, b, radius, paint);
        paint.recycle();
    }

    @Override
    public void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean pressed, boolean enabled) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        int base = enabled ? mTokens.accent : TgTheme.alpha(mTokens.text, 0.14f);
        paint.setColor(pressed ? TgTheme.shade(base, 0.85f) : base);
        canvas.drawRoundRect(l, t, r, b, dp(mTokens.radiusControl), paint);
        paint.recycle();
    }

    @Override
    public void drawGlyph(@NonNull Canvas canvas, int glyph, float cx, float cy, float size, int color) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setColor(color);
        float h = size * 0.5f;
        switch (glyph) {
            case GLYPH_CHECK -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.6f, size * 0.14f));
                canvas.drawLine(cx - h * 0.7f, cy, cx - h * 0.1f, cy + h * 0.6f, paint);
                canvas.drawLine(cx - h * 0.1f, cy + h * 0.6f, cx + h * 0.75f, cy - h * 0.6f, paint);
            }
            case GLYPH_CLOSE -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.6f, size * 0.14f));
                canvas.drawLine(cx - h * 0.6f, cy - h * 0.6f, cx + h * 0.6f, cy + h * 0.6f, paint);
                canvas.drawLine(cx + h * 0.6f, cy - h * 0.6f, cx - h * 0.6f, cy + h * 0.6f, paint);
            }
            case GLYPH_CHEVRON_DOWN -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.13f));
                canvas.drawLine(cx - h * 0.55f, cy - h * 0.2f, cx, cy + h * 0.35f, paint);
                canvas.drawLine(cx, cy + h * 0.35f, cx + h * 0.55f, cy - h * 0.2f, paint);
            }
            case GLYPH_CHEVRON_RIGHT -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.13f));
                canvas.drawLine(cx - h * 0.2f, cy - h * 0.55f, cx + h * 0.35f, cy, paint);
                canvas.drawLine(cx + h * 0.35f, cy, cx - h * 0.2f, cy + h * 0.55f, paint);
            }
            case GLYPH_SEARCH -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawCircle(cx - h * 0.15f, cy - h * 0.15f, h * 0.55f, paint);
                canvas.drawLine(cx + h * 0.3f, cy + h * 0.3f, cx + h * 0.8f, cy + h * 0.8f, paint);
            }
            case GLYPH_SUN -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawCircle(cx, cy, h * 0.42f, paint);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * i / 4.0;
                    float x1 = cx + (float) Math.cos(a) * h * 0.62f;
                    float y1 = cy + (float) Math.sin(a) * h * 0.62f;
                    float x2 = cx + (float) Math.cos(a) * h * 0.88f;
                    float y2 = cy + (float) Math.sin(a) * h * 0.88f;
                    canvas.drawLine(x1, y1, x2, y2, paint);
                }
            }
            case GLYPH_MONITOR -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawRoundRect(cx - h * 0.8f, cy - h * 0.62f, cx + h * 0.8f, cy + h * 0.34f,
                        h * 0.14f, paint);
                canvas.drawLine(cx, cy + h * 0.34f, cx, cy + h * 0.72f, paint);
                canvas.drawLine(cx - h * 0.4f, cy + h * 0.72f, cx + h * 0.4f, cy + h * 0.72f, paint);
            }
            case GLYPH_SCROLL -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawRoundRect(cx - h * 0.55f, cy - h * 0.78f, cx + h * 0.55f, cy + h * 0.78f,
                        h * 0.3f, paint);
                canvas.drawLine(cx - h * 0.25f, cy - h * 0.3f, cx + h * 0.25f, cy - h * 0.3f, paint);
                canvas.drawLine(cx - h * 0.25f, cy + h * 0.1f, cx + h * 0.25f, cy + h * 0.1f, paint);
            }
            case GLYPH_TYPE -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawLine(cx - h * 0.7f, cy - h * 0.6f, cx + h * 0.7f, cy - h * 0.6f, paint);
                canvas.drawLine(cx, cy - h * 0.6f, cx, cy + h * 0.7f, paint);
                canvas.drawLine(cx - h * 0.35f, cy + h * 0.7f, cx + h * 0.35f, cy + h * 0.7f, paint);
            }
            case GLYPH_PLUG -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawRoundRect(cx - h * 0.55f, cy - h * 0.2f, cx + h * 0.55f, cy + h * 0.8f,
                        h * 0.16f, paint);
                canvas.drawLine(cx - h * 0.28f, cy - h * 0.2f, cx - h * 0.28f, cy - h * 0.75f, paint);
                canvas.drawLine(cx + h * 0.28f, cy - h * 0.2f, cx + h * 0.28f, cy - h * 0.75f, paint);
            }
            case GLYPH_TEXT -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                for (int i = 0; i < 3; i++) {
                    float y = cy - h * 0.5f + i * h * 0.5f;
                    canvas.drawLine(cx - h * 0.7f, y, cx + (i == 2 ? h * 0.1f : h * 0.7f), y, paint);
                }
            }
            case GLYPH_PALETTE -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawCircle(cx, cy, h * 0.72f, paint);
                paint.setStyle(Paint.FILL);
                canvas.drawCircle(cx - h * 0.3f, cy - h * 0.25f, h * 0.12f, paint);
                canvas.drawCircle(cx + h * 0.28f, cy - h * 0.3f, h * 0.12f, paint);
                canvas.drawCircle(cx, cy + h * 0.3f, h * 0.12f, paint);
            }
            case GLYPH_DASHBOARD -> {
                paint.setStyle(Paint.FILL);
                float q = h * 0.6f;
                canvas.drawRoundRect(cx - h * 0.72f, cy - h * 0.72f, cx - h * 0.72f + q,
                        cy - h * 0.72f + q, h * 0.12f, paint);
                canvas.drawRoundRect(cx + h * 0.12f, cy - h * 0.72f, cx + h * 0.72f, cy - h * 0.12f,
                        h * 0.12f, paint);
                canvas.drawRoundRect(cx - h * 0.72f, cy + h * 0.12f, cx - h * 0.12f, cy + h * 0.72f,
                        h * 0.12f, paint);
                canvas.drawRoundRect(cx + h * 0.12f, cy + h * 0.12f, cx + h * 0.72f, cy + h * 0.72f,
                        h * 0.12f, paint);
            }
            case GLYPH_SETTINGS -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawCircle(cx, cy, h * 0.4f, paint);
                for (int i = 0; i < 6; i++) {
                    double a = Math.PI * i / 3.0;
                    float x1 = cx + (float) Math.cos(a) * h * 0.6f;
                    float y1 = cy + (float) Math.sin(a) * h * 0.6f;
                    float x2 = cx + (float) Math.cos(a) * h * 0.85f;
                    float y2 = cy + (float) Math.sin(a) * h * 0.85f;
                    canvas.drawLine(x1, y1, x2, y2, paint);
                }
            }
            case GLYPH_CODE -> {
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawLine(cx - h * 0.15f, cy - h * 0.55f, cx - h * 0.72f, cy, paint);
                canvas.drawLine(cx - h * 0.72f, cy, cx - h * 0.15f, cy + h * 0.55f, paint);
                canvas.drawLine(cx + h * 0.15f, cy - h * 0.55f, cx + h * 0.72f, cy, paint);
                canvas.drawLine(cx + h * 0.72f, cy, cx + h * 0.15f, cy + h * 0.55f, paint);
            }
            case GLYPH_MUSIC -> {
                paint.setStyle(Paint.FILL);
                canvas.drawCircle(cx - h * 0.32f, cy + h * 0.45f, h * 0.24f, paint);
                canvas.drawCircle(cx + h * 0.5f, cy + h * 0.25f, h * 0.24f, paint);
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, size * 0.12f));
                canvas.drawLine(cx - h * 0.12f, cy + h * 0.45f, cx - h * 0.12f, cy - h * 0.6f, paint);
                canvas.drawLine(cx + h * 0.7f, cy + h * 0.25f, cx + h * 0.7f, cy - h * 0.75f, paint);
                canvas.drawLine(cx - h * 0.12f, cy - h * 0.6f, cx + h * 0.7f, cy - h * 0.75f, paint);
            }
            default -> {
                paint.setStyle(Paint.FILL);
                canvas.drawCircle(cx, cy, h * 0.35f, paint);
            }
        }
        paint.recycle();
    }

    @Override
    public void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        if (active) {
            paint.setColor(mTokens.accentSoft);
            canvas.drawRoundRect(l, t, r, b, dp(mTokens.radiusControl), paint);
            paint.setColor(mTokens.accent);
            canvas.drawRoundRect(l, t + (b - t) * 0.25f, l + Math.max(2f, dp(2f)),
                    b - (b - t) * 0.25f, dp(1f), paint);
        }
        paint.recycle();
    }

    @Override
    public void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1f));
        paint.setColor(TgTheme.alpha(mTokens.text, 0.30f));
        canvas.drawRoundRect(l, t, r, b, dp(radius), paint);
        paint.recycle();
    }
}
