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

import icyllis.modernui.mc.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    /*@Shadow
    @Final
    private Window window;

    @Shadow
    public abstract boolean isWindowActive();*/

    /**
     * Forge breaks the event, see
     * <a href="https://github.com/MinecraftForge/MinecraftForge/issues/8992">this issue</a>
     * MC 26.2: Minecraft.screen field and setScreen method were moved to Gui,
     * the method is now setScreenAndShow.
     */
    @Inject(method = "setScreenAndShow", at = @At("HEAD"))
    private void onSetScreen(Screen guiScreen, CallbackInfo ci) {
        MuiModApi.dispatchOnScreenChange(
                ((Minecraft) (Object) this).gui.screen(),
                guiScreen);
    }

    @Inject(method = "onGameLoadFinished", at = @At("HEAD"))
    private void beforeGameLoadFinished(@Coerce Object cookie, CallbackInfo ci) {
        try {
            // due to config loading timing and possible parallel loading errors,
            // we only try to apply configs just before the game is loaded.
            if (Config.CLIENT.mLoaded) {
                Config.CLIENT.apply();
            }
            if (Config.TEXT.mLoaded) {
                Config.TEXT.apply();
            }
        } catch (Throwable e) {
            // happen in broken mod state
            ModernUIMod.LOGGER.error(ModernUIMod.MARKER, "Failed to apply configs", e);
        }
    }

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void onGameLoadFinished(@Coerce Object cookie, CallbackInfo ci) {
        try {
            UIManager.getInstance().onGameLoadFinished();
        } catch (Throwable e) {
            // happen in broken mod state
            ModernUIMod.LOGGER.error(ModernUIMod.MARKER, "Failed to load game", e);
        }
    }

    @Inject(method = "allowsTelemetry", at = @At("HEAD"), cancellable = true)
    private void onAllowsTelemetry(CallbackInfoReturnable<Boolean> info) {
        if (ModernUIClient.sRemoveTelemetrySession) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/DeltaTracker$Timer;advanceRealTime(J)V"))
    private void onStartRenderFrameUpdate(boolean advanceGameTime, CallbackInfo ci) {
        MuiModApi.dispatchOnRenderFrame(0, MuiModApi.RENDER_STAGE_UPDATE);
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;extract(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void onStartRenderFrameExtract(boolean advanceGameTime, CallbackInfo ci) {
        MuiModApi.dispatchOnRenderFrame(0, MuiModApi.RENDER_STAGE_EXTRACT);
    }

    // MC 26.3: GameRenderer.render() no longer takes the DeltaTracker / advanceGameTime
    // arguments, so the @At descriptor must be updated; otherwise this injector silently
    // never fires and RENDER_STAGE_RENDER is never dispatched.
    @Inject(method = "renderFrame", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;render()V"))
    private void onStartRenderFrameRender(boolean advanceGameTime, CallbackInfo ci) {
        MuiModApi.dispatchOnRenderFrame(0, MuiModApi.RENDER_STAGE_RENDER);
    }

    // MC 26.2: RenderSystem.flipFrame() was replaced by GpuSurface.present()
    // MC 26.3: GpuSurface moved from com.mojang.blaze3d.systems to
    // com.mojang.renderpearl.api.device, so the @At descriptor must be updated;
    // otherwise this injector silently never fires and RENDER_STAGE_PRESENT is
    // never dispatched (Fabric would then never call Core.flushMainCalls()).
    @Inject(method = "renderFrame", at = @At(value = "INVOKE",
            target = "Lcom/mojang/renderpearl/api/device/GpuSurface;present()V"))
    private void onStartRenderFramePresent(boolean advanceGameTime, CallbackInfo ci) {
        MuiModApi.dispatchOnRenderFrame(0, MuiModApi.RENDER_STAGE_PRESENT);
    }

    /*@Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> info) {
        if ((BlurHandler.sFramerateInactive != 0 ||
                BlurHandler.sFramerateMinimized != 0) &&
                !isWindowActive()) {
            if (BlurHandler.sFramerateMinimized != 0 &&
                    BlurHandler.sFramerateMinimized < BlurHandler.sFramerateInactive &&
                    GLFW.glfwGetWindowAttrib(window.getWindow(), GLFW.GLFW_ICONIFIED) != 0) {
                info.setReturnValue(Math.min(
                        BlurHandler.sFramerateMinimized,
                        window.getFramerateLimit()
                ));
            } else if (BlurHandler.sFramerateInactive != 0) {
                info.setReturnValue(Math.min(
                        BlurHandler.sFramerateInactive,
                        window.getFramerateLimit()
                ));
            }
        }
    }*/

    // MC 26.3: Minecraft.close() calls RenderSystem.shutdownRenderer() (which destroys the GPU
    // device) BEFORE Util.shutdownExecutors(), so injecting at the latter point made Modern UI
    // release its Arc3D Vulkan resources on a destroyed VkDevice -> EXCEPTION_ACCESS_VIOLATION
    // inside lwjgl_vma.dll. Tear down at the very beginning of close() instead (the order was the
    // opposite in 26.2, and HEAD is correct for both).
    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        UIManager.destroy();
    }
}
