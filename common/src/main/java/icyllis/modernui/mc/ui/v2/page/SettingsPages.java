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

package icyllis.modernui.mc.ui.v2.page;

import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.ModernUIClient;
import icyllis.modernui.mc.ModernUIMod;
import icyllis.modernui.mc.ui.AccentPalette;
import icyllis.modernui.mc.ui.v2.TgTheme;
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.mc.ui.v2.widget.TgColorField;
import icyllis.modernui.mc.ui.v2.widget.TgMenu;
import icyllis.modernui.mc.ui.v2.widget.TgRow;
import icyllis.modernui.mc.ui.v2.widget.TgSlider;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

import java.util.List;
import java.util.Locale;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * Binds every player-facing config item into one {@link SettingsPage} tree.
 *
 * <p>No control is created here: each row carries a factory that the shell calls while it
 * materialises the page, so this class stays a pure description of the settings and can be
 * read without a window.</p>
 */
public final class SettingsPages {

    public static final String GROUP_APPEARANCE = "appearance";
    public static final String GROUP_SCREEN = "screen";
    public static final String GROUP_VIEW = "view";
    public static final String GROUP_FONT = "font";
    public static final String GROUP_EXTENSION = "extension";
    public static final String GROUP_TEXT = "text";

    /** Editor kinds for {@link Host#legacyBlock}. */
    public static final String LEGACY_FONT = "font";
    public static final String LEGACY_TOOLTIP = "tooltip";

    /** Bounds of the animation duration scale, which is a live static rather than a config item. */
    private static final float ANIMATION_SCALE_MIN = 0.1f;
    private static final float ANIMATION_SCALE_MAX = 10f;

    /**
     * What the page cannot build itself. The shell implements this, so the page never has to
     * reach into the screen that hosts it.
     */
    public interface Host {

        /** Marks the client config dirty and re-applies it. */
        @NonNull
        Runnable clientChanged();

        /** Marks the common config dirty and re-applies it. */
        @NonNull
        Runnable commonChanged();

        /** Marks the text config dirty and re-applies it. */
        @NonNull
        Runnable textChanged();

        /**
         * An editor the v2 widgets do not reimplement, built by the caller.
         *
         * @param which {@link #LEGACY_FONT} or {@link #LEGACY_TOOLTIP}. The font block holds the
         *              preferred family picker, the fallback list and the registration list only;
         *              the toggles around them are rows of this page.
         */
        @NonNull
        View legacyBlock(@NonNull Context context, @NonNull String which);

        /** Four-corner colour editor for a list-of-colours item, returned as a whole row. */
        @NonNull
        View colorListRow(@NonNull Context context, @NonNull String key,
                          @NonNull ConfigItem<List<? extends String>> item,
                          @NonNull Runnable onChanged);

        /** GUI scale selector, which needs the window and the options file, as a whole row. */
        @NonNull
        View guiScaleRow(@NonNull Context context);

        /** Opens the colour studio over {@code field}. */
        void openColorStudio(@NonNull TgColorField field, boolean alpha);

        /** Opens the style preview picker. */
        void showSkinPicker();
    }

    private SettingsPages() {
    }

    /** Rail anchors in display order; the shell draws them on the index rail. */
    @NonNull
    public static List<PageMeta> meta() {
        return List.of(
                new PageMeta(GROUP_APPEARANCE, "mui.v2.shell.rail.appearance", Skin.GLYPH_SUN),
                new PageMeta(GROUP_SCREEN, "mui.v2.shell.rail.screen", Skin.GLYPH_MONITOR),
                new PageMeta(GROUP_VIEW, "mui.v2.shell.rail.view", Skin.GLYPH_SCROLL),
                new PageMeta(GROUP_FONT, "mui.v2.shell.rail.font", Skin.GLYPH_TYPE),
                new PageMeta(GROUP_EXTENSION, "mui.v2.shell.rail.extension", Skin.GLYPH_PLUG),
                new PageMeta(GROUP_TEXT, "mui.v2.shell.rail.text", Skin.GLYPH_TEXT));
    }

