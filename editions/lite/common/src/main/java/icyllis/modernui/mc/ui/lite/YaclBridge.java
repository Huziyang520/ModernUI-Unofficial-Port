/*
 * Modern UI Lite.
 * Copyright (C) 2026 BloCamLimb. All rights reserved.
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