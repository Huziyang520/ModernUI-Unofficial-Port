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

package icyllis.modernui.mc.ui.studio;

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.ui.AccentPalette;
import icyllis.modernui.resources.TypedValue;

/**
 * Palette and metrics of the settings studio. Colours are derived from the player's accent
 * so every surface in the screen moves together when the theme colour changes.
 */
public final class Studio {

    public static final int RADIUS_TILE = 22;
    public static final int RADIUS_PANEL = 26;
    public static final int RADIUS_CONTROL = 999;
    public static final int ROW_MIN_HEIGHT = 58;

    public static final int GAP = 12;
    public static final int PAD = 18;

    private Studio() {
    }

    public static int dp(@NonNull Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static int attr(@NonNull Context context, @NonNull String name, int fallback) {
        var value = new TypedValue();
        if (context.getTheme().resolveAttribute(R.ns, name, value, true)) {
            return (int) context.getResources().loadColorStateList(value, null, context.getTheme())
                    .getDefaultColor();
        }
        return fallback;
    }

    public static int accent(@NonNull Context context) {
        var palette = AccentPalette.get(context);
        if (palette != null && AccentPalette.isCustom()) {
            return palette.primary;
        }
        return attr(context, R.attr.colorPrimary, 0xFF8AB4F8);
    }

    /** A hue-rotated sibling of the accent, used to give each tile its own colour. */
    public static int variant(@NonNull Context context, int index) {
        int accent = accent(context);
        float[] hsv = new float[3];
        Color.RGBToHSV(accent, hsv);
        float hue = (hsv[0] + index * 32f) % 360f;
        float sat = Math.max(0.30f, Math.min(0.75f, hsv[1]));
        float val = Math.min(0.92f, Math.max(0.55f, hsv[2]));
        return Color.HSVToColor(hue, sat, val);
    }

    public static int surface(@NonNull Context context) {
        return attr(context, R.attr.colorSurface, 0xFF121016);
    }

    public static int surfaceHigh(@NonNull Context context) {
        return attr(context, R.attr.colorSurfaceContainerHigh, 0xFF1E1B23);
    }

    public static int onSurface(@NonNull Context context) {
        return attr(context, R.attr.colorOnSurface, 0xFFE8E2EC);
    }

    public static int muted(@NonNull Context context) {
        return alpha(onSurface(context), 0.62f);
    }

    public static int outline(@NonNull Context context) {
        return alpha(attr(context, R.attr.colorOutlineVariant, 0xFF3A3540), 0.9f);
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

    public static float clamp(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    public static int contrasting(int color) {
        float luma = (((color >> 16) & 0xFF) * 0.299f + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f) / 255f;
        return luma > 0.55f ? 0xFF141018 : 0xFFFFFFFF;
    }

    @NonNull
    public static ShapeDrawable pill(@NonNull Context context, int color) {
        var drawable = new ShapeDrawable();
        drawable.setShape(ShapeDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, RADIUS_CONTROL));
        return drawable;
    }

    @NonNull
    public static ShapeDrawable rounded(@NonNull Context context, int color, float radius) {
        var drawable = new ShapeDrawable();
        drawable.setShape(ShapeDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radius));
        return drawable;
    }
}
