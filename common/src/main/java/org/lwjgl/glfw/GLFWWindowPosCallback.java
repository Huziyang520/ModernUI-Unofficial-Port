/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowPosCallback implements GLFWWindowPosCallbackI {

    public GLFWWindowPosCallback() {
    }

    public static GLFWWindowPosCallback create(GLFWWindowPosCallbackI callback) {
        return new GLFWWindowPosCallback();
    }

    @Override
    public void invoke(long window, int xpos, int ypos) {
    }

    public void free() {
    }

    public void close() {
    }
}
