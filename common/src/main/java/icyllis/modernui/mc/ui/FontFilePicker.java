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

import icyllis.modernui.mc.ModernUIMod;

/**
 * Reflective facade over the native file dialog. The dialog is only present from
 * 26.3 on, so 26.2 must not reference the class at compile time.
 */
public final class FontFilePicker {

    private FontFilePicker() {
    }

    /**
     * Returns the chosen path, or {@code null} when the dialog is absent, cancelled or
     * the platform cannot open one.
     */
    public static String openFontFile() {
        try {
            Class<?> dialogs = Class.forName("icyllis.modernui.mc.FileDialogs");
            var method = dialogs.getMethod("openFileDialog", String.class, String.class,
                    String[].class);
            return (String) method.invoke(null,
                    "TrueType/OpenType Fonts (*.ttf;*.otf;*.ttc;*.otc)",
                    new String[]{"ttf", "otf", "ttc", "otc"});
        } catch (ClassNotFoundException e) {
            ModernUIMod.LOGGER.warn("Native file dialogs are not available on this version; " +
                    "enter the font path manually");
        } catch (Exception e) {
            ModernUIMod.LOGGER.warn("Failed to open the font file dialog", e);
        }
        return null;
    }
}
