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

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.Config;
import icyllis.modernui.mc.ConfigItem;
import icyllis.modernui.mc.ModernUIMod;
import icyllis.modernui.mc.MuiPlatform;
import icyllis.modernui.mc.ui.v2.color.ColorSheet;
import icyllis.modernui.mc.ui.v2.page.PageMeta;
import icyllis.modernui.mc.ui.v2.page.SettingsPage;
import icyllis.modernui.mc.ui.v2.page.SettingsPages;
import icyllis.modernui.mc.ui.v2.shell.CenterShell;
import icyllis.modernui.mc.ui.v2.widget.TgColorField;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import net.minecraft.util.Util;

import java.util.List;

/**
 * The settings screen. This class is only the wiring: it owns the config-dirty flags, the
 * save-on-exit path and the two editors that still live outside the v2 widget set. The
 * layout, the styling and every control belong to {@link CenterShell} and
 * {@link SettingsPages}.
 */
public class PreferencesFragment extends Fragment {

    private static final org.apache.logging.log4j.Marker MARKER =
            org.apache.logging.log4j.MarkerManager.getMarker("Preferences");

    private boolean mCommonConfigChanged;
    private boolean mClientConfigChanged;
    private boolean mTextConfigChanged;

    private final Runnable mOnCommonConfigChanged = () -> {
        mCommonConfigChanged = true;
        Config.COMMON.reload();
    };
    private final Runnable mOnClientConfigChanged = () -> {
        mClientConfigChanged = true;
        Config.CLIENT.reload();
    };
    private final Runnable mOnTextConfigChanged = () -> {
        mTextConfigChanged = true;
        Config.TEXT.reload();
    };

    private CenterShell mShell;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        final long startedAt = System.nanoTime();
        var context = requireContext();

        AccentPalette.init(context);
        mShell = new CenterShell(context);
        mShell.setVersionText("Modern UI " + ModernUIMod.getVersion());
        mShell.setPageSource(new CenterShell.PageSource() {
            @NonNull
            @Override
            public SettingsPage build(@NonNull Context context) {
                return SettingsPages.build(context, mHost);
            }

            @NonNull
            @Override
            public List<PageMeta> meta() {
                return SettingsPages.meta();
            }
        });
        mShell.setRailEnabled(indexRailEnabled());
        ModernUIMod.LOGGER.info(MARKER, "Built the settings screen in {} ms",
                (System.nanoTime() - startedAt) / 1_000_000L);
        return mShell;
    }

    private static boolean indexRailEnabled() {
        try {
            var item = Config.CLIENT.mInterfaceIndexRail;
            return item == null || item.get();
        } catch (Throwable e) {
            return true;
        }
    }

    @Override
    public void onDestroy() {
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

    private final SettingsPages.Host mHost = new SettingsPages.Host() {

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
        public View legacyBlock(@NonNull Context context, @NonNull String which) {
            return switch (which) {
                case SettingsPages.LEGACY_FONT -> createFontBlock(context);
                case SettingsPages.LEGACY_TOOLTIP -> createTooltipBlock(context);
                default -> new icyllis.modernui.widget.LinearLayout(context);
            };
        }

        @NonNull
        @Override
        public View colorListRow(@NonNull Context context, @NonNull String key,
                                 @NonNull ConfigItem<List<? extends String>> item,
                                 @NonNull Runnable onChanged) {
            return UiSupport.colorListRow(context, key, item, onChanged,
                    (field, alpha) -> openColorStudio(field, alpha));
        }

        @NonNull
        @Override
        public View guiScaleRow(@NonNull Context context) {
            return UiSupport.guiScaleRow(context);
        }

        @Override
        public void openColorStudio(@NonNull TgColorField field, boolean alpha) {
            ColorSheet.show(field, field.getColor(), alpha, new icyllis.modernui.mc.ui.v2.color.ColorStudio.OnColor() {
                @Override
                public void onPreview(int argb) {
                    field.previewColor(argb);
                }

                @Override
                public void onCommit(int argb) {
                    field.commitColor(argb);
                }
            });
        }

        @Override
        public void showSkinPicker() {
            if (mShell != null) {
                mShell.showSkinPicker();
            }
        }
    };

    /** Preferred family, fallback list and registration list; the toggles are page rows. */
    @NonNull
    private View createFontBlock(@NonNull Context context) {
        var column = new icyllis.modernui.widget.LinearLayout(context);
        column.setOrientation(icyllis.modernui.widget.LinearLayout.VERTICAL);
        new PreferredFontAccordion(column, mOnClientConfigChanged,
                () -> {
                    mClientConfigChanged = true;
                    Config.CLIENT.reload();
                })
                .buildInto(column);
        return column;
    }

    /** Border style editor: width, radius, shadow, cycle and the four corner colours. */
    @NonNull
    private View createTooltipBlock(@NonNull Context context) {
        var column = new icyllis.modernui.widget.LinearLayout(context);
        column.setOrientation(icyllis.modernui.widget.LinearLayout.VERTICAL);
        new TooltipBorderAccordion(column, mOnClientConfigChanged)
                .buildInto(column);
        return column;
    }
}
