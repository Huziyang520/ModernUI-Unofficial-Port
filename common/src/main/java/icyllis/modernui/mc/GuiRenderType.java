/*
 * Modern UI.
 * Copyright (C) 2024-2025 BloCamLimb. All rights reserved.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.ApiStatus;

/**
 * Modern GUI.
 */
@ApiStatus.Internal
public abstract class GuiRenderType {

    public static final RenderPipeline PIPELINE_TOOLTIP = buildTooltipPipeline();

    /**
     * MC 26.2 renders through an explicit bind-group model: a pipeline must declare the
     * uniform blocks it consumes, otherwise the engine reports them as "unknown and
     * unsupported uniform" and the tooltip background silently disappears.
     * <p>
     * We inherit vanilla's GUI bind groups (Projection / DynamicTransforms / samplers) and
     * append our own {@code ModernTooltip} block, which is filled in by
     * {@code MixinGuiRenderer#onExecuteDrawRange}.
     */
    private static RenderPipeline buildTooltipPipeline() {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(ModernUIMod.location("pipeline/modern_tooltip"))
                .withVertexShader(ModernUIMod.location("core/rendertype_modern_tooltip"))
                .withFragmentShader(ModernUIMod.location("core/rendertype_modern_tooltip"))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS);
        for (BindGroupLayout layout : RenderPipelines.GUI.getBindGroupLayouts()) {
            builder.withBindGroupLayout(layout);
        }
        builder.withBindGroupLayout(BindGroupLayout.builder()
                .withUniform("ModernTooltip", UniformType.UNIFORM_BUFFER)
                .build());
        return builder.build();
    }
}
