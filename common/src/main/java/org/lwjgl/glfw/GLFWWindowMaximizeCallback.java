/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowMaximizeCallback implements GLFWWindowMaximizeCallbackI {

    public GLFWWindowMaximizeCallback() {
    }

    public static GLFWWindowMaximizeCallback create(GLFWWindowMaximizeCallbackI callback) {
        return new GLFWWindowMaximizeCallback();
    }

    @Override
    public void invoke(long window, boolean maximized) {
    }

    public void free() {
    }

    public void close() {
    }
}
