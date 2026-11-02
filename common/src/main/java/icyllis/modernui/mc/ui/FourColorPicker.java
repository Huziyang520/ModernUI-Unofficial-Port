/*
 * Modern UI.
 * Copyright (C) 2019-2023 BloCamLimb. All rights reserved.
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

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Size;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.view.ContextMenu;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * The four corner colours of the tooltip border. Each colour owns a hex field and a
 * clickable chip; picking a chip expands the wheel underneath the grid.
 */
public class FourColorPicker extends LinearLayout {

    // upper left, upper right, lower right, lower left
    private final EditText[] mColorFields = new EditText[4];

    private final ColorSwatchView[] mSwatches = new ColorSwatchView[4];

    private final int[] mColors = {~0, ~0, ~0, ~0};

    private final ConfigItem<List<? extends String>> mConfig;
    private final Runnable mOnChanged;
    private final OnFocusChangeListener mOnFieldFocusChange;

    private final ColorPickerView mWheel;
    private int mActiveIndex = -1;
    private float mBorderWidth = 1;

    public FourColorPicker(Context context,
                           ConfigItem<List<? extends String>> config,
                           Runnable onChanged) {
        super(context);
        setOrientation(VERTICAL);

        mConfig = config;
        mOnChanged = onChanged;

        mOnFieldFocusChange = (v, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            EditText input = (EditText) v;
            int color = 0xFFFFFFFF;
            try {
                color = Color.parseColor(input.getText().toString());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            int index = indexOf(input);
            mColors[index] = color;
            updateChip(index);
            writeConfig(index, String.format(Locale.ROOT, "#%08X", color));
        };

        var colors = mConfig.get();

        for (int i = 0; i < mColorFields.length; i++) {
            mColorFields[i] = createField(i, colors);
        }
        // Resolve the initial colours before the chips exist, so nothing renders blank.
        for (var f : mColorFields) {
            mOnFieldFocusChange.onFocusChange(f, false);
        }
        for (int i = 0; i < mSwatches.length; i++) {
            mSwatches[i] = createChip(i);
        }

        addView(createRow(mColorFields[0], mSwatches[0], mSwatches[1], mColorFields[1]));
        addView(createRow(mColorFields[3], mSwatches[3], mSwatches[2], mColorFields[2]));

        mWheel = new ColorPickerView(context);
        mWheel.setAlphaEnabled(true);
        mWheel.setVisibility(GONE);
        mWheel.setOnColorChangeListener((argb, committed) -> {
            if (mActiveIndex < 0) {
                return;
            }
            mColors[mActiveIndex] = argb;
            updateChip(mActiveIndex);
            if (committed) {
                applyColor(mActiveIndex, argb);
            }
        });
        var wheelParams = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        wheelParams.setMargins(0, dp(10), 0, dp(2));
        addView(mWheel, wheelParams);

        setLongClickable(true);
    }

    @NonNull
    private LinearLayout createRow(@NonNull View... children) {
        var row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int gap = dp(4);
        for (var child : children) {
            var params = new LayoutParams(0, WRAP_CONTENT, 1f);
            params.setMargins(gap, gap, gap, gap);
            row.addView(child, params);
        }
        return row;
    }

    @NonNull
    private ColorSwatchView createChip(int index) {
        var chip = new ColorSwatchView(getContext());
        chip.setColor(mColors[index]);
        chip.setOnClickListener(v -> toggleWheel(index));
        return chip;
    }

    /** Clicking the active chip again collapses the wheel. */
    private void toggleWheel(int index) {
        if (mActiveIndex == index && mWheel.getVisibility() == VISIBLE) {
            mWheel.setVisibility(GONE);
            mActiveIndex = -1;
            requestLayout();
            return;
        }
        mActiveIndex = index;
        mWheel.setColor(mColors[index]);
        mWheel.setVisibility(VISIBLE);
        requestLayout();
    }

    private void updateChip(int index) {
        var chip = mSwatches[index];
        if (chip != null) {
            chip.setColor(mColors[index]);
        }
    }

    private void writeConfig(int index, String formatted) {
        var oldList = mConfig.get();
        var newList = new ArrayList<String>(oldList);
        if (newList.isEmpty()) {
            newList.add("#FFFFFFFF");
        }
        while (newList.size() < 4) {
            newList.add(newList.get(newList.size() - 1));
        }
        newList.set(index, formatted);
        if (!newList.equals(oldList)) {
            mConfig.set(newList);
            mOnChanged.run();
        }
    }

    /** Commits one corner: the hex field follows the wheel, then the config is written. */
    private void applyColor(int index, int color) {
        String formatted = String.format(Locale.ROOT, "#%08X", color);
        mColors[index] = color;
        EditText field = mColorFields[index];
        if (field != null && !formatted.contentEquals(field.getText())) {
            field.setText(formatted);
        }
        updateChip(index);
        writeConfig(index, formatted);
    }

    private int indexOf(@NonNull EditText field) {
        for (int i = 0; i < mColorFields.length; i++) {
            if (mColorFields[i] == field) {
                return i;
            }
        }
        return 0;
    }

    @NonNull
    private EditText createField(int index, @NonNull List<? extends String> colors) {
        var field = new EditText(getContext(), null, R.attr.editTextOutlinedStyle);
        Typeface monoFont = Typeface.getSystemFont("JetBrains Mono Medium");
        if (monoFont != Typeface.SANS_SERIF) {
            field.setTypeface(monoFont);
        }
        field.setSingleLine();
        field.setText(colors.isEmpty()
                ? "#FFFFFFFF"
                : colors.get(Math.min(index, colors.size() - 1)));
        field.setFilters(new InputFilter.LengthFilter(10));
        field.setOnFocusChangeListener(mOnFieldFocusChange);
        return field;
    }

    public void setColors(@NonNull @Size(4) String[] colors) {
        setColors(Arrays.asList(colors));
    }

    public void setColors(@NonNull @Size(4) List<? extends String> colors) {
        for (int i = 0; i < mColorFields.length; i++) {
            mColorFields[i].setText(colors.get(i));
        }
        for (var f : mColorFields) {
            mOnFieldFocusChange.onFocusChange(f, false);
        }
    }

    /** The chips follow the configured corner radius so they match the real tooltip. */
    public void setBorderRadius(float borderRadius) {
        for (var chip : mSwatches) {
            if (chip != null) {
                chip.setCornerRadius(borderRadius);
            }
        }
    }

    /** Kept for the caller's contract; the chips draw no shared frame of their own. */
    public void setBorderWidth(float borderWidth) {
        if (mBorderWidth != borderWidth) {
            mBorderWidth = borderWidth;
            invalidate();
        }
    }

    @Override
    protected void onCreateContextMenu(@NonNull ContextMenu menu) {
        super.onCreateContextMenu(menu);
        PreferencesFragment.addResetToDefaultMenuItem(menu)
                .setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == PreferencesFragment.ID_RESET_TO_DEFAULT) {
                        setColors(mConfig.getDefault());
                        return true;
                    }
                    return false;
                });
    }
}