    @NonNull
    public static SettingsPage build(@NonNull Context context, @NonNull Host host) {
        var onClient = host.clientChanged();
        var onCommon = host.commonChanged();
        var onText = host.textChanged();
        var page = new SettingsPage();
        appearance(page, host, onClient);
        screen(page, host, onClient);
        view(page, onClient, onCommon);
        font(page, host, onClient, onText);
        extension(page, host, onClient);
        text(page, onText);
        return page;
    }

    // ---------------------------------------------------------------- groups

    private static void appearance(@NonNull SettingsPage page, @NonNull Host host,
                                   @NonNull Runnable onClient) {
        page.group(GROUP_APPEARANCE, "modernui.center.category.appearance", Skin.GLYPH_SUN);
        page.section("mui.v2.section.style");

        page.row("modernui.center.appearance.themeColor",
                ctx -> themeColorRow(ctx, host, onClient));
        page.row("mui.v2.appearance.themeStyle", ctx -> styleMenu(ctx, host, onClient));
        page.row("modernui.center.appearance.compact",
                toggleRow("modernui.center.appearance.compact", Config.CLIENT.mInterfaceCompactLayout, onClient));
        page.row("modernui.center.appearance.animations",
                toggleRow("modernui.center.appearance.animations", Config.CLIENT.mInterfaceAnimations, onClient));
        page.row("mui.v2.appearance.indexRail",
                toggleRow("mui.v2.appearance.indexRail", Config.CLIENT.mInterfaceIndexRail, onClient));

        page.section("modernui.center.section.backdrop");
        page.block(ctx -> host.colorListRow(ctx, "modernui.center.screen.backgroundOpacity",
                Config.CLIENT.mBackgroundColor, onClient));
        page.row("modernui.center.screen.backgroundDuration",
                intSliderRow("modernui.center.screen.backgroundDuration",
                        Config.CLIENT.mBackgroundDuration, onClient));
        page.row("modernui.center.screen.blurEffect",
                toggleRow("modernui.center.screen.blurEffect", Config.CLIENT.mBlurEffect, onClient));
        page.row("modernui.center.screen.blurRadius",
                intSliderRow("modernui.center.screen.blurRadius", Config.CLIENT.mBlurRadius, onClient));
        page.row("modernui.center.screen.additionalBlurEffect",
                toggleRow("modernui.center.screen.additionalBlurEffect",
                        Config.CLIENT.mAdditionalBlurEffect, onClient));
        page.row("modernui.center.screen.overrideVanillaBlur",
                toggleRow("modernui.center.screen.overrideVanillaBlur",
                        Config.CLIENT.mOverrideVanillaBlur, onClient));
        page.row("mui.v2.backdrop.blurBlacklist",
                stringListRow("mui.v2.backdrop.blurBlacklist", Config.CLIENT.mBlurBlacklist, onClient));
    }

    private static void screen(@NonNull SettingsPage page, @NonNull Host host,
                               @NonNull Runnable onClient) {
        page.group(GROUP_SCREEN, "modernui.center.category.screen", Skin.GLYPH_MONITOR);

        page.section("modernui.center.section.display");
        // the host returns a finished row, so it spans the column instead of pairing with a control
        page.block(host::guiScaleRow);
        page.row("modernui.center.screen.windowMode",
                enumRow("modernui.center.screen.windowMode", Config.CLIENT.mWindowMode, onClient));
        page.row("modernui.center.screen.framerateInactive",
                intSliderRow("modernui.center.screen.framerateInactive",
                        Config.CLIENT.mFramerateInactive, onClient));

        page.section("modernui.center.section.audio");
        page.row("modernui.center.screen.masterVolumeInactive",
                doubleSliderRow("modernui.center.screen.masterVolumeInactive",
                        Config.CLIENT.mMasterVolumeInactive, onClient));
        page.row("modernui.center.screen.masterVolumeMinimized",
                doubleSliderRow("modernui.center.screen.masterVolumeMinimized",
                        Config.CLIENT.mMasterVolumeMinimized, onClient));
        page.row("modernui.center.screen.globalVolumeControl",
                toggleRow("modernui.center.screen.globalVolumeControl",
                        Config.CLIENT.mGlobalVolumeControl, onClient));

        page.section("modernui.center.section.behaviour");
        page.row("modernui.center.screen.inventoryPause",
                toggleRow("modernui.center.screen.inventoryPause", Config.CLIENT.mInventoryPause, onClient));
    }

