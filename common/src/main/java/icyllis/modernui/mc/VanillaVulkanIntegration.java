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
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import icyllis.arc3d.core.RawPtr;
import icyllis.modernui.mc.mixin.AccessFrontendGpuDevice;
import icyllis.arc3d.engine.Swizzle;
import icyllis.arc3d.vulkan.VKUtil;
import icyllis.arc3d.vulkan.VulkanBackendContext;
import icyllis.arc3d.vulkan.VulkanImage;
import icyllis.arc3d.vulkan.VulkanImageDesc;
import icyllis.arc3d.vulkan.VulkanMemoryAllocator;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.ArrayDeque;
import java.util.Deque;

import static icyllis.modernui.mc.ModernUIMod.LOGGER;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Bridge between Arc3D's Vulkan backend and Minecraft's built-in Vulkan backend
 * ("Graphics API: Prefer Vulkan", available since MC 26.2).
 * <p>
 * Unlike {@link VulkanModIntegration}, Minecraft never exposes a way to import a foreign
 * {@code VkImage}, and its backend keeps <b>every</b> image in {@code VK_IMAGE_LAYOUT_GENERAL}
 * (sampled descriptors and color attachments both declare that layout, and no layout transition
 * is recorded for sampling). Therefore the UI layer that Arc3D renders is copied GPU to GPU
 * into a texture owned by the game's GPU device, and that texture is what Minecraft draws with
 * its own GUI pipeline.
 * <p>
 * This class links renderpearl internals, so it must only be touched when the game really runs
 * on its built-in Vulkan backend.
 */
@ApiStatus.Internal
public final class VanillaVulkanIntegration {

    private static final Marker MARKER = MarkerManager.getMarker("VanillaVulkan");

    // the game's Vulkan backend; non-null means this integration is active
    private static VulkanDevice sDevice;
    // set when close() ran, i.e. the integration must not be used for the rest of the shutdown
    private static boolean sClosed;

    private static VkQueue sQueue;
    // VkCommandPool/VkFence are non-dispatchable handles, i.e. long in LWJGL
    private static long sCommandPool = VK_NULL_HANDLE;
    private static VkCommandBuffer sCommandBuffer;
    private static long sFence = VK_NULL_HANDLE;
    private static boolean sInFlight;

    // GPU-to-GPU copy target, owned by Minecraft's GPU device
    private static GpuTexture sLayerTexture;
    private static int sLayerWidth;
    private static int sLayerHeight;
    private static boolean sLayerLayoutKnown;

    // deferred operations, run once the submission referencing them has finished
    private static final Deque<Runnable> sFrameOps = new ArrayDeque<>();

    private VanillaVulkanIntegration() {
    }

    /**
     * @return true if Minecraft's built-in Vulkan backend has been bridged
     */
    public static boolean isActive() {
        return sDevice != null && !sClosed;
    }

    /**
     * Builds an Arc3D {@link VulkanBackendContext} on top of Minecraft's own Vulkan device, so
     * that ModernUI renders into the same {@code VkDevice}/{@code VkQueue}.
     */
    public static VulkanBackendContext wrapContext() {
        // measured on the render thread during RenderSystem#initRenderer, so it delays the first
        // frame and is therefore worth reporting (startup black screen investigations)
        final long t0 = System.nanoTime();
        final VulkanDevice vk = resolveBackend(RenderSystem.getDevice());
        final VkDevice vkDevice = vk.vkDevice();
        final DeviceInfo info = vk.getDeviceInfo();

        VkPhysicalDevice physicalDevice = vkDevice.getPhysicalDevice();
        if (physicalDevice == null) {
            physicalDevice = findPhysicalDevice(vk, info);
            LOGGER.info(MARKER, "VkDevice#getPhysicalDevice() returned null, recovered by enumeration");
        }
        if (physicalDevice == null) {
            throw new IllegalStateException(
                    "ModernUI: cannot resolve the VkPhysicalDevice of Minecraft's Vulkan backend");
        }

        final int queueFamily = vk.graphicsQueue().queueFamilyIndex();
        int apiVersion = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(physicalDevice, props);
            apiVersion = props.apiVersion();
        }

