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
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Segmented control: one pill per value, the active one filled with the accent.
 */
public class StudioChoice extends LinearLayout {

    public interface OnChosen {
        void onChosen(int index);
    }

    private final TextView[] mSegments;
    private OnChosen mListener;
    private int mSelected = -1;

    public StudioChoice(@NonNull Context context, @NonNull String[] labels) {
        super(context);
        setOrientation(HORIZONTAL);
        setBackground(Studio.pill(context, Studio.alpha(Studio.onSurface(context), 0.07f)));
        int pad = Studio.dp(context, 3);
        setPadding(pad, pad, pad, pad);

        mSegments = new TextView[labels.length];
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            var segment = new TextView(context);
            segment.setText(labels[i]);
            segment.setTextSize(11);
            segment.setGravity(Gravity.CENTER);
            segment.setSingleLine(true);
            segment.setClickable(true);
            segment.setFocusable(true);
            int padH = Studio.dp(context, 10);
            segment.setPadding(padH, Studio.dp(context, 5), padH, Studio.dp(context, 5));
            segment.setOnClickListener(v -> {
                setSelected(index, true);
            });
            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.setMargins(0, 0, Studio.dp(context, 2), 0);
            addView(segment, params);
            mSegments[i] = segment;
        }
    }

    public void setOnChosen(OnChosen listener) {
        mListener = listener;
    }

    public void setSelected(int index, boolean notify) {
        if (index == mSelected) {
            return;
        }
        mSelected = index;
        var context = getContext();
        for (int i = 0; i < mSegments.length; i++) {
            var segment = mSegments[i];
            boolean active = i == index;
            segment.setTextColor(active ? Studio.contrasting(Studio.accent(context))
                    : Studio.muted(context));
            segment.setBackground(active
                    ? Studio.pill(context, Studio.accent(context))
                    : null);
        }
        if (notify && mListener != null) {
            mListener.onChosen(index);
        }
    }
}
