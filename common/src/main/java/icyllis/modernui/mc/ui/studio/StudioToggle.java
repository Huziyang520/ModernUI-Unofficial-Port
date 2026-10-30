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
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;

import java.util.function.Consumer;

/**
 * Pill switch with an animated knob. Drawn from scratch so it follows the studio palette
 * instead of the host theme.
 */
public class StudioToggle extends View {

    private boolean mChecked;
    private float mProgress;
    private Consumer<Boolean> mListener;
    private boolean mPressed;

    private final Paint mPaint = new Paint();
    private final int mAccent;
    private final int mOffColor;
    private final int mKnobOn;
    private final int mKnobOff;

    public StudioToggle(@NonNull Context context) {
        super(context);
        mAccent = Studio.accent(context);
        mOffColor = Studio.alpha(Studio.onSurface(context), 0.22f);
        mKnobOn = Studio.contrasting(mAccent);
        mKnobOff = Studio.mix(Studio.surfaceHigh(context), Studio.onSurface(context), 0.75f);
        mPaint.setAntiAlias(true);
        setClickable(true);
        setFocusable(true);
    }

    public void setOnCheckedChanged(Consumer<Boolean> listener) {
        mListener = listener;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (mChecked == checked && mProgress == (checked ? 1f : 0f)) {
            return;
        }
        mChecked = checked;
        float target = checked ? 1f : 0f;
        if (animate) {
            float from = mProgress;
            StudioAnim.run(this, 160, t -> mProgress = from + (target - from) * t);
        } else {
            mProgress = target;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = Studio.dp(getContext(), 44);
        int h = Studio.dp(getContext(), 24);
        setMeasuredDimension(resolveSize(w, widthMeasureSpec), resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float radius = h * 0.5f;
        mPaint.setStyle(Paint.FILL);
        mPaint.setColor(Studio.mix(mOffColor, mAccent, mProgress));
        canvas.drawRoundRect(mPressed ? 1 : 0.5f, 0.5f, w - 0.5f, h - 0.5f, radius, mPaint);

        float inset = Studio.dp(getContext(), 3);
        float knob = h - inset * 2;
        float left = inset + (w - inset * 2 - knob) * mProgress;
        mPaint.setColor(Studio.mix(mKnobOff, mKnobOn, mProgress));
        canvas.drawCircle(left + knob * 0.5f, h * 0.5f, knob * 0.5f, mPaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mPressed = true;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                mPressed = false;
                setChecked(!mChecked, true);
                if (mListener != null) {
                    mListener.accept(mChecked);
                }
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                mPressed = false;
                invalidate();
                return true;
            }
            default -> {
                return true;
            }
        }
    }
}
