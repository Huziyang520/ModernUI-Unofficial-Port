/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWCharCallback implements GLFWCharCallbackI {

    public GLFWCharCallback() {
    }

    public static GLFWCharCallback create(GLFWCharCallbackI callback) {
        return new GLFWCharCallback();
    }

    @Override
    public void invoke(long window, int codepoint) {
    }

    public void free() {
    }

    public void close() {
    }
}
