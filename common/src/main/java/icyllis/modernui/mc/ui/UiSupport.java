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

package icyllis.modernui.mc.ui;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.core.Core;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.ui.v2.page.Rows;
import icyllis.modernui.mc.ui.v2.widget.TgColorField;
import icyllis.modernui.mc.ui.v2.widget.TgMenu;
import icyllis.modernui.mc.ui.v2.widget.TgRow;
import icyllis.modernui.mc.ui.v2.widget.TgSwatch;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.ArrayAdapter;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.Spinner;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Editors that need the game window or a four-corner layout, so they stay outside the v2
 * widget set and are mounted by the page as whole rows.
 */
public final class UiSupport {

    public static final int ID_RESET_TO_DEFAULT = 0x7f000002;

    private UiSupport() {
    }

    /** Adds the "reset to default" item to a context menu. */
    @NonNull
    public static icyllis.modernui.view.MenuItem addResetToDefaultMenuItem(
            @NonNull icyllis.modernui.view.Menu menu) {
        return menu.add(icyllis.modernui.view.Menu.NONE, ID_RESET_TO_DEFAULT,
                icyllis.modernui.view.Menu.CATEGORY_ALTERNATIVE | 0,
                I18n.get("gui.modernui.resetToDefault"));
    }

    public static boolean isResetItem(@Nullable icyllis.modernui.view.MenuItem item) {
        return item != null && item.getItemId() == ID_RESET_TO_DEFAULT;
    }

    /**
     * Reloads the typeface on the game thread and refreshes every view still holding the
     * previous instance, so an open screen picks the new family up immediately.
     */
    public static void reloadDefaultTypeface(@NonNull Context context,
                                             @Nullable Runnable onFontChanged) {
        var future = Minecraft.getInstance().submit(() -> {
            var oldTypeface = icyllis.modernui.ModernUI.getSelectedTypeface();
            var client = icyllis.modernui.mc.ModernUIClient.getInstance();
            client.reloadTypeface();
            client.reloadFontStrike();
            return oldTypeface;
        });
        future.whenCompleteAsync((oldTypeface, throwable) -> {
            if (throwable == null) {
                if (onFontChanged != null) {
                    onFontChanged.run();
                }
                var manager = icyllis.modernui.mc.UIManager.getInstance();
                if (manager != null) {
                    var decor = manager.getDecorView();
                    if (decor != null) {
                        refreshViewTypeface(decor, oldTypeface);
                    }
                }
                icyllis.modernui.widget.Toast.makeText(context,
                        I18n.get("gui.modernui.font_reloaded"),
                        icyllis.modernui.widget.Toast.LENGTH_SHORT).show();
            }
        }, Core.getUiThreadExecutor());
    }

    private static void refreshViewTypeface(@NonNull ViewGroup vg,
                                            @Nullable icyllis.modernui.text.Typeface oldTypeface) {
        int cc = vg.getChildCount();
        for (int i = 0; i < cc; i++) {
            View v = vg.getChildAt(i);
            if (v instanceof ViewGroup group) {
                refreshViewTypeface(group, oldTypeface);
            } else if (v instanceof TextView tv) {
                if (tv.getTypeface() == oldTypeface) {
                    tv.setTypeface(icyllis.modernui.ModernUI.getSelectedTypeface());
                }
            }
        }
    }

    /**
     * Opens the native font picker. {@code FileDialogs} only exists on the builds whose
     * platform ships a native dialog, so the call is routed reflectively and degrades to a
     * no-op instead of failing to link on the others.
     */
    @Nullable
    public static String pickFontFile() {
        try {
            Class<?> dialogs = Class.forName("icyllis.modernui.mc.FileDialogs");
            var method = dialogs.getMethod("openFileDialog", String.class, String.class,
                    String[].class);
            return (String) method.invoke(null, null,
                    "TrueType/OpenType Fonts (*.ttf;*.otf;*.ttc;*.otc)",
                    new String[]{"ttf", "otf", "ttc", "otc"});
        } catch (Throwable t) {
            icyllis.modernui.mc.ModernUIMod.LOGGER.warn(
                    icyllis.modernui.mc.ModernUIMod.MARKER,
                    "The native file dialog is unavailable on this platform", t);
            return null;
        }
    }

