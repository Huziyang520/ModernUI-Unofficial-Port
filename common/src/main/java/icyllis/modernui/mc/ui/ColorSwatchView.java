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
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.view.View;

/**
 * Rounded color chip that shows transparency over a checkerboard. It is a real click
 * target, and it lightens on hover so that it reads as something you can press.
 */
public class ColorSwatchView extends View {

    private int mColor = 0xFFFFFFFF;
    private float mCornerRadius = -1;

    public ColorSwatchView(Context context) {
        super(context);
        setWillNotDraw(false);
        setClickable(true);
        setFocusable(true);
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            invalidate();
        }
    }

    /** A negative radius keeps the default rounding; used to follow the border radius. */
    public void setCornerRadius(float dp) {
        if (mCornerRadius != dp) {
            mCornerRadius = dp;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = dp(28);
        setMeasuredDimension(resolveSize(size, widthMeasureSpec), resolveSize(size, heightMeasureSpec));
    }

    @Override
    public void setHovered(boolean hovered) {
        if (isHovered() != hovered) {
            super.setHovered(hovered);
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        var paint = Paint.obtain();
        paint.setAntiAlias(true);
        float radius = mCornerRadius >= 0 ? dp(mCornerRadius) : dp(8);
        float w = getWidth();
        float h = getHeight();
        radius = Math.min(radius, Math.min(w, h) / 2f);

        float cell = dp(5);
        boolean dark = true;
        for (float y = 0; y < h; y += cell) {
            dark = !dark;
            for (float x = 0; x < w; x += cell) {
                paint.setColor(dark ? 0xFFBDBDBD : 0xFFF5F5F5);
                canvas.drawRect(x, y, Math.min(x + cell, w), Math.min(y + cell, h), paint);
                dark = !dark;
            }
        }

        paint.setColor(mColor);
        canvas.drawRoundRect(0, 0, w, h, radius, paint);

        if (isHovered() || isPressed()) {
            paint.setColor(0x33FFFFFF);
            canvas.drawRoundRect(0, 0, w, h, radius, paint);
        }

        paint.setColor(0x40000000);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(dp(1));
        canvas.drawRoundRect(0, 0, w, h, radius, paint);
        paint.recycle();
    }
}