    private static void view(@NonNull SettingsPage page, @NonNull Runnable onClient,
                             @NonNull Runnable onCommon) {
        page.group(GROUP_VIEW, "modernui.center.category.view", Skin.GLYPH_SCROLL);

        page.section("modernui.center.section.scrolling");
        page.row("modernui.center.view.scrollbarSize",
                intSliderRow("modernui.center.view.scrollbarSize", Config.CLIENT.mScrollbarSize, onClient));
        page.row("modernui.center.view.touchSlop",
                intSliderRow("modernui.center.view.touchSlop", Config.CLIENT.mTouchSlop, onClient));
        page.row("modernui.center.view.hoverSlop",
                intSliderRow("modernui.center.view.hoverSlop", Config.CLIENT.mHoverSlop, onClient));
        page.row("modernui.center.view.minScrollbarTouchTarget",
                intSliderRow("modernui.center.view.minScrollbarTouchTarget",
                        Config.CLIENT.mMinScrollbarTouchTarget, onClient));
        page.row("modernui.center.view.minimumFlingVelocity",
                intSliderRow("modernui.center.view.minimumFlingVelocity",
                        Config.CLIENT.mMinimumFlingVelocity, onClient));
        page.row("modernui.center.view.maximumFlingVelocity",
                intSliderRow("modernui.center.view.maximumFlingVelocity",
                        Config.CLIENT.mMaximumFlingVelocity, onClient));
        page.row("modernui.center.view.scrollFriction",
                doubleSliderRow("modernui.center.view.scrollFriction",
                        Config.CLIENT.mScrollFriction, onClient));
        page.row("modernui.center.view.overscrollDistance",
                intSliderRow("modernui.center.view.overscrollDistance",
                        Config.CLIENT.mOverscrollDistance, onClient));
        page.row("modernui.center.view.overflingDistance",
                intSliderRow("modernui.center.view.overflingDistance",
                        Config.CLIENT.mOverflingDistance, onClient));
        page.row("modernui.center.view.horizontalScrollFactor",
                doubleSliderRow("modernui.center.view.horizontalScrollFactor",
                        Config.CLIENT.mHorizontalScrollFactor, onClient));
        page.row("modernui.center.view.verticalScrollFactor",
                doubleSliderRow("modernui.center.view.verticalScrollFactor",
                        Config.CLIENT.mVerticalScrollFactor, onClient));

        page.section("modernui.center.section.tooltipTiming");
        page.row("modernui.center.view.hoverTooltipShowTimeout",
                intSliderRow("modernui.center.view.hoverTooltipShowTimeout",
                        Config.CLIENT.mHoverTooltipShowTimeout, onClient));
        page.row("modernui.center.view.hoverTooltipHideTimeout",
                intSliderRow("modernui.center.view.hoverTooltipHideTimeout",
                        Config.CLIENT.mHoverTooltipHideTimeout, onClient));

        page.section("modernui.center.section.layout");
        page.row("modernui.center.system.theme",
                stringListRow("modernui.center.system.theme", Config.CLIENT.mTheme, onClient));
        page.row("modernui.center.system.forceRtlLayout",
                toggleRow("modernui.center.system.forceRtlLayout", Config.CLIENT.mForceRtl, onClient));
        page.row("modernui.center.system.globalFontScale",
                doubleSliderRow("modernui.center.system.globalFontScale", Config.CLIENT.mFontScale, onClient));
        page.row("modernui.center.system.globalAnimationScale",
                ctx -> animationScaleRow(ctx, "modernui.center.system.globalAnimationScale", onClient));
        page.row("modernui.center.system.developerMode",
                toggleRow("modernui.center.system.developerMode", Config.COMMON.developerMode, onCommon));
    }

