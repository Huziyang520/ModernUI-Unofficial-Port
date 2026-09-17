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

package icyllis.modernui.mc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import icyllis.arc3d.core.RawPtr;
import icyllis.arc3d.engine.Swizzle;
import icyllis.arc3d.vulkan.VKUtil;
import icyllis.arc3d.vulkan.VulkanBackendContext;
import icyllis.arc3d.vulkan.VulkanImage;
import icyllis.arc3d.vulkan.VulkanImageView;
import icyllis.arc3d.vulkan.VulkanMemoryAllocator;
import icyllis.modernui.core.VulkanManager;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.MemoryManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;

import java.lang.reflect.Field;
import java.nio.LongBuffer;
import java.util.Objects;

@ApiStatus.Internal
public final class VulkanModIntegration {

    private static Field MAIN_IMAGE_VIEW;

    static {
        try {
            var f = net.vulkanmod.vulkan.texture.VulkanImage.class.getDeclaredField("mainImageView");
            f.setAccessible(true);
            MAIN_IMAGE_VIEW = f;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static VulkanBackendContext wrapContext() {
        VulkanBackendContext backendContext = new VulkanBackendContext();
        VulkanManager vulkanManager = VulkanManager.get();
        backendContext.mInstance = DeviceManager.physicalDevice.getInstance();
        backendContext.mPhysicalDevice = DeviceManager.physicalDevice;
        backendContext.mDevice = DeviceManager.vkDevice;
        var indices = net.vulkanmod.vulkan.queue.Queue.getQueueFamilies();
        backendContext.mGraphicsQueueIndex = indices.graphicsFamily;
        vulkanManager.setPhysicalDeviceFeatures2(VkPhysicalDeviceFeatures2.calloc());
        backendContext.mDeviceFeatures2 = vulkanManager.getPhysicalDeviceFeatures2();
        vulkanManager.setMemoryAllocator(VulkanMemoryAllocator.make(
                backendContext.mInstance, backendContext.mPhysicalDevice,
                backendContext.mDevice, DeviceManager.deviceProperties.apiVersion(),
                0
        ));
        backendContext.mMemoryAllocator = vulkanManager.getMemoryAllocator();
        backendContext.mQueue = DeviceManager.getGraphicsQueue().vkQueue();

        return backendContext;
    }

    // MC 26.3: renderpearl's GpuTextureView is no longer publicly accessible, so the
    // parameter is taken as Object. The body is a no-op (see below).
    // MC 26.3: renderpearl's GpuTextureView (the supertype of VulkanMod's VkTextureView)
    // is no longer publicly accessible, so this bridge cannot be compiled any more.
    public static void replaceMainImageViewWithSwizzle(Object textureView, short swizzle) {
        // disabled on MC 26.3
    }

    // caller must track Arc3D CommandBuffer usage and Client usage ref
    // caller must NOT close the returned object
    public static GpuTexture wrapTextureImageFromArc3D(@RawPtr VulkanImage arc3dVulkanImage) {
        // MC 26.3: GpuDevice#backend (and the renderpearl backend types) are no longer
        // accessible, so the VulkanMod texture bridge cannot be implemented anymore.
        // VulkanMod integration is compile-only and optional; this path stays unused.
        return null;
    }

    public static void syncImageLayoutFromArc3D(GpuTexture vulkanTexture, @RawPtr VulkanImage arc3dVulkanImage) {
        // MC 26.3: see wrapTextureImageFromArc3D - disabled.
    }

    public static void syncImageLayoutFromVulkan(GpuTexture vulkanTexture, @RawPtr VulkanImage arc3dVulkanImage) {
        // MC 26.3: VkGpuTexture's renderpearl supertype is no longer accessible - disabled.
    }

    public static boolean sameImage(GpuTexture vulkanTexture, @RawPtr VulkanImage arc3dVulkanImage) {
        // MC 26.3: VkGpuTexture's renderpearl supertype is no longer accessible - disabled.
        return false;
    }

    public static void addFrameOp(Runnable runnable) {
        MemoryManager.getInstance().addFrameOp(runnable);
    }
}
