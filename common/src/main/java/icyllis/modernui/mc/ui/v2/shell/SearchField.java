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

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.widget.TgGroup;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Consumer;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Search box of the shell header: a skin drawn frame, a leading magnifier glyph and a real
 * {@link EditText} so the caret, selection and IME behave natively.
 */
public final class SearchField extends TgGroup {

    private final EditText mInput;
    private final GlyphIcon mIcon;
    private Consumer<String> mListener;
    private boolean mFocused;

    public SearchField(@NonNull Context context) {
        super(context);
        setClickable(true);

        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int padH = Math.round(dp(10f));

        mIcon = new GlyphIcon(context, Skin.GLYPH_SEARCH);
        var iconParams = new LinearLayout.LayoutParams(Math.round(dp(15f)), Math.round(dp(15f)));
        iconParams.setMargins(0, 0, Math.round(dp(8f)), 0);
        row.addView(mIcon, iconParams);

        mInput = new EditText(context, null, R.attr.editTextOutlinedStyle);
        mInput.setSingleLine(true);
        mInput.setBackground(null);
        mInput.setHint(I18n.get("mui.v2.shell.search"));
        row.addView(mInput, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        var params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        addView(row, params);
        setPadding(padH, Math.round(dp(4f)), padH, Math.round(dp(4f)));

        mInput.setOnFocusChangeListener((v, hasFocus) -> {
            mFocused = hasFocus;
            mIcon.setAccent(hasFocus);
            invalidate();
        });
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mListener != null) {
                    mListener.accept(s == null ? "" : s.toString());
                }
            }
        });

        setOnClickListener(v -> mInput.requestFocus());
    }

    /** Reports every edit, not only the committed value; the filter is live. */
    public void setOnQueryChanged(Consumer<String> listener) {
        mListener = listener;
    }

    public String getQuery() {
        return mInput.getText() == null ? "" : mInput.getText().toString();
    }

    public void setQuery(String query) {
        mInput.setText(query == null ? "" : query);
    }

    public void clear() {
        setQuery("");
    }

    public EditText input() {
        return mInput;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var tk = tokens();
        mInput.setTextSize(tk.valueSize);
        mInput.setTextColor(tk.text);
        mInput.setHintTextColor(tk.muted);
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
