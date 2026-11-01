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

package icyllis.modernui.mc.ui.v2.color;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.LinearGradient;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Shader;
import icyllis.modernui.mc.ui.v2.widget.TgField;
import icyllis.modernui.mc.ui.v2.widget.TgGroup;
import icyllis.modernui.mc.ui.v2.widget.TgSlider;
import icyllis.modernui.mc.ui.v2.widget.TgSwatch;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MeasureSpec;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Full colour editor: saturation/value plane, hue bar, opacity bar, channel sliders, hex
 * entry, presets, recents and a live comparison swatch.
 *
 * <p>The colour wheel of the design is presented as a hue bar. The canvas has no path
 * primitive and a filled annulus cannot be expressed with {@code drawArc}, so a bar under
 * the plane is the honest equivalent; it carries the same hue ramp and the same marker.</p>
 *
 * <p>Dragging only previews. A commit fires when the gesture ends, when a slider is
 * released, when the hex field loses focus, or on a preset/recents click.</p>
 */
public class ColorStudio extends TgGroup {

    /** Receives the colour while it is being edited and once when it settles. */
    public interface OnColor {

        /** Live value while a gesture is in flight; callers should not persist this. */
        void onPreview(int argb);

        /** Final value of a gesture; callers persist here. */
        void onCommit(int argb);
    }

    private static final int[] PRESETS = {
            0xFFFFFFFF, 0xFF000000, 0xFF9E9E9E, 0xFF607D8B,
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
            0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
            0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFFFC107,
    };

    private static final int RECENT_MAX = 8;

    /** Starts white: an unset field reads as "nothing chosen yet", not as black. */
    private final float[] mHsv = {0f, 0f, 1f};
    private int mAlpha = 0xFF;
    private boolean mAlphaEnabled = true;
    /** Guards the model-to-control push so a sync never echoes back as an edit. */
    private boolean mSyncing;
    /** False when the host already draws the surface this studio sits on. */
    private boolean mPanel = true;

    /** Last committed value and the one before it, for the comparison swatch. */
    private int mCommitted = 0xFFFFFFFF;
    private int mPrevious = 0xFFFFFFFF;

    /** Newest first. In-memory only: the studio keeps no state of its own on disk. */
    private final List<Integer> mRecent = new ArrayList<>(RECENT_MAX);

    private OnColor mListener;

    private final LinearLayout mContent;
    private final SvPlane mPlane;
    private final Bar mHueBar;
    private final LinearLayout mAlphaRow;
    private final Bar mAlphaBar;

    private final TgSwatch mPreviewSwatch;
    private final TgSwatch mPreviousSwatch;
    private final TextView mPreviewHex;

    private final TgSlider[] mChannels = new TgSlider[3];
    private final TextView[] mChannelValues = new TextView[3];

    private final TgField mHex;
    private final LinearLayout mRecentRow;
    private final TgSwatch[] mRecentSwatches = new TgSwatch[RECENT_MAX];

    private final List<TextView> mSections = new ArrayList<>();
    private final List<TextView> mCaptions = new ArrayList<>();

