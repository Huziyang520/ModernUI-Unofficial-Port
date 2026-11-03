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
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Captures the hovered stack so the tooltip renderer can look it up.
 * <p>
 * MC 26.3 passes the stack to {@code setTooltipForNextFrame} as a parameter, so the target
 * descriptor has to contain it. A slightly different descriptor never matches, and with
 * {@code CAPTURE_FAILSOFT} the injector fails without saying so, which leaves the stack
 * empty and makes the tooltip lose its item context.
 */
@Mixin(AbstractContainerScreen.class)
public class MixinContainerScreen {

    private static final String TARGET = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame" +
            "(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;" +
            "Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/resources/Identifier;Z)V";

    @Inject(method = "extractTooltip",
            at = @At(value = "INVOKE", target = TARGET),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            require = 1)
    private void preRenderTooltip(GuiGraphicsExtractor gr, int x, int y, CallbackInfo ci, ItemStack stack) {
        ((IModernGuiGraphics) gr).modernUI_MC$setTooltipStack(stack);
    }

    @Inject(method = "extractTooltip",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = TARGET),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            require = 1)
    private void postRenderTooltip(GuiGraphicsExtractor gr, int x, int y, CallbackInfo ci, ItemStack stack) {
        ((IModernGuiGraphics) gr).modernUI_MC$setTooltipStack(ItemStack.EMPTY);
    }
}
