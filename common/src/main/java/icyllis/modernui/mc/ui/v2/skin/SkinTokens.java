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

package icyllis.modernui.mc.ui.v2.skin;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.mc.ui.v2.TgTheme;
import icyllis.modernui.text.Typeface;

/**
 * Pure data of one skin: palette, metrics and motion. Skins build it from the running
 * {@link TgTheme}, so a theme switch only needs a rebuild.
 *
 * <p>All sizes are dp, the drawing code converts with {@link TgTheme#dp(float)}.</p>
 */
public final class SkinTokens {

    /** Pixels per dp, so skins convert their own metrics without a context. */
    public float density = 1f;

    /** Palette roles. See the style spec: every skin uses these names. */
    public int bg;
    public int panel;
    public int panelAlt;
    public int line;
    public int text;
    public int muted;
    public int accent;
    public int onAccent;
    public int accentSoft;
    public int danger;

    /** Metrics. */
    public float radiusPanel = 12f;
    public float radiusControl = 6f;
    public float rowHeight = 56f;
    public float padH = 16f;
    public float padV = 8f;
    public float lineWidth = 1f;
    public float gap = 8f;
    public float sectionGap = 18f;

    /** Type scale, sp. */
    public float titleSize = 13.5f;
    public float descSize = 10.5f;
    public float sectionSize = 11f;
    public float valueSize = 11f;

    /** Typography shape. */
    public boolean upperCaseSections;
    public boolean upperCaseRows;
    public boolean monoNumerals;

    /** Motion, ms. Zero means instant. */
    public long motionMs = 160L;
    public TgEase ease = TgEase.OUT_CUBIC;

    /** Decoration switches the drawing code consults. */
    public boolean texture;          // grid / scanline / checkerboard backdrop
    public boolean cornerMarks;      // L shaped registration marks
    public boolean glow;             // stacked strokes
    public boolean dashedDivider;
    public boolean showGlyphRail;    // index rail uses glyphs instead of letters

    /** True when toggles are drawn as words rather than as a pill. */
    public boolean wordToggle;

    /** How many stacked strokes fake a glow. 0 disables. */
    public int glowLayers = 3;

    /** Resolved typefaces; null means the widget keeps its own. */
    public Typeface typeface;
    public Typeface monoTypeface;
    public Typeface titleTypeface;

    public SkinTokens() {
    }

    @NonNull
    public static SkinTokens defaults(@NonNull TgTheme theme) {
        var t = new SkinTokens();
        t.density = theme.density;
        t.bg = theme.dark ? TgTheme.shade(theme.surface, 0.9f) : theme.surface;
        t.panel = theme.surfaceHigh;
        t.panelAlt = TgTheme.mix(theme.surfaceHigh, theme.surface, 0.5f);
        t.line = TgTheme.alpha(theme.onSurface, 0.14f);
        t.text = theme.onSurface;
        t.muted = TgTheme.alpha(theme.onSurface, 0.62f);
        t.accent = theme.accent;
        t.onAccent = theme.onAccent;
        t.accentSoft = theme.accentSoft;
        t.danger = 0xFFE5484D;
        return t;
    }

    /** Convenience: the easing function of this skin. */
    public float ease(float t) {
        return ease.ease(t);
    }

    public enum TgEase {
        LINEAR,
        OUT_CUBIC,
        IN_OUT_CUBIC,
        OUT_BACK;

        public float ease(float t) {
            return switch (this) {
                case LINEAR -> t;
                case OUT_CUBIC -> 1f - (1f - t) * (1f - t) * (1f - t);
                case IN_OUT_CUBIC -> t < 0.5f
                        ? 4f * t * t * t
                        : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
                case OUT_BACK -> {
                    float c1 = 1.70158f;
                    float c3 = c1 + 1f;
                    yield 1f + c3 * (t - 1f) * (t - 1f) * (t - 1f) + c1 * (t - 1f) * (t - 1f);
                }
            };
        }
    }
}
