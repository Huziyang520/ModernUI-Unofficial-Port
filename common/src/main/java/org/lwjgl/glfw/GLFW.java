/*
 * GLFW compatibility stub for Minecraft 26.3.
 *
 * WHY THIS EXISTS
 * ---------------
 * MC 26.3 (Snapshot 4, 2026-07-16) migrated window management, input and platform
 * integration from GLFW to SDL3, and the LWJGL GLFW module is no longer on the
 * class path. Modern UI's core library (dev.icyllis:modernui-core:3.13.0) predates
 * that change and links against GLFW from 15 classes, so loading those classes
 * fails with:
 *
 *     NoClassDefFoundError: org/lwjgl/glfw/GLFWWindowCloseCallbackI
 *
 * Note that bytecode verification resolves the types appearing in *method
 * signatures*, so every referenced GLFW type must be resolvable - it is not
 * enough to stub only the methods that happen to be called.
 *
 * WHY IT IS SAFE
 * --------------
 * Bytecode analysis (javap) shows only a small set of members are actually
 * reached at runtime on MC 26.3, because Modern UI uses Minecraft's own window,
 * input and render system - its GLFW window layer is never instantiated in game:
 *
 *     Core#initialize()   glfwSetErrorCallback / glfwInit / GLFWErrorCallback.free
 *     Core#terminate()    glfwSetErrorCallback / glfwTerminate
 *     Core#timeNanos()    glfwGetTime          (called frequently)
 *     Core#timeMillis()   glfwGetTime
 *     Clipboard#getText   glfwSetErrorCallback / glfwGetClipboardString
 *     Clipboard#setText   glfwSetClipboardString
 *     PointerIcon         glfwCreateStandardCursor
 *
 * Everything else is inert: window creation/manipulation is a no-op, which is
 * correct here because Minecraft owns the real (SDL3) window.
 *
 * CONSEQUENCES
 * ------------
 * - Timing works normally (System.nanoTime is the same monotonic clock GLFW uses).
 * - Clipboard and custom cursors are non-functional on 26.3; both are auxiliary
 *   and should later be re-implemented against Minecraft's SDL API.
 *
 * This is a shim for an upstream dependency, not an upstream fix: modernui-core
 * needs to be ported to SDL3 by its author.
 */

package org.lwjgl.glfw;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

/**
 * Inert stand-in for {@code org.lwjgl.glfw.GLFW}.
 */
public class GLFW {

    private GLFW() {
    }

    // ------------------------------------------------------------------
    // Members actually reached at runtime on MC 26.3
    // ------------------------------------------------------------------

    public static GLFWErrorCallback glfwSetErrorCallback(GLFWErrorCallbackI callback) {
        return null;
    }

    /** Reports success so {@code Core#initialize()} proceeds; MC owns the real window. */
    public static boolean glfwInit() {
        return true;
    }

    public static void glfwTerminate() {
    }

    /** Backs {@code Core#timeNanos()} / {@code Core#timeMillis()}. */
    public static double glfwGetTime() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    public static String glfwGetClipboardString(long window) {
        return null;
    }

    public static void glfwSetClipboardString(long window, CharSequence string) {
    }

    public static long glfwCreateStandardCursor(int shape) {
        return 0L;
    }

    // ------------------------------------------------------------------
    // Window lifecycle (inert)
    // ------------------------------------------------------------------

    public static long glfwCreateWindow(int width, int height, CharSequence title,
                                        long monitor, long share) {
        return 0L;
    }

    public static void glfwDestroyWindow(long window) {
    }

    public static void glfwShowWindow(long window) {
    }

    public static void glfwHideWindow(long window) {
    }

    public static void glfwIconifyWindow(long window) {
    }

    public static void glfwRestoreWindow(long window) {
    }

    public static void glfwMaximizeWindow(long window) {
    }

    public static void glfwSetWindowTitle(long window, CharSequence title) {
    }

    public static void glfwSetWindowPos(long window, int x, int y) {
    }

    public static void glfwSetWindowShouldClose(long window, boolean value) {
    }

    public static boolean glfwWindowShouldClose(long window) {
        return false;
    }

    public static void glfwGetWindowPos(long window, IntBuffer x, IntBuffer y) {
    }

    public static void glfwGetWindowSize(long window, IntBuffer width, IntBuffer height) {
    }

    public static void glfwGetFramebufferSize(long window, IntBuffer width, IntBuffer height) {
    }

    public static void glfwGetCursorPos(long window, DoubleBuffer x, DoubleBuffer y) {
    }

    public static int glfwGetKey(long window, int key) {
        return 0;
    }

