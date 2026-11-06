package icyllis.modernui.mc.mixin;

import icyllis.modernui.mc.GuiDispatchGuard;
import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Gui.setScreen is the single funnel every screen change goes through, including the
 * direct calls this mod makes and Minecraft.setScreenAndShow, which delegates to it.
 * <p>
 * {@link MixinMinecraft} also listens on setScreenAndShow, so without the guard below a
 * change made through that method would be reported twice: the listeners would run twice
 * per change, and the second run sees the screen already swapped in and reads it as no
 * change at all.
 */
@Mixin(Gui.class)
public abstract class MixinGui implements GuiDispatchGuard {

    @Shadow
    @Nullable
    private Screen screen;

    @Unique
    @Nullable
    private Screen modernUI_MC$previousScreen;

    @Unique
    private boolean modernUI_MC$dispatching;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreenHead(Screen guiScreen, CallbackInfo ci) {
        modernUI_MC$previousScreen = screen;
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onSetScreenTail(Screen guiScreen, CallbackInfo ci) {
        if (modernUI_MC$dispatching) {
            // the outer dispatch already reported this change
            modernUI_MC$previousScreen = null;
            return;
        }
        modernUI_MC$dispatching = true;
        try {
            MuiModApi.dispatchOnScreenChange(modernUI_MC$previousScreen, guiScreen);
        } finally {
            modernUI_MC$dispatching = false;
            modernUI_MC$previousScreen = null;
        }
    }

    @Override
    public void modernUI_MC$suppressNextDispatch() {
        modernUI_MC$dispatching = true;
    }

    @Override
    public void modernUI_MC$restoreDispatch() {
        modernUI_MC$dispatching = false;
    }
}
