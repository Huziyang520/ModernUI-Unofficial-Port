/*
 * Compatibility stub for MC 26.2.
 * MultiBufferSource was removed by Mojang in the new render pipeline.
 * This stub lets ModernUI's text layer compile; runtime text rendering
 * under the new pipeline is not yet wired up.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);

    interface BufferSource extends MultiBufferSource {
        void endBatch();
        void endBatch(RenderType renderType);
    }
}
