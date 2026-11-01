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
import icyllis.modernui.mc.ui.v2.widget.TgGroup;

/**
 * Single skin drawn glyph. The canvas has no text, so icons are either a
 * {@link icyllis.modernui.widget.TextView} or one of these.
 */
final class GlyphIcon extends TgGroup {

    private final int mGlyph;
    private boolean mAccent;

    GlyphIcon(@NonNull Context context, int glyph) {
        super(context);
        mGlyph = glyph;
    }

    void setAccent(boolean accent) {
        if (mAccent != accent) {
            mAccent = accent;
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        var tk = tokens();
        int color = mAccent ? tk.accent : tk.muted;
        float size = Math.min(getWidth(), getHeight()) * 0.82f;
        skin().drawGlyph(canvas, mGlyph, getWidth() * 0.5f, getHeight() * 0.5f, size, color);
    }
}
