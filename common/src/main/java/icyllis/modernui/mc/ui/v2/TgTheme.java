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

package icyllis.modernui.mc.ui.v2;

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.mc.ui.AccentPalette;
import icyllis.modernui.resources.TypedValue;

/**
 * Immutable snapshot of the colours a skin draws with. Everything a skin needs that comes
 * from the running theme is resolved once here, so skins never touch theme attributes.
 */
public final class TgTheme {

    public final float density;
    public final boolean dark;

    /** Player accent when set, otherwise the theme primary role. */
    public final int accent;
    public final int onAccent;
    public final int accentSoft;

    public final int surface;
    public final int surfaceHigh;
    public final int onSurface;
    public final int onSurfaceVariant;
    public final int outline;

    private TgTheme(float density, boolean dark, int accent, int onAccent, int accentSoft,
                    int surface, int surfaceHigh, int onSurface, int onSurfaceVariant, int outline) {
        this.density = density;
        this.dark = dark;
        this.accent = accent;
        this.onAccent = onAccent;
        this.accentSoft = accentSoft;
        this.surface = surface;
        this.surfaceHigh = surfaceHigh;
        this.onSurface = onSurface;
        this.onSurfaceVariant = onSurfaceVariant;
        this.outline = outline;
    }

    @NonNull
    public static TgTheme of(@NonNull Context context) {
        boolean dark = luminance(resolve(context, R.attr.colorSurface, 0xFF141218)) < 0.5f;
        int accent;
        int onAccent;
        int accentSoft;
        var palette = AccentPalette.get(context);
        if (palette != null && AccentPalette.isCustom()) {
            accent = palette.primary;
            onAccent = palette.onPrimary;
            accentSoft = alpha(palette.primary, dark ? 0.22f : 0.16f);
        } else {
            accent = resolve(context, R.attr.colorPrimary, 0xFF8AB4F8);
            onAccent = resolve(context, R.attr.colorOnPrimary, contrasting(accent));
            accentSoft = alpha(accent, dark ? 0.22f : 0.16f);
        }
        return new TgTheme(
                context.getResources().getDisplayMetrics().density,
                dark,
                accent,
                onAccent,
                accentSoft,
                resolve(context, R.attr.colorSurface, 0xFF141218),
                resolve(context, R.attr.colorSurfaceContainerHigh, 0xFF1E1B23),
                resolve(context, R.attr.colorOnSurface, 0xFFE8E2EC),
                resolve(context, R.attr.colorOnSurfaceVariant, 0xFFCAC4D0),
                resolve(context, R.attr.colorOutlineVariant, 0xFF3A3540));
    }

    private static int resolve(@NonNull Context context, @NonNull String name, int fallback) {
        var value = new TypedValue();
        if (context.getTheme().resolveAttribute(R.ns, name, value, true)) {
            return (int) context.getResources().loadColorStateList(value, null, context.getTheme())
                    .getDefaultColor();
        }
        return fallback;
    }

    public int dp(float value) {
        return (int) (value * density + 0.5f);
    }

    public static int alpha(int color, float factor) {
        int a = Math.round(((color >>> 24) & 0xFF) * clamp(factor));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int mix(int from, int to, float t) {
        t = clamp(t);
        int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Scales the luminance of a colour, keeping hue and saturation. */
    public static int shade(int color, float factor) {
        float[] hsv = new float[3];
        Color.RGBToHSV(color, hsv);
        hsv[2] = clamp(hsv[2] * factor);
        return Color.HSVToColor(hsv) | 0xFF000000;
    }

    public static int rotate(int color, float degrees) {
        float[] hsv = new float[3];
        Color.RGBToHSV(color, hsv);
        hsv[0] = (hsv[0] + degrees) % 360f;
        if (hsv[0] < 0f) {
            hsv[0] += 360f;
        }
        return Color.HSVToColor(hsv) | 0xFF000000;
    }

    public static float clamp(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    public static float luminance(int color) {
        return (((color >> 16) & 0xFF) * 0.299f + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f) / 255f;
    }

    public static int contrasting(int color) {
        return luminance(color) > 0.55f ? 0xFF141018 : 0xFFFFFFFF;
    }
}
