package icyllis.modernui.mc.fabric;

import icyllis.modernui.mc.ui.lite.YaclBridge;
import net.minecraft.client.gui.screens.Screen;

/**
 * Every entry point of this edition opens the config library directly; there is no
 * in-house settings screen to route through.
 */
public final class MuiConfigScreens {

    private MuiConfigScreens() {
    }

    public static Screen create(Screen parent) {
        var screen = YaclBridge.open(parent);
        // Without the library the button stays where it was: there is no fallback screen.
        return screen != null ? screen : parent;
    }

    /**
     * Ctrl+K goes straight to the config library, skipping the Fragment hand-off screen.
     */
    public static Screen createCenter() {
        return YaclBridge.open(null);
    }
}