/*
 * Modern UI Lite.
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

package icyllis.modernui.mc.ui.lite;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.graphics.text.FontFamily;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.ModernUIClient;
import icyllis.modernui.mc.ModernUIMod;
import icyllis.modernui.mc.MuiPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.awt.Color;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The lite edition has no settings screen of its own: every option is rendered by
 * YetAnotherConfigLib while reading and writing the existing config items, so both
 * editions share one config file and one set of defaults.
 */
public final class YaclSettings {

    /** Corner colours of the tooltip border, in the order the full edition lays them out. */
    private static final String[][] PRESET_COLORS = {
            {"#FFAADCF0", "#FFFFC3F7", "#FFBFF2B2", "#FFD27F3D"},
            {"#FFAADCF0", "#FFDAD0F4", "#FFFFC3F7", "#FFDAD0F4"},
            {"#FF28007F", "#FF28007F", "#FF14003F", "#FF14003F"},
            {"#FFE0E0E0", "#FFB0B0B0", "#FFFFFFFF", "#FFB0B0B0"}
    };

    private static String sFontFamilyAtOpen;

    private YaclSettings() {
    }

    /**
     * A failing screen must still open: an empty page or a dead button hides the cause,
     * and the reason ends up somewhere the player does not look.
     */
    public static Screen create(Screen parent) {
        try {
            return build(parent);
        } catch (Throwable t) {
            ModernUIMod.LOGGER.error("Failed to build the settings screen", t);
            return YetAnotherConfigLib.createBuilder()
                    .title(label("mui.lite.title"))
                    .category(ConfigCategory.createBuilder()
                            .name(label("mui.lite.category.about"))
                            .option(LabelOption.create(Component.literal(String.valueOf(t))))
                            .build())
                    .build().generateScreen(parent);
        }
    }

    private static Screen build(Screen parent) {
        var config = YetAnotherConfigLib.createBuilder()
                .title(label("mui.lite.title"))
                .save(YaclSettings::save);

        sFontFamilyAtOpen = Config.CLIENT.mFirstFontFamily.get();

        addBackground(config);
        addTooltip(config);
        addTooltipBorder(config);
        addInterface(config);
        addScrolling(config);
        addFont(config);
        addText(config);
        addAdvanced(config);
        addAbout(config);

        return config.build().generateScreen(parent);
    }

    // ---- categories --------------------------------------------------------

    private static void addBackground(YetAnotherConfigLib.Builder config) {
        var client = Config.CLIENT;
        category(config, "mui.lite.category.background",
                Arrays.asList(
                        alphaOf("modernui.center.screen.backgroundOpacity", client.mBackgroundColor),
                        tick("modernui.center.screen.blurEffect", client.mBlurEffect),
                        tick("modernui.center.screen.additionalBlurEffect", client.mAdditionalBlurEffect),
                        tick("modernui.center.screen.overrideVanillaBlur", client.mOverrideVanillaBlur),
                        slider("modernui.center.screen.backgroundDuration", client.mBackgroundDuration),
                        slider("modernui.center.screen.blurRadius", client.mBlurRadius),
                        enumChoice("modernui.center.screen.windowMode", client.mWindowMode),
                        slider("modernui.center.screen.framerateInactive", client.mFramerateInactive),
                        decimal("modernui.center.screen.masterVolumeInactive", client.mMasterVolumeInactive),
                        decimal("modernui.center.screen.masterVolumeMinimized", client.mMasterVolumeMinimized),
                        tick("modernui.center.screen.globalVolumeControl", client.mGlobalVolumeControl),
                        tick("modernui.center.screen.inventoryPause", client.mInventoryPause)),
                Arrays.asList(
                        stringList("modernui.center.screen.blurBlacklist", client.mBlurBlacklist)));
    }

