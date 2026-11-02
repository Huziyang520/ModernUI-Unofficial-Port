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

package icyllis.modernui.mc.ui;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.LinearGradient;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Shader;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;

/**
 * Saturation/value plane with a hue bar and an optional alpha bar.
 * Reports every intermediate color while dragging; the final event is flagged committed
 * so callers can defer persistence until the drag ends.
 */
public class ColorPickerView extends View {

    public interface OnColorChangeListener {
        /**
         * @param argb      the new color
         * @param committed true on the last event of a gesture, false while dragging
         */
        void onColorChanged(int argb, boolean committed);
    }

    private static final int DRAG_NONE = 0;
    private static final int DRAG_SV = 1;
    private static final int DRAG_HUE = 2;
    private static final int DRAG_ALPHA = 3;

    private final float[] mHsv = {0f, 1f, 1f};
    private int mAlpha = 0xFF;
    private boolean mAlphaEnabled = true;
    private int mDragging = DRAG_NONE;

    private OnColorChangeListener mListener;

    private final int mSvHeight;
    private final int mBarHeight;
    private final int mGap;

    private float mSvLeft, mSvTop, mSvRight, mSvBottom;
    private float mHueTop, mHueBottom;
    private float mAlphaTop, mAlphaBottom;

    private final int mOutlineColor;

    public ColorPickerView(Context context) {
        super(context);
        mSvHeight = dp(148);
        mBarHeight = dp(16);
        mGap = dp(10);
        var value = new icyllis.modernui.resources.TypedValue();
        if (context.getTheme().resolveAttribute(icyllis.modernui.R.ns,
                icyllis.modernui.R.attr.colorOutline, value, true)) {
            mOutlineColor = value.data;
        } else {
            mOutlineColor = 0x66FFFFFF;
        }
        setWillNotDraw(false);
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        mListener = listener;
    }

    public void setAlphaEnabled(boolean enabled) {
        if (mAlphaEnabled != enabled) {
            mAlphaEnabled = enabled;
            if (!enabled) {
                mAlpha = 0xFF;
            }
            requestLayout();
            invalidate();
        }
    }

    public int getColor() {
        return colorFromHsv();
    }

    public void setColor(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int rgb = argb | 0xFF000000;
        setHsvFromRgb(rgb);
        mAlpha = mAlphaEnabled ? a : 0xFF;
        invalidate();
    }

    private int colorFromHsv() {
        return (mAlpha << 24) | (hsvToRgb(mHsv[0], mHsv[1], mHsv[2]) & 0xFFFFFF);
    }

    private void setHsvFromRgb(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float h;
        if (delta == 0f) {
            h = mHsv[0];
        } else if (max == r) {
            h = 60f * (((g - b) / delta) % 6f);
        } else if (max == g) {
            h = 60f * ((b - r) / delta + 2f);
        } else {
            h = 60f * ((r - g) / delta + 4f);
        }
        if (h < 0f) {
            h += 360f;
        }
        mHsv[0] = h;
        mHsv[1] = max == 0f ? 0f : delta / max;
        mHsv[2] = max;
    }

    private static int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float hh = (h % 360f) / 60f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float r, g, b;
        if (hh < 1f) {
            r = c; g = x; b = 0f;
        } else if (hh < 2f) {
            r = x; g = c; b = 0f;
        } else if (hh < 3f) {
            r = 0f; g = c; b = x;
        } else if (hh < 4f) {
            r = 0f; g = x; b = c;
        } else if (hh < 5f) {
            r = x; g = 0f; b = c;
        } else {
            r = c; g = 0f; b = x;
        }
        float m = v - c;
        return ((int) ((r + m) * 255f + 0.5f) << 16)
                | ((int) ((g + m) * 255f + 0.5f) << 8)
                | (int) ((b + m) * 255f + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = mSvHeight + mGap + mBarHeight + mGap * 2
                + (mAlphaEnabled ? mBarHeight + mGap : 0)
                + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float l = getPaddingLeft();
        float r = getWidth() - getPaddingRight();
        float t = getPaddingTop();
        mSvLeft = l;
        mSvTop = t;
        mSvRight = r;
        mSvBottom = t + mSvHeight;
        mHueTop = mSvBottom + mGap;
        mHueBottom = mHueTop + mBarHeight;
        mAlphaTop = mHueBottom + mGap;
        mAlphaBottom = mAlphaTop + mBarHeight;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);

        // saturation/value plane: white -> pure hue horizontally, then transparent -> black vertically
        int pureHue = 0xFF000000 | hsvToRgb(mHsv[0], 1f, 1f);
        paint.setShader(new LinearGradient(mSvLeft, 0, mSvRight, 0,
                new int[]{0xFFFFFFFF, pureHue}, null, Shader.TileMode.CLAMP, null));
        canvas.drawRect(mSvLeft, mSvTop, mSvRight, mSvBottom, paint);

        paint.setShader(new LinearGradient(0, mSvTop, 0, mSvBottom,
                new int[]{0x00000000, 0xFF000000}, null, Shader.TileMode.CLAMP, null));
        canvas.drawRect(mSvLeft, mSvTop, mSvRight, mSvBottom, paint);
        paint.setShader(null);

