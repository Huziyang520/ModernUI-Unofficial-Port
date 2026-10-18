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

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * MC 26.3 removed {@code org.lwjgl.util.tinyfd} together with the GLFW dependency, so the
 * native file dialogs are gone. The obvious Swing fallback does not work either:
 * {@code net.minecraft.client.main.Main} sets {@code java.awt.headless=true} in its static
 * initializer, therefore every {@code JFileChooser} / {@code FileDialog} call throws
 * {@code HeadlessException} and no dialog ever shows up (symptom: "Play A Music" and the
 * font file browser do nothing on 26.3).
 * <p>
 * MC 26.3 is built on SDL3, which has a native file dialog API: the platform dialog is run
 * by SDL on its own thread and the selection is delivered through the event queue, so it
 * needs neither AWT nor the caller to be on the main thread. This class is a blocking
 * wrapper around it that keeps the previous tinyfd contract: it returns {@code null} when
 * the dialog is cancelled or cannot be shown.
 *
 * @since 3.13
 */
public final class FileDialogs {

    private static final Marker MARKER = MarkerManager.getMarker("FileDialog");

    private static final long TIMEOUT_MS = 5 * 60 * 1000L;

    /**
     * SDL only stores a native pointer to the callback, so the Java object must stay
     * reachable until it has been invoked.
     */
    private static final Set<SDL_DialogFileCallback> PENDING = ConcurrentHashMap.newKeySet();

    private FileDialogs() {
    }

    /**
     * @param extensions file extensions without the leading dot, e.g. {@code "png"}
     */
    @Nullable
    public static String openFileDialog(@Nullable String title, @Nullable String description,
                                        String... extensions) {
        return showDialog(false, title, description, null, extensions);
    }

    @Nullable
    public static String saveFileDialog(@Nullable String defaultFileName, @Nullable String description,
                                        String... extensions) {
        return showDialog(true, "Save File", description, defaultFileName, extensions);
    }

    @Nullable
    private static String showDialog(boolean save, @Nullable String title,
                                     @Nullable String description, @Nullable String defaultName,
                                     String... extensions) {
        try {
            return showNativeDialog(save, title, description, defaultName, extensions);
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn(MARKER, "Failed to show the native file dialog", t);
        }
        return showFallbackDialog(save, title, description, defaultName, extensions);
    }

