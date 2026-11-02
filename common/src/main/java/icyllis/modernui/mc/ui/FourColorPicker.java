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
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Rect;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.resources.TypedValue;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.view.ContextMenu;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class FourColorPicker extends RelativeLayout {

    // upper left, upper right, lower right, lower left
    private final EditText[] mColorFields = new EditText[4];

    /** One visible chip per corner; clicking a chip opens the wheel for that colour. */
    private final ColorSwatchView[] mSwatches = new ColorSwatchView[4];

    private final int[] mColors = {~0, ~0, ~0, ~0};

    private final Rect mPreviewBox = new Rect();

    private float mBorderRadius;
    private float mBorderWidth = 1;

    private final int mBorderColor;

    private final ConfigItem<List<? extends String>> mConfig;
    private final Runnable mOnChanged;
    private final OnFocusChangeListener mOnFieldFocusChange;

    private ColorPickerPopup mWheel;

    public FourColorPicker(Context context,
                           ConfigItem<List<? extends String>> config,
                           Runnable onChanged) {
        super(context);

        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.ns, R.attr.colorOutline, value, true);
        mBorderColor = value.data;

        mConfig = config;
        mOnChanged = onChanged;

        mOnFieldFocusChange = (v, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            EditText input = (EditText) v;
            int index = indexOf(input);
            int color = 0xFFFFFFFF;
            try {
                color = Color.parseColor(input.getText().toString());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            applyColor(index, color);
        };

        var colors = mConfig.get();

        int dp4 = dp(4);
        mColorFields[0] = createField(0, colors);
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.setMargins(dp4, dp4, dp4, dp4);
            mColorFields[0].setId(601);
            addView(mColorFields[0], params);
        }
        mColorFields[1] = createField(1, colors);
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.setMargins(dp4, dp4, dp4, dp4);
            mColorFields[1].setId(602);
            addView(mColorFields[1], params);
        }
        mColorFields[2] = createField(2, colors);
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, 602);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(dp4, dp4, dp4, dp4);
            addView(mColorFields[2], params);
        }
        mColorFields[3] = createField(3, colors);
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, 601);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(dp4, dp4, dp4, dp4);
            addView(mColorFields[3], params);
        }
        // init colors
        for (var f : mColorFields) {
            mOnFieldFocusChange.onFocusChange(f, false);
        }

        // The preview is split into four independent chips so that picking a corner colour
        // is discoverable: every chip is a visible, clickable target of its own.
        for (int i = 0; i < mSwatches.length; i++) {
            var swatch = new ColorSwatchView(getContext());
            final int index = i;
            swatch.setOnClickListener(v -> openWheel(index));
            mSwatches[i] = swatch;
            addView(swatch, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        }

        setWillNotDraw(false);
        setLongClickable(true);
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

    public void setBorderRadius(float borderRadius) {
        borderRadius = getContext().getResources().getDisplayMetrics().density * borderRadius * 2.0f;
        if (mBorderRadius != borderRadius) {
            mBorderRadius = borderRadius;
            for (var swatch : mSwatches) {
                if (swatch != null) {
                    swatch.setCornerRadius(borderRadius / getContext()
                            .getResources().getDisplayMetrics().density);
                }
            }
            invalidate();
        }
    }

    public void setBorderWidth(float borderWidth) {
        borderWidth = getContext().getResources().getDisplayMetrics().density * borderWidth * 2.0f;
        if (mBorderWidth != borderWidth) {
            mBorderWidth = borderWidth;
            invalidate();
        }
    }

    private int indexOf(@NonNull EditText field) {
        for (int i = 0; i < mColorFields.length; i++) {
            if (mColorFields[i] == field) {
                return i;
            }
        }
        return 0;
    }

    /** Writes one quadrant back to the config and mirrors it into the matching hex field. */
    private void applyColor(int index, int color) {
        String formatted = String.format(Locale.ROOT, "#%08X", color);
        mColors[index] = color;
        EditText field = mColorFields[index];
        if (field != null && !formatted.contentEquals(field.getText())) {
            field.setText(formatted);
        }
        var swatch = mSwatches[index];
        if (swatch != null) {
            swatch.setColor(color);
        }
        invalidate();
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

    private void openWheel(int index) {
        if (mWheel != null) {
            mWheel.dismiss();
        }
        mWheel = new ColorPickerPopup(this, mColors[index], true,
                argb -> applyColor(index, argb));
        mWheel.show(this);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Only the frame is drawn here; the four colours live in their own swatch views,
        // so this view needs no touch handling at all.
        var paint = Paint.obtain();
        paint.setColor(mBorderColor);
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(mBorderWidth);
        canvas.drawRoundRect(mPreviewBox.left, mPreviewBox.top, mPreviewBox.right, mPreviewBox.bottom,
                mBorderRadius, paint);
        paint.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mPreviewBox.set(
                Math.max(mColorFields[0].getRight(), mColorFields[3].getRight()),
                getPaddingTop(),
                Math.min(mColorFields[1].getLeft(), mColorFields[2].getLeft()),
                getHeight() - getPaddingBottom()
        );
        int inset = dp(8);
        mPreviewBox.inset(inset, inset);

        // Lay the four chips out over the quadrants of the preview box. They are inset by
        // the stroke width so that the frame drawn by this view stays visible around them.
        int frame = Math.max((int) Math.ceil(mBorderWidth), 1);
        int l = mPreviewBox.left + frame;
        int t = mPreviewBox.top + frame;
        int r = mPreviewBox.right - frame;
        int b = mPreviewBox.bottom - frame;
        int cx = (l + r) / 2;
        int cy = (t + b) / 2;
        int gap = Math.max(dp(2), 1);
        layoutSwatch(0, l, t, cx - gap, cy - gap);
        layoutSwatch(1, cx + gap, t, r, cy - gap);
        layoutSwatch(2, cx + gap, cy + gap, r, b);
        layoutSwatch(3, l, cy + gap, cx - gap, b);
    }

    private void layoutSwatch(int index, int l, int t, int r, int b) {
        var swatch = mSwatches[index];
        if (swatch == null) {
            return;
        }
        swatch.layout(
                Math.max(l, 0),
                Math.max(t, 0),
                Math.max(r, l),
                Math.max(b, t));
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
