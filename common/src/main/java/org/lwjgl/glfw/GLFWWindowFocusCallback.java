/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowFocusCallback implements GLFWWindowFocusCallbackI {

    public GLFWWindowFocusCallback() {
    }

    public static GLFWWindowFocusCallback create(GLFWWindowFocusCallbackI callback) {
        return new GLFWWindowFocusCallback();
    }

    @Override
    public void invoke(long window, boolean focused) {
    }

    public void free() {
    }

    public void close() {
    }
}
