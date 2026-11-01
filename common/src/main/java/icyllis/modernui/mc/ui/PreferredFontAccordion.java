/*
 * Modern UI.
 * Copyright (C) 2025 BloCamLimb. All rights reserved.
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

import icyllis.modernui.ModernUI;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.core.Core;
import icyllis.modernui.graphics.text.FontFamily;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ModernUIClient;
import icyllis.modernui.mc.ui.v2.page.Rows;
import icyllis.modernui.mc.ui.v2.widget.TgButton;
import icyllis.modernui.mc.ui.v2.widget.TgField;
import icyllis.modernui.mc.ui.v2.widget.TgMenu;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Preferred font family picker plus the fallback and registration lists. Mounted as one
 * block by the settings page; the surrounding toggles are ordinary page rows.
 */
public class PreferredFontAccordion {

    private final ViewGroup mParent;
    private final Runnable mOnChanged;
    private final Runnable mOnFontChanged;

    private TgField mInput;

    public PreferredFontAccordion(@NonNull ViewGroup parent, @NonNull Runnable onChanged,
                                  @NonNull Runnable onFontChanged) {
        mParent = parent;
        mOnChanged = onChanged;
        mOnFontChanged = onFontChanged;
    }

    /** Builds the editor into {@code column}; the caller mounts the column. */
    public void buildInto(@NonNull LinearLayout column) {
        var context = mParent.getContext();

        column.addView(Rows.text(context, "gui.modernui.configValue",
                Config.CLIENT.mFirstFontFamily, () -> {
                    mOnChanged.run();
                    UiSupport.reloadDefaultTypeface(context, mOnFontChanged);
                }));

        column.addView(chooseFontRow(context));
        column.addView(openFileRow(context));
    }

    /** Family list loaded off the game thread; the spinner fills in when it is ready. */
    @NonNull
    private View chooseFontRow(@NonNull Context context) {
        var row = labelRow(context, "modernui.center.font.chooseFont");
        var holder = (LinearLayout) row.getChildAt(1);

        var menu = new TgMenu(context, new CharSequence[]{"\u2026"});
        holder.addView(menu, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        CompletableFuture.supplyAsync(() -> {
            var values = FontFamily.getSystemFontMap().values().stream()
                    .map(family -> new FontFamilyItem(family.getFamilyName(),
                            family.getFamilyName(ModernUI.getSelectedLocale())))
                    .sorted()
                    .collect(Collectors.toList());
            values.add(0, new FontFamilyItem("\u2026", "\u2026"));
            return values;
        }).thenAcceptAsync(values -> {
            var labels = new CharSequence[values.size()];
            for (int i = 0; i < values.size(); i++) {
                labels[i] = values.get(i).localeName();
            }
            var rebuilt = new TgMenu(context, labels);
            rebuilt.setSelected(selectionOf(values), false);
            rebuilt.setOnChosen(index -> {
                if (index == 0) {
                    return;
                }
                applyNewValue(context, values.get(index).rootName(), mOnFontChanged);
            });
            holder.removeView(menu);
            holder.addView(rebuilt, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        }, Core.getUiThreadExecutor());
        return row;
    }

    private static int selectionOf(@NonNull List<FontFamilyItem> values) {
        FontFamily first = ModernUIClient.getInstance().getFirstFontFamily();
        if (first != null) {
            String name = first.getFamilyName();
            for (int i = 1; i < values.size(); i++) {
                if (values.get(i).rootName().equalsIgnoreCase(name)) {
                    return i;
                }
            }
        }
        return 0;
    }

    @NonNull
    private View openFileRow(@NonNull Context context) {
        var row = labelRow(context, "modernui.center.font.openFontFile");
        var holder = (LinearLayout) row.getChildAt(1);
        var browse = new TgButton(context, I18n.get("gui.modernui.browseFiles"));
        browse.setOnAction(() -> CompletableFuture.runAsync(() -> {
            String path = UiSupport.pickFontFile();
            if (path != null) {
                browse.post(() -> applyNewValue(context, path, mOnFontChanged));
            }
        }));
        holder.addView(browse, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return row;
    }

    /** Two column row: label on the left, an empty control holder on the right. */
    @NonNull
    private static LinearLayout labelRow(@NonNull Context context, @NonNull String key) {
        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        float density = context.getResources().getDisplayMetrics().density;
        row.setPadding(Math.round(16f * density), Math.round(8f * density),
                Math.round(16f * density), Math.round(8f * density));
        row.setMinimumHeight(Math.round(52f * density));

        var title = new TextView(context);
        title.setText(Rows.label(key));
        title.setTextSize(13.5f);
        row.addView(title, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(holder, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return row;
    }

    private boolean applyNewValue(@NonNull Context context, @NonNull String newValue,
                                  @NonNull Runnable onFontChanged) {
        if (!newValue.equals(Config.CLIENT.mFirstFontFamily.get())) {
            Config.CLIENT.mFirstFontFamily.set(newValue);
            mOnChanged.run();
            UiSupport.reloadDefaultTypeface(context, onFontChanged);
            if (mInput != null) {
                mInput.setText(newValue);
            }
            return true;
        }
        return false;
    }

    public record FontFamilyItem(String rootName, String localeName) {
    }
}
