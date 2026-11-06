package icyllis.modernui.mc.ui.lite;

import icyllis.modernui.mc.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Tells the player once per world join that this is the reduced build. The switch is a
 * normal config entry so the lite settings screen can turn it off.
 */
public final class LiteGreeting {

    private static boolean sShown;

    private LiteGreeting() {
    }

    public static void tick() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            // Reset on leaving the world, so the next join greets again.
            sShown = false;
            return;
        }
        if (sShown) {
            return;
        }
        sShown = true;
        var item = Config.CLIENT.mLiteNotice;
        if (item != null && !item.get()) {
            return;
        }
        minecraft.gui.hud.getChat().addClientSystemMessage(
                Component.translatableWithFallback("mui.lite.notice",
                        "Modern UI Lite: you are playing the lite edition, which omits some " +
                                "content of the regular edition. This notice can be turned off " +
                                "in the settings screen (press Ctrl+K to open it)."));
    }
}
