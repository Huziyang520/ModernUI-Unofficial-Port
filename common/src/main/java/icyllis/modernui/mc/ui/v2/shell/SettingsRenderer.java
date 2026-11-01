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

package icyllis.modernui.mc.ui.v2.shell;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.mc.ui.v2.page.Rows;
import icyllis.modernui.mc.ui.v2.page.SettingsPage;
import icyllis.modernui.mc.ui.v2.widget.TgGroup;
import icyllis.modernui.mc.ui.v2.widget.TgRow;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Turns the declarative {@link SettingsPage} into views. The page only describes groups,
 * sections and row factories; this class decides how each of those looks and hands the
 * result to a {@link RowList} that keeps the row order for filtering and navigation.
 *
 * <p>Row factories are invoked exactly once per render, so no control is created twice and
 * the whole column is built in one pass rather than lazily per frame.</p>
 */
final class SettingsRenderer {

    /** One rail anchor: the group id and the header view the rail scrolls to. */
    record Anchor(@NonNull String id, @NonNull String titleKey, @NonNull View header) {
    }

    private SettingsRenderer() {
    }

    /**
     * Renders {@code page} into {@code list} and returns the anchors in display order.
     *
     * @param list an empty list; it is filled group by group
     */
    @NonNull
    static List<Anchor> render(@NonNull Context context, @NonNull SettingsPage page,
                               @NonNull RowList list) {
        var anchors = new ArrayList<Anchor>();
        for (var group : page.groups()) {
            var header = new HeadingView(context, group.titleKey, true);
            list.addGroupHeader(group.id, header);
            anchors.add(new Anchor(group.id, group.titleKey, header));

            for (var section : group.sections) {
                var sectionHeader = new HeadingView(context, section.titleKey, false);
                list.addSectionHeader(group.id, section.titleKey, sectionHeader);

                for (var row : section.rows) {
                    View built;
                    try {
                        built = row.factory.create(context);
                    } catch (RuntimeException e) {
                        // one broken factory must not take the whole page down
                        built = errorRow(context, row.key, e);
                    }
                    if (built == null) {
                        continue;
                    }
                    if (built instanceof TgRow tgRow) {
                        // some block rows already are whole rows; they still filter and divide
                        list.addRow(group.id, section.titleKey, tgRow);
                    } else {
                        list.addBlock(group.id, section.titleKey, built);
                    }
                }
            }
        }
        // an empty query marks the initial first/last pairs of every group
        list.filter("");
        return anchors;
    }

    /** Placeholder used when a row factory throws, so the failure is visible in game. */
    @NonNull
    private static View errorRow(@NonNull Context context, String key, @NonNull RuntimeException e) {
        var text = new TextView(context);
        text.setText("! " + (key == null ? "<block>" : key) + ": " + e.getClass().getSimpleName());
        text.setTextSize(11f);
        text.setTextColor(0xFFE5484D);
        text.setSingleLine(false);
        int pad = Math.round(context.getResources().getDisplayMetrics().density * 8f);
        text.setPadding(pad, pad, pad, pad);
        return text;
    }

    /**
     * Group or section heading. Text is a child {@link TextView} because the canvas has no
     * text primitive; the rule under it comes from the skin.
     */
    private static final class HeadingView extends TgGroup {

        private final TextView mLabel;
        private final boolean mGroup;

        HeadingView(@NonNull Context context, @NonNull String titleKey, boolean group) {
            super(context);
            mGroup = group;

            mLabel = new TextView(context);
            mLabel.setText(Rows.label(titleKey));
            mLabel.setSingleLine(false);
            var params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            addView(mLabel, params);
            applyStyle();
        }

        /**
         * Padding and type are layout inputs, so they are applied on construction and on a
         * style change. Setting them from {@code onMeasure} would request another layout from
         * inside the pass that is measuring this view, which is what clipped the text.
         */
        private void applyStyle() {
            var tk = tokens();
            float density = tk.density;
            int padH = Math.round(tk.padH * density);
            int padTop = Math.round((mGroup ? tk.sectionGap : tk.sectionGap * 0.6f) * density);
            int padBottom = Math.round((mGroup ? tk.gap : tk.gap * 0.5f) * density);
            setPadding(padH, padTop, padH, padBottom);

            mLabel.setTextSize(mGroup ? tk.sectionSize * 1.25f : tk.sectionSize);
            mLabel.setTextColor(mGroup ? tk.text : tk.muted);
            String text = Rows.label(mLabel.getText().toString());
            if (tk.upperCaseSections) {
                text = text.toUpperCase(Locale.ROOT);
            }
            mLabel.setText(text);
            var typeface = mGroup ? (tk.titleTypeface != null ? tk.titleTypeface : tk.typeface)
                    : tk.typeface;
            if (typeface != null) {
                mLabel.setTypeface(typeface);
            }
        }

        @Override
        public void onSkinChanged() {
            super.onSkinChanged();
            applyStyle();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            var tk = tokens();
            float inset = tk.padH * tk.density;
            skin().drawSectionRule(canvas, inset, getHeight() - 1f, getWidth() - inset, false);
        }
    }
}