    // ---------------------------------------------------------------- gui scale

    /** GUI scale selector. Reads and writes the vanilla options and re-applies the window scale. */
    @NonNull
    public static View guiScaleRow(@NonNull Context context) {
        var layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        int padH = Math.round(16f * context.getResources().getDisplayMetrics().density);
        layout.setPadding(padH, 0, padH, 0);
        layout.setMinimumHeight(Math.round(52f * context.getResources().getDisplayMetrics().density));

        var title = new TextView(context);
        title.setText(I18n.get("options.guiScale"));
        title.setTextSize(13.5f);
        layout.addView(title, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        var hint = new TextView(context);
        hint.setTextSize(11);
        layout.addView(hint, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        var spinner = new Spinner(context);
        List<GuiScaleItem> values = new ArrayList<>(MuiModApi.MAX_GUI_SCALE);
        for (int i = 0; i <= MuiModApi.MAX_GUI_SCALE; i++) {
            if (i == 1) {
                continue;
            }
            values.add(new GuiScaleItem(i));
        }
        spinner.setAdapter(new ArrayAdapter<>(context, values));
        int current = Minecraft.getInstance().options.guiScale().get();
        spinner.setSelection(current == 0 ? 0 : current - 1);
        spinner.setOnItemSelectedListener((parent, view, position, id) -> {
            int newValue = position == 0 ? 0 : position + 1;
            Core.executeOnMainThread(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.options.guiScale().set(newValue);
                // the window only rescales if the computed scale actually changed
                if ((int) minecraft.getWindow().getGuiScale() !=
                        minecraft.getWindow().calculateScale(newValue, false)) {
                    minecraft.resizeGui();
                }
                minecraft.options.save();
            });
            hint.setText(guiScaleHint(newValue));
        });
        hint.setText(guiScaleHint(current));

        var spinnerParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        spinnerParams.gravity = Gravity.CENTER_VERTICAL;
        spinnerParams.setMargins(Math.round(8f * context.getResources().getDisplayMetrics().density), 0, 0, 0);
        layout.addView(spinner, spinnerParams);
        return layout;
    }

    private record GuiScaleItem(int scale) {
        @Override
        public String toString() {
            if (scale == 0) {
                int r = MuiModApi.calcGuiScales();
                int auto = r >> 4 & 0xf;
                return I18n.get("options.guiScale.auto") + " (" + auto + "x)";
            }
            return guiScaleToString(scale);
        }
    }

    private static CharSequence guiScaleHint(int value) {
        if (value != 0) {
            int r = MuiModApi.calcGuiScales();
            int min = r >> 8 & 0xf;
            int max = r & 0xf;
            if (value < min || value > max) {
                int scale = (value < min ? min : max);
                return I18n.get("gui.modernui.current_s", guiScaleToString(scale));
            }
        }
        return "";
    }

    private static String guiScaleToString(int scale) {
        return (scale * 50) + "% (" + scale + "x)";
    }

    // ------------------------------------------------------------- colour lists

    /**
     * Four-corner colour editor for a list item. Each corner is a swatch that opens the
     * colour studio; the stored list keeps its order, upper-left first.
     */
    @NonNull
    public static View colorListRow(@NonNull Context context, @NonNull String key,
                                    @NonNull ConfigItem<List<? extends String>> item,
                                    @NonNull Runnable onChanged,
                                    @NonNull java.util.function.BiConsumer<TgColorField, Boolean> openStudio) {
        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);

        var corners = new TgColorField[4];
        var current = colorsOf(item);
        for (int i = 0; i < 4; i++) {
            final int index = i;
            var field = new TgColorField(context);
            field.setAlphaEnabled(true);
            field.setColor(current.get(i));
            field.setOnOpen(() -> openStudio.accept(field, true));
            field.setOnColorPicked(argb -> {
                var list = colorsOfStrings(item);
                while (list.size() < 4) {
                    list.add(list.isEmpty() ? "#FF000000" : list.get(list.size() - 1));
                }
                list.set(index, String.format(java.util.Locale.ROOT, "#%08X", argb));
                if (!list.equals(item.get())) {
                    item.set(list);
                    onChanged.run();
                }
            });
            corners[i] = field;
            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.setMargins(0, 0, Math.round(6f * context.getResources().getDisplayMetrics().density), 0);
            holder.addView(field, params);
        }
        return new TgRow(context, key, holder);
    }

