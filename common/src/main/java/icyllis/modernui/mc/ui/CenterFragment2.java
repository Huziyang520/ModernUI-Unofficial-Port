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
import icyllis.modernui.fragment.FragmentContainerView;
import icyllis.modernui.fragment.FragmentTransaction;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.mc.ModernUIMod;
import icyllis.modernui.mc.ui.v2.page.PageMeta;
import icyllis.modernui.mc.ui.v2.page.SettingsPage;
import icyllis.modernui.mc.ui.v2.page.SettingsPages;
import icyllis.modernui.mc.ui.v2.shell.CenterShell;
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayList;
import java.util.List;

import static icyllis.modernui.view.ViewGroup.LayoutParams.*;

/**
 * The operation centre: a narrow destination rail on the left, the shell on the right.
 *
 * <p>The settings destination is the shell's own long page; the other three are fragments
 * hosted in the shell's content area, so every destination shares one backdrop, one header
 * and one visual language. The shell also carries the settings anchor rail, which is what
 * the Plan means by "one long scroll with an index".</p>
 */
public class CenterFragment2 extends Fragment implements SkinRegistry.Listener {

    public static final Marker MARKER = MarkerManager.getMarker("Center");

    private static final int id_dest_container = 0x2002;

    private static final int DEST_DASHBOARD = 1;
    private static final int DEST_SETTINGS = CenterShell.DESTINATION_SETTINGS;
    private static final int DEST_DEVELOPER = 2;
    private static final int DEST_MUSIC = 3;
    private static final int DEST_MARKDOWN = 4;

    private CenterShell mShell;
    private DestinationRail mRail;
    private SettingsHost mHost;
    private int mDestination = -1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getParentFragmentManager().beginTransaction()
                .setPrimaryNavigationFragment(this)
                .commit();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        final long startTime = System.nanoTime();
        var context = requireContext();

        var base = new LinearLayout(context);
        base.setOrientation(LinearLayout.HORIZONTAL);

        mRail = new DestinationRail(context);
        base.addView(mRail, new LinearLayout.LayoutParams(
                Math.round(56f * context.getResources().getDisplayMetrics().density), MATCH_PARENT));

