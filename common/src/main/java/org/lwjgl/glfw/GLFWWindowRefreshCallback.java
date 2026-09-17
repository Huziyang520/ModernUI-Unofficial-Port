/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowRefreshCallback implements GLFWWindowRefreshCallbackI {

    public GLFWWindowRefreshCallback() {
    }

    public static GLFWWindowRefreshCallback create(GLFWWindowRefreshCallbackI callback) {
        return new GLFWWindowRefreshCallback();
    }

    @Override
    public void invoke(long window) {
    }

    public void free() {
    }

    public void close() {
    }
}
