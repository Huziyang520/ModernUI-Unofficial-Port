/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
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

import icyllis.modernui.mc.text.*;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nonnull;

@Mixin(Font.class)
public abstract class MixinFontRenderer {

    @Unique
    private final ModernTextRenderer modernUI_MC$textRenderer =
            TextLayoutEngine.getInstance().getTextRenderer();

    @Redirect(method = "<init>", at = @At(value = "NEW",
            target = "(Lnet/minecraft/client/StringSplitter$WidthProvider;)Lnet/minecraft/client/StringSplitter;"))
    private StringSplitter onNewSplitter(StringSplitter.WidthProvider widthProvider) {
        return new ModernStringSplitter(TextLayoutEngine.getInstance(), widthProvider);
    }

    // MC 26.2: Font.drawInBatch* methods were removed in the new text pipeline
    // (replaced by prepareText/PreparedText). The Modern Text Engine text
    // rendering layer is not yet wired to the new pipeline, so these overwrites
    // are disabled to keep the mod loadable.

    /**
     * Bidi and shaping always works no matter what language is in.
     * So we should analyze the original string without reordering.
     * Do not reorder, we have our layout engine.
     *
     * @author BloCamLimb
     * @reason Modern Text Engine
     */
    @Overwrite
    public String bidirectionalShaping(String text) {
        return text;
    }
}
