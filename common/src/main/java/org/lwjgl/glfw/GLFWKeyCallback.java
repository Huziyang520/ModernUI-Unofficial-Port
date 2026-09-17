/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWKeyCallback implements GLFWKeyCallbackI {

    public GLFWKeyCallback() {
    }

    public static GLFWKeyCallback create(GLFWKeyCallbackI callback) {
        return new GLFWKeyCallback();
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
    }

    public void free() {
    }

    public void close() {
    }
}
