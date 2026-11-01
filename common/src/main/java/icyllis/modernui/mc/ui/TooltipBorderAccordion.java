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

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ui.v2.page.Rows;
import icyllis.modernui.mc.ui.v2.widget.TgRow;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;
import net.minecraft.client.resources.language.I18n;

/**
 * Tooltip border editor: the geometry options plus the four corner colours and their
 * presets. Mounted as one block by the settings page.
 */
public class TooltipBorderAccordion {

    public static final String[][] PRESET_COLORS = {
            {"#FFAADCF0", "#FFFFC3F7", "#FFBFF2B2", "#FFD27F3D"},
            {"#FFAADCF0", "#FFDAD0F4", "#FFFFC3F7", "#FFDAD0F4"},
            {"#FF28007F", "#FF28007F", "#FF14003F", "#FF14003F"},
            {"#FFE0E0E0", "#FFB0B0B0", "#FFFFFFFF", "#FFB0B0B0"}
    };

    private final ViewGroup mParent;
    private final Runnable mOnChanged;

    public TooltipBorderAccordion(@NonNull ViewGroup parent, @NonNull Runnable onChanged) {
        mParent = parent;
        mOnChanged = onChanged;
    }

    /** Builds the whole editor into {@code column}; the caller mounts the column. */
    public void buildInto(@NonNull LinearLayout column) {
        var context = mParent.getContext();
        var onChanged = mOnChanged;

        column.addView(Rows.toggle(context, "modernui.center.tooltip.roundedShapes",
                Config.CLIENT.mRoundedTooltip, onChanged));
        column.addView(Rows.doubleSlider(context, "modernui.center.tooltip.borderWidth",
                Config.CLIENT.mTooltipWidth, onChanged));
        column.addView(Rows.doubleSlider(context, "modernui.center.tooltip.cornerRadius",
                Config.CLIENT.mTooltipRadius, onChanged));
        column.addView(Rows.doubleSlider(context, "modernui.center.tooltip.shadowRadius",
                Config.CLIENT.mTooltipShadowRadius, onChanged));
        column.addView(Rows.doubleSlider(context, "modernui.center.tooltip.shadowOpacity",
                Config.CLIENT.mTooltipShadowAlpha, onChanged));
        column.addView(Rows.toggle(context, "modernui.center.tooltip.adaptiveColors",
                Config.CLIENT.mAdaptiveTooltipColors, onChanged));
        column.addView(Rows.intSlider(context, "modernui.center.tooltip.borderCycle",
                Config.CLIENT.mTooltipCycle, onChanged));

        // presets: each button copies one four corner set into the config
        var presets = new LinearLayout(context);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        int gap = Math.round(4f * context.getResources().getDisplayMetrics().density);
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            final int index = i;
            var button = new icyllis.modernui.mc.ui.v2.widget.TgButton(context,
                    I18n.get("gui.modernui.preset_s", i + 1));
            button.setOnAction(() -> {
                Config.CLIENT.mTooltipStroke.set(java.util.List.of(PRESET_COLORS[index]));
                onChanged.run();
            });
            var params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(gap, 0, gap, 0);
            presets.addView(button, params);
        }
        var presetParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        presetParams.setMargins(0, gap * 2, 0, gap);
        column.addView(presets, presetParams);

        column.addView(UiSupport.colorListRow(context, "modernui.center.tooltip.borderStyle",
                Config.CLIENT.mTooltipStroke, onChanged, (field, alpha) -> {
                }));
    }
}
