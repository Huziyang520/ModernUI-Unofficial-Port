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

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import org.joml.Matrix4f;

/**
 * The glyph renderer is package-private, so its pose and owning feature renderer
 * are read through methods merged by {@link icyllis.modernui.mc.text.mixin.MixinTextFeatureRenderer}.
 */
public interface GlyphRendererAccess {

    Matrix4f modernui$getPose();

    int modernui$getLightCoords();

    Font.DisplayMode modernui$getDisplayMode();

    TextFeatureRenderer modernui$getOwner();
}
