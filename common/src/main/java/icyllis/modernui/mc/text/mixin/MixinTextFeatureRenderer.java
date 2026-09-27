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

import icyllis.modernui.mc.text.GlyphManager;
import icyllis.modernui.mc.text.TextLayoutEngine;
import icyllis.modernui.mc.text.TextRenderType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MC 26.2 removed {@code Font#drawInBatch*}: all 3D world text (entity name tags, signs,
 * text displays, maps, ...) is now rendered by {@code TextFeatureRenderer} using the glyphs
 * returned by {@code Font#prepareText}.
 * <p>
 * Those glyphs are baked by {@link icyllis.modernui.mc.text.StandardFontSet} into our own A8
 * font atlas, but they carry the vanilla render types, which use point sampling and have no
 * distance-field reconstruction, so 3D world text lost the anti-aliasing that the Modern Text
 * Engine provides. This redirects the render type selection to the Modern Text Engine 3D world
 * render types (SDF fill with bilinear sampling), see
 * {@link TextRenderType#getModernWorldType(Font.DisplayMode)}.
 * <p>
 * This is the new text pipeline equivalent of the old {@code MixinFontRenderer} overwrites of
 * {@code Font#drawInBatch*}, and it covers every 3D world text path, because they all go
 * through this feature renderer.
 *
 * @author BloCamLimb
 */
@Mixin(targets = "net.minecraft.client.renderer.feature.TextFeatureRenderer$GlyphRenderer")
public class MixinTextFeatureRenderer {

    @Redirect(method = "acceptRenderable",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/TextRenderable;renderType" +
                            "(Lnet/minecraft/client/gui/Font$DisplayMode;)" +
                            "Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType onRenderType(TextRenderable renderable, Font.DisplayMode mode) {
        RenderType renderType = renderable.renderType(mode);
        if (!TextLayoutEngine.sUseTextShadersInWorld) {
            // OptiFine/Iris shaders are active, vanilla pipelines are required
            return renderType;
        }
        AbstractTexture fontTexture = GlyphManager.getInstance().getFontTexture();
        if (fontTexture == null || renderable.textureView() != fontTexture.getTextureView()) {
            // not a glyph from our own font atlas (bitmap font, emoji, ...)
            return renderType;
        }
        return TextRenderType.getModernWorldType(mode);
    }
}
