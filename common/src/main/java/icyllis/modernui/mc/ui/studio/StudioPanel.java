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
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Full height sheet that slides over the grid. Holds a sticky header and one scrolling
 * column of sections.
 */
public class StudioPanel extends FrameLayout {

    private final LinearLayout mHeader;
    private final LinearLayout mBody;
    private final TextView mTitle;
    private Runnable mOnBack;

    public StudioPanel(@NonNull Context context) {
        super(context);

        var background = Studio.rounded(context, Studio.surface(context), Studio.RADIUS_PANEL);
        setBackground(background);

        var column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        addView(column, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mHeader = new LinearLayout(context);
        mHeader.setOrientation(LinearLayout.HORIZONTAL);
        mHeader.setGravity(Gravity.CENTER_VERTICAL);
        int padH = Studio.dp(context, Studio.PAD);
        int padV = Studio.dp(context, 14);
        mHeader.setPadding(padH, padV, padH, padV);

        var back = new View(context) {
            {
                setWillNotDraw(false);
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                var paint = Paint.obtain();
                paint.setAntiAlias(true);
                paint.setColor(Studio.onSurface(getContext()));
                paint.setStyle(Paint.STROKE);
                paint.setStrokeWidth(Studio.dp(getContext(), 1.8f));
                float w = getWidth();
                float h = getHeight();
                canvas.drawLine(w * 0.62f, h * 0.26f, w * 0.34f, h * 0.5f, paint);
                canvas.drawLine(w * 0.34f, h * 0.5f, w * 0.62f, h * 0.74f, paint);
                paint.recycle();
            }
        };
        int hit = Studio.dp(context, 36);
        var backParams = new LinearLayout.LayoutParams(hit, hit);
        backParams.setMargins(0, 0, Studio.dp(context, 6), 0);
        back.setClickable(true);
        back.setOnClickListener(v -> {
            if (mOnBack != null) {
                mOnBack.run();
            }
        });
        mHeader.addView(back, backParams);

        mTitle = new TextView(context);
        mTitle.setTextSize(17);
        mTitle.setTextColor(Studio.onSurface(context));
        mHeader.addView(mTitle, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1));
        column.addView(mHeader, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        var rule = new View(context);
        rule.setBackground(Studio.rounded(context, Studio.outline(context), 0));
        column.addView(rule, new LinearLayout.LayoutParams(MATCH_PARENT, Math.max(1, Studio.dp(context, 0.6f))));

        mBody = new LinearLayout(context);
        mBody.setOrientation(LinearLayout.VERTICAL);
        mBody.setPadding(padH, Studio.dp(context, 10), padH, Studio.dp(context, 28));

        var scroll = new ScrollView(context);
        scroll.addView(mBody, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        column.addView(scroll, new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f));
    }

    public void setOnBack(Runnable onBack) {
        mOnBack = onBack;
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    public LinearLayout getBody() {
        return mBody;
    }

    /** Section label, muted and small, with generous space above. */
    public void addSection(@NonNull CharSequence label) {
        var text = new TextView(getContext());
        text.setText(label);
        text.setTextSize(11);
        text.setTextColor(Studio.muted(getContext()));
        int top = Studio.dp(getContext(), mBody.getChildCount() == 0 ? 4 : 18);
        text.setPadding(0, top, 0, Studio.dp(getContext(), 6));
        mBody.addView(text, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

    public void addRow(@NonNull View row) {
        mBody.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }
}
