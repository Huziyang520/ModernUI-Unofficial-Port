/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
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

import icyllis.modernui.mc.IModernGuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Captures the hovered stack so the tooltip renderer can look it up.
 * <p>
 * Reading the field instead of capturing a local keeps this free of any method descriptor:
 * a descriptor that does not match after remapping fails the whole screen class, and the
 * stack is passed to setTooltipForNextFrame as a parameter, so it is not even a local of
 * the method that was targeted before.
 */
@Mixin(AbstractContainerScreen.class)
public class MixinContainerScreen {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(method = "extractTooltip", at = @At("HEAD"))
    private void preRenderTooltip(GuiGraphicsExtractor gr, int x, int y, CallbackInfo ci) {
        Slot slot = hoveredSlot;
        if (slot != null && slot.hasItem()) {
            ((IModernGuiGraphics) gr).modernUI_MC$setTooltipStack(slot.getItem());
        }
    }

    @Inject(method = "extractTooltip", at = @At("TAIL"))
    private void postRenderTooltip(GuiGraphicsExtractor gr, int x, int y, CallbackInfo ci) {
        ((IModernGuiGraphics) gr).modernUI_MC$setTooltipStack(ItemStack.EMPTY);
    }
}