    private static void addTooltip(YetAnotherConfigLib.Builder config) {
        var client = Config.CLIENT;
        category(config, "mui.lite.category.tooltip",
                Arrays.asList(
                        tick("modernui.center.extension.tooltip", client.mTooltip),
                        tick("modernui.center.tooltip.centerTitle", client.mCenterTooltipTitle),
                        tick("modernui.center.tooltip.titleBreak", client.mTooltipTitleBreak),
                        tick("modernui.center.tooltip.exactPositioning", client.mExactTooltipPositioning),
                        tick("mui.lite.tooltip.lineWrapping", client.mTooltipLineWrapping),
                        alphaOf("modernui.center.tooltip.backgroundOpacity", client.mTooltipFill),
                        slider("modernui.center.tooltip.arrowScrollFactor", client.mTooltipArrowScrollFactor)),
                Arrays.asList());
    }

    /**
     * The border style block the full edition hides behind an accordion. The four corner
     * colours are edited with the config library's own colour picker, one per corner.
     */
    private static void addTooltipBorder(YetAnotherConfigLib.Builder config) {
        var client = Config.CLIENT;
        category(config, "modernui.center.tooltip.borderStyle",
                Arrays.asList(
                        tick("modernui.center.tooltip.roundedShapes", client.mRoundedTooltip),
                        decimal("modernui.center.tooltip.borderWidth", client.mTooltipWidth),
                        decimal("modernui.center.tooltip.cornerRadius", client.mTooltipRadius),
                        decimal("modernui.center.tooltip.shadowRadius", client.mTooltipShadowRadius),
                        decimal("modernui.center.tooltip.shadowOpacity", client.mTooltipShadowAlpha),
                        tick("modernui.center.tooltip.adaptiveColors", client.mAdaptiveTooltipColors),
                        slider("modernui.center.tooltip.borderCycle", client.mTooltipCycle)),
                Arrays.asList(
                        presets(client.mTooltipStroke)),
                Arrays.asList(
                        cornerColor(0, client.mTooltipStroke),
                        cornerColor(1, client.mTooltipStroke),
                        cornerColor(2, client.mTooltipStroke),
                        cornerColor(3, client.mTooltipStroke)));
    }

    private static void addInterface(YetAnotherConfigLib.Builder config) {
        var client = Config.CLIENT;
        category(config, "mui.lite.category.interface",
                Arrays.asList(
                        tick("modernui.center.system.forceRtlLayout", client.mForceRtl),
                        decimal("modernui.center.system.globalFontScale", client.mFontScale),
                        customDecimal("modernui.center.system.globalAnimationScale",
                                () -> (double) ValueAnimator.sDurationScale,
                                scale -> ValueAnimator.sDurationScale = scale.floatValue(),
                                0.1, 10.0, 1.0),
                        tick("modernui.center.system.developerMode", Config.COMMON.developerMode),
                        tick("mui.lite.removeTelemetry", client.mRemoveTelemetry),
                        tick("mui.lite.useNewGuiScale", client.mUseNewGuiScale),
                        tick("modernui.center.extension.ding", client.mDing),
                        field("mui.lite.dingSound", client.mDingSound),
                        decimal("mui.lite.dingVolume", client.mDingVolume)),
                Arrays.asList(
                        stringList("modernui.center.system.theme", client.mTheme)));
    }

    private static void addScrolling(YetAnotherConfigLib.Builder config) {
        var client = Config.CLIENT;
        category(config, "mui.lite.category.scrolling",
                Arrays.asList(
                        slider("modernui.center.view.scrollbarSize", client.mScrollbarSize),
                        slider("modernui.center.view.touchSlop", client.mTouchSlop),
                        slider("modernui.center.view.hoverSlop", client.mHoverSlop),
                        slider("modernui.center.view.minScrollbarTouchTarget", client.mMinScrollbarTouchTarget),
                        slider("modernui.center.view.minimumFlingVelocity", client.mMinimumFlingVelocity),
                        slider("modernui.center.view.maximumFlingVelocity", client.mMaximumFlingVelocity),
                        decimal("modernui.center.view.scrollFriction", client.mScrollFriction),
                        slider("modernui.center.view.overscrollDistance", client.mOverscrollDistance),
                        slider("modernui.center.view.overflingDistance", client.mOverflingDistance),
                        decimal("modernui.center.view.verticalScrollFactor", client.mVerticalScrollFactor),
                        decimal("modernui.center.view.horizontalScrollFactor", client.mHorizontalScrollFactor),
                        slider("modernui.center.view.hoverTooltipShowTimeout", client.mHoverTooltipShowTimeout),
                        slider("modernui.center.view.hoverTooltipHideTimeout", client.mHoverTooltipHideTimeout)),
                Arrays.asList());
    }

