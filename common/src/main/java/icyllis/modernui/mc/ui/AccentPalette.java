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
import icyllis.modernui.graphics.Color;
import icyllis.modernui.mc.ModernUIClient;
import icyllis.modernui.resources.TypedValue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Player-selected accent color expanded into the Material tonal roles this mod draws with.
 * The palette lives outside the config specs on purpose: it is written to the bootstrap
 * properties, so it can be read before themes are applied.
 */
public final class AccentPalette {

    /** Returned by {@link #resolve(int, int)} when the player did not pick an accent. */
    public static final int NO_OVERRIDE = Integer.MIN_VALUE;

    private static final String BOOTSTRAP_KEY = "modernui_mc_accentColor";

    public interface Listener {
        void onAccentChanged();
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private static volatile int sSeed = -1;
    private static volatile AccentPalette sCurrent;
    private static volatile Boolean sDark;

    public final int seed;
    public final int primary;
    public final int onPrimary;
    public final int primaryContainer;
    public final int onPrimaryContainer;
    public final int secondary;
    public final int onSecondary;
    public final int secondaryContainer;
    public final int onSecondaryContainer;
    public final int highlight;

    private AccentPalette(int seed, boolean dark) {
        this.seed = seed;
        float h = hueOf(seed);
        float s = saturationOf(seed);
        float accentSat = Math.max(0.45f, Math.min(0.85f, s < 0.12f ? 0.42f : s * 1.15f));

        if (dark) {
            primary = hsl(h, accentSat, 0.76f);
            onPrimary = hsl(h, accentSat * 0.9f, 0.14f);
            primaryContainer = hsl(h, accentSat * 0.85f, 0.32f);
            onPrimaryContainer = hsl(h, accentSat * 0.8f, 0.90f);
            secondary = hsl(h, 0.20f, 0.78f);
            onSecondary = hsl(h, 0.16f, 0.16f);
            secondaryContainer = hsl(h, 0.22f, 0.33f);
            onSecondaryContainer = hsl(h, 0.20f, 0.92f);
        } else {
            primary = hsl(h, accentSat, 0.42f);
            onPrimary = 0xFFFFFFFF;
            primaryContainer = hsl(h, accentSat * 0.9f, 0.90f);
            onPrimaryContainer = hsl(h, accentSat * 0.9f, 0.12f);
            secondary = hsl(h, 0.24f, 0.40f);
            onSecondary = 0xFFFFFFFF;
            secondaryContainer = hsl(h, 0.28f, 0.90f);
            onSecondaryContainer = hsl(h, 0.26f, 0.14f);
        }
        highlight = (primary & 0x00FFFFFF) | 0x33000000;
    }

    private static volatile boolean sInitialized;

    /** Warms the palette once so the first screen switch does not pay a theme lookup. */
    public static void init(@NonNull Context context) {
        if (!sInitialized) {
            sInitialized = true;
            seed();
            get(context);
        }
    }

    public static void addListener(@NonNull Listener listener) {
        LISTENERS.add(listener);
    }

