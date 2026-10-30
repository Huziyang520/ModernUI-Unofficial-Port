/*
 * Modern UI.
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

package icyllis.modernui.mc.ui;

import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;

/**
 * Typed access to the appearance options. Every read falls back to the default when
 * the config is not loaded yet, so callers never have to guard against a null item.
 */
public final class SurfacePrefs {

    public static final String DEFAULT_THEME_COLOR = "";

    private SurfacePrefs() {
    }

    public static boolean compactLayout() {
        try {
            return Config.CLIENT.mInterfaceCompactLayout.get();
        } catch (Throwable e) {
            return false;
        }
    }

    public static void setCompactLayout(boolean value) {
        try {
            Config.CLIENT.mInterfaceCompactLayout.set(value);
        } catch (Throwable ignored) {
        }
    }

    public static boolean animations() {
        try {
            return Config.CLIENT.mInterfaceAnimations.get();
        } catch (Throwable e) {
            return true;
        }
    }

    public static void setAnimations(boolean value) {
        try {
            Config.CLIENT.mInterfaceAnimations.set(value);
        } catch (Throwable ignored) {
        }
    }

    public static String themeColor() {
        try {
            var value = Config.CLIENT.mInterfaceThemeColor.get();
            return value != null ? value : DEFAULT_THEME_COLOR;
        } catch (Throwable e) {
            return DEFAULT_THEME_COLOR;
        }
    }

    public static void setThemeColor(String value) {
        try {
            Config.CLIENT.mInterfaceThemeColor.set(value == null ? DEFAULT_THEME_COLOR : value);
        } catch (Throwable ignored) {
        }
    }
}