    private static void font(@NonNull SettingsPage page, @NonNull Host host,
                             @NonNull Runnable onClient, @NonNull Runnable onText) {
        page.group(GROUP_FONT, "modernui.center.category.font", Skin.GLYPH_TYPE);

        page.section("mui.v2.section.fontFamily");
        page.block(ctx -> host.legacyBlock(ctx, LEGACY_FONT));

        page.section("mui.v2.section.fontRendering");
        page.row("modernui.center.font.colorEmoji",
                toggleRow("modernui.center.font.colorEmoji", Config.CLIENT.mUseColorEmoji, onClient));
        // the interface strike and the text engine strike are separate items with separate effects
        page.row("mui.v2.font.linearMetricsUi",
                toggleRow("mui.v2.font.linearMetricsUi", Config.CLIENT.mLinearMetrics, onClient));
        page.row("modernui.center.font.antiAliasing",
                toggleRow("modernui.center.font.antiAliasing", Config.TEXT.mAntiAliasing, onText));
        page.row("modernui.center.font.linearMetrics",
                toggleRow("modernui.center.font.linearMetrics", Config.TEXT.mLinearMetrics, onText));
    }

    private static void extension(@NonNull SettingsPage page, @NonNull Host host,
                                  @NonNull Runnable onClient) {
        page.group(GROUP_EXTENSION, "modernui.center.category.extension", Skin.GLYPH_PLUG);

        page.section("modernui.center.section.sound");
        page.row("modernui.center.extension.ding",
                toggleRow("modernui.center.extension.ding", Config.CLIENT.mDing, onClient));
        page.row("mui.v2.extension.dingSound",
                textRow("mui.v2.extension.dingSound", Config.CLIENT.mDingSound, onClient));
        page.row("mui.v2.extension.dingVolume",
                doubleSliderRow("mui.v2.extension.dingVolume", Config.CLIENT.mDingVolume, onClient));

        page.section("modernui.center.section.interaction");
        // zoom is absent on platforms that do not bind a key for it
        var zoom = Config.CLIENT.mZoom;
        if (zoom != null) {
            page.row("key.modernui.zoom", toggleRow("key.modernui.zoom", zoom, onClient));
        }
        page.row("modernui.center.text.emojiShortcodes",
                toggleRow("modernui.center.text.emojiShortcodes", Config.CLIENT.mEmojiShortcodes, onClient));
        page.row("modernui.center.extension.smoothScrolling",
                bootstrapToggle("modernui.center.extension.smoothScrolling",
                        ModernUIMod.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING));
        page.row("modernui.center.text.enhancedTextField",
                bootstrapToggle("modernui.center.text.enhancedTextField",
                        ModernUIMod.BOOTSTRAP_DISABLE_ENHANCED_TEXT_FIELD));
        page.block(SettingsPages::restartNote);

        page.section("modernui.center.category.tooltip");
        page.row("modernui.center.extension.tooltip",
                toggleRow("modernui.center.extension.tooltip", Config.CLIENT.mTooltip, onClient));
        page.row("modernui.center.tooltip.centerTitle",
                toggleRow("modernui.center.tooltip.centerTitle", Config.CLIENT.mCenterTooltipTitle, onClient));
        page.row("modernui.center.tooltip.titleBreak",
                toggleRow("modernui.center.tooltip.titleBreak", Config.CLIENT.mTooltipTitleBreak, onClient));
        page.row("modernui.center.tooltip.exactPositioning",
                toggleRow("modernui.center.tooltip.exactPositioning",
                        Config.CLIENT.mExactTooltipPositioning, onClient));
        page.row("modernui.center.tooltip.arrowScrollFactor",
                intSliderRow("modernui.center.tooltip.arrowScrollFactor",
                        Config.CLIENT.mTooltipArrowScrollFactor, onClient));
        page.block(ctx -> host.colorListRow(ctx, "modernui.center.tooltip.backgroundOpacity",
                Config.CLIENT.mTooltipFill, onClient));
        // line wrapping only exists on platforms whose tooltip renderer can wrap
        var lineWrapping = Config.CLIENT.mTooltipLineWrapping;
        if (lineWrapping != null) {
            page.row("mui.v2.tooltip.lineWrapping",
                    toggleRow("mui.v2.tooltip.lineWrapping", lineWrapping, onClient));
        }
        page.block(ctx -> host.legacyBlock(ctx, LEGACY_TOOLTIP));
    }