        // hue bar
        paint.setShader(new LinearGradient(mSvLeft, 0, mSvRight, 0,
                new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF,
                        0xFFFF00FF, 0xFFFF0000}, null, Shader.TileMode.CLAMP, null));
        canvas.drawRect(mSvLeft, mHueTop, mSvRight, mHueBottom, paint);
        paint.setShader(null);

        // alpha bar
        if (mAlphaEnabled) {
            drawCheckerboard(canvas, paint, mSvLeft, mAlphaTop, mSvRight, mAlphaBottom);
            int opaque = 0xFF000000 | (colorFromHsv() & 0xFFFFFF);
            paint.setShader(new LinearGradient(mSvLeft, 0, mSvRight, 0,
                    new int[]{opaque & 0x00FFFFFF, opaque}, null, Shader.TileMode.CLAMP, null));
            canvas.drawRect(mSvLeft, mAlphaTop, mSvRight, mAlphaBottom, paint);
            paint.setShader(null);
        }

        // frame
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(mOutlineColor);
        canvas.drawRect(mSvLeft, mSvTop, mSvRight, mSvBottom, paint);
        canvas.drawRect(mSvLeft, mHueTop, mSvRight, mHueBottom, paint);
        if (mAlphaEnabled) {
            canvas.drawRect(mSvLeft, mAlphaTop, mSvRight, mAlphaBottom, paint);
        }
        paint.setStyle(Paint.FILL);

        // markers
        float cx = mSvLeft + mHsv[1] * (mSvRight - mSvLeft);
        float cy = mSvTop + (1f - mHsv[2]) * (mSvBottom - mSvTop);
        float radius = dp(7);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(0xFF000000);
        canvas.drawCircle(cx, cy, radius + dp(1), paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.FILL);

        drawThumb(canvas, paint, mSvLeft + mHsv[0] / 360f * (mSvRight - mSvLeft),
                mHueTop, mHueBottom, 0xFF000000 | hsvToRgb(mHsv[0], 1f, 1f));
        if (mAlphaEnabled) {
            drawThumb(canvas, paint, mSvLeft + mAlpha / 255f * (mSvRight - mSvLeft),
                    mAlphaTop, mAlphaBottom, colorFromHsv());
        }

        paint.recycle();
    }

    private void drawThumb(Canvas canvas, Paint paint, float cx, float top, float bottom, int color) {
        float halfWidth = dp(4);
        float stroke = dp(2);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(0xFF000000);
        canvas.drawRoundRect(cx - halfWidth - stroke, top - stroke, cx + halfWidth + stroke, bottom + stroke,
                dp(3), paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(cx - halfWidth, top, cx + halfWidth, bottom, dp(3), paint);
        paint.setStyle(Paint.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(cx - halfWidth + stroke, top + stroke, cx + halfWidth - stroke, bottom - stroke,
                dp(2), paint);
    }

    private void drawCheckerboard(Canvas canvas, Paint paint, float left, float top, float right, float bottom) {
        float cell = dp(6);
        boolean dark = true;
        for (float y = top; y < bottom; y += cell) {
            dark = !dark;
            for (float x = left; x < right; x += cell) {
                paint.setColor(dark ? 0xFFBDBDBD : 0xFFF5F5F5);
                canvas.drawRect(x, y, Math.min(x + cell, right), Math.min(y + cell, bottom), paint);
                dark = !dark;
            }
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mDragging = hitTest(event.getX(), event.getY());
                if (mDragging == DRAG_NONE) {
                    return false;
                }
                updateFromTouch(event.getX(), event.getY(), false);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (mDragging != DRAG_NONE) {
                    updateFromTouch(event.getX(), event.getY(), false);
                    return true;
                }
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mDragging != DRAG_NONE) {
                    updateFromTouch(event.getX(), event.getY(), true);
                    mDragging = DRAG_NONE;
                    return true;
                }
            }
            default -> {
            }
        }
        return super.onTouchEvent(event);
    }

    private int hitTest(float x, float y) {
        if (y >= mSvTop && y <= mSvBottom) {
            return DRAG_SV;
        }
        if (y >= mHueTop && y <= mHueBottom) {
            return DRAG_HUE;
        }
        if (mAlphaEnabled && y >= mAlphaTop && y <= mAlphaBottom) {
            return DRAG_ALPHA;
        }
        return DRAG_NONE;
    }

    private void updateFromTouch(float x, float y, boolean committed) {
        float fraction = (x - mSvLeft) / Math.max(1f, mSvRight - mSvLeft);
        fraction = Math.max(0f, Math.min(1f, fraction));
        switch (mDragging) {
            case DRAG_SV -> {
                mHsv[1] = fraction;
                float vertical = (y - mSvTop) / Math.max(1f, mSvBottom - mSvTop);
                mHsv[2] = 1f - Math.max(0f, Math.min(1f, vertical));
            }
            case DRAG_HUE -> mHsv[0] = fraction * 360f;
            case DRAG_ALPHA -> mAlpha = Math.round(fraction * 255f);
            default -> {
                return;
            }
        }
        invalidate();
        if (mListener != null) {
            mListener.onColorChanged(colorFromHsv(), committed);
        }
    }

    @NonNull
    public static String toHexString(int argb) {
        return String.format("#%08X", argb);
    }

    public static int parseOrDefault(@NonNull String text, int fallback) {
        try {
            return Color.parseColor(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
