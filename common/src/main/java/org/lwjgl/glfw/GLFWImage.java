/*
 * GLFW compatibility stub for Minecraft 26.3 - see GLFW.java for details.
 */

package org.lwjgl.glfw;

import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;

/**
 * Inert stand-in for {@code org.lwjgl.glfw.GLFWImage}, used by Modern UI's core
 * library when setting the window icon - a path that is never taken on MC 26.3,
 * since Minecraft owns the real window.
 */
public class GLFWImage {

    /** Byte offset of the pixels pointer, mirroring LWJGL. */
    public static final int PIXELS = 0;

    public static Buffer malloc(int count, MemoryStack stack) {
        return new Buffer();
    }

    public static class Buffer {

        public Buffer width(int value) {
            return this;
        }

        public Buffer height(int value) {
            return this;
        }

        public CustomBuffer position(int position) {
            return null;
        }

        public long address() {
            return 0L;
        }

        public CustomBuffer flip() {
            return null;
        }
    }
}
