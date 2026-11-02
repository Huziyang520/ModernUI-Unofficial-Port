/*
 * Modern UI.
 * Copyright (C) 2023-2026 BloCamLimb. All rights reserved.
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
import net.minecraft.client.gui.screens.Screen;

/**
 * Tells the shared sources which edition is being compiled. An edition overlay replaces this
 * file with the same members, so the shared code never links against edition-only classes.
 */
public final class Edition {

    /** {@code true} when an external library renders the settings screen. */
    public static final boolean EXTERNAL_CONFIG_UI = false;

    private Edition() {
    }

    /**
     * Read through a method on purpose: as a constant the value is folded at the call site, and
     * the branch of the other edition becomes unreachable code to javac.
     */
    public static boolean usesExternalConfigUi() {
        return EXTERNAL_CONFIG_UI;
    }

    /** Whether that library is present right now; the settings page must degrade without it. */
    public static boolean hasExternalConfigUi() {
        return false;
    }

    /** Builds the external settings screen, or returns {@code null} when it cannot be built. */
    @Nullable
    public static Screen createExternalConfigScreen(@Nullable Screen parent) {
        return null;
    }
}