        mShell = new CenterShell(context);
        mShell.setVersionText("Modern UI " + ModernUIMod.getVersion());
        mShell.setPageSource(new CenterShell.PageSource() {
            @NonNull
            @Override
            public SettingsPage build(@NonNull Context context) {
                return SettingsPages.build(context, host());
            }

            @NonNull
            @Override
            public List<PageMeta> meta() {
                return SettingsPages.meta();
            }
        });
        base.addView(mShell, new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f));

        var destContainer = new FragmentContainerView(context);
        destContainer.setId(id_dest_container);
        mShell.content().addView(destContainer, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        var args = getArguments();
        boolean toPreferences = args != null && args.getBoolean("navigateToPreferences");
        setDestination(toPreferences ? DEST_SETTINGS : DEST_DASHBOARD);

        ModernUIMod.LOGGER.info(MARKER, "Built the settings navigation in {} ms",
                (System.nanoTime() - startTime) / 1_000_000L);
        return base;
    }

    private void setDestination(int destination) {
        if (mShell == null || destination == mDestination) {
            return;
        }
        mDestination = destination;
        mRail.setActive(destination);
        mShell.setDestination(destination);
        if (destination == CenterShell.DESTINATION_SETTINGS) {
            return;
        }
        Class<? extends Fragment> fragment = switch (destination) {
            case DEST_DEVELOPER -> AdvancedOptionsFragment.class;
            case DEST_MUSIC -> MusicFragment.class;
            case DEST_MARKDOWN -> MarkdownFragment.class;
            default -> DashboardFragment.class;
        };
        getChildFragmentManager().beginTransaction()
                .replace(id_dest_container, fragment, null, "dest" + destination)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .commit();
    }

    @NonNull
    private SettingsHost host() {
        if (mHost == null) {
            mHost = new SettingsHost();
        }
        return mHost;
    }

    @Override
    public void onDestroyView() {
        if (mRail != null) {
            mRail.release();
        }
        saveDirtyConfigs();
        super.onDestroyView();
    }

    /** Writes every config that the player touched, off the render thread. */
    private void saveDirtyConfigs() {
        if (mHost == null) {
            return;
        }
        var dirty = mHost.takeDirty();
        if (dirty.isEmpty()) {
            return;
        }
        Util.ioPool().execute(() -> {
            var platform = icyllis.modernui.mc.MuiPlatform.get();
            for (int type : dirty) {
                platform.saveConfig(type);
            }
        });
    }

    @Override
    public void onSkinChanged() {
        if (mRail != null) {
            mRail.invalidate();
        }
    }

    /**
     * The settings page needs two editors the v2 widget set does not reimplement and the
     * colour studio; everything else it builds itself. Config dirtiness is tracked here
     * because this is the fragment that outlives the page.
     */
    private final class SettingsHost implements SettingsPages.Host {

        private final java.util.Set<Integer> mDirty = new java.util.LinkedHashSet<>();

        java.util.List<Integer> takeDirty() {
            var result = new java.util.ArrayList<>(mDirty);
            mDirty.clear();
            return result;
        }

        @NonNull
        @Override
        public Runnable clientChanged() {
            return () -> {
                mDirty.add(icyllis.modernui.mc.Config.TYPE_CLIENT);
                icyllis.modernui.mc.Config.CLIENT.reload();
            };
        }

        @NonNull
        @Override
        public Runnable commonChanged() {
            return () -> {
                mDirty.add(icyllis.modernui.mc.Config.TYPE_COMMON);
                icyllis.modernui.mc.Config.COMMON.reload();
            };
        }

        @NonNull
        @Override
        public Runnable textChanged() {
            return () -> {
                mDirty.add(icyllis.modernui.mc.Config.TYPE_TEXT);
                icyllis.modernui.mc.Config.TEXT.reload();
            };
        }

        @NonNull
        @Override
        public View legacyBlock(@NonNull Context context, @NonNull String which) {
            var column = new LinearLayout(context);
            column.setOrientation(LinearLayout.VERTICAL);
            switch (which) {
                case SettingsPages.LEGACY_FONT -> new PreferredFontAccordion(column,
                        clientChanged(), clientChanged()).buildInto(column);
                case SettingsPages.LEGACY_TOOLTIP -> new TooltipBorderAccordion(column,
                        clientChanged()).buildInto(column);
                default -> {
                }
            }
            return column;
        }

        @NonNull
        @Override
        public View colorListRow(@NonNull Context context, @NonNull String key,
                                 @NonNull icyllis.modernui.mc.ConfigItem<List<? extends String>> item,
                                 @NonNull Runnable onChanged) {
            return UiSupport.colorListRow(context, key, item, onChanged,
                    (field, alpha) -> openStudio(field, alpha));
        }

        @NonNull
        @Override
        public View guiScaleRow(@NonNull Context context) {
            return UiSupport.guiScaleRow(context);
        }

        @Override
        public void openColorStudio(@NonNull icyllis.modernui.mc.ui.v2.widget.TgColorField field,
                                    boolean alpha) {
            openStudio(field, alpha);
        }

        @Override
        public void showSkinPicker() {
            if (mShell != null) {
                mShell.showSkinPicker();
            }
        }

        private void openStudio(@NonNull icyllis.modernui.mc.ui.v2.widget.TgColorField field,
                                boolean alpha) {
            icyllis.modernui.mc.ui.v2.color.ColorSheet.show(field, field.getColor(), alpha,
                    new icyllis.modernui.mc.ui.v2.color.ColorStudio.OnColor() {
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
    }

    /** Narrow glyph rail that switches between the four destinations. */
    private final class DestinationRail extends View {

        private final List<Integer> mDestinations = new ArrayList<>();
        private final List<Integer> mGlyphs = new ArrayList<>();
        private int mActive;
        private int mPressed = -1;

        DestinationRail(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
            add(DEST_DASHBOARD, Skin.GLYPH_DASHBOARD);
            add(DEST_SETTINGS, Skin.GLYPH_SETTINGS);
            add(DEST_DEVELOPER, Skin.GLYPH_CODE);
            add(DEST_MUSIC, Skin.GLYPH_MUSIC);
            if (ModernUIMod.isDeveloperMode()) {
                add(DEST_MARKDOWN, Skin.GLYPH_TEXT);
            }
            SkinRegistry.addListener(CenterFragment2.this);
        }

        private void add(int destination, int glyph) {
            mDestinations.add(destination);
            mGlyphs.add(glyph);
        }

        void setActive(int destination) {
            mActive = destination;
            invalidate();
        }

        void release() {
            SkinRegistry.removeListener(CenterFragment2.this);
        }

        private float itemHeight() {
            return getHeight() / (float) Math.max(1, mDestinations.size());
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            var skin = SkinRegistry.get(getContext());
            var tk = skin.tokens();
            float h = itemHeight();
            float cx = getWidth() * 0.5f;
            for (int i = 0; i < mDestinations.size(); i++) {
                float top = i * h;
                float bottom = top + h;
                boolean active = mDestinations.get(i) == mActive;
                skin.drawRailMarker(canvas, dp(6f), top + dp(6f), getWidth() - dp(6f),
                        bottom - dp(6f), active);
                skin.drawGlyph(canvas, mGlyphs.get(i), cx, (top + bottom) * 0.5f,
                        dp(22f), active ? tk.accent : tk.muted);
            }
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            float h = itemHeight();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    mPressed = indexAt(event.getY(), h);
                    invalidate();
                    return true;
                }
                case MotionEvent.ACTION_UP -> {
                    int index = indexAt(event.getY(), h);
                    mPressed = -1;
                    if (index >= 0 && index < mDestinations.size()) {
                        setDestination(mDestinations.get(index));
                    }
                    invalidate();
                    return true;
                }
                case MotionEvent.ACTION_CANCEL -> {
                    mPressed = -1;
                    invalidate();
                    return true;
                }
                default -> {
                    return true;
                }
            }
        }

        private int indexAt(float y, float h) {
            if (h <= 0f) {
                return -1;
            }
            return (int) (y / h);
        }
    }
}
