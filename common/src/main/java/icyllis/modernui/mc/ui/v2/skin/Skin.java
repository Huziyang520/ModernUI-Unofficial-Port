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
import icyllis.modernui.graphics.Canvas;

/**
 * A visual style. Widgets hold a skin and delegate every pixel of their chrome to it, so
 * adding a ninth style means adding one implementation and one registry entry.
 *
 * <p>Text is never drawn here: widgets own their labels and only ask the tokens for the
 * typeface, size and colour.</p>
 */
public interface Skin {

    int GLYPH_NONE = 0;
    int GLYPH_SUN = 1;
    int GLYPH_MONITOR = 2;
    int GLYPH_SCROLL = 3;
    int GLYPH_TYPE = 4;
    int GLYPH_PLUG = 5;
    int GLYPH_TEXT = 6;
    int GLYPH_SEARCH = 7;
    int GLYPH_CLOSE = 8;
    int GLYPH_CHECK = 9;
    int GLYPH_CHEVRON_DOWN = 10;
    int GLYPH_CHEVRON_RIGHT = 11;
    int GLYPH_DASHBOARD = 12;
    int GLYPH_SETTINGS = 13;
    int GLYPH_CODE = 14;
    int GLYPH_MUSIC = 15;
    int GLYPH_PALETTE = 16;

    @NonNull
    SkinTokens tokens();

    /** Stable id, also the value stored in the config. */
    @NonNull
    String id();

    /** Whole screen backdrop. Called once per frame behind everything. */
    void drawBackdrop(@NonNull Canvas canvas, int width, int height);

    /** A raised surface: the shell header, a panel, a popup. */
    void drawPanel(@NonNull Canvas canvas, float l, float t, float r, float b, float radius);

    /** Background of one settings row; {@code first}/{@code last} bound a group. */
    void drawRowBackground(@NonNull Canvas canvas, float l, float t, float r, float b,
                           boolean first, boolean last, boolean pressed);

    void drawDivider(@NonNull Canvas canvas, float x1, float y, float x2);

    /** Section heading rule, drawn under the label. */
    void drawSectionRule(@NonNull Canvas canvas, float l, float y, float r, boolean empty);

    void drawToggle(@NonNull Canvas canvas, float l, float t, float r, float b,
                    float progress, boolean checked, boolean pressed);

    void drawSliderTrack(@NonNull Canvas canvas, float l, float t, float r, float b,
                         float fraction, boolean dragging);

    /** One segment of a segmented control; {@code active} is the selected one. */
    void drawSegment(@NonNull Canvas canvas, float l, float t, float r, float b,
                     int index, int count, boolean active, boolean pressed);

    /** Text field / value button frame. */
    void drawField(@NonNull Canvas canvas, float l, float t, float r, float b, boolean focused);

    void drawButton(@NonNull Canvas canvas, float l, float t, float r, float b,
                    boolean pressed, boolean enabled);

    /** Small icon, centred on (cx, cy). Skins may ignore {@code color} to enforce a hue. */
    void drawGlyph(@NonNull Canvas canvas, int glyph, float cx, float cy, float size, int color);

    /** Selection marker of the index rail. */
    void drawRailMarker(@NonNull Canvas canvas, float l, float t, float r, float b, boolean active);

    /** Outline of the colour field swatch. */
    void drawSwatchFrame(@NonNull Canvas canvas, float l, float t, float r, float b, float radius);
}
