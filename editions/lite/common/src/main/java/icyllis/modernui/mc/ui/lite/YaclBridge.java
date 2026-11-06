package icyllis.modernui.mc.ui.lite;

import icyllis.modernui.mc.ModernUIMod;
import net.minecraft.client.gui.screens.Screen;

/**
 * Reaches YetAnotherConfigLib reflectively. The lite edition only recommends that mod,
 * so a direct reference here would turn a missing dependency into a hard crash.
 */
public final class YaclBridge {

    private static final String YACL_API = "dev.isxander.yacl3.api.YetAnotherConfigLib";
    private static final String SCREEN = "icyllis.modernui.mc.ui.lite.YaclSettings";

    private YaclBridge() {
    }

    public static boolean isAvailable() {
        try {
            Class.forName(YACL_API, false, YaclBridge.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Returns the YACL settings screen, or {@code null} when the library is absent or
     * failed to build the screen. Callers must handle the null case.
     */
    public static Screen open(Screen parent) {
        if (!isAvailable()) {
            ModernUIMod.LOGGER.warn("YetAnotherConfigLib is not installed, " +
                    "so this build has no settings screen");
            return null;
        }
        try {
            var type = Class.forName(SCREEN);
            return (Screen) type.getMethod("create", Screen.class).invoke(null, parent);
        } catch (Throwable t) {
            ModernUIMod.LOGGER.error("Failed to build the YetAnotherConfigLib screen", t);
            return null;
        }
    }
}