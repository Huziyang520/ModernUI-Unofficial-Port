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

package icyllis.modernui.mc.ui.v2.page;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.ui.v2.widget.TgButton;
import icyllis.modernui.mc.ui.v2.widget.TgChoice;
import icyllis.modernui.mc.ui.v2.widget.TgColorField;
import icyllis.modernui.mc.ui.v2.widget.TgField;
import icyllis.modernui.mc.ui.v2.widget.TgMenu;
import icyllis.modernui.mc.ui.v2.widget.TgRow;
import icyllis.modernui.mc.ui.v2.widget.TgSlider;
import icyllis.modernui.mc.ui.v2.widget.TgToggle;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Binding helpers: every row of every page is built through one of these, so the pages
 * never construct a control or read the config storage directly.
 */
public final class Rows {

    private Rows() {
    }

    @NonNull
    public static TgRow toggle(@NonNull Context context, @NonNull String key,
                               @NonNull ConfigItem<Boolean> item, @NonNull Runnable onChanged) {
        var toggle = new TgToggle(context);
        toggle.setChecked(item.get(), false);
        toggle.setOnCheckedChanged(value -> {
            item.set(value);
            onChanged.run();
        });
        return new TgRow(context, key, toggle);
    }

    /** Toggle backed by a supplier/setter pair, for values that live outside the config. */
    @NonNull
    public static TgRow toggle(@NonNull Context context, @NonNull String key,
                               @NonNull Supplier<Boolean> getter, @NonNull Consumer<Boolean> setter) {
        var toggle = new TgToggle(context);
        toggle.setChecked(Boolean.TRUE.equals(getter.get()), false);
        toggle.setOnCheckedChanged(setter);
        return new TgRow(context, key, toggle);
    }

    /**
     * Integer slider. The range always comes from the item, never from a hardcoded guess,
     * otherwise a stored value would be clipped the first time the page opens.
     */
    @NonNull
    public static TgRow intSlider(@NonNull Context context, @NonNull String key,
                                  @NonNull ConfigItem<Integer> item, @NonNull Runnable onChanged) {
        var range = item.getRange();
        int min = range != null ? range.getMinimum() : 0;
        int max = range != null ? range.getMaximum() : 100;
        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);

        var slider = new TgSlider(context);
        slider.setRange(min, max, Math.max(1, max - min));
        slider.setValue(item.get());
        holder.addView(slider, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        var value = chip(context, Integer.toString(item.get()));
        var chipParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        chipParams.setMargins(Math.round(context.getResources().getDisplayMetrics().density * 8f), 0, 0, 0);
        holder.addView(value, chipParams);

        slider.setOnValueChanged((v, finished) -> {
            int rounded = Math.round(v);
            value.setText(Integer.toString(rounded));
            if (finished) {
                item.set(rounded);
                onChanged.run();
            }
        });
        return new TgRow(context, key, holder);
    }

    /** Double slider, formatted as a percentage when the range is a 0..1 fraction. */
    @NonNull
    public static TgRow doubleSlider(@NonNull Context context, @NonNull String key,
                                     @NonNull ConfigItem<Double> item, @NonNull Runnable onChanged) {
        var range = item.getRange();
        double min = range != null ? range.getMinimum() : 0d;
        double max = range != null ? range.getMaximum() : 1d;
        // values that live in 0..1 read better as percentages
        final boolean percent = min >= 0d && max <= 1d;

        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);

        var slider = new TgSlider(context);
        slider.setRange((float) min, (float) max, 100);
        slider.setValue((float) (double) item.get());
        holder.addView(slider, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        var value = chip(context, formatDouble(item.get(), percent));
        var chipParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        chipParams.setMargins(Math.round(context.getResources().getDisplayMetrics().density * 8f), 0, 0, 0);
        holder.addView(value, chipParams);

        slider.setOnValueChanged((v, finished) -> {
            double rounded = Math.round(v * 100d) / 100d;
            value.setText(formatDouble(rounded, percent));
            if (finished) {
                item.set(Math.max(min, Math.min(max, rounded)));
                onChanged.run();
            }
        });
        return new TgRow(context, key, holder);
    }