    private static void addFont(YetAnotherConfigLib.Builder config) {
        var client = Config.CLIENT;
        category(config, "mui.lite.category.font",
                Arrays.asList(
                        fontChoice("modernui.center.font.firstFont", client.mFirstFontFamily),
                        tick("modernui.center.font.colorEmoji", client.mUseColorEmoji),
                        tick("modernui.center.font.linearMetrics", client.mLinearMetrics),
                        tick("modernui.center.text.emojiShortcodes", client.mEmojiShortcodes),
                        ButtonOption.createBuilder()
                                .name(label("mui.lite.font.auto"))
                                .text(label("mui.lite.apply"))
                                .action((screen, button) -> {
                                    client.mFirstFontFamily.set("");
                                    save();
                                    Minecraft.getInstance().setScreenAndShow(create(screen));
                                })
                                .build()),
                Arrays.asList(
                        stringList("modernui.center.font.fallbackFonts", client.mFallbackFontFamilyList),
                        stringList("modernui.center.font.fontRegistrationList", client.mFontRegistrationList)));
    }

    private static void addText(YetAnotherConfigLib.Builder config) {
        var text = Config.TEXT;
        category(config, "mui.lite.category.text",
                Arrays.asList(
                        enumChoice("modernui.center.text.bidiHeuristicAlgo", text.mTextDirection),
                        enumChoice("modernui.center.text.defaultFontBehavior", text.mDefaultFontBehavior),
                        enumChoice("modernui.center.text.lineBreakStyle", text.mLineBreakStyle),
                        enumChoice("modernui.center.text.lineBreakWordStyle", text.mLineBreakWordStyle),
                        tick("modernui.center.text.allowAsyncLayout", text.mAllowAsyncLayout),
                        tick("modernui.center.text.useComponentCache", text.mUseComponentCache),
                        tick("modernui.center.text.fixedResolution", text.mFixedResolution),
                        decimal("modernui.center.text.baseFontSize", text.mBaseFontSize),
                        slider("modernui.center.text.cacheLifespan", text.mCacheLifespan),
                        tick("modernui.center.font.antiAliasing", text.mAntiAliasing),
                        tick("modernui.center.font.linearMetrics", text.mLinearMetrics),
                        tick("modernui.center.text.textShadersInWorld", text.mUseTextShadersInWorld),
                        tick("modernui.center.text.allowSDFTextIn2D", text.mAllowSDFTextIn2D),
                        tick("modernui.center.text.computeDeviceFontSize", text.mComputeDeviceFontSize),
                        slider("modernui.center.text.minPixelDensityForSDF", text.mMinPixelDensityForSDF),
                        tick("modernui.center.font.linearSampling", text.mLinearSamplingA8Atlas),
                        tick("modernui.center.text.allowShadow", text.mAllowShadow),
                        tick("mui.lite.tweakExperienceText", text.mTweakExperienceText),
                        decimal("modernui.center.text.shadowOffset", text.mShadowOffset),
                        decimal("modernui.center.text.baselineShift", text.mBaselineShift),
                        decimal("modernui.center.text.outlineOffset", text.mOutlineOffset),
                        decimal("modernui.center.text.bitmapOffset", text.mBitmapOffset)),
                Arrays.asList(
                        stringList("modernui.center.text.defaultFontRuleSet", text.mDefaultFontRuleSet)));
    }

