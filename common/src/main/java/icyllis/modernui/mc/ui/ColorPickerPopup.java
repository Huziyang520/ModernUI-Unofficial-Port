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
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.PopupWindow;

/**
 * Floating color picker used by the swatch based editors.
 */
public final class ColorPickerPopup {

    public interface OnColorPickedListener {
        void onColorPicked(int argb);
    }

    private final PopupWindow mPopup;
    private final ColorPickerView mPicker;

    public ColorPickerPopup(@NonNull View anchor, int initialColor, boolean alpha,
                            @NonNull OnColorPickedListener listener) {
        var context = anchor.getContext();
        var content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = content.dp(14);
        content.setPadding(padding, padding, padding, padding);

        var background = new ShapeDrawable();
        background.setShape(ShapeDrawable.RECTANGLE);
        background.setCornerRadius(content.dp(12));
        background.setColor(panelColor(context));
        content.setBackground(background);
        content.setElevation(content.dp(6));

        mPicker = new ColorPickerView(context);
        mPicker.setAlphaEnabled(alpha);
        mPicker.setColor(initialColor);
        content.addView(mPicker, new ViewGroup.LayoutParams(
                content.dp(260), ViewGroup.LayoutParams.WRAP_CONTENT));

        mPicker.setOnColorChangeListener((argb, committed) -> {
            if (committed) {
                listener.onColorPicked(argb);
            }
        });

        mPopup = new PopupWindow(content);
        mPopup.setFocusable(true);
        mPopup.setOutsideTouchable(true);
        mPopup.setElevation(anchor.dp(8));
    }

    private static int panelColor(@NonNull Context context) {
        var value = new icyllis.modernui.resources.TypedValue();
        if (context.getTheme().resolveAttribute(icyllis.modernui.R.ns,
                icyllis.modernui.R.attr.colorSurfaceContainerHigh, value, true)) {
            return value.data;
        }
        return 0xFF2B2B2B;
    }

    public void show(@NonNull View anchor) {
        mPopup.showAsDropDown(anchor, 0, -anchor.getHeight() / 2, Gravity.TOP);
    }

    public ColorPickerView getPicker() {
        return mPicker;
    }

    public void dismiss() {
        mPopup.dismiss();
    }
}
