/*
 * Modern UI.
 * Copyright (C) 2019-2026 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.text;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;

/**
 * Writes prepared glyphs straight into the feature renderer's buffers.
 * Needed by the 26.2 {@code buildGroup} fallback, which has no {@code GlyphRenderer}
 * instance of its own.
 */
public final class DirectGlyphVisitor implements Font.GlyphVisitor {

    private final WorldBufferSource mSource;
    private final Matrix4fc mPose;
    private final int mLightCoords;
    private final Font.DisplayMode mDisplayMode;

    public DirectGlyphVisitor(@Nonnull WorldBufferSource source, @Nonnull Matrix4fc pose,
                              int lightCoords, @Nonnull Font.DisplayMode displayMode) {
        mSource = source;
        mPose = pose;
        mLightCoords = lightCoords;
        mDisplayMode = displayMode;
    }

    @Override
    public void acceptRenderable(@Nonnull TextRenderable renderable) {
        VertexConsumer buffer = mSource.getBuffer(renderable.renderType(mDisplayMode));
        renderable.render(mPose, buffer, mLightCoords, false);
    }
}