    public static String glfwGetKeyName(int key, int scancode) {
        return null;
    }

    public static int glfwGetMouseButton(long window, int button) {
        return 0;
    }

    public static void glfwSetCursor(long window, long cursor) {
    }

    public static void glfwSwapBuffers(long window) {
    }

    public static void glfwSwapInterval(int interval) {
    }

    public static void glfwMakeContextCurrent(long window) {
    }

    public static void glfwSetWindowIcon(long window, GLFWImage.Buffer images) {
    }

    public static void nglfwSetWindowIcon(long window, int count, long images) {
    }

    // ------------------------------------------------------------------
    // Window hints (inert)
    // ------------------------------------------------------------------

    public static void glfwDefaultWindowHints() {
    }

    public static void glfwWindowHint(int hint, int value) {
    }

    public static void glfwWindowHintString(int hint, CharSequence value) {
    }

    // ------------------------------------------------------------------
    // Event handling (inert)
    // ------------------------------------------------------------------

    public static void glfwPollEvents() {
    }

    public static void glfwWaitEvents() {
    }

    public static void glfwWaitEventsTimeout(double timeout) {
    }

    public static void glfwPostEmptyEvent() {
    }

    // ------------------------------------------------------------------
    // Callbacks (inert)
    // ------------------------------------------------------------------

    public static GLFWCharCallback glfwSetCharCallback(long window, GLFWCharCallbackI callback) {
        return null;
    }

    public static GLFWCursorPosCallback glfwSetCursorPosCallback(long window, GLFWCursorPosCallbackI callback) {
        return null;
    }

    public static GLFWFramebufferSizeCallback glfwSetFramebufferSizeCallback(long window,
                                                                             GLFWFramebufferSizeCallbackI callback) {
        return null;
    }

    public static GLFWKeyCallback glfwSetKeyCallback(long window, GLFWKeyCallbackI callback) {
        return null;
    }

    public static GLFWMonitorCallback glfwSetMonitorCallback(GLFWMonitorCallbackI callback) {
        return null;
    }

    public static GLFWMouseButtonCallback glfwSetMouseButtonCallback(long window,
                                                                     GLFWMouseButtonCallbackI callback) {
        return null;
    }

    public static GLFWScrollCallback glfwSetScrollCallback(long window, GLFWScrollCallbackI callback) {
        return null;
    }

    public static GLFWWindowCloseCallback glfwSetWindowCloseCallback(long window,
                                                                    GLFWWindowCloseCallbackI callback) {
        return null;
    }

    public static GLFWWindowContentScaleCallback glfwSetWindowContentScaleCallback(
            long window, GLFWWindowContentScaleCallbackI callback) {
        return null;
    }

    public static GLFWWindowFocusCallback glfwSetWindowFocusCallback(long window,
                                                                    GLFWWindowFocusCallbackI callback) {
        return null;
    }

    public static GLFWWindowIconifyCallback glfwSetWindowIconifyCallback(long window,
                                                                        GLFWWindowIconifyCallbackI callback) {
        return null;
    }

    public static GLFWWindowMaximizeCallback glfwSetWindowMaximizeCallback(long window,
                                                                          GLFWWindowMaximizeCallbackI callback) {
        return null;
    }

    public static GLFWWindowPosCallback glfwSetWindowPosCallback(long window, GLFWWindowPosCallbackI callback) {
        return null;
    }

    public static GLFWWindowRefreshCallback glfwSetWindowRefreshCallback(long window,
                                                                        GLFWWindowRefreshCallbackI callback) {
        return null;
    }

    public static GLFWWindowSizeCallback glfwSetWindowSizeCallback(long window, GLFWWindowSizeCallbackI callback) {
        return null;
    }

    // ------------------------------------------------------------------
    // Monitors / video modes (inert)
    // ------------------------------------------------------------------

    public static PointerBuffer glfwGetMonitors() {
        return null;
    }

    public static long glfwGetPrimaryMonitor() {
        return 0L;
    }

    public static String glfwGetMonitorName(long monitor) {
        return null;
    }

    public static void glfwGetMonitorPos(long monitor, IntBuffer x, IntBuffer y) {
    }

    public static void glfwGetMonitorPhysicalSize(long monitor, IntBuffer width, IntBuffer height) {
    }

    public static void glfwGetMonitorPhysicalSize(long monitor, int[] width, int[] height) {
    }

    public static void glfwGetMonitorContentScale(long monitor, float[] xscale, float[] yscale) {
    }

    public static GLFWVidMode glfwGetVideoMode(long monitor) {
        return null;
    }

    public static GLFWVidMode.Buffer glfwGetVideoModes(long monitor) {
        return null;
    }
}
