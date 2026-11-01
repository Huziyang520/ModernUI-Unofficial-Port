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
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

import java.util.Locale;
import java.util.function.IntConsumer;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Swatch plus hex readout, the entry point to the colour studio. Clicking anywhere on the
 * control opens the editor.
 */
public class TgColorField extends TgGroup {

    private final TgSwatch mSwatch;
    private final TextView mValue;
    private IntConsumer mListener;
    private Runnable mOnOpen;
    private int mColor = 0xFFFFFFFF;
    private boolean mAlpha;

    public TgColorField(@NonNull Context context) {
        super(context);
        setClickable(true);
        setFocusable(true);

        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        mSwatch = new TgSwatch(context);
        var swatchParams = new LinearLayout.LayoutParams(Math.round(dp(24f)), Math.round(dp(24f)));
        swatchParams.setMargins(0, 0, Math.round(dp(8f)), 0);
        row.addView(mSwatch, swatchParams);

        mValue = new TextView(context);
        mValue.setSingleLine(true);
        mValue.setClickable(false);
        row.addView(mValue, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        addView(row, params);

        setOnClickListener(v -> {
            if (mOnOpen != null) {
                mOnOpen.run();
            }
        });
    }

    public void setAlphaEnabled(boolean alpha) {
        mAlpha = alpha;
        updateLabel();
    }

    public void setOnColorPicked(IntConsumer listener) {
        mListener = listener;
    }

    public void setOnOpen(Runnable onOpen) {
        mOnOpen = onOpen;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int argb) {
        mColor = argb;
        mSwatch.setColor(argb);
        updateLabel();
    }

    /** Called by the studio while the player drags; does not re-notify. */
    public void previewColor(int argb) {
        setColor(argb);
    }

    /** Called once the player releases; notifies the owner. */
    public void commitColor(int argb) {
        setColor(argb);
        if (mListener != null) {
            mListener.accept(argb);
        }
    }

    private void updateLabel() {
        mValue.setText(mAlpha
                ? String.format(Locale.ROOT, "#%08X", mColor)
                : String.format(Locale.ROOT, "#%06X", mColor & 0xFFFFFF));
        var tk = tokens();
        mValue.setTextSize(tk.valueSize);
        mValue.setTextColor(tk.muted);
        if (tk.monoTypeface != null) {
            mValue.setTypeface(tk.monoTypeface);
        }
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        updateLabel();
    }
}
