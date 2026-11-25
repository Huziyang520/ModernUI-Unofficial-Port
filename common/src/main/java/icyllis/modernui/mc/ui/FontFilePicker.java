package icyllis.modernui.mc.ui;

import icyllis.modernui.mc.ModernUIMod;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Reflective facade over the native file dialog. The dialog is only present from
 * 26.3 on, so 26.2 must not reference the class at compile time.
 */
public final class FontFilePicker {

    private static final Marker MARKER = MarkerManager.getMarker("FileDialog");

    private FontFilePicker() {
    }

    /**
     * Returns the chosen path, or {@code null} when the dialog is absent, cancelled or
     * the platform cannot open one.
     */
    public static String openFontFile() {
        try {
            Class<?> dialogs = Class.forName("icyllis.modernui.mc.FileDialogs");
            var method = dialogs.getMethod("openFileDialog", String.class, String.class,
                    String[].class);
            String path = (String) method.invoke(null, "Open Font File",
                    "TrueType/OpenType Fonts (*.ttf;*.otf;*.ttc;*.otc)",
                    new String[]{"ttf", "otf", "ttc", "otc"});
            if (path != null) {
                ModernUIMod.LOGGER.info(MARKER, "Selected font file: {}", path);
            }
            return path;
        } catch (ClassNotFoundException e) {
            ModernUIMod.LOGGER.warn(MARKER, "Native file dialogs are not available on this version; " +
                    "enter the font path manually");
        } catch (Exception e) {
            ModernUIMod.LOGGER.warn(MARKER, "Failed to open the font file dialog", e);
        }
        return null;
    }
}
