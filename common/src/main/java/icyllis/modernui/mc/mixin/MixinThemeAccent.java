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

package icyllis.modernui.mc.mixin;

import icyllis.modernui.mc.ui.AccentPalette;
import icyllis.modernui.resources.ResourceId;
import icyllis.modernui.resources.TypedValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a player-chosen accent color reach every widget that resolves a tonal role.
 * Library classes are not remapped, so both the config and the injections opt out.
 */
@Mixin(targets = "icyllis.modernui.resources.Resources$Theme", remap = false)
public class MixinThemeAccent {

    @Inject(method = "resolveAttribute(Ljava/lang/String;Ljava/lang/String;Licyllis/modernui/resources/TypedValue;Z)Z",
            at = @At("RETURN"), require = 1, remap = false)
    private void modernui$applyAccentByName(String namespace, String name, TypedValue value,
                                            boolean resolveRefs,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            modernui$override(name, value);
        }
    }

    @Inject(method = "resolveAttribute(Licyllis/modernui/resources/ResourceId;Licyllis/modernui/resources/TypedValue;Z)Z",
            at = @At("RETURN"), require = 1, remap = false)
    private void modernui$applyAccentById(ResourceId id, TypedValue value, boolean resolveRefs,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && id != null) {
            modernui$override(id.entry(), value);
        }
    }

    private static void modernui$override(String name, TypedValue value) {
        if (value == null || name == null) {
            return;
        }
        if (value.type < TypedValue.TYPE_FIRST_COLOR_INT || value.type > TypedValue.TYPE_LAST_COLOR_INT) {
            return;
        }
        int accent = AccentPalette.resolve(name, value.data);
        if (accent != AccentPalette.NO_OVERRIDE) {
            value.data = accent;
        }
    }
}