    /**
     * Shows the SDL3 native dialog and waits for the user (or the timeout).
     *
     * @return the selected path, or null when cancelled
     */
    @Nullable
    private static String showNativeDialog(boolean save, @Nullable String title,
                                           @Nullable String description, @Nullable String defaultName,
                                           String... extensions) throws Exception {
        long t0 = System.nanoTime();
        long window = Minecraft.getInstance().getWindow().handle();
        final int count = extensions == null ? 0 : extensions.length;
        final ByteBuffer[] strings = new ByteBuffer[count * 2];
        long filterMemory = NULL;
        SDL_DialogFileFilter.Buffer filters = null;
        SDL_DialogFileCallback callback = null;
        final CompletableFuture<String> result = new CompletableFuture<>();
        try {
            if (count > 0) {
                filterMemory = MemoryUtil.nmemAlloc((long) count * SDL_DialogFileFilter.SIZEOF);
                for (int i = 0; i < count; i++) {
                    String pattern = extensions[i].startsWith("*.") ? extensions[i]
                            : "*." + (extensions[i].startsWith(".") ? extensions[i].substring(1) : extensions[i]);
                    ByteBuffer nameBuffer = MemoryUtil.memUTF8(description != null ? description : pattern);
                    ByteBuffer patternBuffer = MemoryUtil.memUTF8(pattern);
                    strings[i * 2] = nameBuffer;
                    strings[i * 2 + 1] = patternBuffer;
                    SDL_DialogFileFilter.create(
                            filterMemory + (long) i * SDL_DialogFileFilter.SIZEOF
                    ).set(nameBuffer, patternBuffer);
                }
                filters = SDL_DialogFileFilter.create(filterMemory, count);
            }
            callback = SDL_DialogFileCallback.create((userdata, filelist, filter) -> {
                String path = null;
                try {
                    if (filelist != NULL) {
                        long first = MemoryUtil.memGetAddress(filelist);
                        if (first != NULL) {
                            path = MemoryUtil.memUTF8(first);
                        }
                    }
                } catch (Throwable t) {
                    ModernUIMod.LOGGER.warn(MARKER, "Failed to read the selected file", t);
                }
                result.complete(path);
            });
            PENDING.add(callback);
            ByteBuffer defaultLocation = null;
            try {
                if (defaultName != null) {
                    defaultLocation = MemoryUtil.memUTF8(defaultName);
                }
                if (save) {
                    SDLDialog.SDL_ShowSaveFileDialog(callback, NULL, window, filters, defaultLocation);
                } else {
                    SDLDialog.SDL_ShowOpenFileDialog(callback, NULL, window, filters,
                            defaultLocation, false);
                }
            } finally {
                if (defaultLocation != null) {
                    MemoryUtil.memFree(defaultLocation);
                }
            }
            String path;
            try {
                path = result.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // the dialog is still open and SDL still owns the callback, never free it
                ModernUIMod.LOGGER.warn(MARKER, "The native file dialog did not finish within {} ms",
                        TIMEOUT_MS);
                return null;
            }
            ModernUIMod.LOGGER.info(MARKER, "Native {} dialog finished in {} ms, selected: {}",
                    save ? "save" : "open", (System.nanoTime() - t0) / 1_000_000L, path);
            PENDING.remove(callback);
            callback.free();
            callback = null;
            return path;
        } finally {
            // a callback that was already handed to SDL and never invoked must not be freed
            if (callback != null && !PENDING.contains(callback)) {
                callback.free();
            }
            if (filterMemory != NULL) {
                MemoryUtil.nmemFree(filterMemory);
            }
            for (ByteBuffer s : strings) {
                if (s != null) {
                    MemoryUtil.memFree(s);
                }
            }
        }
    }

    /**
     * Last resort for platforms where the SDL3 dialog is unavailable. Only reachable when AWT
     * is not headless, which is not the case in Minecraft (see the class javadoc).
     */
    @Nullable
    private static String showFallbackDialog(boolean save, @Nullable String title,
                                             @Nullable String description, @Nullable String defaultName,
                                             String... extensions) {
        try {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                ModernUIMod.LOGGER.error(MARKER, "Cannot show a file dialog: the game runs with " +
                        "java.awt.headless=true and the SDL3 file dialog is unavailable");
                return null;
            }
            java.awt.FileDialog dialog = new java.awt.FileDialog((java.awt.Frame) null,
                    title != null ? title : (save ? "Save File" : "Open File"),
                    save ? java.awt.FileDialog.SAVE : java.awt.FileDialog.LOAD);
            if (defaultName != null) {
                dialog.setFile(defaultName);
            }
            if (extensions != null && extensions.length > 0) {
                dialog.setFilenameFilter((dir, name) -> {
                    String lower = name.toLowerCase(Locale.ROOT);
                    for (String ext : extensions) {
                        String normalized = ext.startsWith(".") ? ext.substring(1) : ext;
                        if (lower.endsWith("." + normalized.toLowerCase(Locale.ROOT))) {
                            return true;
                        }
                    }
                    return false;
                });
                ModernUIMod.LOGGER.info(MARKER, "Showing the AWT fallback dialog ({})", description);
            }
            dialog.setVisible(true);
            String file = dialog.getFile();
            String dir = dialog.getDirectory();
            dialog.dispose();
            if (file == null) {
                return null;
            }
            return dir != null ? new File(dir, file).getAbsolutePath() : file;
        } catch (Throwable t) {
            ModernUIMod.LOGGER.error(MARKER, "Failed to show the AWT file dialog", t);
        }
        return null;
    }
}