    public ColorStudio(@NonNull Context context) {
        super(context);
        setPadding(Math.round(dp(16f)), Math.round(dp(14f)),
                Math.round(dp(16f)), Math.round(dp(14f)));

        mContent = new LinearLayout(context);
        mContent.setOrientation(LinearLayout.VERTICAL);
        addView(mContent, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        // preview: current colour, the colour before it, and the current hex readout
        var previewRow = new LinearLayout(context);
        previewRow.setOrientation(LinearLayout.HORIZONTAL);
        previewRow.setGravity(Gravity.CENTER_VERTICAL);

        mPreviewSwatch = new TgSwatch(context);
        mPreviewSwatch.setRadiusDp(8f);
        previewRow.addView(mPreviewSwatch, new LinearLayout.LayoutParams(
                Math.round(dp(44f)), Math.round(dp(44f))));

        mPreviousSwatch = new TgSwatch(context);
        mPreviousSwatch.setRadiusDp(8f);
        var previousParams = new LinearLayout.LayoutParams(
                Math.round(dp(44f)), Math.round(dp(44f)));
        previousParams.setMargins(Math.round(dp(8f)), 0, Math.round(dp(10f)), 0);
        previewRow.addView(mPreviousSwatch, previousParams);

        mPreviewHex = new TextView(context);
        mPreviewHex.setSingleLine(true);
        mPreviewHex.setGravity(Gravity.CENTER_VERTICAL);
        previewRow.addView(mPreviewHex, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        mContent.addView(previewRow, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        mPlane = new SvPlane(context);
        var planeParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        planeParams.setMargins(0, Math.round(dp(10f)), 0, 0);
        mContent.addView(mPlane, planeParams);

        mHueBar = new Bar(context, true);
        var hueParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        hueParams.setMargins(0, Math.round(dp(8f)), 0, 0);
        mContent.addView(captionedBar(context, "mui.v2.color.hue", "Hue", mHueBar), hueParams);

        mAlphaBar = new Bar(context, false);
        mAlphaRow = captionedBar(context, "mui.v2.color.alpha", "Opacity", mAlphaBar);
        var alphaParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        alphaParams.setMargins(0, Math.round(dp(6f)), 0, 0);
        mContent.addView(mAlphaRow, alphaParams);

        addSection(context, "mui.v2.color.channels", "Channels");

        String[] channelNames = {"R", "G", "B"};
        for (int i = 0; i < 3; i++) {
            var row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            var name = new TextView(context);
            name.setText(channelNames[i]);
            name.setSingleLine(true);
            mCaptions.add(name);
            var nameParams = new LinearLayout.LayoutParams(Math.round(dp(18f)), WRAP_CONTENT);
            nameParams.gravity = Gravity.CENTER_VERTICAL;
            row.addView(name, nameParams);

            var slider = new TgSlider(context);
            slider.setRange(0f, 255f, 255);
            slider.setOnValueChanged((value, finished) -> {
                if (mSyncing) {
                    return;
                }
                applyChannels();
                if (finished) {
                    commit();
                } else {
                    preview();
                }
            });
            row.addView(slider, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));
            mChannels[i] = slider;

            var value = new TextView(context);
            value.setSingleLine(true);
            value.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            mCaptions.add(value);
            row.addView(value, new LinearLayout.LayoutParams(
                    Math.round(dp(34f)), WRAP_CONTENT));
            mChannelValues[i] = value;

            mContent.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }

        var hexRow = new LinearLayout(context);
        hexRow.setOrientation(LinearLayout.HORIZONTAL);
        hexRow.setGravity(Gravity.CENTER_VERTICAL);
        var hexCaption = new TextView(context);
        hexCaption.setText(tr("mui.v2.color.hex", "HEX"));
        hexCaption.setSingleLine(true);
        mCaptions.add(hexCaption);
        hexRow.addView(hexCaption, new LinearLayout.LayoutParams(
                Math.round(dp(40f)), WRAP_CONTENT));

        mHex = new TgField(context);
        mHex.setMinWidth(Math.round(dp(120f)));
        mHex.setOnCommit(this::onHexCommitted);
        hexRow.addView(mHex, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));

        var hexParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        hexParams.setMargins(0, Math.round(dp(8f)), 0, 0);
        mContent.addView(hexRow, hexParams);

        addSection(context, "mui.v2.color.presets", "Presets");
        mContent.addView(presetGrid(context),
                new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        addSection(context, "mui.v2.color.recent", "Recent");
        mRecentRow = new LinearLayout(context);
        mRecentRow.setOrientation(LinearLayout.HORIZONTAL);
        mRecentRow.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < RECENT_MAX; i++) {
            final int slot = i;
            var swatch = new TgSwatch(context);
            swatch.setVisibility(GONE);
            swatch.setOnClickListener(v -> {
                int argb = recentAt(slot);
                if (argb != 0) {
                    onPresetClicked(argb);
                }
            });
            var params = new LinearLayout.LayoutParams(
                    Math.round(dp(26f)), Math.round(dp(26f)));
            params.setMargins(0, 0, Math.round(dp(6f)), 0);
            mRecentRow.addView(swatch, params);
            mRecentSwatches[i] = swatch;
        }
        mContent.addView(mRecentRow, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        applyType();
        syncUi();
    }

    @NonNull
    public ColorStudio setAlphaEnabled(boolean enabled) {
        if (mAlphaEnabled == enabled) {
            return this;
        }
        mAlphaEnabled = enabled;
        if (!enabled) {
            mAlpha = 0xFF;
        }
        mAlphaRow.setVisibility(enabled ? VISIBLE : GONE);
        syncUi();
        requestLayout();
        return this;
    }

    public boolean isAlphaEnabled() {
        return mAlphaEnabled;
    }

    public void setOnColor(OnColor listener) {
        mListener = listener;
    }

    /** Current colour as ARGB. */
    public int getColor() {
        return ColorMath.compose(mHsv, mAlphaEnabled ? mAlpha : 0xFF);
    }

    /** Colour the studio held before the most recent commit; the comparison swatch. */
    public int getPreviousColor() {
        return mPrevious;
    }

    /** Loads a colour without notifying the listener. */
    public void setColor(int argb) {
        mSyncing = true;
        ColorMath.rgbToHsv(argb | 0xFF000000, mHsv, mHsv[0]);
        mAlpha = mAlphaEnabled ? ((argb >>> 24) & 0xFF) : 0xFF;
        mCommitted = getColor();
        mPrevious = mCommitted;
        syncUi();
        mSyncing = false;
    }

    /** Commits the current colour, e.g. from an enclosing dialog's confirm button. */
    public void commitCurrent() {
        commit();
    }

    private void preview() {
        if (mListener != null) {
            mListener.onPreview(getColor());
        }
    }

    private void commit() {
        int argb = getColor();
        mPrevious = mCommitted;
        mCommitted = argb;
        remember(argb);
        syncUi();
        if (mListener != null) {
            mListener.onCommit(argb);
        }
    }

    private void remember(int argb) {
        mRecent.remove(Integer.valueOf(argb));
        mRecent.add(0, argb);
        while (mRecent.size() > RECENT_MAX) {
            mRecent.remove(mRecent.size() - 1);
        }
    }

    /** @return the recent colour at {@code index}, or 0 when that slot is still empty */
    private int recentAt(int index) {
        return index < mRecent.size() ? mRecent.get(index) : 0;
    }

    /** Pushes the model into every control without echoing back into it. */
    private void syncUi() {
        syncUi(true);
    }

    /**
     * @param syncChannels false while the RGB sliders are the source of the edit, so the
     *                     HSV round trip cannot nudge the slider the player is dragging
     */
    private void syncUi(boolean syncChannels) {
        boolean wasSyncing = mSyncing;
        mSyncing = true;
        int argb = getColor();
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        mPreviewSwatch.setColor(argb);
        mPreviousSwatch.setColor(mPrevious);
        mPreviewHex.setText(ColorMath.toHex(argb, mAlphaEnabled));

        if (syncChannels) {
            mChannels[0].setValue(r);
            mChannels[1].setValue(g);
            mChannels[2].setValue(b);
        }
        mChannelValues[0].setText(Integer.toString(r));
        mChannelValues[1].setText(Integer.toString(g));
        mChannelValues[2].setText(Integer.toString(b));

        String hex = ColorMath.toHex(argb, mAlphaEnabled);
        if (!mHex.getText().equals(hex)) {
            mHex.setText(hex);
        }

        int i = 0;
        for (int recent : mRecent) {
            mRecentSwatches[i].setColor(recent);
            mRecentSwatches[i].setVisibility(VISIBLE);
            i++;
        }
        for (; i < RECENT_MAX; i++) {
            mRecentSwatches[i].setVisibility(GONE);
        }

        mPlane.invalidate();
        mHueBar.invalidate();
        mAlphaBar.invalidate();
        mSyncing = wasSyncing;
    }

    private void applyChannels() {
        int r = Math.round(mChannels[0].getValue());
        int g = Math.round(mChannels[1].getValue());
        int b = Math.round(mChannels[2].getValue());
        ColorMath.rgbToHsv((r << 16) | (g << 8) | b, mHsv, mHsv[0]);
        syncUi(false);
    }

    private void applyFromModel(boolean commit) {
        if (commit) {
            commit();
            return;
        }
        syncUi();
        preview();
    }

    private void onHexCommitted(String text) {
        int parsed;
        try {
            parsed = ColorMath.parseHex(text);
        } catch (RuntimeException e) {
            // an unparsable entry reverts to the colour the studio already holds
            syncUi();
            return;
        }
        ColorMath.rgbToHsv(parsed | 0xFF000000, mHsv, mHsv[0]);
        if (mAlphaEnabled) {
            mAlpha = (parsed >>> 24) & 0xFF;
        }
        applyFromModel(true);
    }

    private void onPresetClicked(int argb) {
        ColorMath.rgbToHsv(argb | 0xFF000000, mHsv, mHsv[0]);
        if (mAlphaEnabled) {
            mAlpha = (argb >>> 24) & 0xFF;
        }
        applyFromModel(true);
    }

    private LinearLayout presetGrid(@NonNull Context context) {
        var grid = new LinearLayout(context);
        grid.setOrientation(LinearLayout.VERTICAL);
        int size = Math.round(dp(28f));
        int gap = Math.round(dp(5f));
        int perRow = 8;
        for (int start = 0; start < PRESETS.length; start += perRow) {
            var row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            var rowParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            if (start > 0) {
                rowParams.setMargins(0, gap, 0, 0);
            }
            grid.addView(row, rowParams);
            for (int i = start; i < Math.min(start + perRow, PRESETS.length); i++) {
                final int preset = PRESETS[i];
                var swatch = new TgSwatch(context);
                swatch.setColor(preset);
                swatch.setRadiusDp(6f);
                swatch.setOnClickListener(v -> onPresetClicked(preset));
                var params = new LinearLayout.LayoutParams(size, size);
                if (i > start) {
                    params.setMargins(gap, 0, 0, 0);
                }
                row.addView(swatch, params);
            }
        }
        return grid;
    }

    /** A small caption on the left, the control taking the rest of the row. */
    private LinearLayout captionedBar(@NonNull Context context, @NonNull String key,
                                      @NonNull String fallback, @NonNull View control) {
        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        var caption = new TextView(context);
        caption.setText(tr(key, fallback));
        caption.setSingleLine(true);
        mCaptions.add(caption);
        row.addView(caption, new LinearLayout.LayoutParams(Math.round(dp(46f)), WRAP_CONTENT));
        row.addView(control, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f));
        return row;
    }

    private void addSection(@NonNull Context context, @NonNull String key, @NonNull String fallback) {
        var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.setMargins(0, Math.round(dp(12f)), 0, Math.round(dp(4f)));
        mContent.addView(section(context, key, fallback), params);
    }

    private TextView section(@NonNull Context context, @NonNull String key, @NonNull String fallback) {
        var label = new TextView(context);
        label.setText(tr(key, fallback));
        label.setSingleLine(true);
        mSections.add(label);
        return label;
    }

    private void applyType() {
        var tk = tokens();
        for (var label : mSections) {
            String text = label.getText().toString();
            label.setTextSize(tk.sectionSize);
            label.setTextColor(tk.muted);
            if (tk.typeface != null) {
                label.setTypeface(tk.typeface);
            }
            if (tk.upperCaseSections) {
                label.setText(text.toUpperCase(Locale.ROOT));
            }
        }
        for (var label : mCaptions) {
            label.setTextSize(tk.descSize);
            label.setTextColor(tk.muted);
            if (tk.typeface != null) {
                label.setTypeface(tk.typeface);
            }
        }
        mPreviewHex.setTextSize(tk.valueSize);
        mPreviewHex.setTextColor(tk.text);
        if (tk.monoTypeface != null) {
            mPreviewHex.setTypeface(tk.monoTypeface);
        }
    }

    @NonNull
    private static String tr(@NonNull String key, @NonNull String fallback) {
        String text = I18n.get(key);
        return text.equals(key) ? fallback : text;
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        applyType();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // an unconstrained host would otherwise collapse the plane to zero width
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.round(dp(300f)), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Whether the studio paints its own card behind the controls. A host that already draws
     * a surface (a popup, a page block) turns this off to avoid stacking two panels.
     */
    public void setPanelBackground(boolean draw) {
        mPanel = draw;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mPanel) {
            skin().drawPanel(canvas, 0, 0, getWidth(), getHeight(), tokens().radiusPanel);
        }
    }

    /**
     * Saturation on the horizontal axis, value on the vertical one. Two stacked gradients,
     * exactly as the canvas primitives require.
     */
    private final class SvPlane extends View {

        SvPlane(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = resolveSize(Math.round(dp(200f)), widthMeasureSpec);
            int height = resolveSize(Math.round(dp(120f)), heightMeasureSpec);
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            var paint = Paint.obtain();
            paint.setAntiAlias(true);

            int pureHue = 0xFF000000 | ColorMath.hsvToRgb(mHsv[0], 1f, 1f);
            paint.setShader(new LinearGradient(0, 0, w, 0,
                    new int[]{0xFFFFFFFF, pureHue}, null, Shader.TileMode.CLAMP, null));
            canvas.drawRect(0, 0, w, h, paint);

            paint.setShader(new LinearGradient(0, 0, 0, h,
                    new int[]{0x00000000, 0xFF000000}, null, Shader.TileMode.CLAMP, null));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            float cx = mHsv[1] * w;
            float cy = (1f - mHsv[2]) * h;
            float radius = dp(7f);
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(dp(2f));
            paint.setColor(0xFF000000);
            canvas.drawCircle(cx, cy, radius + dp(1f), paint);
            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setStyle(Paint.FILL);
            paint.setColor(0xFF000000 | ColorMath.hsvToRgb(mHsv[0], mHsv[1], mHsv[2]));
            canvas.drawCircle(cx, cy, Math.max(1f, radius - dp(2f)), paint);

            skin().drawSwatchFrame(canvas, 0, 0, w, h, tokens().radiusControl);
            paint.recycle();
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    var parent = getParent();
                    if (parent != null) {
                        // the enclosing scroll view would otherwise steal the drag
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    update(event.getX(), event.getY(), false);
                    return true;
                }
                case MotionEvent.ACTION_MOVE -> {
                    update(event.getX(), event.getY(), false);
                    return true;
                }
                case MotionEvent.ACTION_UP -> {
                    update(event.getX(), event.getY(), true);
                    return true;
                }
                case MotionEvent.ACTION_CANCEL -> {
                    return true;
                }
                default -> {
                    return true;
                }
            }
        }

        private void update(float x, float y, boolean commit) {
            float w = Math.max(1f, getWidth());
            float h = Math.max(1f, getHeight());
            mHsv[1] = ColorMath.clamp(x / w);
            mHsv[2] = 1f - ColorMath.clamp(y / h);
            applyFromModel(commit);
        }
    }

