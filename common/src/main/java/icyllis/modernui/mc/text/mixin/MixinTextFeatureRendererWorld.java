/*
 * Modern UI.
 * Copyright (C) 2019-2026 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.text.mixin;

import icyllis.modernui.mc.text.FeatureBufferSource;
import icyllis.modernui.mc.text.ModernWorldText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Coerce;

/**
 * MC 26.3 renders each text submission through {@code renderText}, before the glyph
 * renderer writes vanilla render types. Replacing the draw there lets the Modern Text
 * Engine bake SDF glyphs at its own density and write one modern world render type.
 */
@Mixin(TextFeatureRenderer.class)
public class MixinTextFeatureRendererWorld {

    @Inject(method = "renderText", at = @At("HEAD"), cancellable = true, require = 1)
    private static void modernui$renderText(Font font, @Coerce Object glyphRenderer,
                                            TextFeatureRenderer.Content.Text content,
                                            CallbackInfo ci) {
        GlyphRendererAccess access = (GlyphRendererAccess) glyphRenderer;
        FeatureBufferSource source = new FeatureBufferSource(access.modernui$getOwner());
        if (ModernWorldText.draw(font, content.string(), content.x(), content.y(),
                content.color(), content.dropShadow(), access.modernui$getDisplayMode(),
                access.modernui$getLightCoords(), content.backgroundColor(), content.outlineColor(),
                access.modernui$getPose(), source)) {
            ci.cancel();
        }
    }
}
