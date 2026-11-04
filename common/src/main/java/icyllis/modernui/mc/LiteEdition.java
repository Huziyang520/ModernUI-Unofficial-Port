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

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * The lite build carries an extra class the regular build does not contain, so the lookup
 * is done once here instead of being a hard reference that would break the regular build.
 */
public final class LiteEdition {

    private static final String GREETING = "icyllis.modernui.mc.ui.lite.LiteGreeting";

    private static Method sTick;
    private static boolean sResolved;

    private LiteEdition() {
    }

    public static void onClientTick() {
        Method tick = resolve();
        if (tick != null) {
            try {
                tick.invoke(null);
            } catch (Throwable t) {
                // A greeting must never disturb the tick loop; drop it and keep going.
                sTick = null;
                ModernUIMod.LOGGER.warn("Dropped the lite greeting: {}", t.toString());
            }
        }
    }

    @Nullable
    private static Method resolve() {
        if (!sResolved) {
            sResolved = true;
            try {
                var type = Class.forName(GREETING, true, LiteEdition.class.getClassLoader());
                sTick = type.getMethod("tick");
            } catch (Throwable ignored) {
                // Not the lite build.
            }
        }
        return sTick;
    }
}
