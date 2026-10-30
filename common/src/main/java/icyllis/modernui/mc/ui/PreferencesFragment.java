/*
 * Modern UI.
 * Copyright (C) 2023-2026 BloCamLimb. All rights reserved.
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
import icyllis.modernui.R;
import icyllis.modernui.animation.AnimatorSet;
import icyllis.modernui.animation.LayoutTransition;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.core.Core;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.MathUtil;
import icyllis.modernui.graphics.drawable.BuiltinIconDrawable;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.graphics.drawable.StateListDrawable;
import icyllis.modernui.graphics.text.FontFamily;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.ModernUIClient;
import icyllis.modernui.mc.ModernUIMod;
import icyllis.modernui.mc.ui.studio.StudioHome;
import icyllis.modernui.mc.ui.studio.StudioPanel;
import icyllis.modernui.mc.ui.studio.StudioPages;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.MuiPlatform;
import icyllis.modernui.mc.UIManager;
import icyllis.modernui.resources.TypedValue;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.text.method.DigitsInputFilter;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.ContextMenu;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.Menu;
import icyllis.modernui.view.MenuItem;
import icyllis.modernui.view.OneShotPreDrawListener;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

public class PreferencesFragment extends Fragment {

    private static final org.apache.logging.log4j.Marker MARKER =
            org.apache.logging.log4j.MarkerManager.getMarker("Preferences");

    boolean mCommonConfigChanged;
    boolean mClientConfigChanged;
    boolean mTextConfigChanged;

    final Runnable mOnCommonConfigChanged = () -> {
        mCommonConfigChanged = true;
        Config.COMMON.reload();
    };
    final Runnable mOnClientConfigChanged = () -> {
        mClientConfigChanged = true;
        Config.CLIENT.reload();
    };
    final Runnable mOnTextConfigChanged = () -> {
        mTextConfigChanged = true;
        Config.TEXT.reload();
    };

    private static final int PAGE_COUNT = 6;

    private static final String[] TILE_TITLES = {
            "modernui.center.category.appearance",
            "modernui.center.category.screen",
            "modernui.center.category.view",
            "modernui.center.category.font",
            "modernui.center.category.extension",
            "modernui.center.category.text"
    };

    private static final String[] TILE_SUBTITLES = {
            "modernui.center.tile.appearance",
            "modernui.center.tile.screen",
            "modernui.center.tile.view",
            "modernui.center.tile.font",
            "modernui.center.tile.extension",
            "modernui.center.tile.text"
    };

    private StudioHome mHome;

    private final AccentPalette.Listener mAccentListener = this::refreshAccent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        final long startedAt = System.nanoTime();
        var context = requireContext();

        var titles = new String[PAGE_COUNT];
        var subtitles = new String[PAGE_COUNT];
        for (int i = 0; i < PAGE_COUNT; i++) {
            titles[i] = I18n.get(TILE_TITLES[i]);
            subtitles[i] = I18n.get(TILE_SUBTITLES[i]);
        }

        AccentPalette.init(context);
        AccentPalette.addListener(mAccentListener);

        mHome = new StudioHome(context, titles, subtitles, this::buildPanel,
                "Modern UI", I18n.get("modernui.center.subtitle") + "  ·  3.14.9-beta");
        ModernUIMod.LOGGER.info(MARKER, "Built the settings studio in {} ms",
                (System.nanoTime() - startedAt) / 1_000_000L);
        return mHome;
    }

    private void buildPanel(int index, StudioPanel panel) {
        StudioPages.build(index, panel, mHost);
    }

    private final StudioPages.Host mHost = new StudioPages.Host() {

        @NonNull
        @Override
        public Runnable clientChanged() {
            return mOnClientConfigChanged;
        }

        @NonNull
        @Override
        public Runnable commonChanged() {
            return mOnCommonConfigChanged;
        }

        @NonNull
        @Override
        public Runnable textChanged() {
            return mOnTextConfigChanged;
        }

        @NonNull
        @Override
        public View legacyPage(@NonNull Context context, int page) {
            return switch (page) {
                // font page keeps its family and registration editors for now
                case 3 -> createPage4(context);
                case 5 -> createPage6(context);
                default -> new LinearLayout(context);
            };
        }

        @NonNull
        @Override
        public View colorListRow(@NonNull Context context, @NonNull String key,
                                 @NonNull ConfigItem<List<? extends String>> item,
                                 @NonNull Runnable onChanged) {
            return createColorOpacityOption(context, key, item, onChanged);
        }

        @NonNull
        @Override
        public View guiScaleRow(@NonNull Context context) {
            return createGuiScaleOption(context);
        }
    };

    private void refreshAccent() {
        // the studio reads colours while building, so an open panel is rebuilt on demand
        if (mHome != null) {
            mHome.invalidate();
        }
    }

    @Override
    public void onDestroy() {
        AccentPalette.removeListener(mAccentListener);
        super.onDestroy();
        int count = (mClientConfigChanged ? 1 : 0) +
                (mCommonConfigChanged ? 1 : 0) +
                (mTextConfigChanged ? 1 : 0);
        if (count != 0) {
            int[] configs = new int[count];
            count = 0;
            if (mClientConfigChanged) {
                configs[count++] = Config.TYPE_CLIENT;
                mClientConfigChanged = false;
            }
            if (mCommonConfigChanged) {
                configs[count++] = Config.TYPE_COMMON;
                mCommonConfigChanged = false;
            }
            if (mTextConfigChanged) {
                configs[count++] = Config.TYPE_TEXT;
                mTextConfigChanged = false;
            }
            assert count == configs.length;
            Util.ioPool().execute(() -> {
                var service = MuiPlatform.get();
                for (int type : configs) {
                    service.saveConfig(type);
                }
            });
        }
    }

    public LinearLayout createPage3(Context context) {
        var content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        Runnable onChanged = mOnClientConfigChanged;

        {
            var list = createCategoryList(content, null);

            new IntegerOption(context, "modernui.center.view.scrollbarSize",
                    1, Config.CLIENT.mScrollbarSize, onChanged)
                    .create(list, 4);
            new IntegerOption(context, "modernui.center.view.touchSlop",
                     1, Config.CLIENT.mTouchSlop, onChanged)
                    .create(list, 4);
            new IntegerOption(context, "modernui.center.view.hoverSlop",
                     1, Config.CLIENT.mHoverSlop, onChanged)
                    .create(list, 4);
            new IntegerOption(context, "modernui.center.view.minScrollbarTouchTarget",
                     1, Config.CLIENT.mMinScrollbarTouchTarget, onChanged)
                    .create(list, 4);
            new IntegerOption(context, "modernui.center.view.minimumFlingVelocity",
                     1, Config.CLIENT.mMinimumFlingVelocity, onChanged)
                    .create(list, 4);
            new IntegerOption(context, "modernui.center.view.maximumFlingVelocity",
                     1, Config.CLIENT.mMaximumFlingVelocity, onChanged)
                    .create(list, 4);
            new FloatOption(context, "modernui.center.view.scrollFriction",
                    Config.CLIENT.mScrollFriction, 1000, onChanged)
                    .create(list, 6);
            new IntegerOption(context, "modernui.center.view.overscrollDistance",
                     1, Config.CLIENT.mOverscrollDistance, onChanged)
                    .create(list, 4);
            new IntegerOption(context, "modernui.center.view.overflingDistance",
                     1, Config.CLIENT.mOverflingDistance, onChanged)
                    .create(list, 4);
            new FloatOption(context, "modernui.center.view.horizontalScrollFactor",
                    Config.CLIENT.mHorizontalScrollFactor, 10, onChanged)
                    .create(list, 6);
            new FloatOption(context, "modernui.center.view.verticalScrollFactor",
                    Config.CLIENT.mVerticalScrollFactor, 10, onChanged)
                    .create(list, 6);
            new IntegerOption(context, "modernui.center.view.hoverTooltipShowTimeout",
                     1, Config.CLIENT.mHoverTooltipShowTimeout, onChanged)
                    .create(list, 4);
            new IntegerOption(context, "modernui.center.view.hoverTooltipHideTimeout",
                     1, Config.CLIENT.mHoverTooltipHideTimeout, onChanged)
                    .create(list, 6);

            content.addView(list);
        }

        {
            var list = createCategoryList(content, "modernui.center.category.system");

            list.addView(createStringListOption(context, "modernui.center.system.theme",
                    Config.CLIENT.mTheme, onChanged));

            new BooleanOption(context, "modernui.center.system.forceRtlLayout",
                    Config.CLIENT.mForceRtl, onChanged)
                    .create(list);

            new FloatOption(context, "modernui.center.system.globalFontScale",
                    Config.CLIENT.mFontScale, 100, onChanged)
                    .create(list, 4);

            new FloatOption(context, "modernui.center.system.globalAnimationScale",
                    () -> (double) ValueAnimator.sDurationScale,
                    (scale) -> ValueAnimator.sDurationScale = scale.floatValue())
                    .setRange(0.1, 10.0, 100)
                    .setDefaultValue(1.0)
                    .create(list, 4);

            new BooleanOption(context, "modernui.center.system.developerMode",
                    Config.COMMON.developerMode, mOnCommonConfigChanged)
                    .create(list);

            content.addView(list);
        }

        return content;
    }

    public LinearLayout createPage4(Context context) {
        var content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        var transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        content.setLayoutTransition(transition);

        {
            var category = createCategoryList(content, null);

            final LinearLayout firstLine = new LinearLayout(context);
            firstLine.setOrientation(LinearLayout.HORIZONTAL);
            var dp6 = firstLine.dp(6);
            {
                TextView title = new TextView(context);
                title.setText(I18n.get("modernui.center.font.firstFont"));
                title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                title.setTextSize(14);

                var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1);
                params.gravity = Gravity.CENTER_VERTICAL;
                params.setMargins(dp6, 0, dp6, 0);
                firstLine.addView(title, params);
            }
            Runnable onFontChanged;
            {
                TextView value = new TextView(context);
                onFontChanged = () -> {
                    FontFamily first = ModernUIClient.getInstance().getFirstFontFamily();
                    if (first != null) {
                        value.setText(first.getFamilyName(value.getTextLocale()));
                    } else {
                        value.setText("NONE");
                    }
                };
                onFontChanged.run();
                value.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                value.setTextSize(14);

                var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                params.setMargins(dp6, 0, dp6, 0);
                firstLine.addView(value, params);
            }

            var accordion = new PreferredFontAccordion(
                    category,
                    mOnClientConfigChanged,
                    onFontChanged
            );
            firstLine.setOnClickListener(
                    accordion
            );
            firstLine.setOnCreateContextMenuListener(
                    accordion
            );
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(R.ns, R.attr.colorControlHighlight, value, true);
            firstLine.setBackground(new RippleDrawable(ColorStateList.valueOf(value.data), null,
                    new ColorDrawable(~0)));

            firstLine.setMinimumHeight(firstLine.dp(36));
            var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            category.addView(firstLine, params);

            content.addView(category);
        }

        {
            var category = createCategoryList(content, null);

            {
                var option = createStringListOption(context, "modernui.center.font.fallbackFonts",
                        Config.CLIENT.mFallbackFontFamilyList,
                        () -> {
                            mOnClientConfigChanged.run();
                            reloadDefaultTypeface(context, null);
                        });
                category.addView(option);
            }

            /*list.addView(createBooleanOption(context, "modernui.center.font.vanillaFont",
                    ModernUIText.CONFIG.mUseVanillaFont,
                    ModernUIText.CONFIG::saveAndReloadAsync));*/

            new BooleanOption(context, "modernui.center.font.colorEmoji",
                    Config.CLIENT.mUseColorEmoji, () -> {
                mOnClientConfigChanged.run();
                reloadDefaultTypeface(context, null);
            })
                    .create(category);

            category.addView(createStringListOption(context, "modernui.center.font.fontRegistrationList",
                    Config.CLIENT.mFontRegistrationList, () -> {
                        mClientConfigChanged = true;
                        Toast.makeText(context,
                                        I18n.get("gui.modernui.restart_to_work"),
                                        Toast.LENGTH_SHORT)
                                .show();
                    }));

            new BooleanOption(context, "modernui.center.font.linearMetrics",
                    Config.CLIENT.mLinearMetrics, mOnClientConfigChanged)
                    .create(category);

            content.addView(category);
        }

        return content;
    }

    public LinearLayout createPage5(Context context) {
        var content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        Runnable onChanged = mOnClientConfigChanged;

        boolean curTooltipEnabled = Config.CLIENT.mTooltip.get();

        final View[] tooltipCategories = new View[2];

        {
            var list = createCategoryList(content, null);

            new BooleanOption(context, "modernui.center.extension.ding",
                    Config.CLIENT.mDing, onChanged)
                    .create(list);

            if (Config.CLIENT.mZoom != null) {
                new BooleanOption(context, "key.modernui.zoom",
                        Config.CLIENT.mZoom, onChanged)
                        .create(list);
            }

            new BooleanOption(context, "modernui.center.text.emojiShortcodes",
                    Config.CLIENT.mEmojiShortcodes, onChanged)
                    .create(list);

            new BooleanOption(context, "modernui.center.extension.smoothScrolling",
                    () -> !Boolean.parseBoolean(
                            ModernUIClient.getBootstrapProperty(ModernUIMod.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING)
                    ),
                    (value) -> ModernUIClient.setBootstrapProperty(
                            ModernUIMod.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING,
                            Boolean.toString(!value)
                    ))
                    .setDefaultValue(true)
                    .setNeedsRestart()
                    .create(list);

            new BooleanOption(context, "modernui.center.text.enhancedTextField",
                    () -> !Boolean.parseBoolean(
                            ModernUIClient.getBootstrapProperty(ModernUIMod.BOOTSTRAP_DISABLE_ENHANCED_TEXT_FIELD)
                    ),
                    (value) -> ModernUIClient.setBootstrapProperty(
                            ModernUIMod.BOOTSTRAP_DISABLE_ENHANCED_TEXT_FIELD,
                            Boolean.toString(!value)
                    ))
                    .setDefaultValue(true)
                    .setNeedsRestart()
                    .create(list);

            {
                var option = createSwitchLayout(context, "modernui.center.extension.tooltip");
                var button = option.<Switch>requireViewById(R.id.button1);
                button.setChecked(curTooltipEnabled);
                button.setOnCheckedChangeListener((view, checked) -> {
                    Config.CLIENT.mTooltip.set(checked);
                    onChanged.run();
                    for (var v : tooltipCategories) {
                        ThemeControl.setViewAndChildrenEnabled(v, checked);
                    }
                });
                list.addView(option);
            }

            /*list.addView(createIntegerOption(context, "modernui.center.extension.tooltipDuration",
                    Config.Client.ANIM_DURATION_MIN, Config.Client.ANIM_DURATION_MAX,
                    3, 50, Config.CLIENT.mTooltipDuration, onChanged));*/

            content.addView(list);
        }

        content.addView(tooltipCategories[0] = createTooltipCategory(content));
        content.addView(tooltipCategories[1] = createTooltipBorderCategory(content));
        for (var v : tooltipCategories) {
            ThemeControl.setViewAndChildrenEnabled(v, curTooltipEnabled);
        }

        return content;
    }

    public LinearLayout createPage6(Context context) {
        var content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        boolean curTextEngineEnabled = !Boolean.parseBoolean(
                ModernUIClient.getBootstrapProperty(ModernUIMod.BOOTSTRAP_DISABLE_TEXT_ENGINE));

        final View[] categories = new View[2];

        {
            var list = createCategoryList(content, null);
            {
                var option = createSwitchLayout(context, "modernui.center.text.textEngine");
                var button = option.<Switch>requireViewById(R.id.button1);
                button.setChecked(curTextEngineEnabled);
                button.setOnCheckedChangeListener((view, checked) -> {
                    ModernUIClient.setBootstrapProperty(
                            ModernUIMod.BOOTSTRAP_DISABLE_TEXT_ENGINE,
                            Boolean.toString(!checked)
                    );
                    Toast.makeText(
                            view.getContext(),
                            I18n.get("gui.modernui.restart_to_work"),
                            Toast.LENGTH_SHORT
                    ).show();
                    for (var v : categories) {
                        ThemeControl.setViewAndChildrenEnabled(v, checked);
                    }
                });
                list.addView(option);
            }
            content.addView(list);
        }

        content.addView(categories[0] = createTextLayoutCategory(content));
        content.addView(categories[1] = createTextRenderingCategory(content));
        for (var v : categories) {
            ThemeControl.setViewAndChildrenEnabled(v, curTextEngineEnabled);
        }

        return content;
    }

    public LinearLayout createTooltipCategory(@NonNull ViewGroup page) {
        var context = page.getContext();
        var category = createCategoryList(page, "modernui.center.category.tooltip");

        Runnable onChanged = mOnClientConfigChanged;

        new BooleanOption(context, "modernui.center.tooltip.centerTitle",
                Config.CLIENT.mCenterTooltipTitle, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.tooltip.titleBreak",
                Config.CLIENT.mTooltipTitleBreak, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.tooltip.exactPositioning",
                Config.CLIENT.mExactTooltipPositioning, onChanged)
                .create(category);

        new IntegerOption(context, "modernui.center.tooltip.arrowScrollFactor",
                2, Config.CLIENT.mTooltipArrowScrollFactor, onChanged)
                .create(category, 3);

        category.addView(createColorOpacityOption(context, "modernui.center.tooltip.backgroundOpacity",
                Config.CLIENT.mTooltipFill, onChanged));

        return category;
    }

    public LinearLayout createTooltipBorderCategory(@NonNull ViewGroup page) {
        var context = page.getContext();
        var category = createCategoryList(page, null);
        var transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        category.setLayoutTransition(transition);

        {
            final Button title;
            title = new ToggleButton(context, null, R.attr.borderlessButtonStyle);
            title.setText(I18n.get("modernui.center.tooltip.borderStyle"));
            title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            {
                var icon = new StateListDrawable();
                icon.addState(new int[]{R.attr.state_checked}, new BuiltinIconDrawable(
                        context.getResources(), BuiltinIconDrawable.KEYBOARD_ARROW_UP
                ));
                icon.addState(StateSet.WILD_CARD, new BuiltinIconDrawable(
                        context.getResources(), BuiltinIconDrawable.KEYBOARD_ARROW_DOWN
                ));
                icon.setTintList(title.getTextColors());
                title.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null, null, icon, null
                );
            }
            title.setOnClickListener(new TooltipBorderAccordion(category, mOnClientConfigChanged));

            var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            category.addView(title, params);
        }

        return category;
    }

    public LinearLayout createTextLayoutCategory(@NonNull ViewGroup page) {
        var context = page.getContext();
        var category = createCategoryList(page, "modernui.center.category.textLayout");

        Runnable onChanged = mOnTextConfigChanged;

        new DropDownOption<>(context, "modernui.center.text.defaultFontBehavior",
                Config.Text.DefaultFontBehavior.values(),
                Config.TEXT.mDefaultFontBehavior, onChanged)
                .create(category);

        category.addView(createStringListOption(context, "modernui.center.text.defaultFontRuleSet",
                Config.TEXT.mDefaultFontRuleSet, onChanged));

        new DropDownOption<>(context, "modernui.center.text.bidiHeuristicAlgo",
                Config.Text.TextDirection.values(),
                Config.TEXT.mTextDirection, onChanged)
                .create(category);

        new DropDownOption<>(context, "modernui.center.text.lineBreakStyle",
                Config.Text.LineBreakStyle.values(),
                Config.TEXT.mLineBreakStyle, onChanged)
                .create(category);

        new DropDownOption<>(context, "modernui.center.text.lineBreakWordStyle",
                Config.Text.LineBreakWordStyle.values(),
                Config.TEXT.mLineBreakWordStyle, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.text.allowAsyncLayout",
                Config.TEXT.mAllowAsyncLayout, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.text.useComponentCache",
                Config.TEXT.mUseComponentCache, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.text.fixedResolution",
                Config.TEXT.mFixedResolution, onChanged)
                .create(category);

        new FloatOption(context, "modernui.center.text.baseFontSize",
                Config.TEXT.mBaseFontSize, 10, onChanged)
                .create(category, 4);

        new IntegerOption(context, "modernui.center.text.cacheLifespan",
                1, Config.TEXT.mCacheLifespan, onChanged)
                .create(category, 2);

        return category;
    }

    public LinearLayout createTextRenderingCategory(@NonNull ViewGroup page) {
        var context = page.getContext();
        var category = createCategoryList(page, "modernui.center.category.textRendering");

        Runnable onChanged = mOnTextConfigChanged;

        new BooleanOption(context, "modernui.center.font.antiAliasing",
                Config.TEXT.mAntiAliasing, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.font.linearMetrics",
                Config.TEXT.mLinearMetrics, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.text.textShadersInWorld",
                Config.TEXT.mUseTextShadersInWorld, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.text.allowSDFTextIn2D",
                Config.TEXT.mAllowSDFTextIn2D, onChanged)
                .create(category);

        /*new BooleanOption(context, "modernui.center.text.smartSDFShaders",
                Config.TEXT.mSmartSDFShaders, onChanged)
                .create(category);*/

        new BooleanOption(context, "modernui.center.text.computeDeviceFontSize",
                Config.TEXT.mComputeDeviceFontSize, onChanged)
                .create(category);

        new IntegerOption(context, "modernui.center.text.minPixelDensityForSDF",
                1, Config.TEXT.mMinPixelDensityForSDF, onChanged)
                .create(category, 2);

        new BooleanOption(context, "modernui.center.font.linearSampling",
                Config.TEXT.mLinearSamplingA8Atlas, onChanged)
                .create(category);

        new BooleanOption(context, "modernui.center.text.allowShadow",
                Config.TEXT.mAllowShadow, onChanged)
                .create(category);

        new FloatOption(context, "modernui.center.text.shadowOffset",
                Config.TEXT.mShadowOffset, 100, onChanged)
                .create(category, 4);

        new FloatOption(context, "modernui.center.text.baselineShift",
                Config.TEXT.mBaselineShift, 10, onChanged)
                .create(category, 4);

        new FloatOption(context, "modernui.center.text.outlineOffset",
                Config.TEXT.mOutlineOffset, 100, onChanged)
                .create(category, 4);

        new FloatOption(context, "modernui.center.text.bitmapOffset",
                Config.TEXT.mBitmapOffset, 20, onChanged)
                .create(category, 4);

        return category;
    }

    /**
     * A titled card. The header lives inside the card so pages only stack cards.
     *
     * @param page may be null; the card is then created but not attached
     */
    @NonNull
    public static CardView createCard(@NonNull ViewGroup page, @Nullable String name) {
        var card = createCard(page.getContext(), name);
        var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.setMargins(0, page.dp(Design.SPACE_3), 0, 0);
        page.addView(card, params);
        return card;
    }

    /**
     * Creates a titled card without attaching it anywhere.
     */
    @NonNull
    public static CardView createCard(@NonNull Context context, @Nullable String name) {
        var card = new CardView(context);
        if (name != null) {
            var header = new LinearLayout(context);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);
            int padH = header.dp(Design.SPACE_4);
            header.setPadding(padH, header.dp(Design.SPACE_3), padH, header.dp(Design.SPACE_1));

            var dot = new View(context);
            dot.setBackground(Design.rounded(context, Design.accent(context), Design.RADIUS_PILL));
            var dotParams = new LinearLayout.LayoutParams(dot.dp(6), dot.dp(6));
            dotParams.setMargins(0, 0, dot.dp(Design.SPACE_2), 0);
            header.addView(dot, dotParams);

            var text = new TextView(context);
            text.setText(I18n.get(name));
            text.setTextSize(12);
            text.setTextColor(Design.onSurfaceVariant(context));
            header.addView(text);

            card.addView(header, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
        return card;
    }

    /**
     * @param name section title, null for an untitled card
     */
    /**
     * Legacy entry point: returns an unattached titled card, the caller adds it to the page.
     */
    @NonNull
    public static LinearLayout createCategoryList(@NonNull ViewGroup page,
                                                  @Nullable String name) {
        return createCard(page.getContext(), name);
    }

    /**
     * Title block of a row: the label, plus an optional secondary line taken from
     * the {@code <key>.desc} translation when one exists.
     */
    @NonNull
    public static LinearLayout createRowTitle(@NonNull Context context, @NonNull String name) {
        var block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setGravity(Gravity.CENTER_VERTICAL);

        var title = new TextView(context);
        title.setText(ThemeControl.stripFormattingCodes(I18n.get(name)));
        title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        title.setTextSize(14);
        title.setTextColor(Design.onSurface(context));
        block.addView(title);

        String descKey = name + ".desc";
        String desc = I18n.get(descKey);
        if (!desc.equals(descKey)) {
            var sub = new TextView(context);
            sub.setText(ThemeControl.stripFormattingCodes(desc));
            sub.setTextSize(11);
            sub.setTextColor(Design.alpha(Design.onSurfaceVariant(context), 0.85f));
            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.setMargins(0, block.dp(1), 0, 0);
            block.addView(sub, params);
        }
        return block;
    }

    /** Row skeleton: leading title block and a trailing control slot. */
    @NonNull
    public static LinearLayout createRow(@NonNull Context context, @NonNull String name) {
        var row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int padH = row.dp(Design.SPACE_4);
        row.setPadding(padH, row.dp(Design.SPACE_1), padH, row.dp(Design.SPACE_1));
        row.setMinimumHeight(Design.rowHeight(context));

        var params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1);
        params.gravity = Gravity.CENTER_VERTICAL;
        row.addView(createRowTitle(context, name), params);

        String tooltip = name + ".tooltip";
        String text = I18n.get(tooltip);
        if (!text.equals(tooltip)) {
            row.setTooltipText(text);
        }
        return row;
    }

    public static LinearLayout createSwitchLayout(Context context, String name) {
        var layout = createRow(context, name);

        var button = new Switch(context);
        button.setId(R.id.button1);
        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        layout.addView(button, params);

        return layout;
    }

    public static final int ID_RESET_TO_DEFAULT = 0x7f000002;

    @NonNull
    public static MenuItem addResetToDefaultMenuItem(@NonNull Menu menu) {
        return menu.add(Menu.NONE, ID_RESET_TO_DEFAULT, Menu.CATEGORY_ALTERNATIVE | 0,
                I18n.get("gui.modernui.resetToDefault"));
    }

    public static class BooleanOption implements
            Checkable.OnCheckedChangeListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

        protected final LinearLayout layout;
        protected final Switch button;

        protected final Supplier<Boolean> getter;
        protected final Consumer<Boolean> setter;
        @Nullable
        protected Runnable onChanged;

        protected boolean hasDefaultValue = false;
        protected boolean defaultValue;

        protected boolean needsRestart;

        public BooleanOption(Context context, String name,
                             Supplier<Boolean> getter,
                             Consumer<Boolean> setter) {
            layout = createSwitchLayout(context, name);
            button = layout.<Switch>requireViewById(R.id.button1);
            this.getter = getter;
            this.setter = setter;
        }

        public BooleanOption(Context context, String name,
                             ConfigItem<Boolean> config,
                             Consumer<Boolean> setter,
                             @Nullable Runnable onChanged) {
            this(context, name, config, setter);
            hasDefaultValue = true;
            defaultValue = config.getDefault();
            this.onChanged = onChanged;
        }

        public BooleanOption(Context context, String name,
                             ConfigItem<Boolean> config,
                             @Nullable Runnable onChanged) {
            this(context, name, config, config, onChanged);
        }

        public BooleanOption setDefaultValue(boolean defaultValue) {
            this.hasDefaultValue = true;
            this.defaultValue = defaultValue;
            return this;
        }

        public BooleanOption setOnChanged(Runnable onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public BooleanOption setNeedsRestart() {
            this.needsRestart = true;
            return this;
        }

        @NonNull
        public LinearLayout create(@Nullable ViewGroup parent) {
            button.setChecked(getter.get());
            button.setOnCheckedChangeListener(this);
            if (hasDefaultValue) {
                layout.setOnCreateContextMenuListener(this);
            }
            if (parent != null) {
                parent.addView(layout);
            }
            return layout;
        }

        @Override
        public void onCheckedChanged(View buttonView, boolean isChecked) {
            assert buttonView == button;
            setter.accept(isChecked);
            if (onChanged != null) {
                onChanged.run();
            }
            if (needsRestart) {
                Toast.makeText(
                        buttonView.getContext(),
                        I18n.get("gui.modernui.restart_to_work"),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            addResetToDefaultMenuItem(menu)
                    .setEnabled(button.isChecked() != defaultValue)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (item.getItemId() == ID_RESET_TO_DEFAULT) {
                button.setChecked(defaultValue);
                return true;
            }
            return false;
        }
    }

    private static LinearLayout createGuiScaleOption(Context context) {
        var layout = createRow(context, "options.guiScale");

        final int dp6 = layout.dp(Design.SPACE_2);

        var tv = new TextView(context);
        {
            tv.setTextAppearance(R.attr.textAppearanceLabelMedium);
            var value = new TypedValue();
            if (context.getTheme().resolveAttribute(R.ns, R.attr.colorError, value, true))
                tv.setTextColor(context.getResources().loadColorStateList(value, null, context.getTheme()));
            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.setMargins(dp6, 0, dp6, 0);
            layout.addView(tv, params);
        }

        {
            var spinner = new Spinner(context);

            List<GuiScaleItem> values = new ArrayList<>(MuiModApi.MAX_GUI_SCALE);
            for (int i = 0; i <= MuiModApi.MAX_GUI_SCALE; i++) {
                if (i == 1) continue;
                values.add(new GuiScaleItem(i));
            }

            spinner.setAdapter(new ArrayAdapter<>(context, values));
            int curValue = Minecraft.getInstance().options.guiScale().get();
            spinner.setSelection(curValue == 0 ? 0 : curValue - 1);
            spinner.setOnItemSelectedListener((parent, view, position, id) -> {
                int newValue = position == 0 ? 0 : position + 1;
                Core.executeOnMainThread(() -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.options.guiScale().set(newValue);
                    // ensure it's applied
                    if ((int) minecraft.getWindow().getGuiScale() !=
                            minecraft.getWindow().calculateScale(newValue, false)) {
                        minecraft.resizeGui();
                    }
                    minecraft.options.save();
                });
                tv.setText(guiScaleToHintText(newValue));
            });
            tv.setText(guiScaleToHintText(curValue));

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            layout.addView(spinner, params);
        }

        var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(dp6, 0, dp6, 0);
        layout.setMinimumHeight(layout.dp(44));
        layout.setLayoutParams(params);

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

    private static CharSequence guiScaleToHintText(int value) {
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

    @NonNull
    public static LinearLayout createInputBox(Context context, String name) {
        var layout = createRow(context, name);

        var input = new EditText(context, null, R.attr.editTextOutlinedStyle);
        input.setId(R.id.input);
        input.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(layout.dp(Design.SPACE_3), 0, 0, 0);
        layout.addView(input, params);

        return layout;
    }

    @NonNull
    private static SeekBar addSliderToLayout(@NonNull LinearLayout layout,
                                             boolean discrete) {
        var slider = new SeekBar(layout.getContext(), null, null,
                discrete ? R.style.Widget_Material3_SeekBar_Discrete_Slider : R.style.Widget_Material3_SeekBar_Slider);
        slider.setId(R.id.button2);
        slider.setUserAnimationEnabled(true);
        var params = new LinearLayout.LayoutParams(slider.dp(210), WRAP_CONTENT);
        int dp8 = slider.dp(8);
        params.setMargins(dp8, 0, dp8, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        layout.addView(slider, 1, params);
        return slider;
    }

    public static class IntegerOption implements
            View.OnFocusChangeListener,
            SeekBar.OnSeekBarChangeListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

        protected final LinearLayout layout;
        protected final EditText input;

        protected final Supplier<Integer> getter;
        protected final Consumer<Integer> setter;
        @Nullable
        protected Runnable onChanged;

        @Nullable
        protected SeekBar slider;
        protected int minValue = Integer.MIN_VALUE;
        protected int maxValue = Integer.MAX_VALUE;
        protected int stepSize = 1;

        protected boolean hasDefaultValue = false;
        protected int defaultValue;

        public IntegerOption(Context context, String name,
                             Supplier<Integer> getter,
                             Consumer<Integer> setter) {
            layout = createInputBox(context, name);
            input = layout.<EditText>requireViewById(R.id.input);
            this.getter = getter;
            this.setter = setter;
        }

        public IntegerOption(Context context, String name,
                             ConfigItem<Integer> config,
                             Consumer<Integer> setter,
                             @Nullable Runnable onChanged) {
            this(context, name, config, setter);
            var range = config.getRange();
            if (range != null) {
                minValue = range.getMinimum();
                maxValue = range.getMaximum();
            }
            hasDefaultValue = true;
            defaultValue = config.getDefault();
            this.onChanged = onChanged;
        }

        public IntegerOption(Context context, String name,
                             int stepSize,
                             ConfigItem<Integer> config,
                             Consumer<Integer> setter,
                             @Nullable Runnable onChanged) {
            this(context, name, config, setter, onChanged);
            this.stepSize = stepSize;
        }

        public IntegerOption(Context context, String name,
                             int stepSize,
                             ConfigItem<Integer> config,
                             @Nullable Runnable onChanged) {
            this(context, name, stepSize, config, config, onChanged);
        }

        public IntegerOption setRange(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            return this;
        }

        public IntegerOption setRange(int minValue, int maxValue,
                                      int stepSize) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.stepSize = stepSize;
            return this;
        }

        public IntegerOption setDefaultValue(int defaultValue) {
            this.hasDefaultValue = true;
            this.defaultValue = defaultValue;
            return this;
        }

        public IntegerOption setOnChanged(Runnable onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        @NonNull
        public LinearLayout create(@Nullable ViewGroup parent) {
            return create(parent, -1);
        }

        @NonNull
        public LinearLayout create(@Nullable ViewGroup parent, int maxLength) {
            int curValue = getter.get();
            int steps = (maxValue - minValue) / stepSize;
            if (steps <= 200) {
                slider = addSliderToLayout(layout, steps <= 10);
                slider.setMax(steps);
                slider.setProgress((curValue - minValue) / stepSize);
                slider.setOnSeekBarChangeListener(this);
            }
            InputFilter digitsFilter = DigitsInputFilter.getInstance((Locale) null);
            if (maxLength > 0) {
                input.setFilters(digitsFilter,
                        new InputFilter.LengthFilter(maxLength));
            } else {
                input.setFilters(digitsFilter);
            }
            input.setText(Integer.toString(curValue));
            input.setOnFocusChangeListener(this);
            input.setMinWidth(slider != null ? slider.dp(60) : input.dp(80));
            if (hasDefaultValue) {
                defaultValue = (defaultValue / stepSize) * stepSize;
                layout.setOnCreateContextMenuListener(this);
            }
            if (parent != null) {
                parent.addView(layout);
            }
            return layout;
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            assert view == input;
            if (!hasFocus) {
                EditText v = (EditText) view;
                int newValue;
                try {
                    newValue = MathUtil.clamp(Integer.parseInt(v.getText().toString()),
                            minValue, maxValue);
                } catch (NumberFormatException e) {
                    newValue = hasDefaultValue ? defaultValue : minValue;
                }
                replaceText(v, Integer.toString(newValue));
                if (newValue != getter.get()) {
                    setter.accept(newValue);
                    if (slider != null) {
                        int curProgress = (newValue - minValue) / stepSize;
                        slider.setProgress(curProgress, true);
                    }
                    if (onChanged != null) {
                        onChanged.run();
                    }
                }
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int newValue = seekBar.getProgress() * stepSize + minValue;
            replaceText(input, Integer.toString(newValue));
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int newValue = seekBar.getProgress() * stepSize + minValue;
            if (newValue != getter.get()) {
                setter.accept(newValue);
                replaceText(input, Integer.toString(newValue));
                if (onChanged != null) {
                    onChanged.run();
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            boolean canReset;
            if (slider != null) {
                int curValue = slider.getProgress() * stepSize + minValue;
                canReset = curValue != defaultValue;
            } else {
                try {
                    int curValue = Integer.parseInt(input.getText().toString());
                    canReset = curValue != defaultValue;
                } catch (NumberFormatException e) {
                    canReset = true;
                }
            }
            addResetToDefaultMenuItem(menu)
                    .setEnabled(canReset)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (item.getItemId() == ID_RESET_TO_DEFAULT) {
                replaceText(input, Integer.toString(defaultValue));
                setter.accept(defaultValue);
                if (slider != null) {
                    int newProgress = (defaultValue - minValue) / stepSize;
                    slider.setProgress(newProgress, true);
                }
                if (onChanged != null) {
                    onChanged.run();
                }
                return true;
            }
            return false;
        }
    }

    public static double getFirstAlphaValue(List<? extends String> colors) {
        if (colors != null && !colors.isEmpty()) {
            try {
                int color = Color.parseColor(colors.get(0));
                return (color >>> 24) / 255.0;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return 1.0;
    }

    @NonNull
    public static LinearLayout createColorOpacityOption(
            Context context, String name,
            ConfigItem<List<? extends String>> config,
            Runnable onChanged) {
        Supplier<Double> getter = () -> {
            List<? extends String> colors = config.get();
            return getFirstAlphaValue(colors);
        };
        Consumer<Double> setter = (d) -> {
            int alpha = (int) (d.floatValue() * 255.0f + 0.5f);
            var newList = new ArrayList<String>(config.get());
            if (newList.isEmpty()) {
                newList.add("#FF000000");
            }
            for (var it = newList.listIterator();
                 it.hasNext();
            ) {
                try {
                    int color = Color.parseColor(it.next());
                    color = (color & 0xFFFFFF) | (alpha << 24);
                    it.set(String.format(Locale.ROOT, "#%08X", color));
                } catch (IllegalArgumentException ignored) {
                }
            }
            config.set(newList);
        };
        double defaultValue = getFirstAlphaValue(config.getDefault());
        return new FloatOption(context, name, getter, setter)
                .setRange(0.0, 1.0, 100)
                .setDefaultValue(defaultValue)
                .setOnChanged(onChanged)
                .create(null, 4);
    }

    public static class FloatOption implements
            View.OnFocusChangeListener,
            SeekBar.OnSeekBarChangeListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

        protected final LinearLayout layout;
        protected final EditText input;

        protected final Supplier<Double> getter;
        protected final Consumer<Double> setter;
        @Nullable
        protected Runnable onChanged;

        // there is a slider when denominator is not NaN
        @Nullable
        protected SeekBar slider;
        protected double minValue = Double.NEGATIVE_INFINITY;
        protected double maxValue = Double.POSITIVE_INFINITY;
        // 10 means step=0.1, 100 means step=0.01
        protected double denominator = Double.NaN;

        protected double defaultValue = Double.NaN;

        public FloatOption(Context context, String name,
                           Supplier<Double> getter,
                           Consumer<Double> setter) {
            layout = createInputBox(context, name);
            input = layout.<EditText>requireViewById(R.id.input);
            this.getter = getter;
            this.setter = setter;
        }

        public FloatOption(Context context, String name,
                           ConfigItem<Double> config,
                           Consumer<Double> setter,
                           @Nullable Runnable onChanged) {
            this(context, name, config, setter);
            var range = config.getRange();
            if (range != null) {
                minValue = range.getMinimum();
                maxValue = range.getMaximum();
            }
            defaultValue = config.getDefault();
            this.onChanged = onChanged;
        }

        public FloatOption(Context context, String name,
                           ConfigItem<Double> config,
                           Consumer<Double> setter,
                           double denominator,
                           @Nullable Runnable onChanged) {
            this(context, name, config, setter, onChanged);
            this.denominator = denominator;
        }

        public FloatOption(Context context, String name,
                           ConfigItem<Double> config,
                           double denominator,
                           @Nullable Runnable onChanged) {
            this(context, name, config, config, denominator, onChanged);
        }

        public FloatOption setRange(double minValue, double maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            return this;
        }

        public FloatOption setRange(double minValue, double maxValue,
                             double denominator) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.denominator = denominator;
            return this;
        }

        public FloatOption setDefaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public FloatOption setOnChanged(Runnable onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        @NonNull
        public LinearLayout create(@Nullable ViewGroup parent) {
            return create(parent, -1);
        }

        @NonNull
        public LinearLayout create(@Nullable ViewGroup parent, int maxLength) {
            double curValue = getter.get();
            if (!Double.isNaN(denominator)) {
                int steps = (int) Math.round((maxValue - minValue) * denominator);
                if (steps <= 200) {
                    slider = addSliderToLayout(layout, steps <= 10);
                    slider.setMax(steps);
                    int curProgress = (int) Math.round((curValue - minValue) * denominator);
                    slider.setProgress(curProgress);
                    slider.setOnSeekBarChangeListener(this);
                }
            }
            InputFilter digitsFilter = DigitsInputFilter.getInstance(
                    null, minValue < 0, true);
            if (maxLength > 0) {
                input.setFilters(digitsFilter,
                        new InputFilter.LengthFilter(maxLength));
            } else {
                input.setFilters(digitsFilter);
            }
            input.setText(floatValueToString(curValue, denominator));
            input.setOnFocusChangeListener(this);
            input.setMinWidth(slider != null ? slider.dp(60) : input.dp(80));
            if (!Double.isNaN(defaultValue)) {
                if (!Double.isNaN(denominator)) {
                    defaultValue = Math.round(defaultValue * denominator) / denominator;
                }
                layout.setOnCreateContextMenuListener(this);
            }
            if (parent != null) {
                parent.addView(layout);
            }
            return layout;
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            assert view == input;
            if (!hasFocus) {
                EditText v = (EditText) view;
                double newValue;
                try {
                    newValue = MathUtil.clamp(Double.parseDouble(v.getText().toString()),
                            minValue, maxValue);
                } catch (NumberFormatException e) {
                    newValue = defaultValue;
                }
                if (Double.isNaN(newValue)) {
                    newValue = minValue;
                }
                replaceText(v, floatValueToString(newValue, denominator));
                if (newValue != getter.get()) {
                    setter.accept(newValue);
                    if (slider != null) {
                        int curProgress = (int) Math.round((newValue - minValue) * denominator);
                        slider.setProgress(curProgress, true);
                    }
                    if (onChanged != null) {
                        onChanged.run();
                    }
                }
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            double newValue = seekBar.getProgress() / denominator + minValue;
            replaceText(input, floatValueToString(newValue, denominator));
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            double newValue = seekBar.getProgress() / denominator + minValue;
            if (newValue != getter.get()) {
                setter.accept(newValue);
                replaceText(input, floatValueToString(newValue, denominator));
                if (onChanged != null) {
                    onChanged.run();
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            boolean canReset;
            if (slider != null) {
                double curValue = slider.getProgress() / denominator + minValue;
                canReset = curValue != defaultValue;
            } else {
                try {
                    double curValue = Double.parseDouble(input.getText().toString());
                    canReset = curValue != defaultValue;
                } catch (NumberFormatException e) {
                    canReset = true;
                }
            }
            addResetToDefaultMenuItem(menu)
                    .setEnabled(canReset)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (item.getItemId() == ID_RESET_TO_DEFAULT) {
                replaceText(input, floatValueToString(defaultValue, denominator));
                setter.accept(defaultValue);
                if (slider != null) {
                    int newProgress = (int) Math.round((defaultValue - minValue) * denominator);
                    slider.setProgress(newProgress, true);
                }
                if (onChanged != null) {
                    onChanged.run();
                }
                return true;
            }
            return false;
        }

        private static String floatValueToString(double value, double denominator) {
            if (Double.isNaN(denominator))
                return Double.toString(value);
            return Double.toString(Math.round(value * denominator) / denominator);
        }
    }

    public static class DropDownOption<E> implements
            AdapterView.OnItemSelectedListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

        protected final LinearLayout layout;
        protected final Spinner spinner;

        protected final E[] values;
        protected final ToIntFunction<E> toIndex;
        protected final Supplier<E> getter;
        protected final Consumer<E> setter;
        @Nullable
        protected Runnable onChanged;

        protected E defaultValue;

        public DropDownOption(Context context, String name,
                              E[] values,
                              ToIntFunction<E> toIndex,
                              Supplier<E> getter,
                              Consumer<E> setter) {
            var option = new LinearLayout(context);
            option.setOrientation(LinearLayout.HORIZONTAL);
            option.setHorizontalGravity(Gravity.START);

            final int dp6 = option.dp(6);
            {
                var title = new TextView(context);
                title.setText(I18n.get(name));
                title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                title.setTextSize(14);

                String tooltip = name + ".tooltip";
                if (!I18n.get(tooltip).equals(tooltip)) {
                    title.setTooltipText(I18n.get(tooltip));
                }

                var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1);
                params.gravity = Gravity.CENTER_VERTICAL;
                option.addView(title, params);
            }
            {
                var spinner = new Spinner(context);
                //spinner.setGravity(Gravity.END);
                spinner.setAdapter(new ArrayAdapter<>(context, values));

                var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                option.addView(spinner, params);
                this.spinner = spinner;
            }

            var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.setMargins(dp6, 0, dp6, 0);
            option.setMinimumHeight(option.dp(44));
            option.setLayoutParams(params);

            this.layout = option;
            this.values = values;
            this.toIndex = toIndex;
            this.getter = getter;
            this.setter = setter;
        }

        public DropDownOption(Context context, String name,
                              E[] values,
                              ConfigItem<E> config,
                              Consumer<E> setter,
                              @Nullable Runnable onChanged) {
            this(context, name, values, defaultToIndex(values), config, setter);
            defaultValue = config.getDefault();
            this.onChanged = onChanged;
        }

        public DropDownOption(Context context, String name,
                              E[] values,
                              ConfigItem<E> config,
                              @Nullable Runnable onChanged) {
            this(context, name, values, config, config, onChanged);
        }

        @NonNull
        private static <E> ToIntFunction<E> defaultToIndex(E[] values) {
            if (values.getClass().getComponentType().isEnum()) {
                return v -> ((Enum<?>) v).ordinal();
            } else {
                return v -> {
                    for (int i = 0; i < values.length; i++) {
                        if (v.equals(values[i])) {
                            return i;
                        }
                    }
                    return AdapterView.INVALID_POSITION;
                };
            }
        }

        public DropDownOption<E> setDefaultValue(E defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public DropDownOption<E> setOnChanged(Runnable onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        @NonNull
        public LinearLayout create(@Nullable ViewGroup parent) {
            spinner.setSelection(toIndex.applyAsInt(getter.get()));
            spinner.setOnItemSelectedListener(this);
            if (defaultValue != null) {
                layout.setOnCreateContextMenuListener(this);
            }
            if (parent != null) {
                parent.addView(layout);
            }
            return layout;
        }

        @Override
        public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
            E newValue = values[position];
            if (getter.get() != newValue) {
                setter.accept(newValue);
                if (onChanged != null) {
                    onChanged.run();
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            addResetToDefaultMenuItem(menu)
                    .setEnabled(!defaultValue.equals(spinner.getSelectedItem()))
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (item.getItemId() == ID_RESET_TO_DEFAULT) {
                spinner.setSelection(toIndex.applyAsInt(defaultValue));
                return true;
            }
            return false;
        }
    }

    public static class StringListOption implements
            View.OnFocusChangeListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

        protected final LinearLayout layout;
        protected final EditText input;

        protected final ConfigItem<List<? extends String>> config;

        @Nullable
        protected Runnable onChanged;

        public StringListOption(Context context, String name,
                                ConfigItem<List<? extends String>> config) {
            var option = new LinearLayout(context);
            option.setOrientation(LinearLayout.HORIZONTAL);
            option.setHorizontalGravity(Gravity.START);

            final int dp3 = option.dp(3);
            {
                var title = new TextView(context);
                title.setText(I18n.get(name));
                title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                title.setTextSize(14);
                title.setMinWidth(option.dp(60));

                String tooltip = name + ".tooltip";
                if (!I18n.get(tooltip).equals(tooltip)) {
                    title.setTooltipText(I18n.get(tooltip));
                }

                var params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 2);
                params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                option.addView(title, params);
            }
            {
                input = new EditText(context, null, R.attr.editTextOutlinedStyle);
                input.setId(R.id.input);
                input.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

                var params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 5);
                params.gravity = Gravity.CENTER_VERTICAL;
                option.addView(input, params);
            }

            var params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.setMargins(option.dp(6), dp3, option.dp(6), dp3);
            option.setLayoutParams(params);

            layout = option;
            this.config = config;
        }

        public StringListOption setOnChanged(Runnable onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        @NonNull
        public LinearLayout create(@Nullable ViewGroup parent) {
            var curValue = config.get();
            input.setText(String.join("\n", curValue));
            input.setOnFocusChangeListener(this);
            layout.setOnCreateContextMenuListener(this);
            if (parent != null) {
                parent.addView(layout);
            }
            return layout;
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            assert view == input;
            if (!hasFocus) {
                EditText v = (EditText) view;
                ArrayList<String> result = new ArrayList<>();
                for (String s : v.getText().toString().split("\n")) {
                    if (!s.isBlank()) {
                        String strip = s.strip();
                        if (!strip.isEmpty() && !result.contains(strip)) {
                            result.add(strip);
                        }
                    }
                }
                replaceText(v, String.join("\n", result));
                if (!Objects.equals(config.get(), result)) {
                    config.set(result);
                    if (onChanged != null) {
                        onChanged.run();
                    }
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            boolean canReset = !Objects.equals(config.get(), config.getDefault());
            addResetToDefaultMenuItem(menu)
                    .setEnabled(canReset)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (item.getItemId() == ID_RESET_TO_DEFAULT) {
                var defaultValue = config.getDefault();
                replaceText(input, String.join("\n", defaultValue));
                config.set(defaultValue);
                if (onChanged != null) {
                    onChanged.run();
                }
                return true;
            }
            return false;
        }
    }

    public static LinearLayout createStringListOption(Context context,
                                                      String name,
                                                      ConfigItem<List<? extends String>> config,
                                                      Runnable onChanged) {
        return new StringListOption(context, name, config)
                .setOnChanged(onChanged)
                .create(null);
    }

    private static void replaceText(@NonNull EditText editText, @NonNull CharSequence newText) {
        Editable text = editText.getText();
        text.replace(0, text.length(), newText);
    }

    static void reloadDefaultTypeface(@NonNull Context context, @Nullable Runnable onFontChanged) {
        var future = Minecraft.getInstance().submit(() -> {
            var oldTypeface = ModernUI.getSelectedTypeface();
            var client = ModernUIClient.getInstance();
            client.reloadTypeface();
            client.reloadFontStrike();
            return oldTypeface;
        });
        future.whenCompleteAsync((oldTypeface, throwable) -> {
            if (throwable == null) {
                if (onFontChanged != null) {
                    onFontChanged.run();
                }
                refreshViewTypeface(
                        UIManager.getInstance().getDecorView(),
                        oldTypeface
                );
                Toast.makeText(context,
                        I18n.get("gui.modernui.font_reloaded"),
                        Toast.LENGTH_SHORT).show();
            }
        }, Core.getUiThreadExecutor());
    }

    private static void refreshViewTypeface(@NonNull ViewGroup vg,
                                            Typeface oldTypeface) {
        int cc = vg.getChildCount();
        for (int i = 0; i < cc; i++) {
            View v = vg.getChildAt(i);
            if (v instanceof ViewGroup) {
                refreshViewTypeface((ViewGroup) v, oldTypeface);
            } else if (v instanceof TextView tv) {
                if (tv.getTypeface() == oldTypeface) {
                    tv.setTypeface(ModernUI.getSelectedTypeface());
                }
            }
        }
    }
}
