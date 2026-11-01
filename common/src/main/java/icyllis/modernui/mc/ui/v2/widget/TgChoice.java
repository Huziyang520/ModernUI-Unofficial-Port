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
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.widget.TextView;

import java.util.function.IntConsumer;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Segmented control: one cell per value. The active cell is filled by the skin; the labels
 * are real text views so they follow the skin typeface.
 */
public class TgChoice extends TgGroup {

    private final TextView[] mLabels;
    private final int[] mCellBounds;
    private IntConsumer mListener;
    private int mSelected = -1;
    private int mPressed = -1;

    public TgChoice(@NonNull Context context, @NonNull String[] labels) {
        super(context);
        mLabels = new TextView[labels.length];
        mCellBounds = new int[labels.length];
        int gap = Math.round(dp(2f));
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            var text = new TextView(context);
            text.setText(labels[i]);
            text.setGravity(Gravity.CENTER);
            text.setSingleLine(true);
            text.setClickable(false);
            // cells are laid out by hand in onLayout, so no weight is needed here
            addView(text, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            mLabels[i] = text;
        }
        setClickable(true);
        setFocusable(true);
    }

    public void setOnChosen(IntConsumer listener) {
        mListener = listener;
    }

    public int getSelected() {
        return mSelected;
    }

    public void setSelected(int index, boolean notify) {
        if (index == mSelected) {
            return;
        }
        mSelected = index;
        applyColors();
        invalidate();
        if (notify && mListener != null) {
            mListener.accept(index);
        }
    }

    private void applyColors() {
        var tk = tokens();
        for (int i = 0; i < mLabels.length; i++) {
            boolean active = i == mSelected;
            mLabels[i].setTextColor(active ? tk.onAccent : tk.muted);
            mLabels[i].setTextSize(tk.valueSize);
            if (tk.typeface != null) {
                mLabels[i].setTypeface(tk.typeface);
            }
        }
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        applyColors();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // the cells are equal width, so the height comes from the labels plus padding
        int padV = Math.round(dp(6f));
        int h = 0;
        for (var label : mLabels) {
            h = Math.max(h, label.getMeasuredHeight());
        }
        setMeasuredDimension(getMeasuredWidth(), h + padV * 2);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int padV = Math.round(dp(6f));
        int count = mLabels.length;
        if (count == 0) {
            return;
        }
        int w = right - left;
        for (int i = 0; i < count; i++) {
            var label = mLabels[i];
            int cellLeft = w * i / count;
            int cellRight = w * (i + 1) / count;
            mCellBounds[i] = cellLeft;
            int lh = label.getMeasuredHeight();
            int childTop = top + (bottom - top - lh) / 2;
            label.layout(cellLeft, childTop, cellRight, childTop + lh);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int count = mLabels.length;
        for (int i = 0; i < count; i++) {
            int cellLeft = mCellBounds[i];
            int cellRight = i + 1 < count ? mCellBounds[i + 1] : getWidth();
            skin().drawSegment(canvas, cellLeft, 0, cellRight, getHeight(),
                    i, count, i == mSelected, i == mPressed);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mPressed = cellAt(event.getX());
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                int index = cellAt(event.getX());
                mPressed = -1;
                if (index >= 0) {
                    setSelected(index, true);
                }
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                mPressed = -1;
                invalidate();
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private int cellAt(float x) {
        int count = mLabels.length;
        if (count == 0 || getWidth() <= 0) {
            return -1;
        }
        int index = (int) (x / getWidth() * count);
        return Math.max(0, Math.min(count - 1, index));
    }
}
