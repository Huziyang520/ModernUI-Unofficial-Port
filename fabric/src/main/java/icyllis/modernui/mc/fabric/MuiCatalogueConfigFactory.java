/*
 * Modern UI.
 * Copyright (C) 2019-2026 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.fabric;

import icyllis.modernui.mc.ui.CenterFragment2;
import icyllis.modernui.util.DataSet;
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
        var args = new DataSet();
        args.putBoolean("navigateToPreferences", true);
        var fragment = new CenterFragment2();
        fragment.setArguments(args);
        return MuiFabricApi.get().createScreen(fragment, null, parent);
    }
}