    private static void addAdvanced(YetAnotherConfigLib.Builder config) {
        category(config, "mui.lite.category.advanced",
                Arrays.asList(
                        bootstrapTick("modernui.center.text.textEngine",
                                ModernUIMod.BOOTSTRAP_DISABLE_TEXT_ENGINE, true),
                        tick("key.modernui.zoom", Config.CLIENT.mZoom),
                        bootstrapTick("modernui.center.extension.smoothScrolling",
                                ModernUIMod.BOOTSTRAP_DISABLE_SMOOTH_SCROLLING, true),
                        bootstrapTick("modernui.center.text.enhancedTextField",
                                ModernUIMod.BOOTSTRAP_DISABLE_ENHANCED_TEXT_FIELD, true),
                        ButtonOption.createBuilder()
                                .name(label("mui.lite.openFolder"))
                                .text(label("mui.lite.openFolder.text"))
                                .action((screen, button) -> openConfigFolder())
                                .build()),
                Arrays.asList());
    }

    private static void addAbout(YetAnotherConfigLib.Builder config) {
        var links = OptionGroup.createBuilder()
                .name(label("mui.lite.group.links"))
                .option(link("mui.lite.link.source",
                        "https://github.com/Huziyang520/ModernUI-Unofficial-Port"))
                .option(link("mui.lite.link.releases",
                        "https://www.curseforge.com/minecraft/mc-mods/modernui-unofficial-port"))
                .option(link("mui.lite.link.preReleases",
                        "https://github.com/Huziyang520/ModernUI-Unofficial-Port/releases"))
                .option(link("mui.lite.link.mcmod", "https://www.mcmod.cn/class/31032.html"))
                .build();
        var original = OptionGroup.createBuilder()
                .name(label("mui.lite.group.original"))
                .option(link("mui.lite.link.sourceOriginal", "https://github.com/BloCamLimb/ModernUI"))
                .option(link("mui.lite.link.sourceOriginalMc",
                        "https://github.com/BloCamLimb/ModernUI-MC"))
                .option(link("mui.lite.link.releasesOriginal",
                        "https://www.curseforge.com/minecraft/mc-mods/modern-ui"))
                .option(link("mui.lite.link.modrinthOriginal", "https://modrinth.com/mod/modern-ui"))
                .option(link("mui.lite.link.discord", "https://discord.gg/kmyGKt2"))
                .build();
        config.category(ConfigCategory.createBuilder()
                .name(label("mui.lite.category.about"))
                .group(links)
                .group(original)
                .group(OptionGroup.createBuilder()
                        .name(label("mui.lite.group.changelog"))
                        .option(LabelOption.create(label("mui.lite.changelog.version")))
                        .option(LabelOption.create(label("mui.lite.changelog.fix")))
                        .option(LabelOption.create(label("mui.lite.changelog.preview")))
                        .build())
                .build());
    }

    // ---- category helper ---------------------------------------------------

    private static void category(YetAnotherConfigLib.Builder config, String name,
                                 List<Option<?>> options, List<OptionGroup> groups) {
        category(config, name, options, groups, Arrays.asList());
    }

    /**
     * Drops options that failed to build, keeps at least one row, and skips the whole
     * category when nothing is left: the library rejects empty categories outright.
     */
    private static void category(YetAnotherConfigLib.Builder config, String name,
                                 List<Option<?>> options, List<OptionGroup> groups,
                                 List<Option<?>> extra) {
        var builder = ConfigCategory.createBuilder().name(label(name));
        int count = 0;
        var all = new ArrayList<Option<?>>(options);
        all.addAll(extra);
        for (var option : all) {
            if (option != null) {
                builder.option(option);
                count++;
            }
        }
        for (var group : groups) {
            if (group != null) {
                builder.group(group);
                count++;
            }
        }
        if (count == 0) {
            ModernUIMod.LOGGER.warn("Skipping the empty settings category '{}'", name);
            return;
        }
        try {
            config.category(builder.build());
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the settings category '{}': {}", name, t.toString());
        }
    }

