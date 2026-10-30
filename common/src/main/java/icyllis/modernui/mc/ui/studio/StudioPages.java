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

package icyllis.modernui.mc.ui.studio;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.ui.AccentPalette;
import icyllis.modernui.mc.ui.ColorPickerPopup;
import icyllis.modernui.mc.ui.ColorSwatchView;
import icyllis.modernui.mc.ui.SurfacePrefs;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Content of the six panels. Every row is built from a config item so the panel never
 * touches storage directly.
 */
public final class StudioPages {

    /** What the fragment offers that lives outside this package. */
    public interface Host {
        @NonNull
        Runnable clientChanged();

        @NonNull
        Runnable commonChanged();

        @NonNull
        Runnable textChanged();

        /** Old, richer editors that the studio does not reimplement yet. */
        @NonNull
        View legacyPage(@NonNull Context context, int page);

        /** Four-corner colour editor. */
        @NonNull
        View colorListRow(@NonNull Context context, @NonNull String key,
                          @NonNull ConfigItem<List<? extends String>> item, @NonNull Runnable onChanged);

        @NonNull
        View guiScaleRow(@NonNull Context context);
    }

    private StudioPages() {
    }

    public static void build(int index, @NonNull StudioPanel panel, @NonNull Host host) {
        switch (index) {
            case 0 -> appearance(panel, host);
            case 1 -> screen(panel, host);
            case 2 -> view(panel, host);
            case 3 -> panel.getBody().addView(host.legacyPage(panel.getBody().getContext(), 3),
                    new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            case 4 -> extension(panel, host);
            default -> panel.getBody().addView(host.legacyPage(panel.getBody().getContext(), 5),
                    new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
    }

    private static void appearance(@NonNull StudioPanel panel, @NonNull Host host) {
        var context = panel.getBody().getContext();
        var onChanged = host.clientChanged();

        panel.addSection(I18n.get("modernui.center.section.theme"));
        panel.addRow(themeColorRow(context));
        panel.addRow(divider(context));

        panel.addRow(toggle(context, "modernui.center.appearance.compact",
                SurfacePrefs::compactLayout,
                v -> {
                    SurfacePrefs.setCompactLayout(v);
                    onChanged.run();
                }));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.appearance.animations",
                SurfacePrefs::animations,
                v -> {
                    SurfacePrefs.setAnimations(v);
                    onChanged.run();
                }));

        panel.addSection(I18n.get("modernui.center.section.backdrop"));
        panel.addRow(host.colorListRow(context, "modernui.center.screen.backgroundOpacity",
                Config.CLIENT.mBackgroundColor, onChanged));
        panel.addRow(intSlider(context, "modernui.center.screen.backgroundDuration", Config.CLIENT.mBackgroundDuration, 50, onChanged));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.screen.blurEffect",
                Config.CLIENT.mBlurEffect::get, v -> {
                    Config.CLIENT.mBlurEffect.set(v);
                    onChanged.run();
                }));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.screen.blurRadius", Config.CLIENT.mBlurRadius, 1, onChanged));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.screen.additionalBlurEffect",
                Config.CLIENT.mAdditionalBlurEffect::get, v -> {
                    Config.CLIENT.mAdditionalBlurEffect.set(v);
                    onChanged.run();
                }));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.screen.overrideVanillaBlur",
                Config.CLIENT.mOverrideVanillaBlur::get, v -> {
                    Config.CLIENT.mOverrideVanillaBlur.set(v);
                    onChanged.run();
                }));
    }

    private static void screen(@NonNull StudioPanel panel, @NonNull Host host) {
        var context = panel.getBody().getContext();
        var onChanged = host.clientChanged();

        panel.addSection(I18n.get("modernui.center.section.display"));
        panel.addRow(host.guiScaleRow(context));
        panel.addRow(divider(context));
        panel.addRow(enumRow(context, "modernui.center.screen.windowMode",
                Config.CLIENT.mWindowMode, onChanged));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.screen.framerateInactive", Config.CLIENT.mFramerateInactive, 10, onChanged));

        panel.addSection(I18n.get("modernui.center.section.audio"));
        panel.addRow(percentSlider(context, "modernui.center.screen.masterVolumeInactive",
                Config.CLIENT.mMasterVolumeInactive, onChanged));
        panel.addRow(divider(context));
        panel.addRow(percentSlider(context, "modernui.center.screen.masterVolumeMinimized",
                Config.CLIENT.mMasterVolumeMinimized, onChanged));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.screen.globalVolumeControl",
                Config.CLIENT.mGlobalVolumeControl::get, v -> {
                    Config.CLIENT.mGlobalVolumeControl.set(v);
                    onChanged.run();
                }));

        panel.addSection(I18n.get("modernui.center.section.behaviour"));
        panel.addRow(toggle(context, "modernui.center.screen.inventoryPause",
                Config.CLIENT.mInventoryPause::get, v -> {
                    Config.CLIENT.mInventoryPause.set(v);
                    onChanged.run();
                }));
    }

    private static void view(@NonNull StudioPanel panel, @NonNull Host host) {
        var context = panel.getBody().getContext();
        var onChanged = host.clientChanged();

        panel.addSection(I18n.get("modernui.center.section.scrolling"));
        panel.addRow(intSlider(context, "modernui.center.view.scrollbarSize", Config.CLIENT.mScrollbarSize, 1, onChanged));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.view.touchSlop", Config.CLIENT.mTouchSlop, 1, onChanged));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.view.minimumFlingVelocity", Config.CLIENT.mMinimumFlingVelocity, 10, onChanged));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.view.maximumFlingVelocity", Config.CLIENT.mMaximumFlingVelocity, 100, onChanged));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.view.overscrollDistance", Config.CLIENT.mOverscrollDistance, 1, onChanged));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.view.overflingDistance", Config.CLIENT.mOverflingDistance, 1, onChanged));

        panel.addSection(I18n.get("modernui.center.section.tooltipTiming"));
        panel.addRow(intSlider(context, "modernui.center.view.hoverTooltipShowTimeout", Config.CLIENT.mHoverTooltipShowTimeout, 50, onChanged));
        panel.addRow(divider(context));
        panel.addRow(intSlider(context, "modernui.center.view.hoverTooltipHideTimeout", Config.CLIENT.mHoverTooltipHideTimeout, 50, onChanged));

        panel.addSection(I18n.get("modernui.center.section.layout"));
        panel.addRow(toggle(context, "modernui.center.system.forceRtlLayout",
                Config.CLIENT.mForceRtl::get, v -> {
                    Config.CLIENT.mForceRtl.set(v);
                    onChanged.run();
                }));
        panel.addRow(divider(context));
        panel.addRow(percentSlider(context, "modernui.center.system.globalFontScale",
                Config.CLIENT.mFontScale, onChanged));
    }

    private static void extension(@NonNull StudioPanel panel, @NonNull Host host) {
        var context = panel.getBody().getContext();
        var onChanged = host.clientChanged();

        panel.addSection(I18n.get("modernui.center.section.sound"));
        panel.addRow(toggle(context, "modernui.center.extension.ding",
                Config.CLIENT.mDing::get, v -> {
                    Config.CLIENT.mDing.set(v);
                    onChanged.run();
                }));

        panel.addSection(I18n.get("modernui.center.section.interaction"));
        panel.addRow(toggle(context, "key.modernui.zoom",
                Config.CLIENT.mZoom::get, v -> {
                    Config.CLIENT.mZoom.set(v);
                    onChanged.run();
                }));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.text.emojiShortcodes",
                Config.CLIENT.mEmojiShortcodes::get, v -> {
                    Config.CLIENT.mEmojiShortcodes.set(v);
                    onChanged.run();
                }));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.extension.smoothScrolling",
                () -> !Boolean.parseBoolean(
                        icyllis.modernui.mc.ModernUIClient.getBootstrapProperty(
                                icyllis.modernui.mc.ModernUIMod.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING)),
                v -> icyllis.modernui.mc.ModernUIClient.setBootstrapProperty(
                        icyllis.modernui.mc.ModernUIMod.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING,
                        Boolean.toString(!v))));
        panel.addRow(divider(context));
        panel.addRow(toggle(context, "modernui.center.text.enhancedTextField",
                () -> !Boolean.parseBoolean(
                        icyllis.modernui.mc.ModernUIClient.getBootstrapProperty(
                                icyllis.modernui.mc.ModernUIMod.BOOTSTRAP_DISABLE_ENHANCED_TEXT_FIELD)),
                v -> icyllis.modernui.mc.ModernUIClient.setBootstrapProperty(
                        icyllis.modernui.mc.ModernUIMod.BOOTSTRAP_DISABLE_ENHANCED_TEXT_FIELD,
                        Boolean.toString(!v))));

        panel.addSection(I18n.get("modernui.center.section.typing"));
    }

    // ---------------------------------------------------------------- rows

    private static View divider(@NonNull Context context) {
        return StudioRows.divider(context);
    }

    private static View toggle(@NonNull Context context, @NonNull String key,
                               @NonNull java.util.function.Supplier<Boolean> getter,
                               @NonNull java.util.function.Consumer<Boolean> setter) {
        var toggle = new StudioToggle(context);
        toggle.setChecked(Boolean.TRUE.equals(getter.get()), false);
        toggle.setOnCheckedChanged(setter::accept);
        return StudioRows.row(context, key, toggle);
    }

    private static View intSlider(@NonNull Context context, @NonNull String key,
                                  @NonNull ConfigItem<Integer> item, int step,
                                  @NonNull Runnable onChanged) {
        var range = item.getRange();
        int min = range != null ? range.getMinimum() : 0;
        int max = range != null ? range.getMaximum() : 100;
        var slider = new StudioSlider(context);
        slider.setRange(min, max, Math.max(1, (max - min) / Math.max(1, step)));
        slider.setValue(item.get());
        var value = StudioRows.chip(context, Integer.toString(item.get()));

        slider.setOnValueChanged((v, finished) -> {
            int rounded = Math.round(v);
            value.setText(Integer.toString(rounded));
            if (finished) {
                item.set(rounded);
                onChanged.run();
            }
        });

        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);
        holder.addView(slider, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(Studio.dp(context, 8), 0, 0, 0);
        holder.addView(value, params);

        return StudioRows.row(context, key, holder);
    }

    private static View percentSlider(@NonNull Context context, @NonNull String key,
                                      @NonNull ConfigItem<Double> item,
                                      @NonNull Runnable onChanged) {
        var range = item.getRange();
        double min = range != null ? range.getMinimum() : 0d;
        double max = range != null ? range.getMaximum() : 1d;
        // values that live in 0..1 read better as percentages, scales keep their decimals
        final boolean percent = min >= 0d && max <= 1d;
        var slider = new StudioSlider(context);
        slider.setRange((float) min, (float) max, 100);
        slider.setValue((float) (double) item.get());
        var value = StudioRows.chip(context, formatDouble(item.get(), percent));

        slider.setOnValueChanged((v, finished) -> {
            double rounded = Math.round(v * (percent ? 100d : 100d)) / (percent ? 100d : 100d);
            value.setText(formatDouble(rounded, percent));
            if (finished) {
                item.set(Math.max(min, Math.min(max, rounded)));
                onChanged.run();
            }
        });

        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);
        holder.addView(slider, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(Studio.dp(context, 8), 0, 0, 0);
        holder.addView(value, params);
        return StudioRows.row(context, key, holder);
    }

    private static <E> View enumRow(@NonNull Context context, @NonNull String key,
                                    @NonNull ConfigItem<E> item, @NonNull Runnable onChanged) {
        var values = item.getDefault() != null && item.getDefault().getClass().isEnum()
                ? item.getDefault().getClass().getEnumConstants() : null;
        if (values == null) {
            return StudioRows.row(context, key, StudioRows.chip(context, String.valueOf(item.get())));
        }
        var labels = new CharSequence[values.length];
        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = prettify(String.valueOf(values[i]));
            if (values[i] == item.get()) {
                selected = i;
            }
        }
        var menu = new StudioMenu(context, labels);
        menu.setSelected(selected, false);
        menu.setOnChosen(index -> {
            @SuppressWarnings("unchecked")
            var value = (E) values[index];
            item.set(value);
            onChanged.run();
        });
        return StudioRows.row(context, key, menu);
    }

    private static String formatDouble(double value, boolean percent) {
        if (percent) {
            return Math.round(value * 100) + "%";
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String prettify(String raw) {
        String text = raw.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Theme colour with a live swatch; opens the full colour editor. */
    private static View themeColorRow(@NonNull Context context) {
        int initial = AccentPalette.isCustom() ? AccentPalette.seed() : Studio.accent(context);
        var swatch = new ColorSwatchView(context);
        swatch.setColor(initial);
        var label = StudioRows.chip(context,
                AccentPalette.isCustom() ? String.format(Locale.ROOT, "#%06X", initial & 0xFFFFFF)
                        : I18n.get("modernui.center.accent.follow"));

        swatch.setOnClickListener(v -> {
            var popup = new ColorPickerPopup(swatch, AccentPalette.isCustom()
                    ? AccentPalette.seed() : Studio.accent(context), false, argb -> {
                AccentPalette.setSeed(argb);
                swatch.setColor(argb);
                label.setText(String.format(Locale.ROOT, "#%06X", argb & 0xFFFFFF));
            });
            popup.show(swatch);
        });

        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);
        var swatchParams = new LinearLayout.LayoutParams(Studio.dp(context, 26), Studio.dp(context, 26));
        swatchParams.setMargins(0, 0, Studio.dp(context, 8), 0);
        holder.addView(swatch, swatchParams);
        holder.addView(label, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        var row = StudioRows.row(context, "modernui.center.appearance.themeColor", holder);
        row.setOnClickListener(v -> swatch.performClick());
        return row;
    }
}
