/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

public class GLFWMonitorCallback implements GLFWMonitorCallbackI {

    public GLFWMonitorCallback() {
    }

    public static GLFWMonitorCallback create(GLFWMonitorCallbackI callback) {
        return new GLFWMonitorCallback();
    }

    @Override
    public void invoke(long monitor, int event) {
    }

    public void free() {
    }

    public void close() {
    }
}