    // ---- option factories --------------------------------------------------

    private static Option<Boolean> tick(String key, @Nullable ConfigItem<Boolean> item) {
        return item == null ? null : create(key, item, TickBoxControllerBuilder::create);
    }

    private static Option<String> field(String key, @Nullable ConfigItem<String> item) {
        return item == null ? null : create(key, item, StringControllerBuilder::create);
    }

    /** A slider needs both a range and a step; without them it is a plain number field. */
    private static Option<Integer> slider(String key, @Nullable ConfigItem<Integer> item) {
        if (item == null) {
            return null;
        }
        return create(key, item, option -> {
            var range = item.getRange();
            if (range == null) {
                return IntegerFieldControllerBuilder.create(option);
            }
            Integer min = range.getMinimum(), max = range.getMaximum();
            if (min == null || max == null || min >= max) {
                return IntegerFieldControllerBuilder.create(option);
            }
            return IntegerSliderControllerBuilder.create(option)
                    .range(min, max)
                    .step(Math.max(1, (max - min) / 200));
        });
    }

    private static Option<Double> decimal(String key, @Nullable ConfigItem<Double> item) {
        if (item == null) {
            return null;
        }
        return create(key, item, option -> {
            var range = item.getRange();
            if (range == null) {
                return DoubleFieldControllerBuilder.create(option);
            }
            Double min = range.getMinimum(), max = range.getMaximum();
            if (min == null || max == null || min >= max) {
                return DoubleFieldControllerBuilder.create(option);
            }
            return DoubleSliderControllerBuilder.create(option)
                    .range(min, max)
                    .step(Math.max(1.0E-4, (max - min) / 200));
        });
    }

    private static Option<Double> customDecimal(String key, Supplier<Double> getter,
                                                Consumer<Double> setter, double min, double max,
                                                double def) {
        try {
            return Option.<Double>createBuilder()
                    .name(label(key))
                    .description(OptionDescription.of(describe(key)))
                    .binding(def, getter, setter)
                    .controller(option -> DoubleSliderControllerBuilder.create(option)
                            .range(min, max)
                            .step((max - min) / 200))
                    .build();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the option '{}': {}", key, t.toString());
            return null;
        }
    }

    /** The cycling controller has no way to learn the enum type, so it is passed explicitly. */
    private static <E extends Enum<E>> Option<E> enumChoice(String key, @Nullable ConfigItem<E> item) {
        if (item == null || item.getDefault() == null) {
            return null;
        }
        Class<E> enumClass = item.getDefault().getDeclaringClass();
        return create(key, item, option -> EnumControllerBuilder.<E>create(option).enumClass(enumClass));
    }

    /**
     * The first font family is picked from a menu instead of typed; the current value is
     * added when it is not an installed family, otherwise the widget would show nothing.
     */
    private static Option<String> fontChoice(String key, @Nullable ConfigItem<String> item) {
        if (item == null) {
            return null;
        }
        var names = new TreeSet<String>();
        try {
            for (var family : FontFamily.getSystemFontMap().values()) {
                names.add(family.getFamilyName());
            }
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Cannot enumerate the system fonts", t);
        }
        String current = item.get();
        if (current != null && !current.isEmpty()) {
            names.add(current);
        }
        var values = new ArrayList<>(names);
        return create(key, item, option -> DropdownStringControllerBuilder.create(option)
                .values(values)
                .allowAnyValue(true)
                .allowEmptyValue(false));
    }

