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

package icyllis.modernui.mc.ui;

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.resources.TypedValue;

/**
 * Design tokens for the settings UI: one place for spacing, radii and the surface
 * roles, so the whole screen keeps a single visual language.
 */
public final class Design {

    /** Spacing scale, in dp. */
    public static final int SPACE_1 = 4;
    public static final int SPACE_2 = 8;
    public static final int SPACE_3 = 12;
    public static final int SPACE_4 = 16;
    public static final int SPACE_5 = 20;
    public static final int SPACE_6 = 24;

    /** Corner radii, in dp. */
    public static final float RADIUS_CARD = 18f;
    public static final float RADIUS_ROW = 12f;
    public static final float RADIUS_PILL = 999f;

    /** Row metrics, in dp. Comfortable is the default, compact trims the rhythm. */
    public static final int ROW_HEIGHT = 52;
    public static final int ROW_HEIGHT_COMPACT = 42;

    /** Content column, in dp. */
    public static final int CONTENT_MAX_WIDTH = 760;

    private Design() {
    }

    public static boolean compact() {
        return SurfacePrefs.compactLayout();
    }

    public static int rowHeight(@NonNull Context context) {
        return dp(context, compact() ? ROW_HEIGHT_COMPACT : ROW_HEIGHT);
    }

    public static int dp(@NonNull Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int resolve(@NonNull Context context, @NonNull String attr, int fallback) {
        var value = new TypedValue();
        if (context.getTheme().resolveAttribute(R.ns, attr, value, true)) {
            return (int) context.getResources().loadColorStateList(value, null, context.getTheme())
                    .getDefaultColor();
        }
        return fallback;
    }

    /** The player's accent when set, otherwise the theme's primary role. */
    public static int accent(@NonNull Context context) {
        var palette = AccentPalette.get(context);
        if (palette != null && AccentPalette.isCustom()) {
            return palette.primary;
        }
        return resolve(context, R.attr.colorPrimary, 0xFF8AB4F8);
    }

    public static int accentContainer(@NonNull Context context) {
        var palette = AccentPalette.get(context);
        if (palette != null && AccentPalette.isCustom()) {
            return palette.primaryContainer;
        }
        return resolve(context, R.attr.colorPrimaryContainer, 0xFF24425F);
    }

    public static int onAccentContainer(@NonNull Context context) {
        var palette = AccentPalette.get(context);
        if (palette != null && AccentPalette.isCustom()) {
            return palette.onPrimaryContainer;
        }
        return resolve(context, R.attr.colorOnPrimaryContainer, 0xFFD6E6FF);
    }

    public static int surface(@NonNull Context context) {
        return resolve(context, R.attr.colorSurface, 0xFF141218);
    }

    public static int surfaceHigh(@NonNull Context context) {
        return resolve(context, R.attr.colorSurfaceContainerHigh, 0xFF22201A);
    }

    public static int onSurface(@NonNull Context context) {
        return resolve(context, R.attr.colorOnSurface, 0xFFE6E1E5);
    }

    public static int onSurfaceVariant(@NonNull Context context) {
        return resolve(context, R.attr.colorOnSurfaceVariant, 0xFFCAC4D0);
    }

    public static int outline(@NonNull Context context) {
        return resolve(context, R.attr.colorOutline, 0xFF938F99);
    }

    public static int outlineVariant(@NonNull Context context) {
        return resolve(context, R.attr.colorOutlineVariant, 0xFF49454F);
    }

    public static int alpha(int color, float factor) {
        int a = Math.round(((color >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, factor)));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int mix(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Black or white, whichever stays readable on top of the given color. */
    public static int contrasting(int color) {
        float luminance = (((color >> 16) & 0xFF) * 0.299f
                + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f) / 255f;
        return luminance > 0.55f ? 0xFF1A1A1A : 0xFFFFFFFF;
    }

    @NonNull
    public static ShapeDrawable rounded(@NonNull Context context, int color, float radiusDp) {
        var drawable = new ShapeDrawable();
        drawable.setShape(ShapeDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    @NonNull
    public static ShapeDrawable outlined(@NonNull Context context, int color,
                                         int strokeColor, float radiusDp, float strokeDp) {
        var drawable = rounded(context, color, radiusDp);
        drawable.setStroke(dp(context, strokeDp), strokeColor);
        return drawable;
    }
}
