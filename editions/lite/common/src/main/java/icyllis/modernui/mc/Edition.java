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

package icyllis.modernui.mc;

import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.mc.ui.lite.YaclBridge;
import net.minecraft.client.gui.screens.Screen;

/**
 * Lite settings are rendered by the config library the player installs, so every option page
 * of the operation center only hands over to it.
 */
public final class Edition {

    public static final boolean EXTERNAL_CONFIG_UI = true;

    private Edition() {
    }

    public static boolean usesExternalConfigUi() {
        return EXTERNAL_CONFIG_UI;
    }

    public static boolean hasExternalConfigUi() {
        return YaclBridge.isAvailable();
    }

    @Nullable
    public static Screen createExternalConfigScreen(@Nullable Screen parent) {
        return YaclBridge.open(parent);
    }
}