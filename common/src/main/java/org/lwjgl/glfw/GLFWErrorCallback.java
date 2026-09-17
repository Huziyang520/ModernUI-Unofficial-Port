/*
 * Minimal GLFW compatibility stub for Minecraft 26.3 - see GLFWErrorCallbackI.
 */

package org.lwjgl.glfw;

/**
 * Mirrors {@code org.lwjgl.glfw.GLFWErrorCallback}: an inert callback object.
 * Modern UI only sets it and frees it, so no-op behaviour is sufficient.
 */
public class GLFWErrorCallback implements GLFWErrorCallbackI {

    public GLFWErrorCallback() {
    }

    public static GLFWErrorCallback create() {
        return new GLFWErrorCallback();
    }

    public static GLFWErrorCallback create(GLFWErrorCallbackI callback) {
        return new GLFWErrorCallback();
    }

    @Override
    public void invoke(int error, long description) {
    }

    public void free() {
    }

    public void close() {
    }
}