    /** Hue or opacity strip with a marker; {@code mHue} selects which channel it edits. */
    private final class Bar extends View {

        private final boolean mHue;

        Bar(@NonNull Context context, boolean hue) {
            super(context);
            mHue = hue;
            setWillNotDraw(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = resolveSize(Math.round(dp(120f)), widthMeasureSpec);
            int height = resolveSize(Math.round(dp(14f)), heightMeasureSpec);
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            var paint = Paint.obtain();
            paint.setAntiAlias(true);

            if (mHue) {
                paint.setShader(new LinearGradient(0, 0, w, 0,
                        new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                                0xFF0000FF, 0xFFFF00FF, 0xFFFF0000},
                        null, Shader.TileMode.CLAMP, null));
                canvas.drawRect(0, 0, w, h, paint);
                paint.setShader(null);
            } else {
                ColorMath.checkerboard(canvas, paint, 0, 0, w, h, dp(5f));
                int opaque = 0xFF000000 | ColorMath.hsvToRgb(mHsv[0], mHsv[1], mHsv[2]);
                paint.setShader(new LinearGradient(0, 0, w, 0,
                        new int[]{opaque & 0x00FFFFFF, opaque}, null, Shader.TileMode.CLAMP, null));
                canvas.drawRect(0, 0, w, h, paint);
                paint.setShader(null);
            }

            float fraction = mHue ? mHsv[0] / 360f : (mAlphaEnabled ? mAlpha / 255f : 1f);
            float cx = fraction * w;
            float half = dp(3.5f);
            paint.setStyle(Paint.STROKE);
            paint.setStrokeWidth(dp(2f));
            paint.setColor(0xFF000000);
            canvas.drawRoundRect(cx - half - dp(2f), -dp(2f), cx + half + dp(2f), h + dp(2f),
                    dp(3f), paint);
            paint.setColor(0xFFFFFFFF);
            canvas.drawRoundRect(cx - half, 0, cx + half, h, dp(3f), paint);
            paint.setStyle(Paint.FILL);
            paint.setColor(mHue
                    ? 0xFF000000 | ColorMath.hsvToRgb(mHsv[0], 1f, 1f)
                    : ColorMath.compose(mHsv, mAlphaEnabled ? mAlpha : 0xFF));
            canvas.drawRoundRect(cx - half + dp(2f), dp(2f), cx + half - dp(2f), h - dp(2f),
                    dp(2f), paint);

            skin().drawSwatchFrame(canvas, 0, 0, w, h, tokens().radiusControl);
            paint.recycle();
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    var parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    update(event.getX(), false);
                    return true;
                }
                case MotionEvent.ACTION_MOVE -> {
                    update(event.getX(), false);
                    return true;
                }
                case MotionEvent.ACTION_UP -> {
                    update(event.getX(), true);
                    return true;
                }
                case MotionEvent.ACTION_CANCEL -> {
                    return true;
                }
                default -> {
                    return true;
                }
            }
        }

        private void update(float x, boolean commit) {
            float fraction = ColorMath.clamp(x / Math.max(1f, getWidth()));
            if (mHue) {
                mHsv[0] = fraction * 360f;
            } else if (mAlphaEnabled) {
                mAlpha = Math.round(fraction * 255f);
            }
            applyFromModel(commit);
        }
    }
}
