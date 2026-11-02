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
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.MuiPlatform;
import icyllis.modernui.resources.TypedValue;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.view.ContextMenu;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.RelativeLayout;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * The four corner colours of the tooltip border, previewed as a single rounded rectangle.
 * Tapping a quadrant opens the wheel below the grid; the hex fields edit the same values.
 */
public class FourColorPicker extends RelativeLayout {

    private static final int id_field_tl = 601;
    private static final int id_field_tr = 602;
    private static final int id_field_br = 603;
    private static final int id_field_bl = 604;

    // upper left, upper right, lower right, lower left
    private final EditText[] mColorFields = new EditText[4];

    private final int[] mColors = {~0, ~0, ~0, ~0};

    private final Rect mPreviewBox = new Rect();

    private final ColorPickerView mWheel;
    private int mActiveIndex = -1;
    private int mPressedIndex = -1;

    private float mBorderRadius;
    private float mBorderWidth = 1;

    private final int mBorderColor;

    private final ConfigItem<List<? extends String>> mConfig;
    private final Runnable mOnChanged;
    private final OnFocusChangeListener mOnFieldFocusChange;

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
            if (index < 0) {
                return;
            }
            int color;
            try {
                color = Color.parseColor(input.getText().toString().trim());
            } catch (IllegalArgumentException e) {
                // Restore the last valid colour: an unparseable field must not overwrite it.
                input.setText(format(mColors[index]));
                return;
            }
            mColors[index] = color;
            invalidate();
            writeAll();
        };

        var colors = mConfig.get();

        int dp4 = dp(4);
        mColorFields[0] = createField(0, colors);
        mColorFields[1] = createField(1, colors);
        mColorFields[2] = createField(2, colors);
        mColorFields[3] = createField(3, colors);
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.setMargins(dp4, dp4, dp4, dp4);
            mColorFields[0].setId(id_field_tl);
            addView(mColorFields[0], params);
        }
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.setMargins(dp4, dp4, dp4, dp4);
            mColorFields[1].setId(id_field_tr);
            addView(mColorFields[1], params);
        }
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, id_field_tr);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(dp4, dp4, dp4, dp4);
            mColorFields[2].setId(id_field_br);
            addView(mColorFields[2], params);
        }
        {
            var params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, id_field_tl);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(dp4, dp4, dp4, dp4);
            mColorFields[3].setId(id_field_bl);
            addView(mColorFields[3], params);
        }
        // Seed the preview from the config without writing anything back.
        for (int i = 0; i < mColorFields.length; i++) {
            mColors[i] = readField(mColorFields[i], mColors[i]);
        }

        mWheel = new ColorPickerView(getContext());
        mWheel.setAlphaEnabled(true);
        mWheel.setVisibility(GONE);
        mWheel.setOnColorChangeListener((argb, committed) -> {
            if (mActiveIndex < 0) {
                return;
            }
            mColors[mActiveIndex] = argb;
            invalidate();
            if (committed) {
                mColorFields[mActiveIndex].setText(format(argb));
                writeAll();
            }
        });
        {
            var params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, id_field_bl);
            params.setMargins(dp4, dp(6), dp4, dp4);
            addView(mWheel, params);
        }

        // Clickable so the pointer turns into a hand over the preview, as for any other control.
        setClickable(true);
        setLongClickable(true);
        setWillNotDraw(false);
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

    private int indexOf(@NonNull EditText field) {
        for (int i = 0; i < mColorFields.length; i++) {
            if (mColorFields[i] == field) {
                return i;
            }
        }
        return -1;
    }

    private static int readField(@NonNull EditText field, int fallback) {
        try {
            return Color.parseColor(field.getText().toString().trim());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    @NonNull
    private static String format(int color) {
        return String.format(Locale.ROOT, "#%08X", color);
    }

    /**
     * Persists the current preview colours. Writing here instead of waiting for the hosting
     * fragment to be destroyed keeps the values even if the screen is left in another way.
     */
    private void writeAll() {
        var newList = new ArrayList<String>(4);
        for (int color : mColors) {
            newList.add(format(color));
        }
        var oldList = mConfig.get();
        if (!newList.equals(oldList)) {
            mConfig.set(newList);
            mOnChanged.run();
            Util.ioPool().execute(() -> MuiPlatform.get().saveConfig(Config.TYPE_CLIENT));
        }
    }

    public void setColors(@NonNull @Size(4) String[] colors) {
        setColors(Arrays.asList(colors));
    }

    public void setColors(@NonNull @Size(4) List<? extends String> colors) {
        for (int i = 0; i < mColorFields.length; i++) {
            String text;
            if (colors.isEmpty()) {
                text = "#FFFFFFFF";
            } else {
                text = colors.get(Math.min(i, colors.size() - 1));
            }
            mColorFields[i].setText(text);
            mColors[i] = readField(mColorFields[i], mColors[i]);
        }
        invalidate();
        writeAll();
    }

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

    /**
     * @return the quadrant under the given point, or -1 when it hits the padding
     */
    private int hitTestQuadrant(float x, float y) {
        if (mPreviewBox.isEmpty()) {
            return -1;
        }
        if (x < mPreviewBox.left || x >= mPreviewBox.right ||
                y < mPreviewBox.top || y >= mPreviewBox.bottom) {
            return -1;
        }
        boolean right = x >= mPreviewBox.centerX();
        boolean bottom = y >= mPreviewBox.centerY();
        if (!bottom) {
            return right ? 1 : 0;
        }
        return right ? 2 : 3;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                int index = hitTestQuadrant(event.getX(), event.getY());
                if (index < 0) {
                    return false;
                }
                mPressedIndex = index;
                setPressed(true);
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                int index = hitTestQuadrant(event.getX(), event.getY());
                setPressed(false);
                if (index >= 0 && index == mPressedIndex) {
                    mPressedIndex = -1;
                    toggleWheel(index);
                    return true;
                }
                mPressedIndex = -1;
                return false;
            }
            case MotionEvent.ACTION_CANCEL -> {
                mPressedIndex = -1;
                setPressed(false);
                return false;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setBorderRadius(float borderRadius) {
        float value = getContext().getResources().getDisplayMetrics().density * borderRadius * 2.0f;
        if (mBorderRadius != value) {
            mBorderRadius = value;
            invalidate();
        }
    }

    public void setBorderWidth(float borderWidth) {
        float value = getContext().getResources().getDisplayMetrics().density * borderWidth * 2.0f;
        if (mBorderWidth != value) {
            mBorderWidth = value;
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (mPreviewBox.isEmpty()) {
            return;
        }
        final float left = mPreviewBox.left;
        final float top = mPreviewBox.top;
        final float right = mPreviewBox.right;
        final float bottom = mPreviewBox.bottom;
        final float centerX = mPreviewBox.centerX();
        final float centerY = mPreviewBox.centerY();
        final float border = mBorderWidth;
        final float maxRadius = Math.min(right - left, bottom - top) / 2f;
        final float radius = Math.max(0f, Math.min(mBorderRadius, maxRadius));

        var paint = Paint.obtain();
        paint.setAntiAlias(true);

        // Half the stroke lies inside the outline, so the fills start there and meet it
        // exactly instead of leaving a dark ring between them.
        final float inset = border * 0.5f;

        // Each quadrant rounds only its outer corner, so the four fills form exactly one
        // rounded rectangle and nothing can stick out past that outline.
        paint.setColor(mColors[0]);
        canvas.drawRoundRect(left + inset, top + inset, centerX, centerY,
                radius, 0f, 0f, 0f, paint);
        paint.setColor(mColors[1]);
        canvas.drawRoundRect(centerX, top + inset, right - inset, centerY,
                0f, radius, 0f, 0f, paint);
        paint.setColor(mColors[2]);
        canvas.drawRoundRect(centerX, centerY, right - inset, bottom - inset,
                0f, 0f, radius, 0f, paint);
        paint.setColor(mColors[3]);
        canvas.drawRoundRect(left + inset, centerY, centerX, bottom - inset,
                0f, 0f, 0f, radius, paint);

        if (border > 0f) {
            paint.setColor(mBorderColor);
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(border);
            canvas.drawRoundRect(left, top, right, bottom, radius, paint);
        }
        paint.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // Anchor on the hex fields rather than the whole height: the wheel lives below them
        // and must not stretch the preview over itself.
        int previewBottom = Math.max(mColorFields[2].getBottom(), mColorFields[3].getBottom());
        mPreviewBox.set(
                Math.max(mColorFields[0].getRight(), mColorFields[3].getRight()),
                getPaddingTop(),
                Math.min(mColorFields[1].getLeft(), mColorFields[2].getLeft()),
                previewBottom - getPaddingBottom()
        );
        int inset = dp(8);
        mPreviewBox.inset(inset, inset);
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