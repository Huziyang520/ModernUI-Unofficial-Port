/*
 * Modern UI.
 * Copyright (C) 2025 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.mixin;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.commands.RenderPass;
import icyllis.modernui.mc.TooltipRenderer;
import icyllis.modernui.mc.UIManager;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public class MixinGuiRenderer {

    /**
     * MC 26.2 changed {@code executeDrawRange} from 3 parameters to 5 by appending
     * {@code int startIndex, int endIndex}, so the handler must declare those two
     * parameters as well. Declaring the 26.1 leftovers (GpuBufferSlice/GpuBuffer/Object/
     * two ints) makes Mixin choke on the local variable table ("incompatible changes"),
     * silently passing a wrong/null renderPass, and the tooltip background never gets
     * its ModernTooltip uniform. Only {@code renderPass} (slot 6) is needed here.
     * <p>
     * MC 26.3: {@code RenderPass} moved from {@code com.mojang.blaze3d.systems} to
     * {@code com.mojang.renderpearl.api.commands}, so the {@code @At} target descriptor
     * must be updated too. A stale literal here does not fail the build and does not
     * abort the game; the injector silently never fires, the ModernTooltip UBO is never
     * bound, and the first tooltip draw dies with a NPE in {@code GlCommandEncoder.setupDraw}.
     */
    @Inject(method = "executeDrawRange",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindDefaultUniforms" +
                    "(Lcom/mojang/renderpearl/api/commands/RenderPass;)V", shift = At.Shift.AFTER, remap = false),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onExecuteDrawRange(Supplier<String> $$0, RenderTarget $$1, GpuBufferSlice $$2,
                                    int $$3, int $$4, CallbackInfo ci, RenderPass renderPass) {
        if (TooltipRenderer.sTooltip) {
            GpuBufferSlice tooltipUniforms = UIManager.getInstance().mTooltipRenderer.mUniforms;
            if (tooltipUniforms != null) {
                renderPass.setUniform("ModernTooltip", tooltipUniforms);
            }
        }
    }
}