    /** Enum value as a dropdown, labels prettified from the constant name. */
    @NonNull
    public static <E extends Enum<E>> TgRow enumMenu(@NonNull Context context, @NonNull String key,
                                                     @NonNull ConfigItem<E> item,
                                                     @NonNull Runnable onChanged) {
        var values = item.getDefault() != null
                ? item.getDefault().getDeclaringClass().getEnumConstants()
                : null;
        if (values == null) {
            return new TgRow(context, key, chip(context, String.valueOf(item.get())));
        }
        var labels = new CharSequence[values.length];
        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = prettify(values[i].name());
            if (values[i] == item.get()) {
                selected = i;
            }
        }
        var menu = new TgMenu(context, labels);
        menu.setSelected(selected, false);
        menu.setOnChosen(index -> {
            item.set(values[index]);
            onChanged.run();
        });
        return new TgRow(context, key, menu);
    }

    /** Enum value as a segmented control; use only for two or three short labels. */
    @NonNull
    public static <E extends Enum<E>> TgRow enumChoice(@NonNull Context context, @NonNull String key,
                                                       @NonNull ConfigItem<E> item,
                                                       @NonNull Runnable onChanged) {
        var values = item.getDefault() != null
                ? item.getDefault().getDeclaringClass().getEnumConstants()
                : null;
        if (values == null) {
            return new TgRow(context, key, chip(context, String.valueOf(item.get())));
        }
        var labels = new String[values.length];
        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = prettify(values[i].name());
            if (values[i] == item.get()) {
                selected = i;
            }
        }
        var choice = new TgChoice(context, labels);
        choice.setSelected(selected, false);
        choice.setOnChosen(index -> {
            item.set(values[index]);
            onChanged.run();
        });
        return new TgRow(context, key, choice);
    }

    /** String list edited as one entry per line. */
    @NonNull
    public static TgRow stringList(@NonNull Context context, @NonNull String key,
                                   @NonNull ConfigItem<List<? extends String>> item,
                                   @NonNull Runnable onChanged) {
        var field = new TgField(context);
        field.setText(String.join("\n", item.get()));
        field.setMinWidth(Math.round(context.getResources().getDisplayMetrics().density * 220f));
        field.setOnCommit(text -> {
            var result = new java.util.ArrayList<String>();
            for (String line : text.split("\n")) {
                String strip = line.strip();
                if (!strip.isEmpty() && !result.contains(strip)) {
                    result.add(strip);
                }
            }
            if (!result.equals(item.get())) {
                item.set(result);
                onChanged.run();
            }
        });
        return new TgRow(context, key, field);
    }

    /** Single string value, e.g. the font family name. */
    @NonNull
    public static TgRow text(@NonNull Context context, @NonNull String key,
                             @NonNull ConfigItem<String> item, @NonNull Runnable onChanged) {
        var field = new TgField(context);
        field.setText(item.get());
        field.setMinWidth(Math.round(context.getResources().getDisplayMetrics().density * 220f));
        field.setOnCommit(text -> {
            if (!text.equals(item.get())) {
                item.set(text);
                onChanged.run();
            }
        });
        return new TgRow(context, key, field);
    }

    /** Colour entry point; the caller supplies what happens when the studio commits. */
    @NonNull
    public static TgColorField colorField(@NonNull Context context, int initial, boolean alpha) {
        var field = new TgColorField(context);
        field.setAlphaEnabled(alpha);
        field.setColor(initial);
        return field;
    }

    @NonNull
    public static TgButton button(@NonNull Context context, @NonNull String label,
                                  @NonNull Runnable action) {
        var button = new TgButton(context, label);
        button.setOnAction(action);
        return button;
    }

    /** Small read only value chip. */
    @NonNull
    public static TextView chip(@NonNull Context context, @NonNull CharSequence text) {
        var chip = new TextView(context);
        chip.setText(text);
        chip.setTextSize(11);
        chip.setSingleLine(true);
        float density = context.getResources().getDisplayMetrics().density;
        chip.setPadding((int) (10f * density), (int) (5f * density),
                (int) (10f * density), (int) (5f * density));
        chip.setBackground(icyllis.modernui.mc.ui.v2.TgDrawable.rounded(context,
                icyllis.modernui.mc.ui.v2.skin.SkinRegistry.get(context).tokens().accentSoft, 999f));
        chip.setTextColor(icyllis.modernui.mc.ui.v2.skin.SkinRegistry.get(context).tokens().muted);
        return chip;
    }

    /** Read only line of explanation, spanning the column. */
    @NonNull
    public static TextView note(@NonNull Context context, @NonNull String text) {
        var note = new TextView(context);
        note.setText(text);
        note.setTextSize(11);
        note.setTextColor(icyllis.modernui.mc.ui.v2.skin.SkinRegistry.get(context).tokens().muted);
        float density = context.getResources().getDisplayMetrics().density;
        note.setPadding(0, (int) (2f * density), 0, (int) (6f * density));
        return note;
    }

    /** Translation lookup with a prettified fallback when the key is missing. */
    @NonNull
    public static String label(@NonNull String key) {
        String text = I18n.get(key);
        return text.equals(key) ? prettify(key.substring(key.lastIndexOf('.') + 1)) : text;
    }

    @NonNull
    public static String prettify(@NonNull String raw) {
        String text = raw.replace('_', ' ').toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    @NonNull
    private static String formatDouble(double value, boolean percent) {
        if (percent) {
            return Math.round(value * 100) + "%";
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** Full width spacer used to give block rows breathing room. */
    @NonNull
    public static View spacer(@NonNull Context context, float dp) {
        var view = new View(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT,
                Math.round(context.getResources().getDisplayMetrics().density * dp)));
        return view;
    }
}
