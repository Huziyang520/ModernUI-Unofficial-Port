/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowContentScaleCallback implements GLFWWindowContentScaleCallbackI {

    public GLFWWindowContentScaleCallback() {
    }

    public static GLFWWindowContentScaleCallback create(GLFWWindowContentScaleCallbackI callback) {
        return new GLFWWindowContentScaleCallback();
    }

    @Override
    public void invoke(long window, float xscale, float yscale) {
    }

    public void free() {
    }

    public void close() {
    }
}
