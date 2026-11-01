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
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Filled button with a text label and an optional leading glyph.
 */
public class TgButton extends TgGroup {

    private final TextView mLabel;
    private final View mGlyph;
    private boolean mPressed;
    private boolean mEnabled = true;
    private int mGlyphId = Skin.GLYPH_NONE;

    public TgButton(@NonNull Context context, @NonNull CharSequence label) {
        super(context);
        setClickable(true);
        setFocusable(true);

        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        mGlyph = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                skin().drawGlyph(canvas, mGlyphId, getWidth() * 0.5f, getHeight() * 0.5f,
                        getWidth(), tokens().onAccent);
            }
        };
        mGlyph.setWillNotDraw(false);
        mGlyph.setVisibility(GONE);
        var glyphParams = new LinearLayout.LayoutParams(Math.round(dp(15f)), Math.round(dp(15f)));
        glyphParams.gravity = Gravity.CENTER_VERTICAL;
        glyphParams.setMargins(0, 0, Math.round(dp(6f)), 0);
        row.addView(mGlyph, glyphParams);

        mLabel = new TextView(context);
        mLabel.setText(label);
        mLabel.setSingleLine(true);
        mLabel.setClickable(false);
        mLabel.setGravity(Gravity.CENTER);
        row.addView(mLabel, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        var params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        addView(row, params);

        int padV = Math.round(dp(9f));
        setPadding(Math.round(dp(14f)), padV, Math.round(dp(14f)), padV);
    }

    public void setText(CharSequence text) {
        mLabel.setText(text);
    }

    public void setGlyph(int glyph) {
        mGlyphId = glyph;
        mGlyph.setVisibility(glyph == Skin.GLYPH_NONE ? GONE : VISIBLE);
    }

    public void setOnAction(Runnable action) {
        setOnClickListener(v -> action.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        super.setEnabled(enabled);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var tk = tokens();
        mLabel.setTextSize(tk.valueSize);
        mLabel.setTextColor(tk.onAccent);
        if (tk.typeface != null) {
            mLabel.setTypeface(tk.typeface);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        skin().drawButton(canvas, 0, 0, getWidth(), getHeight(), mPressed, mEnabled);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!mEnabled) {
            return false;
        }
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
