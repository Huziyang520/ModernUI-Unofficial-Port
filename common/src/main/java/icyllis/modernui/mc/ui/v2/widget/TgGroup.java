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
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.mc.ui.v2.skin.SkinTokens;
import icyllis.modernui.widget.FrameLayout;

/**
 * Base of the controls that need a real {@link icyllis.modernui.widget.TextView} for their
 * label, because the canvas has no text primitive. Chrome is drawn by the skin in
 * {@code onDraw}, the label is an ordinary child laid out on top.
 *
 * <p>A frame, so a control may stack its chrome and its label. Controls that lay their
 * label and value out <em>side by side</em> must not rely on weights here: a frame drops
 * them and collapses the weighted child to zero width. Those controls nest a
 * {@link LinearLayout} row instead.</p>
 */
public abstract class TgGroup extends FrameLayout implements SkinRegistry.Listener {

    private Skin mSkin;

    protected TgGroup(@NonNull Context context) {
        super(context);
        setWillNotDraw(false);
        setClipChildren(false);
    }

    @NonNull
    public final Skin skin() {
        var skin = mSkin;
        if (skin == null) {
            skin = SkinRegistry.get(getContext());
            mSkin = skin;
        }
        return skin;
    }

    @NonNull
    protected final SkinTokens tokens() {
        return skin().tokens();
    }

    @Override
    public void onSkinChanged() {
        mSkin = null;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        SkinRegistry.addListener(this);
        // a style switch while detached leaves the cached skin stale
        mSkin = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        SkinRegistry.removeListener(this);
        super.onDetachedFromWindow();
    }
}
