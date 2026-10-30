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
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.widget.LinearLayout;

/**
 * Rounded panel that groups related rows. The fill is drawn in onDraw so a page with
 * ten cards costs ten draw calls instead of ten nested drawables.
 */
public class CardView extends LinearLayout {

    private final float mRadius;
    private final int mFill;
    private final int mStroke;

    private final Paint mPaint = new Paint();

    public CardView(@NonNull Context context) {
        this(context, 0.62f);
    }

    public CardView(@NonNull Context context, float fillAlpha) {
        super(context);
        setOrientation(VERTICAL);
        mRadius = dp(Design.RADIUS_CARD);
        mFill = Design.alpha(Design.surfaceHigh(context), fillAlpha);
        mStroke = Design.alpha(Design.outlineVariant(context), 0.45f);
        mPaint.setAntiAlias(true);
        setWillNotDraw(false);
        int pad = dp(Design.SPACE_1);
        setPadding(0, pad, 0, pad);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float stroke = dp(1);
        float inset = stroke * 0.5f;
        mPaint.setStyle(Paint.FILL);
        mPaint.setShader(null);
        mPaint.setColor(mFill);
        canvas.drawRoundRect(inset, inset, getWidth() - inset, getHeight() - inset,
                mRadius, mPaint);
        mPaint.setStyle(Paint.STROKE);
        mPaint.setStrokeWidth(stroke);
        mPaint.setColor(mStroke);
        canvas.drawRoundRect(inset, inset, getWidth() - inset, getHeight() - inset,
                mRadius, mPaint);
        mPaint.setStyle(Paint.FILL);
    }
}
