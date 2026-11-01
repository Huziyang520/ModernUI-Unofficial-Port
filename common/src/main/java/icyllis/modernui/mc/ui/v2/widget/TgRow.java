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
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * One settings line: label block on the left, one control on the right, and a skin drawn
 * background. The label comes from a translation key plus an optional {@code .desc} line.
 */
public class TgRow extends TgGroup {

    private final TextView mTitle;
    private final TextView mDesc;
    private final View mControl;
    private boolean mFirst = true;
    private boolean mLast = true;
    private boolean mPressed;

    public TgRow(@NonNull Context context, @NonNull String key, @NonNull View control) {
        super(context);
        mControl = control;

        // the row is a frame, so the label/value split needs a real linear row inside it;
        // a frame would drop the weight and collapse the label to zero width
        var line = new LinearLayout(context);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);

        var block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setGravity(Gravity.CENTER_VERTICAL);

        mTitle = new TextView(context);
        mTitle.setText(strip(I18n.get(key)));
        block.addView(mTitle);

        String descKey = key + ".desc";
        String desc = I18n.get(descKey);
        mDesc = new TextView(context);
        if (!desc.equals(descKey)) {
            mDesc.setText(strip(desc));
            mDesc.setVisibility(VISIBLE);
            var descParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            descParams.setMargins(0, Math.round(dp(1f)), 0, 0);
            block.addView(mDesc, descParams);
        } else {
            mDesc.setVisibility(GONE);
        }

        var blockParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        blockParams.setMargins(0, 0, Math.round(dp(10f)), 0);
        line.addView(block, blockParams);

        var controlParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        controlParams.gravity = Gravity.CENTER_VERTICAL;
        line.addView(control, controlParams);

        addView(line, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        setClickable(true);
        applyType();
        applyMetrics();
    }

    /** The translation keys carry {@code §} formatting on some entries; the UI styles text. */
    private static String strip(String text) {
        if (text.indexOf(167) < 0) {
            return text;
        }
        var sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == 167 && i + 1 < text.length()) {
                i++;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public void setPosition(boolean first, boolean last) {
        mFirst = first;
        mLast = last;
        invalidate();
    }

    public View control() {
        return mControl;
    }

    public TextView title() {
        return mTitle;
    }

    public TextView description() {
        return mDesc;
    }

    /** Whether this row matches a search query; used by the shell filter. */
    public boolean matches(@NonNull String query) {
        String lower = query.toLowerCase(Locale.ROOT);
        return mTitle.getText().toString().toLowerCase(Locale.ROOT).contains(lower)
                || (mDesc.getVisibility() == VISIBLE
                && mDesc.getText().toString().toLowerCase(Locale.ROOT).contains(lower));
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        applyType();
        applyMetrics();
    }

    private void applyType() {
        var tk = tokens();
        mTitle.setTextSize(tk.titleSize);
        mTitle.setTextColor(tk.text);
        mDesc.setTextSize(tk.descSize);
        mDesc.setTextColor(tk.muted);
        if (tk.typeface != null) {
            mTitle.setTypeface(tk.typeface);
            mDesc.setTypeface(tk.typeface);
        }
        if (tk.upperCaseRows) {
            mTitle.setText(mTitle.getText().toString().toUpperCase(Locale.ROOT));
        }
    }

    /**
     * Padding and the minimum height are layout inputs, so they are applied on construction
     * and on a style change. Setting them from {@code onMeasure} would request another layout
     * from inside the pass that is measuring this view, which clipped the label.
     */
    private void applyMetrics() {
        var tk = tokens();
        setPadding(Math.round(dp(tk.padH)), Math.round(dp(tk.padV)),
                Math.round(dp(tk.padH)), Math.round(dp(tk.padV)));
        setMinimumHeight(Math.round(dp(tk.rowHeight)));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        skin().drawRowBackground(canvas, 0, 0, getWidth(), getHeight(), mFirst, mLast, mPressed);
    }

    @Override
    public void setPressed(boolean pressed) {
        mPressed = pressed;
        invalidate();
    }

    @Nullable
    public static TgRow find(@NonNull View view) {
        var parent = view.getParent();
        while (parent instanceof View v) {
            if (v instanceof TgRow row) {
                return row;
            }
            parent = v.getParent();
        }
        return null;
    }
}
