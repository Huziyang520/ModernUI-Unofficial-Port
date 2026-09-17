/*
 * Modern UI.
 * Copyright (C) 2019-2025 BloCamLimb. All rights reserved.
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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * MC 26.3 removed {@code org.lwjgl.util.tinyfd} along with the GLFW dependency,
 * so the native file dialogs are no longer available. This is a Swing-based
 * fallback that keeps the same contract as before: it returns {@code null} when
 * the dialog is cancelled or cannot be shown.
 *
 * @since 3.13
 */
public final class FileDialogs {

    private FileDialogs() {
    }

    /**
     * @param extensions file extensions without the leading dot, e.g. {@code "png"}
     */
    public static String openFileDialog(String title, String description, String... extensions) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(title != null ? title : "Open File");
            if (description != null && extensions != null && extensions.length > 0) {
                chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
            }
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile().getAbsolutePath();
            }
        } catch (Throwable t) {
            // headless environment or AWT unavailable
        }
        return null;
    }

    public static String saveFileDialog(String defaultFileName, String description, String... extensions) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save File");
            if (defaultFileName != null) {
                chooser.setSelectedFile(new File(defaultFileName));
            }
            if (description != null && extensions != null && extensions.length > 0) {
                chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
            }
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile().getAbsolutePath();
            }
        } catch (Throwable t) {
            // headless environment or AWT unavailable
        }
        return null;
    }
}
