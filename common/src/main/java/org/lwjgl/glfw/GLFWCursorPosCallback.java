/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWCursorPosCallback implements GLFWCursorPosCallbackI {

    public GLFWCursorPosCallback() {
    }

    public static GLFWCursorPosCallback create(GLFWCursorPosCallbackI callback) {
        return new GLFWCursorPosCallback();
    }

    @Override
    public void invoke(long window, double xpos, double ypos) {
    }

    public void free() {
    }

    public void close() {
    }
}
