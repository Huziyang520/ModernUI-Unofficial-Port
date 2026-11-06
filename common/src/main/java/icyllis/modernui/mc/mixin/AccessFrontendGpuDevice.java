package icyllis.modernui.mc.mixin;

import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the backend behind Minecraft's {@code GpuDevice} facade.
 * <p>
 * An access widener entry would do the same on Fabric, but loom silently drops class tweaker
 * entries outside {@code net.minecraft} when generating the NeoForge access transformer
 * (verified: the produced {@code META-INF/accesstransformer.cfg} only contains the
 * {@code net.minecraft.*} lines), which caused an {@code IllegalAccessError} on NeoForge.
 * A mixin accessor works on both loaders.
 */
@Mixin(FrontendGpuDevice.class)
public interface AccessFrontendGpuDevice {

    @Accessor("backend")
    GpuDeviceBackend getBackend();
}
