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

package icyllis.modernui.mc.ui.v2.shell;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.mc.ui.v2.widget.TgGroup;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Header control that names the active style and opens the picker. Drawn as a field so it
 * reads as a value selector rather than as an action button.
 */
final class StyleButton extends TgGroup {

    private final TextView mLabel;
    private final GlyphIcon mGlyph;
    private boolean mPressed;

    StyleButton(@NonNull Context context) {
        super(context);
        setClickable(true);
        setFocusable(true);

        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        mGlyph = new GlyphIcon(context, Skin.GLYPH_PALETTE);
        mGlyph.setAccent(true);
        row.addView(mGlyph, new LinearLayout.LayoutParams(Math.round(dp(16f)), Math.round(dp(16f))));

        mLabel = new TextView(context);
        mLabel.setSingleLine(true);
        mLabel.setClickable(false);
        var labelParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        labelParams.setMargins(Math.round(dp(7f)), 0, 0, 0);
        row.addView(mLabel, labelParams);

        var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        addView(row, params);

        int padH = Math.round(dp(10f));
        int padV = Math.round(dp(7f));
        setPadding(padH, padV, padH, padV);

        refreshLabel();
    }

    /** Re-reads the active style name; called when the registry changes. */
    void refreshLabel() {
        mLabel.setText(I18n.get("mui.v2.style." + SkinRegistry.activeId()));
        var tk = tokens();
        mLabel.setTextSize(tk.valueSize);
        mLabel.setTextColor(tk.text);
        if (tk.typeface != null) {
            mLabel.setTypeface(tk.typeface);
        }
        requestLayout();
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        refreshLabel();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        skin().drawField(canvas, 0, 0, getWidth(), getHeight(), mPressed);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                mPressed = true;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                mPressed = false;
                invalidate();
                performClick();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                mPressed = false;
                invalidate();
                return true;
            }
            default -> {
                return true;
            }
        }
    }
}
