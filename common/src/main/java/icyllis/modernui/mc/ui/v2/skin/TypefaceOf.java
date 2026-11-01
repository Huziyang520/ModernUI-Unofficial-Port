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

package icyllis.modernui.mc.ui.v2.skin;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.text.Typeface;

/**
 * Small typeface helpers shared by the styles that ask the platform for a named face.
 */
final class TypefaceOf {

    private TypefaceOf() {
    }

    @NonNull
    static Typeface mono() {
        for (String name : new String[]{"JetBrains Mono Medium", "Consolas", "Cascadia Mono"}) {
            var font = Typeface.getSystemFont(name);
            if (font != null) {
                return font;
            }
        }
        return Typeface.MONOSPACED;
    }

    @NonNull
    static Typeface serif() {
        for (String name : new String[]{"Georgia", "Times New Roman", "Cambria"}) {
            var font = Typeface.getSystemFont(name);
            if (font != null) {
                return font;
            }
        }
        return Typeface.SERIF;
    }
}
