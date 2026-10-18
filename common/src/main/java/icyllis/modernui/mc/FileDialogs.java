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
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLInit;
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
 * MC 26.3 is built on SDL3, which has a native file dialog API. SDL3 only accepts the call
 * from the <b>main thread</b> (the thread that initialized SDL video, i.e. Minecraft's game
 * thread): calling it from the Modern UI thread makes SDL invoke the callback immediately
 * with a null selection and the dialog never appears. So the call is handed over to the game
 * thread and this thread waits for the selection, which keeps the previous tinyfd contract:
 * it returns {@code null} when the dialog is cancelled or cannot be shown.
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

    private static final class ShowResult {
        @Nullable
        String path;
        /**
         * SDL reported an error (as opposed to the user cancelling the dialog)
         */
        boolean failed;
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
        try {
            if (count > 0) {
                filterMemory = MemoryUtil.nmemAlloc((long) count * SDL_DialogFileFilter.SIZEOF);
                for (int i = 0; i < count; i++) {
                    String extension = extensions[i];
                    if (extension.startsWith("*.")) {
                        extension = extension.substring(2);
                    } else if (extension.startsWith(".")) {
                        extension = extension.substring(1);
                    }
                    // SDL3 validates the pattern itself: only [a-zA-Z0-9_.-] is allowed and a
                    // '*' may only appear alone, so "*.ogg" is rejected with
                    // "Invalid dialog file filters" (and the dialog never opens). Pass the bare
                    // extension, SDL adds the wildcard.
                    ByteBuffer nameBuffer = MemoryUtil.memUTF8(
                            description != null ? description : "*." + extension);
                    ByteBuffer patternBuffer = MemoryUtil.memUTF8(extension);
                    strings[i * 2] = nameBuffer;
                    strings[i * 2 + 1] = patternBuffer;
                    SDL_DialogFileFilter.create(
                            filterMemory + (long) i * SDL_DialogFileFilter.SIZEOF
                    ).set(nameBuffer, patternBuffer);
                }
                filters = SDL_DialogFileFilter.create(filterMemory, count);
            }
            ShowResult result = showOnce(save, window, filters, defaultName);
            if (result.failed && window != NULL) {
                // the window handle is the only input that can be wrong here, retry unparented
                ModernUIMod.LOGGER.info(MARKER, "Retrying the native file dialog without a parent window");
                result = showOnce(save, NULL, filters, defaultName);
            }
            ModernUIMod.LOGGER.info(MARKER, "Native {} dialog finished in {} ms, selected: {}",
                    save ? "save" : "open", (System.nanoTime() - t0) / 1_000_000L, result.path);
            return result.path;
        } finally {
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
     * One attempt: creates the callback, hands the SDL call over to the game thread and
     * waits for the selection.
     */
    private static ShowResult showOnce(boolean save, long window,
                                       @Nullable SDL_DialogFileFilter.Buffer filters,
                                       @Nullable String defaultName) throws Exception {
        SDLError.SDL_ClearError();
        final CompletableFuture<ShowResult> future = new CompletableFuture<>();
        SDL_DialogFileCallback callback = SDL_DialogFileCallback.create((userdata, filelist, filter) -> {
            ShowResult r = new ShowResult();
            try {
                if (filelist != NULL) {
                    long first = MemoryUtil.memGetAddress(filelist);
                    if (first != NULL) {
                        r.path = MemoryUtil.memUTF8(first);
                    }
                } else {
                    // SDL reports both a cancel and a failure with a null list; only a
                    // failure leaves an error message behind (cleared before each attempt)
                    String error = SDLError.SDL_GetError();
                    if (filter == -1 && error != null && !error.isEmpty()) {
                        r.failed = true;
                        ModernUIMod.LOGGER.warn(MARKER, "The native file dialog failed: {}", error);
                    }
                }
            } catch (Throwable t) {
                ModernUIMod.LOGGER.warn(MARKER, "Failed to read the selected file", t);
            }
            future.complete(r);
        });
        PENDING.add(callback);
        final ByteBuffer defaultLocation = defaultName != null ? MemoryUtil.memUTF8(defaultName) : null;
        Runnable show = () -> {
            try {
                if (save) {
                    SDLDialog.SDL_ShowSaveFileDialog(callback, NULL, window, filters, defaultLocation);
                } else {
                    SDLDialog.SDL_ShowOpenFileDialog(callback, NULL, window, filters, defaultLocation, false);
                }
            } catch (Throwable t) {
                ModernUIMod.LOGGER.warn(MARKER, "Failed to open the native file dialog", t);
                future.complete(new ShowResult());
            } finally {
                if (defaultLocation != null) {
                    // SDL only reads the location while creating the dialog
                    MemoryUtil.memFree(defaultLocation);
                }
            }
        };
        // SDL3 only supports being called from the main thread, which is not the UI thread
        if (SDLInit.SDL_IsMainThread()) {
            show.run();
        } else {
            Minecraft.getInstance().execute(show);
        }
        ShowResult result;
        try {
            result = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // the dialog is still open and SDL still owns the callback, never free it
            ModernUIMod.LOGGER.warn(MARKER, "The native file dialog did not finish within {} ms",
                    TIMEOUT_MS);
            return new ShowResult();
        }
        PENDING.remove(callback);
        callback.free();
        return result;
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
