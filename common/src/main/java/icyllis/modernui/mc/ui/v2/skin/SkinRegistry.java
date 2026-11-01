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
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.mc.ui.v2.TgTheme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The eight styles, resolved against the running theme. Widgets ask the registry for the
 * active skin; switching a style rebuilds it and repaints every listener.
 */
public final class SkinRegistry {

    public static final String INDUSTRIAL = "industrial";
    public static final String PIXEL = "pixel";
    public static final String MONO = "mono";
    public static final String NEON = "neon";
    public static final String GLASS = "glass";
    public static final String BLUEPRINT = "blueprint";
    public static final String TERMINAL = "terminal";
    public static final String EDITORIAL = "editorial";

    public static final String DEFAULT_ID = INDUSTRIAL;

    /** Display order of the picker; also the whitelist the config validates against. */
    public static final List<String> IDS = List.of(
            INDUSTRIAL, PIXEL, MONO, NEON, GLASS, BLUEPRINT, TERMINAL, EDITORIAL);

    public interface Listener {
        void onSkinChanged();
    }

    /** Theme in, skin out: a style owns its palette, the theme only seeds the accent. */
    public interface Factory {
        @NonNull
        Skin create(@NonNull TgTheme theme);
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();
    private static final Map<String, Factory> FACTORIES = new LinkedHashMap<>();

    static {
        FACTORIES.put(INDUSTRIAL, IndustrialSkin::new);
        FACTORIES.put(PIXEL, PixelSkin::new);
        FACTORIES.put(MONO, MonoSkin::new);
        FACTORIES.put(NEON, NeonSkin::new);
        FACTORIES.put(GLASS, GlassSkin::new);
        FACTORIES.put(BLUEPRINT, BlueprintSkin::new);
        FACTORIES.put(TERMINAL, TerminalSkin::new);
        FACTORIES.put(EDITORIAL, EditorialSkin::new);
    }

    private static volatile String sActiveId = DEFAULT_ID;
    private static volatile Skin sActive;

    private SkinRegistry() {
    }

    /** True when {@code id} names a known style; used as the config whitelist. */
    public static boolean isValid(@Nullable String id) {
        return id != null && FACTORIES.containsKey(id);
    }

    @NonNull
    public static String activeId() {
        return sActiveId;
    }

    /** Ids in display order. */
    @NonNull
    public static List<String> ids() {
        return new ArrayList<>(IDS);
    }

    /** Switches the active style; unknown ids fall back to the default. */
    public static void setActive(@Nullable String id) {
        String next = isValid(id) ? id : DEFAULT_ID;
        if (next.equals(sActiveId) && sActive != null) {
            return;
        }
        sActiveId = next;
        sActive = null;
        for (var listener : LISTENERS) {
            listener.onSkinChanged();
        }
    }

    public static void addListener(@NonNull Listener listener) {
        LISTENERS.add(listener);
    }

    public static void removeListener(@NonNull Listener listener) {
        LISTENERS.remove(listener);
    }

    /** Drops the cached skin after a theme or accent change, then repaints. */
    public static void invalidate() {
        sActive = null;
        for (var listener : LISTENERS) {
            listener.onSkinChanged();
        }
    }

    /**
     * The active skin. Cached: the tokens only change when the theme or the accent changes,
     * and both paths call {@link #invalidate()}.
     */
    @NonNull
    public static Skin get(@NonNull Context context) {
        var skin = sActive;
        if (skin != null) {
            return skin;
        }
        skin = create(context, sActiveId);
        sActive = skin;
        return skin;
    }

    /** Builds a skin by id without touching the active one; used by the picker previews. */
    @NonNull
    public static Skin create(@NonNull Context context, @Nullable String id) {
        var factory = FACTORIES.get(isValid(id) ? id : DEFAULT_ID);
        return factory.create(TgTheme.of(context));
    }
}
