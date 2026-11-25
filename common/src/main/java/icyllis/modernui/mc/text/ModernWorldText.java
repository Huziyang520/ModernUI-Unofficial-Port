/*
 * Modern UI.
 * Copyright (C) 2019-2026 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.text;

import icyllis.modernui.mc.ModernUIMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;

/**
 * Decides whether a piece of 3D world text can use the Modern Text Engine and
 * performs the draw with the feature renderer's vertex buffers.
 */
public final class ModernWorldText {

    private static volatile boolean sLogged;

    private ModernWorldText() {
    }

    public static boolean shouldUse(@Nonnull Font font) {
        if (!ModernUIMod.isTextEngineEnabled()
                || !TextLayoutEngine.sCurrentInWorldRendering
                || !TextLayoutEngine.sUseTextShadersInWorld) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || font != minecraft.font) {
            return false;
        }
        TextLayoutEngine engine = TextLayoutEngine.getInstance();
        if (engine == null || engine.getTextRenderer() == null) {
            return false;
        }
        GlyphManager glyphManager = GlyphManager.getInstance();
        if (glyphManager == null) {
            return false;
        }
        AbstractTexture texture = glyphManager.getFontTexture();
        return texture != null && texture.getTextureView() != null;
    }

    /**
     * @return true when the Modern Text Engine handled the whole submission
     */
    public static boolean draw(@Nonnull Font font, @Nonnull FormattedCharSequence text,
                               float x, float y, int color, boolean dropShadow,
                               Font.DisplayMode displayMode, int lightCoords,
                               int backgroundColor, int outlineColor,
                               @Nonnull Matrix4fc pose, @Nonnull WorldBufferSource source) {
        if (text == FormattedCharSequence.EMPTY || displayMode == null || !shouldUse(font)) {
            return false;
        }
        try {
            ModernTextRenderer renderer = TextLayoutEngine.getInstance().getTextRenderer();
            if (outlineColor != 0) {
                renderer.drawText8xOutlineWorld(text, x, y, color, outlineColor,
                        pose, source, displayMode, lightCoords);
            } else {
                renderer.drawTextWorld(text, x, y, color, dropShadow,
                        pose, source, displayMode, backgroundColor, lightCoords);
            }
            logOnce();
            return true;
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn(TextLayoutEngine.MARKER,
                    "Failed to render 3D world text with the Modern Text Engine", t);
            return false;
        }
    }

    /**
     * One line per session: per-frame logging would flood the log and hide real problems.
     */
    private static void logOnce() {
        if (!sLogged) {
            sLogged = true;
            ModernUIMod.LOGGER.info(TextLayoutEngine.MARKER,
                    "3D world text now uses the Modern Text Engine at SDF pixel density {} (independent of GUI scale)",
                    TextLayoutEngine.sMinPixelDensityForSDF);
        }
    }
}