    private static void text(@NonNull SettingsPage page, @NonNull Runnable onText) {
        page.group(GROUP_TEXT, "modernui.center.category.text", Skin.GLYPH_TEXT);

        page.section("mui.v2.section.textEngine");
        page.row("modernui.center.text.textEngine",
                bootstrapToggle("modernui.center.text.textEngine",
                        ModernUIMod.BOOTSTRAP_DISABLE_TEXT_ENGINE));
        page.block(SettingsPages::restartNote);

        page.section("modernui.center.category.textLayout");
        page.row("modernui.center.text.defaultFontBehavior",
                enumRow("modernui.center.text.defaultFontBehavior",
                        Config.TEXT.mDefaultFontBehavior, onText));
        page.row("modernui.center.text.defaultFontRuleSet",
                stringListRow("modernui.center.text.defaultFontRuleSet",
                        Config.TEXT.mDefaultFontRuleSet, onText));
        page.row("modernui.center.text.bidiHeuristicAlgo",
                enumRow("modernui.center.text.bidiHeuristicAlgo", Config.TEXT.mTextDirection, onText));
        page.row("modernui.center.text.lineBreakStyle",
                enumRow("modernui.center.text.lineBreakStyle", Config.TEXT.mLineBreakStyle, onText));
        page.row("modernui.center.text.lineBreakWordStyle",
                enumRow("modernui.center.text.lineBreakWordStyle", Config.TEXT.mLineBreakWordStyle, onText));
        page.row("modernui.center.text.allowAsyncLayout",
                toggleRow("modernui.center.text.allowAsyncLayout", Config.TEXT.mAllowAsyncLayout, onText));
        page.row("modernui.center.text.useComponentCache",
                toggleRow("modernui.center.text.useComponentCache", Config.TEXT.mUseComponentCache, onText));
        page.row("modernui.center.text.fixedResolution",
                toggleRow("modernui.center.text.fixedResolution", Config.TEXT.mFixedResolution, onText));
        page.row("modernui.center.text.baseFontSize",
                doubleSliderRow("modernui.center.text.baseFontSize", Config.TEXT.mBaseFontSize, onText));
        page.row("modernui.center.text.cacheLifespan",
                intSliderRow("modernui.center.text.cacheLifespan", Config.TEXT.mCacheLifespan, onText));

        page.section("modernui.center.category.textRendering");
        page.row("modernui.center.text.textShadersInWorld",
                toggleRow("modernui.center.text.textShadersInWorld",
                        Config.TEXT.mUseTextShadersInWorld, onText));
        page.row("modernui.center.text.allowSDFTextIn2D",
                toggleRow("modernui.center.text.allowSDFTextIn2D", Config.TEXT.mAllowSDFTextIn2D, onText));
        page.row("modernui.center.text.computeDeviceFontSize",
                toggleRow("modernui.center.text.computeDeviceFontSize",
                        Config.TEXT.mComputeDeviceFontSize, onText));
        page.row("modernui.center.text.minPixelDensityForSDF",
                intSliderRow("modernui.center.text.minPixelDensityForSDF",
                        Config.TEXT.mMinPixelDensityForSDF, onText));
        page.row("modernui.center.font.linearSampling",
                toggleRow("modernui.center.font.linearSampling", Config.TEXT.mLinearSamplingA8Atlas, onText));
        page.row("modernui.center.text.allowShadow",
                toggleRow("modernui.center.text.allowShadow", Config.TEXT.mAllowShadow, onText));
        page.row("modernui.center.text.shadowOffset",
                doubleSliderRow("modernui.center.text.shadowOffset", Config.TEXT.mShadowOffset, onText));
        page.row("modernui.center.text.baselineShift",
                doubleSliderRow("modernui.center.text.baselineShift", Config.TEXT.mBaselineShift, onText));
        page.row("modernui.center.text.outlineOffset",
                doubleSliderRow("modernui.center.text.outlineOffset", Config.TEXT.mOutlineOffset, onText));
        page.row("modernui.center.text.bitmapOffset",
                doubleSliderRow("modernui.center.text.bitmapOffset", Config.TEXT.mBitmapOffset, onText));
    }

