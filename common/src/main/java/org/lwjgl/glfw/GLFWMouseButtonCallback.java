/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWMouseButtonCallback implements GLFWMouseButtonCallbackI {

    public GLFWMouseButtonCallback() {
    }

    public static GLFWMouseButtonCallback create(GLFWMouseButtonCallbackI callback) {
        return new GLFWMouseButtonCallback();
    }

    @Override
    public void invoke(long window, int button, int action, int mods) {
    }

    public void free() {
    }

    public void close() {
    }
}
