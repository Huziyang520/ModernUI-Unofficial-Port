/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWFramebufferSizeCallback implements GLFWFramebufferSizeCallbackI {

    public GLFWFramebufferSizeCallback() {
    }

    public static GLFWFramebufferSizeCallback create(GLFWFramebufferSizeCallbackI callback) {
        return new GLFWFramebufferSizeCallback();
    }

    @Override
    public void invoke(long window, int width, int height) {
    }

    public void free() {
    }

    public void close() {
    }
}
