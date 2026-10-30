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
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.HorizontalScrollView;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Horizontally scrolling pill navigation. Selection state is expressed by fill and
 * text colour only, so switching pages never rebuilds the labels.
 */
public class ChipBar extends HorizontalScrollView {

    public interface OnSelectionChanged {
        void onSelectionChanged(int index, boolean animate);
    }

    private final LinearLayout mList;
    private final TextView[] mChips;
    private OnSelectionChanged mListener;
    private int mSelected = -1;

    public ChipBar(@NonNull Context context, int count) {
        super(context);
        setClipToPadding(false);
        int pad = dp(Design.SPACE_4);
        setPadding(pad, dp(Design.SPACE_2), pad, dp(Design.SPACE_2));

        mList = new LinearLayout(context);
        mList.setOrientation(LinearLayout.HORIZONTAL);
        var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        addView(mList, params);
        mChips = new TextView[count];
    }

    public void setOnSelectionChanged(OnSelectionChanged listener) {
        mListener = listener;
    }

    public TextView addChip(@NonNull CharSequence label, int index) {
        var chip = new TextView(getContext());
        chip.setText(label);
        chip.setTextSize(13);
        chip.setSingleLine(true);
        chip.setGravity(Gravity.CENTER);
        chip.setClickable(true);
        chip.setFocusable(true);
        int padH = dp(14);
        chip.setPadding(padH, 0, padH, 0);
        chip.setOnClickListener(v -> select(index, true));

        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, dp(36));
        params.setMargins(0, 0, dp(Design.SPACE_2), 0);
        mList.addView(chip, params);
        mChips[index] = chip;
        paintChip(chip, index == mSelected);
        return chip;
    }

    public void select(int index, boolean animate) {
        if (index == mSelected) {
            return;
        }
        mSelected = index;
        for (int i = 0; i < mChips.length; i++) {
            var chip = mChips[i];
            if (chip != null) {
                paintChip(chip, i == index);
            }
        }
        var chip = index >= 0 && index < mChips.length ? mChips[index] : null;
        if (chip != null) {
            // keep the active chip inside the viewport when the row scrolls
            int target = chip.getLeft() - (getWidth() - chip.getWidth()) / 2;
            smoothScrollTo(Math.max(0, target));
            chip.setSelected(true);
            if (animate && SurfacePrefs.animations()) {
                chip.setAlpha(0.55f);
                var fade = icyllis.modernui.animation.ObjectAnimator
                        .ofFloat(chip, View.ALPHA, 0.55f, 1f);
                fade.setDuration(180);
                fade.start();
            } else {
                chip.setAlpha(1f);
            }
        }
        if (mListener != null) {
            mListener.onSelectionChanged(index, animate);
        }
    }

    /** Re-applies the palette, e.g. after the accent colour changed. */
    public void repaint() {
        for (int i = 0; i < mChips.length; i++) {
            var chip = mChips[i];
            if (chip != null) {
                paintChip(chip, i == mSelected);
            }
        }
    }

    public int getSelectedIndex() {
        return mSelected;
    }

    private void paintChip(@NonNull TextView chip, boolean selected) {
        var context = chip.getContext();
        int accent = Design.accent(context);
        chip.setTextColor(selected ? Design.contrasting(accent) : Design.onSurfaceVariant(context));
        chip.setBackground(selected
                ? Design.rounded(context, accent, Design.RADIUS_PILL)
                : Design.outlined(context, 0x00000000, Design.alpha(Design.outline(context), 0.65f),
                Design.RADIUS_PILL, 1));
    }
}
