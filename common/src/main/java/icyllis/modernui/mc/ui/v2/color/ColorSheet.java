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

package icyllis.modernui.mc.ui.v2.color;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.mc.ui.v2.widget.TgButton;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.PopupWindow;
import icyllis.modernui.widget.ScrollView;
import net.minecraft.client.resources.language.I18n;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Floating host for {@link ColorStudio}: the popup, the scroll container and the
 * apply/cancel footer. The studio itself stays a plain view that any screen may embed.
 *
 * <p>The studio commits on every gesture end, so {@code apply} only closes the sheet.
 * {@code cancel} restores the colour the sheet opened with and closes it.</p>
 */
public final class ColorSheet {

    private static final float WIDTH_DP = 320f;
    private static final float MAX_HEIGHT_DP = 470f;

    private final PopupWindow mPopup;
    private final ColorStudio mStudio;
    private final int mInitialColor;
    private final ColorStudio.OnColor mListener;

    private ColorSheet(@NonNull View anchor, int initialColor, boolean alpha,
                       @NonNull ColorStudio.OnColor listener) {
        Context context = anchor.getContext();
        mInitialColor = initialColor;
        mListener = listener;

        var content = new SheetSurface(context);
        content.setOrientation(LinearLayout.VERTICAL);

        mStudio = new ColorStudio(context);
        mStudio.setPanelBackground(false);
        mStudio.setAlphaEnabled(alpha);
        mStudio.setColor(initialColor);
        mStudio.setOnColor(listener);

        var scroll = new ScrollView(context);
        scroll.addView(mStudio, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        content.addView(scroll, new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f));

        var footer = new LinearLayout(context);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        int gap = Math.round(anchor.dp(8f));

        var cancel = new TgButton(context, tr("mui.v2.color.cancel", "Cancel"));
        var cancelParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        cancelParams.setMargins(Math.round(anchor.dp(16f)), gap,
                Math.round(anchor.dp(4f)), Math.round(anchor.dp(14f)));
        cancel.setOnAction(() -> {
            mListener.onPreview(mInitialColor);
            mListener.onCommit(mInitialColor);
            dismiss();
        });
        footer.addView(cancel, cancelParams);

        var apply = new TgButton(context, tr("mui.v2.color.apply", "Apply"));
        var applyParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        applyParams.setMargins(Math.round(anchor.dp(4f)), gap,
                Math.round(anchor.dp(16f)), Math.round(anchor.dp(14f)));
        apply.setOnAction(this::dismiss);
        footer.addView(apply, applyParams);

        content.addView(footer, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        mPopup = new PopupWindow(content);
        mPopup.setFocusable(true);
        mPopup.setOutsideTouchable(true);
        mPopup.setElevation(anchor.dp(12f));
        mPopup.setWidth(Math.round(anchor.dp(WIDTH_DP)));
        mPopup.setHeight(Math.min(Math.round(anchor.dp(MAX_HEIGHT_DP)),
                mPopup.getMaxAvailableHeight(anchor)));
    }

    /**
     * Opens the studio under {@code anchor}.
     *
     * @param alpha    whether the opacity bar and the alpha channel of the hex are editable
     * @param listener receives live previews and the settled colour
     */
    @NonNull
    public static ColorSheet show(@NonNull View anchor, int initialColor, boolean alpha,
                                  @NonNull ColorStudio.OnColor listener) {
        var sheet = new ColorSheet(anchor, initialColor, alpha, listener);
        sheet.mPopup.showAsDropDown(anchor, 0, Math.round(anchor.dp(4f)), Gravity.TOP);
        return sheet;
    }

    @NonNull
    public ColorStudio studio() {
        return mStudio;
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    public void dismiss() {
        mPopup.dismiss();
    }

    @NonNull
    private static String tr(@NonNull String key, @NonNull String fallback) {
        String text = I18n.get(key);
        return text.equals(key) ? fallback : text;
    }

    /** Panel background for the popup, painted with the active skin. */
    private static final class SheetSurface extends LinearLayout {

        SheetSurface(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            var skin = SkinRegistry.get(getContext());
            skin.drawPanel(canvas, 0, 0, getWidth(), getHeight(), skin.tokens().radiusPanel);
        }
    }
}