    /** Parses the stored colour list into exactly four ARGB values, repeating the last one. */
    @NonNull
    public static List<Integer> colorsOf(@NonNull ConfigItem<List<? extends String>> item) {
        var result = new ArrayList<Integer>(4);
        var stored = item.get();
        if (stored != null) {
            for (String s : stored) {
                try {
                    result.add(icyllis.modernui.graphics.Color.parseColor(s));
                } catch (IllegalArgumentException ignored) {
                    result.add(0xFF000000);
                }
            }
        }
        if (result.isEmpty()) {
            result.add(0xFF000000);
        }
        while (result.size() < 4) {
            result.add(result.get(result.size() - 1));
        }
        return result.subList(0, 4);
    }

    /** A colour list edited as a single alpha slider over all four corners. */
    @NonNull
    public static View colorAlphaRow(@NonNull Context context, @NonNull String key,
                                     @NonNull ConfigItem<List<? extends String>> item,
                                     @NonNull Runnable onChanged) {
        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);

        var swatch = new TgSwatch(context);
        swatch.setColor(firstColor(item));
        holder.addView(swatch, new LinearLayout.LayoutParams(Math.round(24f *
                context.getResources().getDisplayMetrics().density), Math.round(24f *
                context.getResources().getDisplayMetrics().density)));

        var slider = new icyllis.modernui.mc.ui.v2.widget.TgSlider(context);
        slider.setRange(0f, 1f, 100);
        slider.setValue((float) firstAlpha(item.get()));
        var value = Rows.chip(context, Math.round(firstAlpha(item.get()) * 100f) + "%");
        slider.setOnValueChanged((v, finished) -> {
            value.setText(Math.round(v * 100f) + "%");
            swatch.setColor(applyAlpha(firstColor(item), v));
            if (finished) {
                var next = withAlpha(colorsOfStrings(item), v);
                if (!next.equals(item.get())) {
                    item.set(next);
                    onChanged.run();
                }
            }
        });
        holder.addView(slider, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        var chipParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        chipParams.setMargins(Math.round(8f * context.getResources().getDisplayMetrics().density), 0, 0, 0);
        holder.addView(value, chipParams);
        return new TgRow(context, key, holder);
    }

    @NonNull
    private static List<String> colorsOfStrings(@NonNull ConfigItem<List<? extends String>> item) {
        var stored = item.get();
        return stored == null ? new ArrayList<>() : new ArrayList<>(stored);
    }

    private static int firstColor(@NonNull ConfigItem<List<? extends String>> item) {
        var colors = colorsOf(item);
        return colors.get(0);
    }

    private static int applyAlpha(int color, float alpha) {
        int a = Math.round(alpha * 255f);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    /** First colour's alpha as a 0..1 fraction; 1.0 when the list is empty or invalid. */
    public static double firstAlpha(List<? extends String> colors) {
        if (colors != null && !colors.isEmpty()) {
            try {
                int color = icyllis.modernui.graphics.Color.parseColor(colors.get(0));
                return (color >>> 24) / 255.0;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return 1.0;
    }

    /** Rewrites every colour of the list to the given alpha, keeping its RGB. */
    @NonNull
    public static List<String> withAlpha(@NonNull List<String> colors, double alpha) {
        int a = (int) (alpha * 255.0f + 0.5f);
        var result = new ArrayList<>(colors);
        if (result.isEmpty()) {
            result.add("#FF000000");
        }
        for (var it = result.listIterator(); it.hasNext(); ) {
            try {
                int color = icyllis.modernui.graphics.Color.parseColor(it.next());
                color = (color & 0xFFFFFF) | (a << 24);
                it.set(String.format(java.util.Locale.ROOT, "#%08X", color));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }
}
