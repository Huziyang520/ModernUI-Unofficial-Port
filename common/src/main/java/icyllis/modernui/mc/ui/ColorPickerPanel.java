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

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.TextView;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Full colour editor: pick on the plane, then fine tune with the channel sliders or by
 * typing a hex value. Nothing is committed while a gesture is in flight.
 */
public class ColorPickerPanel extends LinearLayout implements ColorPickerView.OnColorChangeListener,
        SeekBar.OnSeekBarChangeListener, View.OnFocusChangeListener {

    public interface OnColorCommitted {
        void onColorCommitted(int argb);
    }

    private final ColorPickerView mPicker;
    private final EditText mHex;
    private final SeekBar[] mChannels = new SeekBar[4];
    private final TextView[] mChannelValues = new TextView[4];
    private final LinearLayout mAlphaRow;
    private boolean mAlphaEnabled;
    private final TextView mRgbSummary;

    private OnColorCommitted mCallback;
    private boolean mUpdating;

    public ColorPickerPanel(@NonNull Context context) {
        super(context);
        setOrientation(VERTICAL);

        mPicker = new ColorPickerView(context);
        mPicker.setAlphaEnabled(false);
        mPicker.setOnColorChangeListener(this);
        addView(mPicker, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        var hexRow = new LinearLayout(context);
        hexRow.setOrientation(HORIZONTAL);
        hexRow.setGravity(Gravity.CENTER_VERTICAL);
        int padV = hexRow.dp(Design.SPACE_2);
        hexRow.setPadding(0, padV, 0, padV);

        var label = new TextView(context);
        label.setText("HEX");
        label.setTextSize(11);
        label.setTextColor(Design.onSurfaceVariant(context));
        hexRow.addView(label, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1));

        mHex = new EditText(context, null, R.attr.editTextOutlinedStyle);
        mHex.setSingleLine(true);
        mHex.setFilters(new InputFilter.LengthFilter(9));
        mHex.setTextSize(12);
        mHex.setOnFocusChangeListener(this);
        var hexParams = new LinearLayout.LayoutParams(hexRow.dp(112), WRAP_CONTENT);
        hexParams.gravity = Gravity.CENTER_VERTICAL;
        hexRow.addView(mHex, hexParams);
        addView(hexRow);

        String[] names = {"R", "G", "B", "A"};
        LinearLayout alphaRow = null;
        for (int i = 0; i < 4; i++) {
            var row = new LinearLayout(context);
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            var name = new TextView(context);
            name.setText(names[i]);
            name.setTextSize(11);
            name.setTextColor(Design.onSurfaceVariant(context));
            var nameParams = new LinearLayout.LayoutParams(row.dp(20), WRAP_CONTENT);
            nameParams.gravity = Gravity.CENTER_VERTICAL;
            row.addView(name, nameParams);

            var slider = new SeekBar(context, null, null, R.style.Widget_Material3_SeekBar_Slider);
            slider.setMax(255);
            slider.setOnSeekBarChangeListener(this);
            var sliderParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1);
            sliderParams.gravity = Gravity.CENTER_VERTICAL;
            row.addView(slider, sliderParams);
            mChannels[i] = slider;

            var value = new TextView(context);
            value.setTextSize(11);
            value.setTextColor(Design.onSurface(context));
            value.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
            var valueParams = new LinearLayout.LayoutParams(row.dp(34), WRAP_CONTENT);
            valueParams.gravity = Gravity.CENTER_VERTICAL;
            row.addView(value, valueParams);
            mChannelValues[i] = value;

            if (i == 3) {
                alphaRow = row;
                row.setVisibility(GONE);
            } else {
                addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            }
        }
        mAlphaRow = alphaRow;

        mRgbSummary = new TextView(context);
        mRgbSummary.setTextSize(11);
        mRgbSummary.setTextColor(Design.alpha(Design.onSurfaceVariant(context), 0.8f));
        var summaryParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        summaryParams.setMargins(0, dp(Design.SPACE_1), 0, 0);
        addView(mRgbSummary, summaryParams);
    }

    public void setOnColorCommitted(OnColorCommitted callback) {
        mCallback = callback;
    }

    public int getColor() {
        return mPicker.getColor();
    }

    public void setAlphaEnabled(boolean enabled) {
        mAlphaEnabled = enabled;
        mPicker.setAlphaEnabled(enabled);
        mAlphaRow.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setColor(int argb) {
        mUpdating = true;
        mPicker.setColor(argb);
        syncFromPicker();
        mUpdating = false;
    }

    private void syncFromPicker() {
        int argb = mPicker.getColor();
        mHex.setText(toHalfHex(argb));
        mChannels[0].setProgress((argb >> 16) & 0xFF);
        mChannels[1].setProgress((argb >> 8) & 0xFF);
        mChannels[2].setProgress(argb & 0xFF);
        mChannels[3].setProgress((argb >>> 24) & 0xFF);
        mChannelValues[0].setText(Integer.toString((argb >> 16) & 0xFF));
        mChannelValues[1].setText(Integer.toString((argb >> 8) & 0xFF));
        mChannelValues[2].setText(Integer.toString(argb & 0xFF));
        mChannelValues[3].setText(Integer.toString((argb >>> 24) & 0xFF));
        mRgbSummary.setText("rgb(" + ((argb >> 16) & 0xFF) + ", " + ((argb >> 8) & 0xFF)
                + ", " + (argb & 0xFF) + ")");
    }

    private static String toHalfHex(int argb) {
        return String.format("#%06X", argb & 0xFFFFFF);
    }

    private void commit(int argb) {
        if (mCallback != null) {
            mCallback.onColorCommitted(argb);
        }
    }

    @Override
    public void onColorChanged(int argb, boolean committed) {
        if (mUpdating) {
            return;
        }
        mUpdating = true;
        syncFromPicker();
        mUpdating = false;
        if (committed) {
            commit(argb);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mUpdating || !fromUser) {
            return;
        }
        mUpdating = true;
        int r = mChannels[0].getProgress();
        int g = mChannels[1].getProgress();
        int b = mChannels[2].getProgress();
        int a = mAlphaEnabled ? mChannels[3].getProgress() : 0xFF;
        mPicker.setColor((a << 24) | (r << 16) | (g << 8) | b);
        syncFromPicker();
        mUpdating = false;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        commit(mPicker.getColor());
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            return;
        }
        int parsed = ColorPickerView.parseOrDefault(mHex.getText().toString(), -1);
        if (parsed != -1) {
            int argb = (parsed & 0x00FFFFFF) | 0xFF000000;
            mUpdating = true;
            mPicker.setColor(argb);
            syncFromPicker();
            mUpdating = false;
            commit(argb);
        } else {
            mUpdating = true;
            syncFromPicker();
            mUpdating = false;
        }
    }
}
