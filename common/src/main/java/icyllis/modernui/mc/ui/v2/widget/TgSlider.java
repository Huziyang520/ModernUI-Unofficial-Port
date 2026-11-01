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

package icyllis.modernui.mc.ui.v2.widget;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.view.MotionEvent;

/**
 * Continuous slider over a fixed range. Values are reported while dragging and once more
 * when the gesture ends, so callers persist only on release.
 */
public class TgSlider extends TgGroup {

    public interface OnValueChanged {
        /**
         * @param value    the new value, already snapped and clamped
         * @param finished true on the last event of a gesture
         */
        void onValueChanged(float value, boolean finished);
    }

    private float mMin;
    private float mMax = 1f;
    private int mSteps = 100;
    private float mValue;
    private boolean mDragging;
    private OnValueChanged mListener;

    public TgSlider(@NonNull Context context) {
        super(context);
    }

    public void setRange(float min, float max, int steps) {
        mMin = min;
        mMax = max;
        mSteps = Math.max(1, steps);
        invalidate();
    }

    public void setOnValueChanged(OnValueChanged listener) {
        mListener = listener;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        float clamped = snap(value);
        if (clamped != mValue) {
            mValue = clamped;
            invalidate();
        }
    }

    private float snap(float value) {
        float clamped = Math.max(mMin, Math.min(mMax, value));
        if (mSteps > 0 && mMax > mMin) {
            float stepSize = (mMax - mMin) / mSteps;
            clamped = mMin + Math.round((clamped - mMin) / stepSize) * stepSize;
        }
        return clamped;
    }

    private float fraction() {
        return mMax > mMin ? (mValue - mMin) / (mMax - mMin) : 0f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = Math.round(dp(150f));
        int h = Math.round(dp(30f));
        setMeasuredDimension(resolveSize(w, widthMeasureSpec), resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float pad = dp(8f);
        skin().drawSliderTrack(canvas, pad, 0, getWidth() - pad, getHeight(), fraction(), mDragging);
    }

    private void updateFromTouch(float x) {
        float pad = dp(8f);
        float right = getWidth() - pad;
        float f = Math.max(0f, Math.min(1f, (x - pad) / Math.max(1f, right - pad)));
        setValue(mMin + (mMax - mMin) * f);
        if (mListener != null) {
            mListener.onValueChanged(mValue, false);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mDragging = true;
                var parent = getParent();
                if (parent != null) {
                    // the enclosing scroll view would otherwise steal the drag
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                updateFromTouch(event.getX());
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (mDragging) {
                    updateFromTouch(event.getX());
                }
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if (mDragging) {
                    mDragging = false;
                    updateFromTouch(event.getX());
                    if (mListener != null) {
                        mListener.onValueChanged(mValue, true);
                    }
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                mDragging = false;
                invalidate();
                return true;
            }
            default -> {
                return true;
            }
        }
    }
}
