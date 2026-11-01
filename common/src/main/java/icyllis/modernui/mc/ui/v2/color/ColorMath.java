/*
 * Modern UI.
 * Copyright (C) 2026 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.ui.v2.color;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;

import java.util.Locale;

/**
 * HSV and hex conversions shared by the colour studio. The conversion keeps the hue when
 * the colour is grey, otherwise dragging value to zero would reset the plane cursor.
 */
final class ColorMath {

    private ColorMath() {
    }

    static float clamp(float value) {
        return value < 0f ? 0f : (value > 1f ? 1f : value);
    }

    /** @return the colour as 0xRRGGBB */
    static int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float hh = (h % 360f) / 60f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float r, g, b;
        if (hh < 1f) {
            r = c;
            g = x;
            b = 0f;
        } else if (hh < 2f) {
            r = x;
            g = c;
            b = 0f;
        } else if (hh < 3f) {
            r = 0f;
            g = c;
            b = x;
        } else if (hh < 4f) {
            r = 0f;
            g = x;
            b = c;
        } else if (hh < 5f) {
            r = x;
            g = 0f;
            b = c;
        } else {
            r = c;
            g = 0f;
            b = x;
        }
        float m = v - c;
        return ((int) ((r + m) * 255f + 0.5f) << 16)
                | ((int) ((g + m) * 255f + 0.5f) << 8)
                | (int) ((b + m) * 255f + 0.5f);
    }

    /**
     * @param fallbackHue hue to keep when the colour is grey and carries no hue of its own
     */
    static void rgbToHsv(int rgb, @NonNull float[] hsv, float fallbackHue) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float h;
        if (delta == 0f) {
            h = fallbackHue;
        } else if (max == r) {
            h = 60f * (((g - b) / delta) % 6f);
        } else if (max == g) {
            h = 60f * ((b - r) / delta + 2f);
        } else {
            h = 60f * ((r - g) / delta + 4f);
        }
        if (h < 0f) {
            h += 360f;
        }
        hsv[0] = h;
        hsv[1] = max == 0f ? 0f : delta / max;
        hsv[2] = max;
    }

    static int compose(@NonNull float[] hsv, int alpha) {
        return (alpha << 24) | (hsvToRgb(hsv[0], hsv[1], hsv[2]) & 0xFFFFFF);
    }

    /**
     * Accepts {@code #RRGGBB} and {@code #AARRGGBB}, with or without the leading hash.
     *
     * @throws NumberFormatException when the text is not one of those two forms
     */
    static int parseHex(@NonNull String text) {
        String body = text.trim();
        if (body.startsWith("#")) {
            body = body.substring(1);
        }
        if (body.length() == 6) {
            return 0xFF000000 | (int) Long.parseLong(body, 16);
        }
        if (body.length() == 8) {
            return (int) Long.parseLong(body, 16);
        }
        throw new NumberFormatException(text);
    }

    @NonNull
    static String toHex(int argb, boolean alpha) {
        return alpha
                ? String.format(Locale.ROOT, "#%08X", argb)
                : String.format(Locale.ROOT, "#%06X", argb & 0xFFFFFF);
    }

    /** Two-tone checkerboard, so a translucent colour stays readable. */
    static void checkerboard(@NonNull Canvas canvas, @NonNull Paint paint,
                             float l, float t, float r, float b, float cell) {
        boolean dark = true;
        for (float y = t; y < b; y += cell) {
            dark = !dark;
            for (float x = l; x < r; x += cell) {
                paint.setColor(dark ? 0xFFBDBDBD : 0xFFF5F5F5);
                canvas.drawRect(x, y, Math.min(x + cell, r), Math.min(y + cell, b), paint);
                dark = !dark;
            }
        }
    }
}
