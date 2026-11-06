package icyllis.modernui.mc.fabric;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Config screen factory for Catalogue (https://github.com/MrCrayfish/Catalogue).
 * <p>
 * Catalogue reads the class name from {@code fabric.mod.json}:
 * <pre>
 * "custom": {
 *   "catalogue": {
 *     "configFactory": "icyllis.modernui.mc.fabric.MuiCatalogueConfigFactory"
 *   }
 * }
 * </pre>
 * and then reflectively invokes the <b>public static</b> method
 * {@code Screen createConfigScreen(Screen, ModContainer)}.
 * <p>
 * Unlike {@link MuiModMenuApi}, this class does <b>not</b> reference any Mod Menu
 * class, so it still loads (and the Config button in Catalogue still works) on
 * Fabric instances where only Catalogue is installed and Mod Menu is absent.
 */
public final class MuiCatalogueConfigFactory {

    private MuiCatalogueConfigFactory() {
    }

    public static Screen createConfigScreen(Screen parent, ModContainer container) {
        return MuiConfigScreens.create(parent);
    }
}
