/*
 * Modern UI Lite.
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

package icyllis.modernui.mc;

import icyllis.modernui.ModernUI;
import icyllis.modernui.graphics.text.FontFamily;
import icyllis.modernui.mc.ModernUIMod;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The lite edition ships no fonts, so the default typeface is taken from the operating
 * system and follows the game language. Anything the player configured explicitly wins.
 */
public final class FontDefaults {

    /** Empty means "pick one for the current language", see {@link #resolveFirstFamily}. */
    public static final String FIRST_FAMILY = "";

    public static final List<String> FALLBACK_FAMILIES = List.of();

    /** No emoji images are shipped, so colour emoji must stay off or every glyph warns. */
    public static final boolean USE_COLOR_EMOJI = false;

    private static final List<String> CHINESE = List.of(
            "Microsoft YaHei UI", "Microsoft YaHei", "PingFang SC", "Hiragino Sans GB",
            "Noto Sans CJK SC", "Noto Sans SC", "Noto Sans CJK TC", "Source Han Sans SC",
            "Source Han Sans CN", "Heiti SC", "STHeiti", "SimHei", "WenQuanYi Micro Hei",
            "Droid Sans Fallback", "SimSun");

    private static final List<String> JAPANESE = List.of(
            "Yu Gothic UI", "Yu Gothic", "Meiryo", "Hiragino Sans", "Hiragino Kaku Gothic ProN",
            "Noto Sans CJK JP", "Noto Sans JP", "Noto Sans CJK SC");

    private static final List<String> KOREAN = List.of(
            "Malgun Gothic", "Apple SD Gothic Neo", "Noto Sans CJK KR", "Noto Sans KR",
            "Noto Sans CJK SC");

    private FontDefaults() {
    }

    /**
     * Keeps a configured family when it can actually be loaded, otherwise substitutes the
     * best family for the current language. A stale value from the full edition therefore
     * degrades to a working font instead of leaving the game without a typeface.
     */
    public static String resolveFirstFamily(String configured) {
        if (configured != null && !configured.isEmpty() && find(configured) != null) {
            return configured;
        }
        String picked = pickFor(ModernUI.getSelectedLocale());
        if (picked == null) {
            picked = FontFamily.SANS_SERIF.getFamilyName();
        }
        ModernUIMod.LOGGER.info("Modern UI Lite typeface: {} (requested '{}')",
                picked, configured);
        return picked;
    }

    private static String pickFor(Locale locale) {
        if (locale == null) {
            return null;
        }
        List<String> preferred = switch (locale.getLanguage()) {
            case "zh" -> CHINESE;
            case "ja" -> JAPANESE;
            case "ko" -> KOREAN;
            default -> null;
        };
        if (preferred == null) {
            return null;
        }
        for (String name : preferred) {
            FontFamily family = find(name);
            if (family != null) {
                return family.getFamilyName();
            }
        }
        // Nothing matched by name: take any family the platform marks as East Asian.
        for (FontFamily family : FontFamily.getSystemFontMap().values()) {
            if (family.isEastAsianFamily()) {
                return family.getFamilyName();
            }
        }
        ModernUIMod.LOGGER.warn("No East Asian system font was found for {}; " +
                "text will fall back to the bundled compatibility font", locale);
        return null;
    }

    private static FontFamily find(String value) {
        File file = new File(value.replaceAll("\\\\", "/"));
        if (file.isFile()) {
            try {
                return FontFamily.createFamily(file, /*register*/ false);
            } catch (Exception ignored) {
            }
        }
        FontFamily family = FontFamily.getSystemFontWithAlias(value);
        if (family != null) {
            return family;
        }
        Map<String, FontFamily> map = FontFamily.getSystemFontMap();
        family = map.get(value);
        if (family != null) {
            return family;
        }
        for (FontFamily candidate : map.values()) {
            if (candidate.getFamilyName().equalsIgnoreCase(value)) {
                return candidate;
            }
        }
        return null;
    }
}
