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
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.PopupWindow;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Consumer;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Floating list of the eight styles. Every thumbnail draws a miniature toggle, slider and
 * swatch with that style's own skin, so the preview is the real thing rather than a
 * description of it.
 */
public final class SkinPicker {

    private final PopupWindow mPopup;
    private final Consumer<String> mOnPicked;

    public SkinPicker(@NonNull View anchor, @NonNull Consumer<String> onPicked) {
        mOnPicked = onPicked;
        var context = anchor.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        int pad = Math.round(8f * density);

        var column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(pad, pad, pad, pad);
        column.setBackground(icyllis.modernui.mc.ui.v2.TgDrawable.rounded(context,
                SkinRegistry.get(context).tokens().panel, 12f));

        for (String id : SkinRegistry.IDS) {
            column.addView(createEntry(context, id, onPicked));
        }

        var scroll = new ScrollView(context);
        scroll.addView(column, new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        mPopup = new PopupWindow(scroll);
        mPopup.setFocusable(true);
        mPopup.setOutsideTouchable(true);
        mPopup.setElevation(anchor.dp(8f));
        mPopup.setWidth(Math.round(240f * density));
    }

    @NonNull
    private View createEntry(@NonNull Context context, @NonNull String id,
                             @NonNull Consumer<String> onPicked) {
        float density = context.getResources().getDisplayMetrics().density;
        var skin = SkinRegistry.create(context, id);

        var entry = new LinearLayout(context);
        entry.setOrientation(LinearLayout.HORIZONTAL);
        entry.setGravity(Gravity.CENTER_VERTICAL);
        entry.setClickable(true);
        entry.setFocusable(true);
        int padH = Math.round(8f * density);
        int padV = Math.round(6f * density);
        entry.setPadding(padH, padV, padH, padV);

        var preview = new Thumbnail(context, skin);
        entry.addView(preview, new LinearLayout.LayoutParams(Math.round(96f * density),
                Math.round(44f * density)));

        var label = new TextView(context);
        label.setText(I18n.get("mui.v2.style." + id));
        label.setTextSize(skin.tokens().titleSize);
        label.setTextColor(skin.tokens().text);
        label.setSingleLine(true);
        if (skin.tokens().typeface != null) {
            label.setTypeface(skin.tokens().typeface);
        }
        var labelParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        labelParams.setMargins(Math.round(10f * density), 0, 0, 0);
        entry.addView(label, labelParams);

        entry.setOnClickListener(v -> {
            mPopup.dismiss();
            onPicked.accept(id);
        });
        return entry;
    }

    public void show(@NonNull View anchor) {
        float density = anchor.getContext().getResources().getDisplayMetrics().density;
        mPopup.showAsDropDown(anchor, 0, Math.round(4f * density), Gravity.TOP);
    }

    public void dismiss() {
        mPopup.dismiss();
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    /**
     * Three real controls of one style, drawn with that style's own primitives: a switch, a
     * slider track and a swatch. No text, so the shapes carry the style.
     */
    private static final class Thumbnail extends View {

        private final Skin mSkin;

        Thumbnail(@NonNull Context context, @NonNull Skin skin) {
            super(context);
            mSkin = skin;
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            var tk = mSkin.tokens();
            float density = tk.density;
            float w = getWidth();
            float h = getHeight();

            var paint = Paint.obtain();
            paint.setAntiAlias(true);
            paint.setColor(tk.bg);
            canvas.drawRoundRect(0, 0, w, h, tk.radiusPanel * density, paint);
            paint.recycle();

            float inset = 8f * density;
            float toggleW = 30f * density;
            float toggleH = 16f * density;
            mSkin.drawToggle(canvas, inset, inset, inset + toggleW, inset + toggleH, 0.7f, true,
                    false);

            float sliderTop = h * 0.52f;
            mSkin.drawSliderTrack(canvas, inset, sliderTop, w - inset, sliderTop + 14f * density,
                    0.45f, false);

            float swatchSize = 12f * density;
            float swatchX = w - inset - swatchSize;
            float swatchY = h - inset - swatchSize;
            var swatchPaint = Paint.obtain();
            swatchPaint.setAntiAlias(true);
            swatchPaint.setColor(tk.accent);
            canvas.drawRoundRect(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize,
                    tk.radiusControl * density, swatchPaint);
            swatchPaint.recycle();
            mSkin.drawSwatchFrame(canvas, swatchX, swatchY, swatchX + swatchSize,
                    swatchY + swatchSize, tk.radiusControl);

            mSkin.drawField(canvas, inset, h - inset - swatchSize, w * 0.55f,
                    h - inset, false);
        }
    }
}
