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
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.ModernUIMod;
import icyllis.modernui.mc.MuiPlatform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

/**
 * The lite edition has no settings screen of its own: every option is rendered by
 * YetAnotherConfigLib while reading and writing the existing config items, so both
 * editions share one config file and one set of defaults.
 */
public final class YaclSettings {

    private YaclSettings() {
    }

    public static Screen create(Screen parent) {
        var config = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("mui.lite.title"))
                .save(YaclSettings::save);

        var client = Config.CLIENT;

        config.category(ConfigCategory.createBuilder()
                .name(Component.translatable("mui.lite.category.background"))
                .group(group("mui.lite.group.blur",
                        tick("mui.lite.blurEffect", client.mBlurEffect),
                        tick("mui.lite.additionalBlurEffect", client.mAdditionalBlurEffect),
                        tick("mui.lite.overrideVanillaBlur", client.mOverrideVanillaBlur),
                        slider("mui.lite.backgroundDuration", client.mBackgroundDuration),
                        slider("mui.lite.blurRadius", client.mBlurRadius)))
                .build());

        config.category(ConfigCategory.createBuilder()
                .name(Component.translatable("mui.lite.category.tooltip"))
                .group(group("mui.lite.group.tooltip",
                        tick("mui.lite.tooltip", client.mTooltip),
                        tick("mui.lite.roundedTooltip", client.mRoundedTooltip),
                        tick("mui.lite.centerTooltipTitle", client.mCenterTooltipTitle),
                        tick("mui.lite.tooltipTitleBreak", client.mTooltipTitleBreak),
                        tick("mui.lite.exactTooltipPositioning", client.mExactTooltipPositioning),
                        tick("mui.lite.adaptiveTooltipColors", client.mAdaptiveTooltipColors),
                        tick("mui.lite.tooltipLineWrapping", client.mTooltipLineWrapping),
                        decimal("mui.lite.tooltipWidth", client.mTooltipWidth),
                        decimal("mui.lite.tooltipRadius", client.mTooltipRadius),
                        decimal("mui.lite.tooltipShadowRadius", client.mTooltipShadowRadius),
                        decimal("mui.lite.tooltipShadowAlpha", client.mTooltipShadowAlpha),
                        slider("mui.lite.tooltipArrowScrollFactor", client.mTooltipArrowScrollFactor)))
                .build());

        config.category(ConfigCategory.createBuilder()
                .name(Component.translatable("mui.lite.category.interface"))
                .group(group("mui.lite.group.window",
                        tick("mui.lite.inventoryPause", client.mInventoryPause),
                        tick("mui.lite.forceRtl", client.mForceRtl),
                        tick("mui.lite.useNewGuiScale", client.mUseNewGuiScale),
                        tick("mui.lite.removeTelemetry", client.mRemoveTelemetry),
                        decimal("mui.lite.fontScale", client.mFontScale),
                        enumChoice("mui.lite.windowMode", client.mWindowMode)))
                .group(group("mui.lite.group.performance",
                        slider("mui.lite.framerateInactive", client.mFramerateInactive),
                        decimal("mui.lite.masterVolumeInactive", client.mMasterVolumeInactive),
                        decimal("mui.lite.masterVolumeMinimized", client.mMasterVolumeMinimized),
                        tick("mui.lite.globalVolumeControl", client.mGlobalVolumeControl)))
                .group(group("mui.lite.group.sound",
                        tick("mui.lite.ding", client.mDing),
                        field("mui.lite.dingSound", client.mDingSound),
                        decimal("mui.lite.dingVolume", client.mDingVolume)))
                .build());

        config.category(ConfigCategory.createBuilder()
                .name(Component.translatable("mui.lite.category.scrolling"))
                .group(group("mui.lite.group.scrolling",
                        slider("mui.lite.scrollbarSize", client.mScrollbarSize),
                        slider("mui.lite.touchSlop", client.mTouchSlop),
                        slider("mui.lite.hoverSlop", client.mHoverSlop),
                        slider("mui.lite.minScrollbarTouchTarget", client.mMinScrollbarTouchTarget),
                        slider("mui.lite.minimumFlingVelocity", client.mMinimumFlingVelocity),
                        slider("mui.lite.maximumFlingVelocity", client.mMaximumFlingVelocity),
                        decimal("mui.lite.scrollFriction", client.mScrollFriction),
                        slider("mui.lite.overscrollDistance", client.mOverscrollDistance),
                        slider("mui.lite.overflingDistance", client.mOverflingDistance),
                        decimal("mui.lite.verticalScrollFactor", client.mVerticalScrollFactor),
                        decimal("mui.lite.horizontalScrollFactor", client.mHorizontalScrollFactor),
                        slider("mui.lite.hoverTooltipShowTimeout", client.mHoverTooltipShowTimeout),
                        slider("mui.lite.hoverTooltipHideTimeout", client.mHoverTooltipHideTimeout)))
                .build());

