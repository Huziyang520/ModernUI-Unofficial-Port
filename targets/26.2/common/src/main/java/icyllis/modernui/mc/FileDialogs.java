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

package icyllis.modernui.mc;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.annotation.Nullable;

/**
 * MC 26.2 still ships GLFW and tinyfd, so the native dialogs use the same API and
 * the same signatures as the 26.3 SDL3 implementation.
 */
public final class FileDialogs {

    private static final Marker MARKER = MarkerManager.getMarker("FileDialog");

    private FileDialogs() {
    }

    @Nullable
    public static String openFileDialog(@Nullable String title, @Nullable String description,
                                        String... extensions) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = filters(stack, extensions);
            if (filters == null) {
                return null;
            }
            return TinyFileDialogs.tinyfd_openFileDialog(title, null, filters, description, false);
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn(MARKER, "Failed to show the native open dialog", t);
            return null;
        }
    }

    @Nullable
    public static String saveFileDialog(@Nullable String defaultFileName, @Nullable String description,
                                        String... extensions) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = filters(stack, extensions);
            if (filters == null) {
                return null;
            }
            return TinyFileDialogs.tinyfd_saveFileDialog(null, defaultFileName, filters, description);
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn(MARKER, "Failed to show the native save dialog", t);
            return null;
        }
    }

    @Nullable
    private static PointerBuffer filters(MemoryStack stack, @Nullable String[] extensions) {
        if (extensions == null || extensions.length == 0) {
            return null;
        }
        PointerBuffer filters = stack.mallocPointer(extensions.length);
        for (String extension : extensions) {
            filters.put(stack.nUTF8("*." + extension, true));
        }
        filters.rewind();
        return filters;
    }
}
