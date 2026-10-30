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

package icyllis.modernui.mc.ui.studio;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.LinearGradient;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Shader;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Bento tile: a tinted gradient plate, a procedural glyph and two lines of text. Each tile
 * gets its own hue so the home grid reads as a set rather than a list.
 */
public class StudioTile extends FrameLayout {

    private final int mTint;
    private final int mGlyph;
    private final int mIndex;
    private final Paint mPaint = new Paint();

    public StudioTile(@NonNull Context context, int index, String title, String subtitle) {
        super(context);
        mIndex = index;
        mTint = Studio.variant(context, index);
        mGlyph = Studio.alpha(0xFFFFFFFF, 0.30f);
        mPaint.setAntiAlias(true);
        setWillNotDraw(false);
        setClickable(true);
        setFocusable(true);

        var column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.BOTTOM);
        int pad = Studio.dp(context, Studio.PAD);
        column.setPadding(pad, pad, pad, pad);

        var titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(15);
        titleView.setTextColor(0xFFFFFFFF);
        column.addView(titleView);

        var subView = new TextView(context);
        subView.setText(subtitle);
        subView.setTextSize(11);
        subView.setTextColor(0xB3FFFFFF);
        var subParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        subParams.setMargins(0, Studio.dp(context, 2), 0, 0);
        column.addView(subView, subParams);

        addView(column, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float radius = Studio.dp(getContext(), Studio.RADIUS_TILE);
        mPaint.setStyle(Paint.FILL);
        mPaint.setShader(new LinearGradient(0, 0, w, h,
                new int[]{Studio.alpha(mTint, 0.95f), Studio.alpha(Studio.mix(mTint, 0xFF000000, 0.45f), 0.95f)},
                null, Shader.TileMode.CLAMP, null));
        canvas.drawRoundRect(0, 0, w, h, radius, mPaint);
        mPaint.setShader(null);

        drawGlyph(canvas, w, h);
    }

    /** Six different marks, drawn from primitives; no bitmaps involved. */
    private void drawGlyph(Canvas canvas, float w, float h) {
        float cx = w * 0.74f;
        float cy = h * 0.30f;
        float size = Studio.dp(getContext(), 20);
        mPaint.setColor(mGlyph);
        mPaint.setStyle(Paint.STROKE);
        mPaint.setStrokeWidth(Studio.dp(getContext(), 2));
        switch (mIndex % 6) {
            case 0 -> {
                canvas.drawCircle(cx, cy, size * 0.6f, mPaint);
                canvas.drawCircle(cx, cy, size * 0.28f, mPaint);
            }
            case 1 -> {
                for (int i = -1; i <= 1; i++) {
                    float y = cy + i * size * 0.42f;
                    canvas.drawLine(cx - size * 0.6f, y, cx + size * 0.6f, y, mPaint);
                }
            }
            case 2 -> {
                // three stacked bars of different lengths
                canvas.drawLine(cx - size * 0.6f, cy - size * 0.34f, cx + size * 0.2f, cy - size * 0.34f, mPaint);
                canvas.drawLine(cx - size * 0.6f, cy, cx + size * 0.55f, cy, mPaint);
                canvas.drawLine(cx - size * 0.6f, cy + size * 0.34f, cx - size * 0.05f, cy + size * 0.34f, mPaint);
                canvas.drawCircle(cx + size * 0.5f, cy - size * 0.34f, Studio.dp(getContext(), 2.2f), mPaint);
            }
            case 3 -> {
                canvas.drawRect(cx - size * 0.55f, cy - size * 0.55f,
                        cx + size * 0.55f, cy + size * 0.55f, mPaint);
                canvas.drawLine(cx - size * 0.55f, cy, cx + size * 0.55f, cy, mPaint);
            }
            case 4 -> {
                canvas.drawLine(cx - size * 0.55f, cy + size * 0.5f, cx, cy - size * 0.5f, mPaint);
                canvas.drawLine(cx, cy - size * 0.5f, cx + size * 0.55f, cy + size * 0.5f, mPaint);
            }
            default -> {
                for (int i = 0; i < 3; i++) {
                    float r = size * (0.25f + i * 0.22f);
                    canvas.drawCircle(cx, cy, r, mPaint);
                }
            }
        }
        mPaint.setStyle(Paint.FILL);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                animateScale(0.97f, 90);
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                animateScale(1f, 140);
                performClick();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                animateScale(1f, 140);
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private void animateScale(float target, long duration) {
        float from = getScaleX();
        StudioAnim.run(this, duration, t -> {
            float s = from + (target - from) * t;
            setScaleX(s);
            setScaleY(s);
        });
    }
}
