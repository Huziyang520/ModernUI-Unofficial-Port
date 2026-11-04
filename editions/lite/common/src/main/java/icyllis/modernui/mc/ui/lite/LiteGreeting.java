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

import icyllis.modernui.mc.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Tells the player once per world join that this is the reduced build. The switch is a
 * normal config entry so the lite settings screen can turn it off.
 */
public final class LiteGreeting {

    private static boolean sShown;

    private LiteGreeting() {
    }

    public static void tick() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            // Reset on leaving the world, so the next join greets again.
            sShown = false;
            return;
        }
        if (sShown) {
            return;
        }
        sShown = true;
        var item = Config.CLIENT.mLiteNotice;
        if (item != null && !item.get()) {
            return;
        }
        minecraft.gui.hud.getChat().addClientSystemMessage(
                Component.translatableWithFallback("mui.lite.notice",
                        "Modern UI Lite: you are playing the lite edition, which omits some " +
                                "content of the regular edition. This notice can be turned off " +
                                "in the settings screen (press Ctrl+K to open it)."));
    }
}