    /** A 0..1 slider that drives the alpha channel of every colour in a list. */
    private static Option<Double> alphaOf(String key, @Nullable ConfigItem<List<? extends String>> item) {
        if (item == null || item.getDefault() == null) {
            return null;
        }
        try {
            double def = firstAlpha(item.getDefault());
            return Option.<Double>createBuilder()
                    .name(label(key))
                    .description(OptionDescription.of(describe(key)))
                    .binding(def, () -> firstAlpha(item.get()),
                            value -> item.set(withAlpha(item.get(), value)))
                    .controller(option -> DoubleSliderControllerBuilder.create(option)
                            .range(0.0, 1.0)
                            .step(0.01))
                    .build();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the option '{}': {}", key, t.toString());
            return null;
        }
    }

    private static Option<Color> cornerColor(int index,
                                             @Nullable ConfigItem<List<? extends String>> item) {
        if (item == null || item.getDefault() == null) {
            return null;
        }
        String key = "mui.lite.border." + index;
        try {
            int def = colorAt(item.getDefault(), index);
            return Option.<Color>createBuilder()
                    .name(label(key))
                    .description(OptionDescription.of(describe(key)))
                    .binding(new Color(def, true),
                            () -> new Color(colorAt(item.get(), index), true),
                            color -> item.set(withColor(item.get(), index, color.getRGB())))
                    .controller(option -> ColorControllerBuilder.create(option).allowAlpha(true))
                    .build();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the option '{}': {}", key, t.toString());
            return null;
        }
    }

    /** The four-colour presets of the full edition, as buttons that fill the colour rows. */
    private static OptionGroup presets(@Nullable ConfigItem<List<? extends String>> item) {
        if (item == null) {
            return null;
        }
        try {
            var builder = OptionGroup.createBuilder().name(label("mui.lite.group.presets"));
            for (int i = 0; i < PRESET_COLORS.length; i++) {
                final int index = i;
                builder.option(ButtonOption.createBuilder()
                        .name(label("gui.modernui.preset_s", String.valueOf(i + 1)))
                        .text(label("mui.lite.apply"))
                        .action((screen, button) -> {
                            item.set(Arrays.asList(PRESET_COLORS[index]));
                            save();
                            Minecraft.getInstance().setScreenAndShow(create(screen));
                        })
                        .build());
            }
            return builder.build();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the colour presets: {}", t.toString());
            return null;
        }
    }

    /** A text list the player can extend, used for the fallback fonts and the blur blacklist. */
    private static OptionGroup stringList(String key, @Nullable ConfigItem<List<? extends String>> item) {
        if (item == null || item.getDefault() == null) {
            return null;
        }
        try {
            return ListOption.<String>createBuilder(String.class)
                    .name(label(key))
                    .description(OptionDescription.of(describe(key)))
                    .initial("")
                    .controller(option -> StringControllerBuilder.create(option))
                    .binding(new ArrayList<>(item.getDefault()),
                            () -> new ArrayList<>(item.get()),
                            list -> item.set(list))
                    .build();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the list option '{}': {}", key, t.toString());
            return null;
        }
    }

    /** A switch stored in the bootstrap properties rather than in a config file. */
    private static Option<Boolean> bootstrapTick(String key, String property, boolean inverted) {
        try {
            Supplier<Boolean> getter = () -> {
                boolean disabled = Boolean.parseBoolean(
                        ModernUIClient.getBootstrapProperty(property));
                return inverted ? !disabled : disabled;
            };
            Consumer<Boolean> setter = value -> ModernUIClient.setBootstrapProperty(property,
                    Boolean.toString(inverted ? !value : value));
            return Option.<Boolean>createBuilder()
                    .name(label(key))
                    .description(OptionDescription.of(describe(key)))
                    .binding(!inverted, getter, setter)
                    .controller(TickBoxControllerBuilder::create)
                    .build();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the option '{}': {}", key, t.toString());
            return null;
        }
    }

    private static <T> Option<T> create(String key, ConfigItem<T> item,
                                        Function<Option<T>, ControllerBuilder<T>> controller) {
        T def;
        try {
            def = item.getDefault();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the option '{}': {}", key, t.toString());
            return null;
        }
        if (def == null) {
            return null;
        }
        try {
            return Option.<T>createBuilder()
                    .name(label(key))
                    .description(OptionDescription.of(describe(key)))
                    .binding(def, item::get, item::set)
                    .controller(controller)
                    .build();
        } catch (Throwable t) {
            ModernUIMod.LOGGER.warn("Skipping the option '{}': {}", key, t.toString());
            return null;
        }
    }

