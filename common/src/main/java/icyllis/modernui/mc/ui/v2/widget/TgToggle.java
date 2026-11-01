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
import icyllis.modernui.mc.ui.v2.TgAnim;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

import java.util.function.Consumer;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Themed switch. The skin draws the chrome; this class owns state, gesture and the
 * optional word label some styles use instead of a pill.
 */
public class TgToggle extends TgGroup {

    private final LinearLayout mWord;
    private final TextView mOn;
    private final TextView mOff;
    private final View mPill;

    private boolean mChecked;
    private float mProgress;
    private boolean mPressed;
    private Consumer<Boolean> mListener;

    public TgToggle(@NonNull Context context) {
        super(context);

        mWord = new LinearLayout(context);
        mWord.setOrientation(LinearLayout.HORIZONTAL);
        mWord.setGravity(Gravity.CENTER);
        mWord.setVisibility(GONE);
        mOn = new TextView(context);
        mOn.setTextSize(13);
        mOn.setGravity(Gravity.CENTER);
        mOff = new TextView(context);
        mOff.setTextSize(13);
        mOff.setGravity(Gravity.CENTER);
        int gap = Math.round(dp(6f));
        var onParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        onParams.setMargins(0, 0, gap, 0);
        mWord.addView(mOn, onParams);
        mWord.addView(mOff, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        addView(mWord, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        mPill = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                skin().drawToggle(canvas, 0.5f, 0.5f, getWidth() - 0.5f, getHeight() - 0.5f,
                        mProgress, mChecked, mPressed);
            }
        };
        mPill.setWillNotDraw(false);
        var pillParams = new LayoutParams(Math.round(dp(44f)), Math.round(dp(24f)));
        pillParams.gravity = Gravity.CENTER;
        addView(mPill, pillParams);

        setClickable(true);
        setFocusable(true);
    }

    public void setOnCheckedChanged(Consumer<Boolean> listener) {
        mListener = listener;
    }

    /** Switches between the pill form and the word form used by the mono/terminal styles. */
    public void setWordMode(boolean word) {
        mWord.setVisibility(word ? VISIBLE : GONE);
        mPill.setVisibility(word ? GONE : VISIBLE);
        requestLayout();
    }

    public void setLabels(@NonNull String on, @NonNull String off) {
        mOn.setText(on);
        mOff.setText(off);
        requestLayout();
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (mChecked == checked) {
            return;
        }
        mChecked = checked;
        float target = checked ? 1f : 0f;
        var tk = tokens();
        if (animate && tk.motionMs > 0) {
            float from = mProgress;
            TgAnim.run(this, tk.motionMs, tk::ease, t -> {
                mProgress = from + (target - from) * t;
                invalidate();
            });
        } else {
            mProgress = target;
            invalidate();
        }
        applyWordState();
    }

    private void applyWordState() {
        var tk = tokens();
        mOn.setTextColor(mChecked ? tk.accent : tk.muted);
        mOff.setTextColor(mChecked ? tk.muted : tk.accent);
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        applyWordState();
        setWordMode(tokens().wordToggle);
        mOn.setTextSize(tokens().titleSize);
        mOff.setTextSize(tokens().titleSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var tk = tokens();
        if (tk.typeface != null) {
            mOn.setTypeface(tk.typeface);
            mOff.setTypeface(tk.typeface);
        }
        if (tk.wordToggle) {
            setWordMode(true);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
                setChecked(!mChecked, true);
                if (mListener != null) {
                    mListener.accept(mChecked);
                }
                invalidate();
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
