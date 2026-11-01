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

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.EditText;

import java.util.function.Consumer;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Text or number input. The frame is drawn by the skin, the editor is a real
 * {@link EditText} on top of it, so caret, selection and IME behave natively.
 */
public class TgField extends TgGroup {

    private final EditText mInput;
    private boolean mFocused;
    private Consumer<String> mListener;

    public TgField(@NonNull Context context) {
        super(context);
        mInput = new EditText(context, null, R.attr.editTextOutlinedStyle);
        mInput.setSingleLine(true);
        mInput.setTextAlignment(icyllis.modernui.view.View.TEXT_ALIGNMENT_VIEW_END);
        mInput.setBackground(null);
        var params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        int padH = Math.round(dp(10f));
        params.setMargins(padH, Math.round(dp(6f)), padH, Math.round(dp(6f)));
        addView(mInput, params);
        mInput.setOnFocusChangeListener((v, hasFocus) -> {
            mFocused = hasFocus;
            invalidate();
            if (!hasFocus) {
                commit();
            }
        });
    }

    public EditText input() {
        return mInput;
    }

    public void setText(String text) {
        mInput.setText(text);
    }

    public String getText() {
        return mInput.getText().toString();
    }

    /** Called on every edit; the value is not parsed, the caller owns the format. */
    public void setOnCommit(Consumer<String> listener) {
        mListener = listener;
    }

    private void commit() {
        if (mListener != null) {
            mListener.accept(mInput.getText().toString());
        }
    }

    public void setMinWidth(int px) {
        mInput.setMinWidth(px);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var tk = tokens();
        mInput.setTextSize(tk.valueSize);
        mInput.setTextColor(tk.text);
        if (tk.typeface != null) {
            mInput.setTypeface(tk.typeface);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        skin().drawField(canvas, 0, 0, getWidth(), getHeight(), mFocused);
    }
}