    // ------------------------------------------------------------- factories

    @NonNull
    private static SettingsPage.RowFactory toggleRow(@NonNull String key,
                                                     @NonNull ConfigItem<Boolean> item,
                                                     @NonNull Runnable onChanged) {
        return ctx -> Rows.toggle(ctx, key, item, onChanged);
    }

    @NonNull
    private static SettingsPage.RowFactory intSliderRow(@NonNull String key,
                                                        @NonNull ConfigItem<Integer> item,
                                                        @NonNull Runnable onChanged) {
        return ctx -> Rows.intSlider(ctx, key, item, onChanged);
    }

    @NonNull
    private static SettingsPage.RowFactory doubleSliderRow(@NonNull String key,
                                                           @NonNull ConfigItem<Double> item,
                                                           @NonNull Runnable onChanged) {
        return ctx -> Rows.doubleSlider(ctx, key, item, onChanged);
    }

    @NonNull
    private static SettingsPage.RowFactory stringListRow(@NonNull String key,
                                                         @NonNull ConfigItem<List<? extends String>> item,
                                                         @NonNull Runnable onChanged) {
        return ctx -> Rows.stringList(ctx, key, item, onChanged);
    }

    @NonNull
    private static SettingsPage.RowFactory textRow(@NonNull String key,
                                                   @NonNull ConfigItem<String> item,
                                                   @NonNull Runnable onChanged) {
        return ctx -> Rows.text(ctx, key, item, onChanged);
    }

    @NonNull
    private static <E extends Enum<E>> SettingsPage.RowFactory enumRow(@NonNull String key,
                                                                      @NonNull ConfigItem<E> item,
                                                                      @NonNull Runnable onChanged) {
        return ctx -> Rows.enumMenu(ctx, key, item, onChanged);
    }

    /**
     * Toggle for an option that lives in the bootstrap properties rather than in the config.
     * The stored property is the negated "disable" flag and only takes effect after a restart.
     */
    @NonNull
    private static SettingsPage.RowFactory bootstrapToggle(@NonNull String key,
                                                           @NonNull String property) {
        return ctx -> Rows.toggle(ctx, key,
                () -> !Boolean.parseBoolean(ModernUIClient.getBootstrapProperty(property)),
                value -> ModernUIClient.setBootstrapProperty(property, Boolean.toString(!value)));
    }

    /** Reminder that the option above is read before the client exists. */
    @NonNull
    private static View restartNote(@NonNull Context context) {
        return Rows.note(context, Rows.label("gui.modernui.restart_to_work"));
    }

    /**
     * Accent colour with a way back to the theme colour: the stored value is empty when the
     * player follows the theme, and a colour field cannot express "empty" on its own.
     */
    @NonNull
    private static View themeColorRow(@NonNull Context context, @NonNull Host host,
                                      @NonNull Runnable onChanged) {
        String key = "modernui.center.appearance.themeColor";
        var field = Rows.colorField(context, currentThemeColor(context), false);
        field.setOnColorPicked(argb -> {
            // the seed also refreshes the bootstrap copy the theme resolves from
            AccentPalette.setSeed(argb);
            // every skin derives its accent from the palette, so the cached one is stale now
            SkinRegistry.invalidate();
            onChanged.run();
        });
        field.setOnOpen(() -> host.openColorStudio(field, false));

        var follow = Rows.button(context, Rows.label("mui.v2.color.followTheme"), () -> {
            AccentPalette.setSeed(0);
            field.setColor(TgTheme.of(context).accent);
            SkinRegistry.invalidate();
            onChanged.run();
        });

        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);
        holder.addView(field, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(Math.round(context.getResources().getDisplayMetrics().density * 8f), 0, 0, 0);
        holder.addView(follow, params);
        return new TgRow(context, key, holder);
    }

