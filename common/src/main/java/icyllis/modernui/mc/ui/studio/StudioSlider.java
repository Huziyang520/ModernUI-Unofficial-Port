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

/**
 * Thin track with a glowing knob. Values are reported while dragging and once more when the
 * gesture ends, so callers can persist only on release.
 */
public class StudioSlider extends View {

    public interface OnValueChanged {
        void onValueChanged(float value, boolean finished);
    }

    private float mMin;
    private float mMax = 1f;
    private int mSteps = 100;
    private float mValue;
    private boolean mDragging;
    private OnValueChanged mListener;

    private final Paint mPaint = new Paint();
    private final int mAccent;
    private final int mTrack;
    private final int mKnob;

    public StudioSlider(@NonNull Context context) {
        super(context);
        mAccent = Studio.accent(context);
        mTrack = Studio.alpha(Studio.onSurface(context), 0.16f);
        mKnob = Studio.onSurface(context);
        mPaint.setAntiAlias(true);
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
        float clamped = Math.max(mMin, Math.min(mMax, value));
        if (mSteps > 0) {
            float stepSize = (mMax - mMin) / mSteps;
            clamped = mMin + Math.round((clamped - mMin) / stepSize) * stepSize;
        }
        if (clamped != mValue) {
            mValue = clamped;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = Studio.dp(getContext(), 148);
        int h = Studio.dp(getContext(), 30);
        setMeasuredDimension(resolveSize(w, widthMeasureSpec), resolveSize(h, heightMeasureSpec));
    }

    private float fraction() {
        return mMax > mMin ? (mValue - mMin) / (mMax - mMin) : 0f;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float w = getWidth();
        float cy = getHeight() * 0.5f;
        float pad = Studio.dp(getContext(), 8);
        float left = pad;
        float right = w - pad;
        float thumb = Studio.dp(getContext(), 7);
        float trackHeight = Studio.dp(getContext(), 4);

        mPaint.setStyle(Paint.FILL);
        mPaint.setColor(mTrack);
        canvas.drawRoundRect(left, cy - trackHeight * 0.5f, right, cy + trackHeight * 0.5f,
                trackHeight * 0.5f, mPaint);

        float x = left + (right - left) * fraction();
        mPaint.setColor(mAccent);
        if (x > left) {
            canvas.drawRoundRect(left, cy - trackHeight * 0.5f, x, cy + trackHeight * 0.5f,
                    trackHeight * 0.5f, mPaint);
        }
        // halo keeps the knob readable on bright tiles
        mPaint.setColor(Studio.alpha(mAccent, mDragging ? 0.35f : 0.2f));
        canvas.drawCircle(x, cy, thumb * 2.1f, mPaint);
        mPaint.setColor(mKnob);
        canvas.drawCircle(x, cy, thumb, mPaint);
        mPaint.setColor(mAccent);
        canvas.drawCircle(x, cy, thumb * 0.62f, mPaint);
    }

    private void updateFromTouch(float x) {
        float pad = Studio.dp(getContext(), 8);
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
