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
package icyllis.modernui.mc.neoforge;

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
}