    // ---- labels ------------------------------------------------------------

    /**
     * The shared tree names the options but rarely describes them, so a missing entry
     * falls back to something readable instead of showing the raw translation key.
     */
    private static Component label(String key) {
        return Component.translatableWithFallback(key, key);
    }

    private static Component label(String key, String arg) {
        return Component.translatableWithFallback(key, key, arg);
    }

    private static Component describe(String key) {
        return Component.translatableWithFallback(key + ".desc", "");
    }

    // ---- colour list helpers ----------------------------------------------

    private static int colorAt(List<? extends String> list, int index) {
        if (list == null || list.isEmpty()) {
            return 0xFFFFFFFF;
        }
        try {
            return icyllis.modernui.graphics.Color.parseColor(
                    list.get(Math.min(index, list.size() - 1)));
        } catch (RuntimeException e) {
            return 0xFFFFFFFF;
        }
    }

    private static List<String> withColor(List<? extends String> list, int index, int argb) {
        var out = new ArrayList<String>(4);
        for (int i = 0; i < 4; i++) {
            out.add(i == index ? formatColor(argb) : formatColor(colorAt(list, i)));
        }
        return out;
    }

    private static double firstAlpha(List<? extends String> list) {
        return (colorAt(list, 0) >>> 24) / 255.0;
    }

    private static List<String> withAlpha(List<? extends String> list, double alpha) {
        int a = (int) (alpha * 255.0 + 0.5);
        var out = new ArrayList<String>(list == null ? 1 : Math.max(1, list.size()));
        if (list == null || list.isEmpty()) {
            out.add(formatColor(a << 24));
            return out;
        }
        for (String entry : list) {
            try {
                int color = icyllis.modernui.graphics.Color.parseColor(entry);
                out.add(formatColor((color & 0xFFFFFF) | (a << 24)));
            } catch (RuntimeException e) {
                out.add(entry);
            }
        }
        return out;
    }

    private static String formatColor(int argb) {
        return String.format(Locale.ROOT, "#%08X", argb);
    }

    // ---- lifecycle ---------------------------------------------------------

    /** The full edition writes on close; here the library owns the lifecycle, so save on demand. */
    private static void save() {
        Config.CLIENT.reload();
        Config.COMMON.reload();
        Config.TEXT.reload();
        var platform = MuiPlatform.get();
        platform.saveConfig(Config.TYPE_CLIENT);
        platform.saveConfig(Config.TYPE_COMMON);
        platform.saveConfig(Config.TYPE_TEXT);
        String current = Config.CLIENT.mFirstFontFamily.get();
        if (!Objects.equals(current, sFontFamilyAtOpen)) {
            sFontFamilyAtOpen = current;
            reloadTypeface();
        }
    }

    private static void reloadTypeface() {
        Minecraft.getInstance().execute(() -> {
            var client = ModernUIClient.getInstance();
            client.reloadTypeface();
            client.reloadFontStrike();
        });
    }

    private static ButtonOption link(String name, String url) {
        return ButtonOption.createBuilder()
                .name(label(name))
                .text(label("mui.lite.link.open"))
                .action((screen, button) -> ConfirmLinkScreen.confirmLinkNow(screen, URI.create(url)))
                .build();
    }

    private static void openConfigFolder() {
        var dir = MuiPlatform.get().getBootstrapPath();
        try {
            java.awt.Desktop.getDesktop().open(dir.toFile());
        } catch (Exception e) {
            // A headless JVM cannot open a folder, so the path goes to the log instead.
            ModernUIMod.LOGGER.warn("Cannot open the config folder '{}'", dir);
        }
    }
}