package icyllis.modernui.mc.mixin;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the backend behind Minecraft's {@code GpuDevice} facade.
 * <p>
 * The class tweaker entry for this field only works on Fabric: loom silently drops class tweaker
 * entries outside {@code net.minecraft} when generating the NeoForge access transformer
 * (the produced {@code META-INF/accesstransformer.cfg} only contains the {@code net.minecraft.*}
 * lines), which caused an {@code IllegalAccessError} on NeoForge. A mixin accessor works on both.
 */
@Mixin(GpuDevice.class)
public interface AccessGpuDevice {

    @Accessor("backend")
    GpuDeviceBackend getBackend();
}
