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
import org.jetbrains.annotations.ApiStatus;

/**
 * Modern GUI.
 */
@ApiStatus.Internal
public abstract class GuiRenderType {

    /**
     * The tooltip shader declares exactly three std140 uniform blocks: the two vanilla
     * ones it imports (DynamicTransforms, Projection) plus its own ModernTooltip block
     * which is filled in by {@code MixinGuiRenderer#onExecuteDrawRange}.
     * <p>
     * MC 26.2 replaced the old {@code withUniform(name, type)} builder call with an
     * explicit bind group layout, so the three blocks must be declared here in the same
     * order as the vanilla 26.1 definition. Declaring anything else (e.g. inheriting
     * vanilla's GUI layout with its samplers) shifts the uniform slots and the tooltip
     * background silently disappears.
     */
    public static final RenderPipeline PIPELINE_TOOLTIP = RenderPipeline.builder()
            .withLocation(ModernUIMod.location("pipeline/modern_tooltip"))
            .withVertexShader(ModernUIMod.location("core/rendertype_modern_tooltip"))
            .withFragmentShader(ModernUIMod.location("core/rendertype_modern_tooltip"))
            .withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withUniform("ModernTooltip", UniformType.UNIFORM_BUFFER)
                    .build())
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();
}
