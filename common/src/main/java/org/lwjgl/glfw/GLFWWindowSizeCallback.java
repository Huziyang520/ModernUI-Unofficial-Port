/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowSizeCallback implements GLFWWindowSizeCallbackI {

    public GLFWWindowSizeCallback() {
    }

    public static GLFWWindowSizeCallback create(GLFWWindowSizeCallbackI callback) {
        return new GLFWWindowSizeCallback();
    }

    @Override
    public void invoke(long window, int width, int height) {
    }

    public void free() {
    }

    public void close() {
    }
}
