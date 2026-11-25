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
import icyllis.modernui.mc.text.mixin.RenderTypeFeatureRendererAccess;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Routes text vertices to the feature renderer that owns the current frame.
 * The feature renderer writes into its own buffer set, which is how vanilla
 * batches text without exposing a {@code MultiBufferSource}.
 */
public final class FeatureBufferSource implements WorldBufferSource {

    private final RenderTypeFeatureRendererAccess mRenderer;

    public FeatureBufferSource(RenderTypeFeatureRenderer<?> renderer) {
        mRenderer = (RenderTypeFeatureRendererAccess) renderer;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return mRenderer.modernui$getVertexBuilder(renderType);
    }
}
