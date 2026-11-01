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
import icyllis.modernui.mc.ui.v2.widget.TgGroup;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Header control that restores every setting of the mod to its default. Drawn as an outlined
 * field so it reads as a secondary action next to the style selector.
 */
final class ResetButton extends TgGroup {

    private final TextView mLabel;
    private final View mGlyph;

    ResetButton(@NonNull Context context) {
        super(context);
        setClickable(true);
        setFocusable(true);

        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        mGlyph = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                // a counter-clockwise arrow, drawn as an arc plus a head
                var paint = icyllis.modernui.graphics.Paint.obtain();
                paint.setAntiAlias(true);
                paint.setColor(tokens().muted);
                paint.setStyle(icyllis.modernui.graphics.Paint.STROKE);
                paint.setStrokeWidth(Math.max(1.4f, getWidth() * 0.11f));
                float inset = getWidth() * 0.16f;
                canvas.drawArc(inset, inset, getWidth() - inset, getHeight() - inset, 40f, paint);
                float cx = getWidth() * 0.5f;
                float cy = getHeight() * 0.5f;
                canvas.drawLine(cx + getWidth() * 0.24f, cy - getHeight() * 0.34f,
                        cx + getWidth() * 0.30f, cy - getHeight() * 0.10f, paint);
                canvas.drawLine(cx + getWidth() * 0.24f, cy - getHeight() * 0.34f,
                        cx + getWidth() * 0.02f, cy - getHeight() * 0.30f, paint);
                paint.recycle();
            }
        };
        mGlyph.setWillNotDraw(false);
        row.addView(mGlyph, new LinearLayout.LayoutParams(Math.round(dp(14f)), Math.round(dp(14f))));

        mLabel = new TextView(context);
        mLabel.setText(I18n.get("mui.v2.shell.resetAll"));
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
        applyType();
    }

    private void applyType() {
        var tk = tokens();
        mLabel.setTextSize(tk.valueSize);
        mLabel.setTextColor(tk.muted);
        if (tk.typeface != null) {
            mLabel.setTypeface(tk.typeface);
        }
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        applyType();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        skin().drawField(canvas, 0, 0, getWidth(), getHeight(), false);
    }
}
