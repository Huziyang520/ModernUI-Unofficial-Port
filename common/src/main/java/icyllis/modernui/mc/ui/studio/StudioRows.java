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
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Row builders: a label block on the left, one studio control on the right.
 */
public final class StudioRows {

    private StudioRows() {
    }

    @NonNull
    public static LinearLayout row(@NonNull Context context, @NonNull String key,
                                   @NonNull View control) {
        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int minHeight = Studio.dp(context, Studio.ROW_MIN_HEIGHT);
        row.setMinimumHeight(minHeight);
        row.setPadding(0, Studio.dp(context, 7), 0, Studio.dp(context, 7));

        var block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setGravity(Gravity.CENTER_VERTICAL);

        var title = new TextView(context);
        title.setText(I18n.get(key));
        title.setTextSize(13.5f);
        title.setTextColor(Studio.onSurface(context));
        block.addView(title);

        String descKey = key + ".desc";
        String desc = I18n.get(descKey);
        if (!desc.equals(descKey)) {
            var sub = new TextView(context);
            sub.setText(desc);
            sub.setTextSize(10.5f);
            sub.setTextColor(Studio.muted(context));
            var subParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            subParams.setMargins(0, Studio.dp(context, 1), 0, 0);
            block.addView(sub, subParams);
        }

        var blockParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        blockParams.setMargins(0, 0, Studio.dp(context, 10), 0);
        row.addView(block, blockParams);

        var controlParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        controlParams.gravity = Gravity.CENTER_VERTICAL;
        row.addView(control, controlParams);
        return row;
    }

    /** Separator drawn as a one pixel inset rule. */
    @NonNull
    public static View divider(@NonNull Context context) {
        var line = new View(context);
        line.setBackground(Studio.rounded(context, Studio.alpha(Studio.onSurface(context), 0.07f), 0));
        int inset = Studio.dp(context, 2);
        var params = new LinearLayout.LayoutParams(MATCH_PARENT, Math.max(1, Studio.dp(context, 0.7f)));
        params.setMargins(inset, 0, inset, 0);
        line.setLayoutParams(params);
        return line;
    }

    /** Small read-only value chip, used for derived information. */
    @NonNull
    public static TextView chip(@NonNull Context context, @NonNull CharSequence text) {
        var chip = new TextView(context);
        chip.setText(text);
        chip.setTextSize(11);
        chip.setTextColor(Studio.muted(context));
        int padH = Studio.dp(context, 10);
        chip.setPadding(padH, Studio.dp(context, 5), padH, Studio.dp(context, 5));
        chip.setBackground(Studio.pill(context, Studio.alpha(Studio.onSurface(context), 0.07f)));
        return chip;
    }

    /** Full width text button. */
    @NonNull
    public static TextView action(@NonNull Context context, @NonNull String label,
                                  @NonNull Runnable onClick) {
        var button = new TextView(context);
        button.setText(label);
        button.setTextSize(12);
        button.setTextColor(Studio.contrasting(Studio.accent(context)));
        button.setGravity(Gravity.CENTER);
        button.setClickable(true);
        button.setFocusable(true);
        int padV = Studio.dp(context, 9);
        button.setPadding(0, padV, 0, padV);
        button.setBackground(Studio.pill(context, Studio.accent(context)));
        button.setOnClickListener(v -> onClick.run());
        return button;
    }
}