    public static void removeListener(@NonNull Listener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Reads the persisted accent. Safe to call repeatedly, cheap after the first call.
     */
    public static int seed() {
        int seed = sSeed;
        if (seed < 0) {
            seed = loadSeedFromConfig();
            if (seed == 0) {
                // value written before the option existed, adopt it once
                seed = loadSeed();
            }
            sSeed = seed;
        }
        return seed;
    }

    private static int loadSeedFromConfig() {
        String value = SurfacePrefs.themeColor();
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Color.parseColor(value.trim()) | 0xFF000000;
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isCustom() {
        return seed() != 0;
    }

    /**
     * @param argb seed color, or 0 to follow the theme
     */
    public static void setSeed(int argb) {
        if (argb != 0) {
            argb |= 0xFF000000;
        }
        sSeed = argb;
        sCurrent = null;
        String text = argb == 0 ? "" : String.format(java.util.Locale.ROOT, "#%08X", argb);
        SurfacePrefs.setThemeColor(text);
        // the bootstrap copy is read during theme resolution, before config maps exist
        ModernUIClient.setBootstrapProperty(BOOTSTRAP_KEY, text);
        for (var listener : LISTENERS) {
            listener.onAccentChanged();
        }
    }

    private static int loadSeed() {
        String value = ModernUIClient.getBootstrapProperty(BOOTSTRAP_KEY);
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Color.parseColor(value.trim()) | 0xFF000000;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Detects light/dark from the running theme, then caches the derived palette.
     */
    public static AccentPalette get(@NonNull Context context) {
        var palette = sCurrent;
        if (palette != null) {
            return palette;
        }
        Boolean dark = sDark;
        if (dark == null) {
            dark = detectDark(context);
            sDark = dark;
        }
        palette = new AccentPalette(seed(), dark);
        sCurrent = palette;
        return palette;
    }

    /** Accent roles are rebuilt when the theme switches between light and dark. */
    public static void invalidate() {
        sCurrent = null;
        sDark = null;
    }

    private static boolean detectDark(@NonNull Context context) {
        var value = new TypedValue();
        if (context.getTheme().resolveAttribute(R.ns, R.attr.colorSurface, value, true)) {
            int argb = value.data;
            float luminance = (((argb >> 16) & 0xFF) * 0.299f
                    + ((argb >> 8) & 0xFF) * 0.587f
                    + (argb & 0xFF) * 0.114f) / 255f;
            return luminance < 0.5f;
        }
        return true;
    }

    /**
     * Hook for the theme attribute override. Returns {@link #NO_OVERRIDE} for everything
     * that is not a tonal role or when no accent is configured.
     */
    public static int resolve(String attrName, int original) {
        if (attrName == null || seed() == 0) {
            return NO_OVERRIDE;
        }
        var palette = sCurrent;
        if (palette == null) {
            // no display metrics available in this path, assume the dark theme
            palette = new AccentPalette(seed(), true);
            sCurrent = palette;
        }
        return switch (attrName) {
            case R.attr.colorPrimary -> palette.primary;
            case R.attr.colorSecondary -> palette.secondary;
            case R.attr.colorSecondaryContainer -> palette.secondaryContainer;
            case R.attr.colorControlHighlight -> palette.highlight;
            default -> NO_OVERRIDE;
        };
    }

    // --- color math ---------------------------------------------------------

    private static float hueOf(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        if (delta == 0f) {
            return 0f;
        }
        float h;
        if (max == r) {
            h = 60f * (((g - b) / delta) % 6f);
        } else if (max == g) {
            h = 60f * ((b - r) / delta + 2f);
        } else {
            h = 60f * ((r - g) / delta + 4f);
        }
        return h < 0f ? h + 360f : h;
    }

    private static float saturationOf(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float lightness = (max + min) / 2f;
        float delta = max - min;
        if (delta == 0f) {
            return 0f;
        }
        return delta / (1f - Math.abs(2f * lightness - 1f));
    }

    private static int hsl(float h, float s, float l) {
        float c = (1f - Math.abs(2f * l - 1f)) * s;
        float hh = (h % 360f) / 60f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float r, g, b;
        if (hh < 1f) {
            r = c; g = x; b = 0f;
        } else if (hh < 2f) {
            r = x; g = c; b = 0f;
        } else if (hh < 3f) {
            r = 0f; g = c; b = x;
        } else if (hh < 4f) {
            r = 0f; g = x; b = c;
        } else if (hh < 5f) {
            r = x; g = 0f; b = c;
        } else {
            r = c; g = 0f; b = x;
        }
        float m = l - c / 2f;
        return 0xFF000000
                | ((int) ((r + m) * 255f + 0.5f) << 16)
                | ((int) ((g + m) * 255f + 0.5f) << 8)
                | (int) ((b + m) * 255f + 0.5f);
    }
}
