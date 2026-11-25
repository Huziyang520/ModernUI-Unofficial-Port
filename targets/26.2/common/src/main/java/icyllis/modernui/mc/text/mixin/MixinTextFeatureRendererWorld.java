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

import icyllis.modernui.mc.text.DirectGlyphVisitor;
import icyllis.modernui.mc.text.FeatureBufferSource;
import icyllis.modernui.mc.text.ModernWorldText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * MC 26.2 has no per-submission {@code renderText}; it builds one group and visits
 * already prepared glyphs. This replaces that group builder for the Modern Text
 * Engine path and keeps a byte-for-byte vanilla fallback for shader packs.
 */
@Mixin(TextFeatureRenderer.class)
public class MixinTextFeatureRendererWorld {

    @Overwrite
    protected void buildGroup(@Nonnull FeatureFrameContext context,
                              @Nonnull List<TextFeatureRenderer.Submit> submits) {
        Font font = context.font();
        TextFeatureRenderer self = (TextFeatureRenderer) (Object) this;
        FeatureBufferSource source = new FeatureBufferSource((RenderTypeFeatureRenderer<?>) self);
        for (var submit : submits) {
            if (ModernWorldText.draw(font, submit.string(), submit.x(), submit.y(),
                    submit.color(), submit.dropShadow(), submit.displayMode(), submit.lightCoords(),
                    submit.backgroundColor(), submit.outlineColor(), submit.pose(), source)) {
                continue;
            }
            if (submit.outlineColor() == 0) {
                font.prepareText(submit.string(), submit.x(), submit.y(), submit.color(),
                        submit.dropShadow(), false, submit.backgroundColor())
                        .visit(new DirectGlyphVisitor(source, submit.pose(), submit.lightCoords(),
                                submit.displayMode()));
            } else {
                font.prepare8xTextOutline(submit.string(), submit.x(), submit.y(), submit.outlineColor())
                        .visit(new DirectGlyphVisitor(source, submit.pose(), submit.lightCoords(),
                                Font.DisplayMode.NORMAL));
                font.prepareText(submit.string(), submit.x(), submit.y(), submit.color(),
                        false, false, 0)
                        .visit(new DirectGlyphVisitor(source, submit.pose(), submit.lightCoords(),
                                Font.DisplayMode.POLYGON_OFFSET));
            }
        }
    }
}
