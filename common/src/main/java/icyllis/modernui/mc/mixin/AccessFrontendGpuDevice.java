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
