/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWWindowIconifyCallback implements GLFWWindowIconifyCallbackI {

    public GLFWWindowIconifyCallback() {
    }

    public static GLFWWindowIconifyCallback create(GLFWWindowIconifyCallbackI callback) {
        return new GLFWWindowIconifyCallback();
    }

    @Override
    public void invoke(long window, boolean iconified) {
    }

    public void free() {
    }

    public void close() {
    }
}
