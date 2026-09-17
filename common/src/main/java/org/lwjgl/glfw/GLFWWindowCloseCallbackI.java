/*
 * GLFW compatibility stub for Minecraft 26.3.
 *
 * MC 26.3 moved window management and input from GLFW to SDL3 and no longer ships
 * the LWJGL GLFW module, while modernui-core 3.13.0 still links against it.
 * These minimal types exist so the core classes can be loaded and verified.
 */

package org.lwjgl.glfw;

public interface GLFWWindowCloseCallbackI {

    void invoke(long window);
}
