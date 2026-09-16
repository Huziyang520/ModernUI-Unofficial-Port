/*
 * Modern UI.
 * Accessor for GuiTextRenderState private text field in MC 26.2.
 */
package icyllis.modernui.mc.text.mixin;

import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextRenderState.class)
public interface IGuiTextRenderStateAccessor {
    @Accessor("text")
    FormattedCharSequence mui$getText();
}