        config.category(ConfigCategory.createBuilder()
                .name(Component.translatable("mui.lite.category.font"))
                .group(group("mui.lite.group.font",
                        field("mui.lite.firstFontFamily", client.mFirstFontFamily),
                        tick("mui.lite.useColorEmoji", client.mUseColorEmoji),
                        tick("mui.lite.emojiShortcodes", client.mEmojiShortcodes),
                        tick("mui.lite.linearMetricsUi", client.mLinearMetrics)))
                .build());

        var text = Config.TEXT;
        config.category(ConfigCategory.createBuilder()
                .name(Component.translatable("mui.lite.category.text"))
                .group(group("mui.lite.group.textRendering",
                        tick("mui.lite.allowShadow", text.mAllowShadow),
                        tick("mui.lite.fixedResolution", text.mFixedResolution),
                        decimal("mui.lite.baseFontSize", text.mBaseFontSize),
                        decimal("mui.lite.baselineShift", text.mBaselineShift),
                        decimal("mui.lite.shadowOffset", text.mShadowOffset),
                        decimal("mui.lite.outlineOffset", text.mOutlineOffset),
                        decimal("mui.lite.bitmapOffset", text.mBitmapOffset),
                        slider("mui.lite.cacheLifespan", text.mCacheLifespan),
                        tick("mui.lite.useTextShadersInWorld", text.mUseTextShadersInWorld),
                        tick("mui.lite.antiAliasing", text.mAntiAliasing),
                        slider("mui.lite.minPixelDensityForSDF", text.mMinPixelDensityForSDF),
                        tick("mui.lite.linearSamplingA8Atlas", text.mLinearSamplingA8Atlas)))
                .group(group("mui.lite.group.textEngine",
                        enumChoice("mui.lite.textDirection", text.mTextDirection),
                        enumChoice("mui.lite.defaultFontBehavior", text.mDefaultFontBehavior),
                        enumChoice("mui.lite.lineBreakStyle", text.mLineBreakStyle),
                        enumChoice("mui.lite.lineBreakWordStyle", text.mLineBreakWordStyle),
                        tick("mui.lite.useComponentCache", text.mUseComponentCache),
                        tick("mui.lite.allowAsyncLayout", text.mAllowAsyncLayout),
                        tick("mui.lite.computeDeviceFontSize", text.mComputeDeviceFontSize),
                        tick("mui.lite.allowSDFTextIn2D", text.mAllowSDFTextIn2D),
                        tick("mui.lite.linearMetrics", text.mLinearMetrics)))
                .build());

        var developerGroup = OptionGroup.createBuilder()
                .name(Component.translatable("mui.lite.group.developer"))
                .option(tick("mui.lite.developerMode", Config.COMMON.developerMode))
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("mui.lite.openFolder"))
                        .text(Component.translatable("mui.lite.openFolder.text"))
                        .action((screen, button) -> openConfigFolder())
                        .build())
                .build();
        config.category(ConfigCategory.createBuilder()
                .name(Component.translatable("mui.lite.category.advanced"))
                .group(developerGroup)
                .build());

        return config.build().generateScreen(parent);
    }

    /** The full edition writes on close; here YACL owns the lifecycle, so save on demand. */
    private static void save() {
        Config.CLIENT.reload();
        Config.COMMON.reload();
        Config.TEXT.reload();
        var platform = MuiPlatform.get();
        platform.saveConfig(Config.TYPE_CLIENT);
        platform.saveConfig(Config.TYPE_COMMON);
        platform.saveConfig(Config.TYPE_TEXT);
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

    private static OptionGroup group(String name, Option<?>... options) {
        var builder = OptionGroup.createBuilder().name(Component.translatable(name));
        for (var option : options) {
            builder.option(option);
        }
        return builder.build();
    }

    private static Option<Boolean> tick(String key, ConfigItem<Boolean> item) {
        return create(key, item, TickBoxControllerBuilder::create);
    }

    private static Option<String> field(String key, ConfigItem<String> item) {
        return create(key, item, StringControllerBuilder::create);
    }

    private static Option<Integer> slider(String key, ConfigItem<Integer> item) {
        return create(key, item, option -> {
            var builder = IntegerSliderControllerBuilder.create(option);
            var range = item.getRange();
            if (range != null && range.getMinimum() != null && range.getMaximum() != null) {
                builder.range(range.getMinimum(), range.getMaximum());
            }
            return builder;
        });
    }

    private static Option<Double> decimal(String key, ConfigItem<Double> item) {
        return create(key, item, option -> {
            var builder = DoubleSliderControllerBuilder.create(option);
            var range = item.getRange();
            if (range != null && range.getMinimum() != null && range.getMaximum() != null) {
                builder.range(range.getMinimum(), range.getMaximum());
            }
            return builder;
        });
    }

    private static <E extends Enum<E>> Option<E> enumChoice(String key, ConfigItem<E> item) {
        return create(key, item, EnumControllerBuilder::create);
    }

    private static <T> Option<T> create(String key, ConfigItem<T> item,
                                        Function<Option<T>, ControllerBuilder<T>> controller) {
        return Option.<T>createBuilder()
                .name(Component.translatable(key))
                .description(OptionDescription.of(Component.translatable(key + ".desc")))
                .binding(item.getDefault(), item::get, item::set)
                .controller(controller)
                .build();
    }
}
