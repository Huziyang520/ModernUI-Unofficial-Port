/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWScrollCallback implements GLFWScrollCallbackI {

    public GLFWScrollCallback() {
    }

    public static GLFWScrollCallback create(GLFWScrollCallbackI callback) {
        return new GLFWScrollCallback();
    }

    @Override
    public void invoke(long window, double xoffset, double yoffset) {
    }

    public void free() {
    }

    public void close() {
    }
}
