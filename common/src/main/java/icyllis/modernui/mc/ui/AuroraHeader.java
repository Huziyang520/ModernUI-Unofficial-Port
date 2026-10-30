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
import icyllis.modernui.graphics.LinearGradient;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.RadialGradient;
import icyllis.modernui.graphics.Shader;
import icyllis.modernui.widget.FrameLayout;

/**
 * Banner at the top of the settings screen: an accent wash, a soft glow and a dot
 * grid, all drawn once per frame with shaders cached for the current size.
 */
public class AuroraHeader extends FrameLayout {

    private int mAccent;
    private final int mSurface;
    private int mGridColor;

    private final Paint mPaint = new Paint();

    private Shader mWash;
    private Shader mGlow;
    private int mWidth;
    private int mHeight;

    public AuroraHeader(@NonNull Context context) {
        super(context);
        mAccent = Design.accent(context);
        mSurface = Design.surface(context);
        mGridColor = Design.alpha(Design.onSurface(context), 0.10f);
        mPaint.setAntiAlias(true);
        setWillNotDraw(false);
    }

    /** Re-reads the accent and rebuilds the shaders, e.g. after the player picked a colour. */
    public void reloadAccent() {
        var context = getContext();
        mAccent = Design.accent(context);
        mGridColor = Design.alpha(Design.onSurface(context), 0.10f);
        rebuildShader();
        invalidate();
    }

    private void rebuildShader() {
        if (mWidth <= 0 || mHeight <= 0) {
            return;
        }
        mWash = new LinearGradient(0, 0, 0, mHeight,
                new int[]{Design.alpha(mAccent, 0.42f),
                        Design.alpha(mAccent, 0.16f),
                        Design.alpha(mSurface, 0f)},
                new float[]{0f, 0.55f, 1f},
                Shader.TileMode.CLAMP, null);
        mGlow = new RadialGradient(mWidth * 0.82f, mHeight * 0.12f,
                Math.max(mWidth, mHeight) * 0.62f,
                new int[]{Design.alpha(mAccent, 0.34f), Design.alpha(mAccent, 0f)},
                null, Shader.TileMode.CLAMP, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        // the wash fades out before the content area starts, so cards stay readable
        rebuildShader();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mWidth <= 0 || mHeight <= 0) {
            return;
        }
        mPaint.setStyle(Paint.FILL);
        if (mWash != null) {
            mPaint.setShader(mWash);
            canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        }
        if (mGlow != null) {
            mPaint.setShader(mGlow);
            canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        }
        mPaint.setShader(null);

        // dot grid: a quiet texture on the right half, fading towards the left
        float step = dp(22);
        float radius = dp(1.1f);
        int rows = (int) (mHeight / step);
        float startX = mWidth * 0.46f;
        mPaint.setColor(mGridColor);
        for (int row = 0; row <= rows; row++) {
            float y = row * step + step * 0.5f;
            float fade = 1f - Math.abs(y - mHeight * 0.45f) / (mHeight * 0.7f);
            if (fade <= 0f) {
                continue;
            }
            mPaint.setAlpha(Math.round(255 * Math.min(1f, fade)));
            for (float x = startX; x < mWidth; x += step) {
                canvas.drawCircle(x, y, radius, mPaint);
            }
        }
        mPaint.setAlpha(255);

        // bottom hairline keeps the banner from floating on the cards
        mPaint.setColor(Design.alpha(Design.outlineVariant(getContext()), 0.55f));
        canvas.drawRect(0, mHeight - dp(1), mWidth, mHeight, mPaint);
    }
}
