/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowCloseCallback implements GLFWWindowCloseCallbackI {

    public GLFWWindowCloseCallback() {
    }

    public static GLFWWindowCloseCallback create(GLFWWindowCloseCallbackI callback) {
        return new GLFWWindowCloseCallback();
    }

    @Override
    public void invoke(long window) {
    }

    public void free() {
    }

    public void close() {
    }
}
