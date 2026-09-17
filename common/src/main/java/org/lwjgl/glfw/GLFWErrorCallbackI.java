/*
 * Minimal GLFW compatibility stub for Minecraft 26.3.
 *
 * MC 26.3 replaced GLFW with SDL3 and no longer ships the LWJGL GLFW module,
 * but Modern UI's core library (dev.icyllis:modernui-core:3.13.0, built before
 * that migration) still links against it. Providing these members lets the
 * core classes load; the functionality that is actually needed on MC 26.3 comes
 * from Minecraft itself.
 */

package org.lwjgl.glfw;

/**
 * Mirrors {@code org.lwjgl.glfw.GLFWErrorCallbackI}.
 */
public interface GLFWErrorCallbackI {

    void invoke(int error, long description);
}
