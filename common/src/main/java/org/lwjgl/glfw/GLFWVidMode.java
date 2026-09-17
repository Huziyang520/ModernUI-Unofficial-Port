/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

import org.lwjgl.system.CustomBuffer;

/**
 * Inert stand-in for {@code org.lwjgl.glfw.GLFWVidMode}. Modern UI's core library
 * reads monitor video modes to build its own display list; on MC 26.3 that data
 * comes from Minecraft, so returning zeroed values is harmless.
 */
public class GLFWVidMode {

    public int width() {
        return 0;
    }

    public int height() {
        return 0;
    }

    public int refreshRate() {
        return 0;
    }

    public int redBits() {
        return 0;
    }

    public int greenBits() {
        return 0;
    }

    public int blueBits() {
        return 0;
    }

    public static class Buffer {

        public CustomBuffer position(int position) {
            return null;
        }

        public int limit() {
            return 0;
        }

        public int width() {
            return 0;
        }

        public int height() {
            return 0;
        }

        public int refreshRate() {
            return 0;
        }

        public int redBits() {
            return 0;
        }

        public int greenBits() {
            return 0;
        }

        public int blueBits() {
            return 0;
        }
    }
}