    /** The eight styles, read from the registry so a ninth style needs no change here. */
    @NonNull
    private static View styleMenu(@NonNull Context context, @NonNull Host host,
                                  @NonNull Runnable onChanged) {
        String key = "mui.v2.appearance.themeStyle";
        var ids = SkinRegistry.IDS;
        var labels = new CharSequence[ids.size()];
        String current = currentStyleId();
        int selected = 0;
        for (int i = 0; i < ids.size(); i++) {
            labels[i] = Rows.label("mui.v2.style." + ids.get(i));
            if (ids.get(i).equals(current)) {
                selected = i;
            }
        }
        var menu = new TgMenu(context, labels);
        menu.setSelected(selected, false);
        menu.setOnChosen(index -> {
            String id = ids.get(index);
            var item = Config.CLIENT.mInterfaceThemeStyle;
            if (item != null) {
                item.set(id);
            }
            SkinRegistry.setActive(id);
            onChanged.run();
        });

        // the menu picks a style by name; the button opens the visual previews
        var preview = Rows.button(context, Rows.label("mui.v2.shell.style"), host::showSkinPicker);
        preview.setGlyph(Skin.GLYPH_PALETTE);

        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);
        holder.addView(menu, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(Math.round(context.getResources().getDisplayMetrics().density * 8f), 0, 0, 0);
        holder.addView(preview, params);
        return new TgRow(context, key, holder);
    }

    /**
     * Animation duration scale: a live static rather than a config item, so the slider carries
     * its own bounds and writes the field directly.
     */
    @NonNull
    private static View animationScaleRow(@NonNull Context context, @NonNull String key,
                                          @NonNull Runnable onChanged) {
        var slider = new TgSlider(context);
        slider.setRange(ANIMATION_SCALE_MIN, ANIMATION_SCALE_MAX, 99);
        slider.setValue(ValueAnimator.sDurationScale);
        var value = Rows.chip(context, formatScale(ValueAnimator.sDurationScale));
        slider.setOnValueChanged((v, finished) -> {
            float rounded = Math.round(v * 10f) / 10f;
            value.setText(formatScale(rounded));
            if (finished) {
                ValueAnimator.sDurationScale = rounded;
                onChanged.run();
            }
        });
        return sliderRow(context, key, slider, value);
    }

    /** Slider plus its readout chip, laid out the way the shared rows are. */
    @NonNull
    private static View sliderRow(@NonNull Context context, @NonNull String key,
                                  @NonNull TgSlider slider, @NonNull TextView value) {
        var holder = new LinearLayout(context);
        holder.setOrientation(LinearLayout.HORIZONTAL);
        holder.setGravity(Gravity.CENTER_VERTICAL);
        holder.addView(slider, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        var params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(Math.round(context.getResources().getDisplayMetrics().density * 8f), 0, 0, 0);
        holder.addView(value, params);
        return new TgRow(context, key, holder);
    }

    @NonNull
    private static String formatScale(float scale) {
        return String.format(Locale.ROOT, "%.1f", scale);
    }

    /** Active style id, falling back to the registry when the config holds something unknown. */
    @NonNull
    private static String currentStyleId() {
        var item = Config.CLIENT.mInterfaceThemeStyle;
        if (item != null) {
            try {
                String id = item.get();
                if (SkinRegistry.isValid(id)) {
                    return id;
                }
            } catch (RuntimeException ignored) {
                // an unloaded config map leaves the item without a value
            }
        }
        return SkinRegistry.activeId();
    }

    /**
     * The stored accent, or the accent the theme resolves to while the player follows it: the
     * colour studio needs a concrete colour even when the config stores nothing.
     */
    private static int currentThemeColor(@NonNull Context context) {
        var item = Config.CLIENT.mInterfaceThemeColor;
        if (item != null) {
            try {
                String text = item.get();
                if (text != null && !text.isBlank()) {
                    return Color.parseColor(text.trim());
                }
            } catch (RuntimeException ignored) {
                // a malformed value falls through to the theme accent
            }
        }
        return TgTheme.of(context).accent;
    }
}
