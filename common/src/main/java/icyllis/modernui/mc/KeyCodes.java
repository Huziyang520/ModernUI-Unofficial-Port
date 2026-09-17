/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc;

/**
 * MC 26.3 replaced GLFW with SDL3, so vanilla now reports SDL scancodes
 * ({@code InputConstants.KEY_*} values) and SDL key modifiers (KMOD bits),
 * while Modern UI's core library still expects GLFW key codes and
 * GLFW_MOD bits (compile-time inlined constants). This class converts
 * between the two numbering systems.
 */
public final class KeyCodes {

    private KeyCodes() {
    }

    // SDL KMOD masks (verified against 26.3 InputWithModifiers bytecode:
    // hasControlDown() & 0xC0, hasAltDown() & 0x300)
    private static final int KMOD_SHIFT = 0x0003; // LSHIFT|RSHIFT
    private static final int KMOD_CTRL = 0x00C0;  // LCTRL|RCTRL
    private static final int KMOD_ALT = 0x0300;   // LALT|RALT
    private static final int KMOD_GUI = 0x0C00;   // LGUI|RGUI

    // GLFW_MOD bits expected by the core library
    private static final int GLFW_MOD_SHIFT = 0x0001;
    private static final int GLFW_MOD_CONTROL = 0x0002;
    private static final int GLFW_MOD_ALT = 0x0004;
    private static final int GLFW_MOD_SUPER = 0x0008;

    /**
     * Lookup table: index = SDL scancode, value = GLFW key code (0 if unmapped).
     * Size covers SDL_NUM_SCANCODES for keyboard region (up to 232, RGUI).
     */
    private static final int[] SDL_TO_GLFW = buildTable();

    private static void set(int[] table, int sdl, int glfw) {
        if (sdl < table.length) {
            table[sdl] = glfw;
        }
    }

    private static int[] buildTable() {
        int[] table = new int[232];
        // A-Z: SDL 4-29 -> GLFW 65-90
        for (int i = 0; i < 26; i++) {
            set(table, 4 + i, 65 + i);
        }
        // 0-9: SDL 30-39 -> GLFW 48-57
        for (int i = 0; i < 10; i++) {
            set(table, 30 + i, 48 + i);
        }
        set(table, 40, 257);  // RETURN -> GLFW_KEY_ENTER
        set(table, 41, 256);  // ESCAPE -> GLFW_KEY_ESCAPE
        set(table, 42, 259);  // BACKSPACE
        set(table, 43, 258);  // TAB
        set(table, 44, 32);   // SPACE
        set(table, 45, 45);   // MINUS
        set(table, 46, 61);   // EQUALS
        set(table, 47, 91);   // LEFTBRACKET
        set(table, 48, 93);   // RIGHTBRACKET
        set(table, 49, 92);   // BACKSLASH
        set(table, 50, 92);   // NONUSHASH -> BACKSLASH
        set(table, 51, 59);   // SEMICOLON
        set(table, 52, 39);   // APOSTROPHE
        set(table, 53, 96);   // GRAVE
        set(table, 54, 44);   // COMMA
        set(table, 55, 46);   // PERIOD
        set(table, 56, 47);   // SLASH
        set(table, 57, 280);  // CAPSLOCK
        // F1-F12: SDL 58-69 -> GLFW 290-301
        for (int i = 0; i < 12; i++) {
            set(table, 58 + i, 290 + i);
        }
        set(table, 70, 283);  // PRINTSCREEN
        set(table, 71, 281);  // SCROLLLOCK
        set(table, 72, 284);  // PAUSE
        set(table, 73, 260);  // INSERT
        set(table, 74, 268);  // HOME
        set(table, 75, 266);  // PAGEUP
        set(table, 76, 261);  // DELETE
        set(table, 77, 269);  // END
        set(table, 78, 267);  // PAGEDOWN
        set(table, 79, 262);  // RIGHT
        set(table, 80, 263);  // LEFT
        set(table, 81, 264);  // DOWN
        set(table, 82, 265);  // UP
        set(table, 83, 282);  // NUMLOCKCLEAR
        set(table, 84, 331);  // KP_DIVIDE
        set(table, 85, 332);  // KP_MULTIPLY
        set(table, 86, 333);  // KP_MINUS
        set(table, 87, 334);  // KP_PLUS
        set(table, 88, 335);  // KP_ENTER
        // KP_1-KP_9: SDL 89-97 -> GLFW 321-329
        for (int i = 0; i < 9; i++) {
            set(table, 89 + i, 321 + i);
        }
        set(table, 98, 320);  // KP_0
        set(table, 99, 330);  // KP_PERIOD
        set(table, 100, 92);  // NONUSBACKSLASH -> BACKSLASH
        set(table, 102, 348); // APPLICATION -> GLFW_KEY_MENU
        set(table, 104, 336); // KP_EQUALS
        // F13-F24: SDL 105-116 -> GLFW 302-313
        for (int i = 0; i < 12; i++) {
            set(table, 105 + i, 302 + i);
        }
        set(table, 224, 341); // LCTRL
        set(table, 225, 340); // LSHIFT
        set(table, 226, 342); // LALT
        set(table, 227, 343); // LGUI -> LEFT_SUPER
        set(table, 228, 345); // RCTRL
        set(table, 229, 344); // RSHIFT
        set(table, 230, 346); // RALT
        set(table, 231, 347); // RGUI -> RIGHT_SUPER
        return table;
    }

    /**
     * Convert an SDL scancode (26.3 {@code InputConstants.KEY_*} value, as
     * reported by {@code KeyEvent.key()}) to the GLFW key code expected by
     * the Modern UI core library. Returns 0 for unmapped keys, which the
     * core library safely ignores.
     */
    public static int toGlfwKey(int sdlScancode) {
        if (sdlScancode < 0 || sdlScancode >= SDL_TO_GLFW.length) {
            return 0;
        }
        return SDL_TO_GLFW[sdlScancode];
    }

    /**
     * Convert SDL KMOD modifier bits (26.3 {@code KeyEvent.modifiers()}) to
     * the GLFW_MOD bits expected by the Modern UI core library.
     */
    public static int toGlfwMods(int sdlMods) {
        int mods = 0;
        if ((sdlMods & KMOD_SHIFT) != 0) {
            mods |= GLFW_MOD_SHIFT;
        }
        if ((sdlMods & KMOD_CTRL) != 0) {
            mods |= GLFW_MOD_CONTROL;
        }
        if ((sdlMods & KMOD_ALT) != 0) {
            mods |= GLFW_MOD_ALT;
        }
        if ((sdlMods & KMOD_GUI) != 0) {
            mods |= GLFW_MOD_SUPER;
        }
        return mods;
    }
}
