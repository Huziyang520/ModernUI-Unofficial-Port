package icyllis.modernui.mc;

import java.util.List;

/**
 * Typeface defaults of the regular build, which always carries the bundled font pack.
 * The lite edition replaces this class because it ships no fonts at all.
 */
public final class FontDefaults {

    public static final String FIRST_FAMILY = "Inter Frozen Medium";

    public static final List<String> FALLBACK_FAMILIES = List.of("Source Han Sans CN Medium");

    /** The regular build bundles the emoji images, so the option may be on by default. */
    public static final boolean USE_COLOR_EMOJI = true;

    private FontDefaults() {
    }

    /**
     * Maps the configured first family to the family that is actually loaded. The
     * bundled families are always registered here, so the configured value is used as is.
     */
    public static String resolveFirstFamily(String configured) {
        return configured;
    }
}