        final VulkanBackendContext context = new VulkanBackendContext();
        context.mInstance = vk.instance().vkInstance();
        context.mPhysicalDevice = physicalDevice;
        context.mDevice = vkDevice;
        context.mQueue = vk.graphicsQueue().vkQueue();
        context.mGraphicsQueueIndex = queueFamily;
        context.mMaxAPIVersion = apiVersion;
        // The VulkanMod integration passes an all-zero features struct for the same reason: we
        // must never assume a device feature the game did not enable on its VkDevice.
        context.mDeviceFeatures2 = VkPhysicalDeviceFeatures2.calloc();
        context.mMemoryAllocator = VulkanMemoryAllocator.make(
                context.mInstance, physicalDevice, vkDevice, apiVersion, 0);
        if (context.mMemoryAllocator == null) {
            throw new IllegalStateException(
                    "ModernUI: failed to create the Arc3D Vulkan memory allocator");
        }

        sDevice = vk;
        sClosed = false;
        sQueue = context.mQueue;
        sCommandPool = VK_NULL_HANDLE;
        sCommandBuffer = null;
        sFence = VK_NULL_HANDLE;
        sInFlight = false;

        LOGGER.info(MARKER, "Bridged Minecraft's built-in Vulkan backend: device={}, vendor={}, "
                        + "driver={}, apiVersion={}.{}.{}, graphicsQueueFamily={}, took {} ms",
                info.name(), info.vendorName(), info.driverInfo(),
                (apiVersion >> 22), ((apiVersion >> 12) & 0x3FF), (apiVersion & 0xFFF),
                queueFamily, (System.nanoTime() - t0) / 1_000_000L);
        return context;
    }

    private static VulkanDevice resolveBackend(GpuDevice device) {
        final GpuDeviceBackend backend = ((AccessFrontendGpuDevice) device).getBackend();
        if (!(backend instanceof VulkanDevice vk)) {
            throw new IllegalStateException("ModernUI: expected Minecraft's Vulkan backend, but got " +
                    (backend == null ? device.getClass().getName() : backend.getClass().getName()));
        }
        return vk;
    }

    private static VkPhysicalDevice findPhysicalDevice(VulkanDevice vk, DeviceInfo info) {
        final VkInstance instance = vk.instance().vkInstance();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final var pCount = stack.ints(0);
            int result = vkEnumeratePhysicalDevices(instance, pCount, null);
            if (result != VK_SUCCESS || pCount.get(0) == 0) {
                LOGGER.error(MARKER, "vkEnumeratePhysicalDevices failed: {}", result);
                return null;
            }
            final PointerBuffer devices = stack.mallocPointer(pCount.get(0));
            result = vkEnumeratePhysicalDevices(instance, pCount, devices);
            if (result != VK_SUCCESS) {
                LOGGER.error(MARKER, "vkEnumeratePhysicalDevices failed: {}", result);
                return null;
            }
            final VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.malloc(stack);
            VkPhysicalDevice first = null;
            for (int i = 0; i < pCount.get(0); i++) {
                final VkPhysicalDevice device = new VkPhysicalDevice(devices.get(i), instance);
                vkGetPhysicalDeviceProperties(device, props);
                if (first == null) {
                    first = device;
                }
                final String name = props.deviceNameString();
                if (name != null && name.equals(info.name())) {
                    LOGGER.info(MARKER, "Matched the Vulkan physical device by name: {}", name);
                    return device;
                }
            }
            return first;
        }
    }

    // ---------------------------------------------------------------------------------------
    // UI layer texture
    // ---------------------------------------------------------------------------------------

    /**
     * Returns a Minecraft-owned texture that mirrors the Arc3D UI layer. It is (re)created when
     * the layer size or pixel format changes.
     */
    public static GpuTexture wrapTextureImageFromArc3D(@RawPtr VulkanImage layer) {
        final int width = layer.getWidth();
        final int height = layer.getHeight();
        final GpuFormat format = toGpuFormat(layer.getVulkanDesc());
        if (sLayerTexture == null || sLayerWidth != width || sLayerHeight != height) {
            releaseLayerTexture();
            sLayerTexture = RenderSystem.getDevice().createTexture(
                    "ModernUI_UI_Layer",
                    GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                    format, width, height, 1, 1);
            sLayerWidth = width;
            sLayerHeight = height;
            // the layout of a freshly created texture is only known after Minecraft's
            // initialization command buffer ran, so stay conservative for the first copy
            sLayerLayoutKnown = false;
            LOGGER.info(MARKER, "Created the UI layer texture {}x{} ({})", width, height, format);
        }
        return sLayerTexture;
    }

    /**
     * @return true if the given texture still mirrors the given Arc3D layer
     */
    public static boolean sameImage(GpuTexture texture, @RawPtr VulkanImage layer) {
        return texture == sLayerTexture &&
                layer.getWidth() == sLayerWidth &&
                layer.getHeight() == sLayerHeight;
    }

    /**
     * Arc3D is an independent renderer, so all we can do here is copy its layer into our
     * texture. Layout bookkeeping is kept on Arc3D's side, see below.
     */
    public static void syncImageLayoutFromArc3D(GpuTexture texture, @RawPtr VulkanImage layer) {
        if (texture != sLayerTexture || !isActive()) {
            return;
        }
        try {
            copyLayer(layer);
        } catch (Throwable t) {
            LOGGER.error(MARKER, "Failed to copy the ModernUI layer into Minecraft's texture", t);
        }
    }

    /**
     * Nothing needs to be synced back: our texture stays in {@code VK_IMAGE_LAYOUT_GENERAL}
     * forever, which is exactly what Minecraft's backend assumes. This also acts as the frame
     * end hook for deferred releases.
     */
    public static void syncImageLayoutFromVulkan(GpuTexture texture, @RawPtr VulkanImage layer) {
        drainFrameOps(false);
    }

    public static void addFrameOp(Runnable runnable) {
        sFrameOps.addLast(runnable);
    }

    private static void copyLayer(@RawPtr VulkanImage layer) {
        // finish the previous submission first, so that both the command buffer and the Arc3D
        // layer referenced by it are safe to reuse
        drainFrameOps(true);
        ensureCommandBuffer();

        final VulkanImageDesc desc = layer.getVulkanDesc();
        final int width = layer.getWidth();
        final int height = layer.getHeight();
        final int layers = Math.max(1, desc.getLayerCount());
        final int oldLayout = layer.getVulkanMutableState().getImageLayout();
        final long srcImage = layer.vkImage();
        final long dstImage = ((VulkanGpuTexture) sLayerTexture).vkImage();
        // GENERAL is a legal layout for both sides of vkCmdCopyImage, so both images can simply
        // stay in the layout Minecraft's backend assumes
        final int dstOldLayout = sLayerLayoutKnown ? VK_IMAGE_LAYOUT_GENERAL : VK_IMAGE_LAYOUT_UNDEFINED;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            checkResult(vkResetCommandBuffer(sCommandBuffer, 0), "vkResetCommandBuffer");
            checkResult(vkBeginCommandBuffer(sCommandBuffer, beginInfo), "vkBeginCommandBuffer");

            final VkImageMemoryBarrier.Buffer barriers = VkImageMemoryBarrier.calloc(2, stack);
            barriers.get(0)
                    .sType$Default()
                    .srcAccessMask(VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_MEMORY_READ_BIT)
                    .dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT)
                    .oldLayout(oldLayout)
                    .newLayout(VK_IMAGE_LAYOUT_GENERAL)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .image(srcImage);
            barriers.get(0).subresourceRange()
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(Math.max(1, desc.getMipLevelCount()))
                    .baseArrayLayer(0)
                    .layerCount(layers);
            barriers.get(1)
                    .sType$Default()
                    .srcAccessMask(VK_ACCESS_MEMORY_WRITE_BIT)
                    .dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                    .oldLayout(dstOldLayout)
                    .newLayout(VK_IMAGE_LAYOUT_GENERAL)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .image(dstImage);
            barriers.get(1).subresourceRange()
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);
            vkCmdPipelineBarrier(sCommandBuffer,
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
                    0, null, null, barriers);

            final VkImageCopy.Buffer regions = VkImageCopy.calloc(1, stack);
            regions.get(0).srcSubresource()
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(0)
                    .baseArrayLayer(0)
                    .layerCount(layers);
            regions.get(0).dstSubresource()
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(0)
                    .baseArrayLayer(0)
                    .layerCount(1);
            regions.get(0).srcOffset().set(0, 0, 0);
            regions.get(0).dstOffset().set(0, 0, 0);
            regions.get(0).extent().set(width, height, 1);
            vkCmdCopyImage(sCommandBuffer,
                    srcImage, VK_IMAGE_LAYOUT_GENERAL,
                    dstImage, VK_IMAGE_LAYOUT_GENERAL,
                    regions);

            // make the copy visible to whatever Minecraft draws afterwards
            final VkMemoryBarrier.Buffer memoryBarrier = VkMemoryBarrier.calloc(1, stack)
                    .sType$Default()
                    .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                    .dstAccessMask(VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_SHADER_READ_BIT |
                            VK_ACCESS_MEMORY_WRITE_BIT);
            vkCmdPipelineBarrier(sCommandBuffer,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                    0, memoryBarrier, null, null);

            checkResult(vkEndCommandBuffer(sCommandBuffer), "vkEndCommandBuffer");

            final var pCommandBuffers = stack.mallocPointer(1);
            pCommandBuffers.put(0, sCommandBuffer.address());
            final VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack)
                    .sType$Default()
                    .pCommandBuffers(pCommandBuffers);
            checkResult(vkResetFences(sDevice.vkDevice(), sFence), "vkResetFences");
            checkResult(vkQueueSubmit(sQueue, submitInfo, sFence), "vkQueueSubmit");
            sInFlight = true;
            sLayerLayoutKnown = true;

            // Arc3D must not believe its image is still in the layout it was left in
            layer.getVulkanMutableState().setImageLayout(VK_IMAGE_LAYOUT_GENERAL);
        }
    }

    private static void ensureCommandBuffer() {
        if (sCommandPool != VK_NULL_HANDLE) {
            return;
        }
        final VkDevice vkDevice = sDevice.vkDevice();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(sDevice.graphicsQueue().queueFamilyIndex());
            final var pPool = stack.mallocLong(1);
            checkResult(vkCreateCommandPool(vkDevice, poolInfo, null, pPool), "vkCreateCommandPool");
            sCommandPool = pPool.get(0);

            final VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType$Default()
                    .commandPool(sCommandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);
            final var pCommandBuffer = stack.mallocPointer(1);
            checkResult(vkAllocateCommandBuffers(vkDevice, allocInfo, pCommandBuffer),
                    "vkAllocateCommandBuffers");
            sCommandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), vkDevice);

            final VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack).sType$Default();
            final var pFence = stack.mallocLong(1);
            checkResult(vkCreateFence(vkDevice, fenceInfo, null, pFence), "vkCreateFence");
            sFence = pFence.get(0);
            LOGGER.debug(MARKER, "Created the ModernUI Vulkan copy resources (queue family {})",
                    sDevice.graphicsQueue().queueFamilyIndex());
        }
    }

    /**
     * Runs the deferred operations of a finished submission.
     *
     * @param wait {@code true} to block until the pending submission finished
     */
    private static void drainFrameOps(boolean wait) {
        if (sInFlight) {
            if (wait) {
                checkResult(vkWaitForFences(sDevice.vkDevice(), sFence, true, Long.MAX_VALUE),
                        "vkWaitForFences");
                sInFlight = false;
            } else if (vkGetFenceStatus(sDevice.vkDevice(), sFence) == VK_SUCCESS) {
                sInFlight = false;
            } else {
                return;
            }
        }
        while (!sFrameOps.isEmpty()) {
            try {
                sFrameOps.removeFirst().run();
            } catch (Throwable t) {
                LOGGER.error(MARKER, "A deferred frame operation failed", t);
            }
        }
    }

    /**
     * Releases the resources we own on Minecraft's Vulkan device. Must be called before the game
     * destroys its {@code VkDevice} (i.e. at the beginning of {@code Minecraft#close()}).
     */
    public static void close() {
        if (sDevice == null || sClosed) {
            return;
        }
        sClosed = true;
        final VkDevice vkDevice = sDevice.vkDevice();
        try {
            drainFrameOps(true);
            if (sCommandPool != VK_NULL_HANDLE) {
                // the command buffer is owned by the pool
                sCommandBuffer = null;
                vkDestroyCommandPool(vkDevice, sCommandPool, null);
                sCommandPool = VK_NULL_HANDLE;
            }
            if (sFence != VK_NULL_HANDLE) {
                vkDestroyFence(vkDevice, sFence, null);
                sFence = VK_NULL_HANDLE;
            }
            if (sLayerTexture != null) {
                // Minecraft owns the texture, we only drop our reference
                sLayerTexture.close();
                sLayerTexture = null;
            }
            LOGGER.info(MARKER, "Released the Vulkan bridge resources");
        } catch (Throwable t) {
            LOGGER.warn(MARKER, "Failed to release the Vulkan bridge resources", t);
        } finally {
            sLayerWidth = 0;
            sLayerHeight = 0;
            sLayerLayoutKnown = false;
            // NOTE: sDevice is intentionally kept. Renderpearl destroys the texture views we
            // returned from its own destruction queue while the device is closing, i.e. after this
            // method ran, and those views capture their own device handle. sClosed disables this
            // integration for the rest of the shutdown instead.
        }
    }

    private static void releaseLayerTexture() {
        drainFrameOps(true);
        if (sLayerTexture != null) {
            sLayerTexture.close();
            sLayerTexture = null;
        }
        sLayerWidth = 0;
        sLayerHeight = 0;
        sLayerLayoutKnown = false;
    }

    private static void checkResult(int result, String call) {
        if (result != VK_SUCCESS) {
            throw new IllegalStateException("ModernUI: " + call + " failed with VkResult " + result);
        }
    }

    private static GpuFormat toGpuFormat(VulkanImageDesc desc) {
        return switch (desc.mVkFormat) {
            case VK_FORMAT_R8G8B8A8_UNORM -> GpuFormat.RGBA8_UNORM;
            case VK_FORMAT_R8_UNORM -> GpuFormat.R8_UNORM;
            default -> throw new IllegalStateException("ModernUI: unsupported UI layer format "
                    + VKUtil.vkFormatName(desc.mVkFormat));
        };
    }

    // ---------------------------------------------------------------------------------------
    // Texture view swizzle (font atlas in the A8 mask format)
    // ---------------------------------------------------------------------------------------

    /**
     * Replaces the given texture view with one that applies a component swizzle, which is what
     * the OpenGL path configures through {@code GL_TEXTURE_SWIZZLE_*} and the VulkanMod path
     * through {@code VkImageViewCreateInfo::components}.
     *
     * @return the replacement view, or the given view if the swizzle cannot be applied
     */
    public static GpuTextureView replaceImageViewWithSwizzle(GpuTexture texture,
                                                            GpuTextureView view,
                                                            short swizzle) {
        if (!isActive()) {
            return view;
        }
        if (!(texture instanceof VulkanGpuTexture vkTexture) ||
                !(view instanceof VulkanGpuTextureView vkView)) {
            LOGGER.warn(MARKER, "Cannot apply the font atlas swizzle, unexpected types: {} / {}",
                    texture.getClass().getName(), view.getClass().getName());
            return view;
        }
        final long newImageView;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType$Default()
                    .image(vkTexture.vkImage())
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(VulkanConst.toVk(vkTexture.getFormat()));
            createInfo.components().set(
                    VKUtil.toVkComponentSwizzle(Swizzle.getR(swizzle)),
                    VKUtil.toVkComponentSwizzle(Swizzle.getG(swizzle)),
                    VKUtil.toVkComponentSwizzle(Swizzle.getB(swizzle)),
                    VKUtil.toVkComponentSwizzle(Swizzle.getA(swizzle))
            );
            createInfo.subresourceRange()
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(Math.max(1, vkTexture.getMipLevels()))
                    .baseArrayLayer(0)
                    .layerCount(1);
            final var pView = stack.mallocLong(1);
            final int result = vkCreateImageView(sDevice.vkDevice(), createInfo, null, pView);
            if (result != VK_SUCCESS) {
                LOGGER.error(MARKER, "vkCreateImageView (swizzle) failed: {}", result);
                return view;
            }
            newImageView = pView.get(0);
        }
        LOGGER.info(MARKER, "Applied the font atlas component swizzle on Vulkan");
        return new SwizzledVulkanTextureView(sDevice, vkTexture, vkView, newImageView);
    }

    /**
     * A view that reports a swizzled {@code VkImageView}. The super class still creates its own
     * identity image view, which is destroyed together with this object.
     * <p>
     * The {@code VkDevice} is captured on construction: renderpearl destroys these views from its
     * {@code DestructionQueue} while the device is being closed, which is after ModernUI released
     * its own resources (see {@link #close()}), so the static handle is not necessarily usable.
     */
    private static final class SwizzledVulkanTextureView extends VulkanGpuTextureView {

        private final VkDevice mDevice;
        private final long mSwizzledImageView;

        SwizzledVulkanTextureView(VulkanDevice device,
                                 VulkanGpuTexture texture,
                                 VulkanGpuTextureView original,
                                 long swizzledImageView) {
            super(device, texture, original.baseMipLevel(), original.mipLevels());
            mDevice = device.vkDevice();
            mSwizzledImageView = swizzledImageView;
        }

        @Override
        public long vkImageView() {
            return mSwizzledImageView;
        }

        @Override
        public void destroy() {
            vkDestroyImageView(mDevice, mSwizzledImageView, null);
            super.destroy();
        }
    }
}
