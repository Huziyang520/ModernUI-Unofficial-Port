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
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.widget.PopupWindow;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Compact value button that opens a floating list. Used where a segmented control would
 * overflow, i.e. enums with many values.
 */
public class StudioMenu extends LinearLayout {

    public interface OnChosen {
        void onChosen(int index);
    }

    private final TextView mLabel;
    private final CharSequence[] mItems;
    private PopupWindow mPopup;
    private OnChosen mListener;
    private int mSelected = -1;

    public StudioMenu(@NonNull Context context, @NonNull CharSequence[] items) {
        super(context);
        mItems = items;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setClickable(true);
        setFocusable(true);
        int padH = Studio.dp(context, 12);
        int padV = Studio.dp(context, 6);
        setPadding(padH, padV, padH, padV);
        setBackground(Studio.pill(context, Studio.alpha(Studio.onSurface(context), 0.07f)));

        mLabel = new TextView(context);
        mLabel.setTextSize(11);
        mLabel.setSingleLine(true);
        mLabel.setTextColor(Studio.onSurface(context));
        addView(mLabel, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        var chevron = new View(context) {
            {
                setWillNotDraw(false);
            }

            @Override
            protected void onDraw(@NonNull icyllis.modernui.graphics.Canvas canvas) {
                var paint = Paint.obtain();
                paint.setAntiAlias(true);
                paint.setColor(Studio.muted(getContext()));
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Studio.dp(getContext(), 1.4f));
                float w = getWidth();
                float h = getHeight();
                canvas.drawLine(w * 0.25f, h * 0.42f, w * 0.5f, h * 0.62f, paint);
                canvas.drawLine(w * 0.5f, h * 0.62f, w * 0.75f, h * 0.42f, paint);
                paint.recycle();
            }
        };
        int chevronSize = Studio.dp(context, 14);
        var chevronParams = new LinearLayout.LayoutParams(chevronSize, chevronSize);
        chevronParams.setMargins(Studio.dp(context, 4), 0, 0, 0);
        addView(chevron, chevronParams);

        var list = new LinearLayout(context);
        list.setOrientation(VERTICAL);
        int pad = Studio.dp(context, 6);
        list.setPadding(pad, pad, pad, pad);
        list.setBackground(Studio.rounded(context, Studio.surfaceHigh(context), 16));

        var scroll = new ScrollView(context);
        scroll.addView(list, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            var row = new TextView(context);
            row.setText(items[i]);
            row.setTextSize(12);
            row.setSingleLine(true);
            row.setClickable(true);
            int rowPadH = Studio.dp(context, 14);
            int rowPadV = Studio.dp(context, 9);
            row.setPadding(rowPadH, rowPadV, rowPadH, rowPadV);
            row.setOnClickListener(v -> {
                setSelected(index, true);
                mPopup.dismiss();
            });
            list.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }

        mPopup = new PopupWindow(scroll);
        mPopup.setFocusable(true);
        mPopup.setOutsideTouchable(true);
        mPopup.setElevation(Studio.dp(context, 10));

        setOnClickListener(v -> {
            mPopup.setWidth(getWidth());
            mPopup.showAsDropDown(this, 0, Studio.dp(getContext(), 4), Gravity.TOP);
        });
    }

    public void setOnChosen(OnChosen listener) {
        mListener = listener;
    }

    public void setSelected(int index, boolean notify) {
        mSelected = index;
        var context = getContext();
        if (index >= 0 && index < mItems.length) {
            mLabel.setText(mItems[index]);
            mLabel.setTextColor(Studio.onSurface(context));
        }
        if (notify && mListener != null) {
            mListener.onChosen(index);
        }
    }
}
