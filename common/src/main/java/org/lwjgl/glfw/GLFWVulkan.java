/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

import org.lwjgl.PointerBuffer;

/**
 * Inert stand-in for {@code org.lwjgl.glfw.GLFWVulkan}. Reports "not supported"
 * so that Modern UI's core library does not attempt to derive Vulkan instance
 * extensions from a GLFW window that does not exist on MC 26.3.
 */
public class GLFWVulkan {

    public static boolean glfwVulkanSupported() {
        return false;
    }

    public static PointerBuffer glfwGetRequiredInstanceExtensions() {
        return null;
    }
}
