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

package icyllis.modernui.mc;

import java.util.List;

/**
 * Typeface defaults of the regular build, which always carries the bundled font pack.
 * The lite edition replaces this class because it ships no fonts at all.
 */
public final class FontDefaults {

    public static final String FIRST_FAMILY = "Inter Frozen Medium";

    public static final List<String> FALLBACK_FAMILIES = List.of("Source Han Sans CN Medium");

    /** The regular build bundles the emoji images, so the option may be on by default. */
    public static final boolean USE_COLOR_EMOJI = true;

    private FontDefaults() {
    }

    /**
     * Maps the configured first family to the family that is actually loaded. The
     * bundled families are always registered here, so the configured value is used as is.
     */
    public static String resolveFirstFamily(String configured) {
        return configured;
    }
}
