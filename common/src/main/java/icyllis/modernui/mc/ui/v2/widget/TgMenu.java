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
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.PopupWindow;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;

import java.util.function.IntConsumer;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Value button that opens a floating list. Used where a segmented control would overflow,
 * i.e. enums with many values or long translated names.
 */
public class TgMenu extends TgGroup {

    private final TextView mLabel;
    private final View mChevron;
    private final CharSequence[] mItems;
    private PopupWindow mPopup;
    private IntConsumer mListener;
    private int mSelected = -1;

    public TgMenu(@NonNull Context context, @NonNull CharSequence[] items) {
        super(context);
        mItems = items;
        setClickable(true);
        setFocusable(true);

        mLabel = new TextView(context);
        mLabel.setSingleLine(true);
        mLabel.setClickable(false);
        var labelParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        labelParams.gravity = Gravity.CENTER_VERTICAL;
        addView(mLabel, labelParams);

        mChevron = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                skin().drawGlyph(canvas, Skin.GLYPH_CHEVRON_DOWN,
                        getWidth() * 0.5f, getHeight() * 0.5f, getWidth(), tokens().muted);
            }
        };
        mChevron.setWillNotDraw(false);
        var chevronParams = new LayoutParams(Math.round(dp(14f)), Math.round(dp(14f)));
        chevronParams.gravity = Gravity.CENTER_VERTICAL;
        chevronParams.setMargins(Math.round(dp(5f)), 0, 0, 0);
        addView(mChevron, chevronParams);

        var list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(dp(6f));
        list.setPadding(pad, pad, pad, pad);
        list.setBackground(icyllis.modernui.mc.ui.v2.TgDrawable.rounded(context,
                skin().tokens().panel, skin().tokens().radiusControl));

        var scroll = new ScrollView(context);
        scroll.addView(list, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            var row = new TextView(context);
            row.setText(items[i]);
            row.setSingleLine(true);
            row.setClickable(true);
            int padH = Math.round(dp(14f));
            int padV = Math.round(dp(9f));
            row.setPadding(padH, padV, padH, padV);
            row.setOnClickListener(v -> {
                setSelected(index, true);
                mPopup.dismiss();
            });
            list.addView(row, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }

        mPopup = new PopupWindow(scroll);
        mPopup.setFocusable(true);
        mPopup.setOutsideTouchable(true);
        mPopup.setElevation(dp(10f));

        setOnClickListener(v -> {
            mPopup.setWidth(Math.max(getWidth(), Math.round(dp(160f))));
            mPopup.showAsDropDown(this, 0, Math.round(dp(4f)), Gravity.TOP);
        });
    }

    public void setOnChosen(IntConsumer listener) {
        mListener = listener;
    }

    public int getSelected() {
        return mSelected;
    }

    public void setSelected(int index, boolean notify) {
        if (index < 0 || index >= mItems.length) {
            return;
        }
        mSelected = index;
        mLabel.setText(mItems[index]);
        applyColors();
        requestLayout();
        invalidate();
        if (notify && mListener != null) {
            mListener.accept(index);
        }
    }

    private void applyColors() {
        var tk = tokens();
        mLabel.setTextColor(tk.text);
        mLabel.setTextSize(tk.valueSize);
        if (tk.typeface != null) {
            mLabel.setTypeface(tk.typeface);
        }
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        applyColors();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        applyColors();
        int padH = Math.round(dp(12f));
        int padV = Math.round(dp(6f));
        setPadding(padH, padV, padH, padV);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float radius = tokens().radiusControl;
        skin().drawField(canvas, 0, 0, getWidth(), getHeight(), false);
    }
}
