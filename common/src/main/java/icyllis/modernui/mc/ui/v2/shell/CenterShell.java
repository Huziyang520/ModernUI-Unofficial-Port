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

package icyllis.modernui.mc.ui.v2.shell;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.mc.ui.v2.TgAnim;
import icyllis.modernui.mc.ui.v2.TgTheme;
import icyllis.modernui.mc.ui.v2.page.PageMeta;
import icyllis.modernui.mc.ui.v2.page.SettingsPage;
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.mc.ui.v2.widget.TgGroup;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * The settings host: one header, one index rail and one long scrolling page.
 *
 * <p><b>Destination contract.</b> The shell owns the settings destination only.
 * {@link #setDestination(int)} hides the settings page and shows an empty
 * {@link #content()} area that the caller fills with its own view; {@code DESTINATION_SETTINGS}
 * shows the page again. The shell never adds anything to {@link #content()}, so the caller
 * owns that subtree and may swap its single child freely.</p>
 *
 * <p><b>Page contract.</b> The tree comes from {@link #setPageSource(PageSource)} (lazy, so
 * the shell may exist before the config loads) or from {@link #setPage(SettingsPage, List)}.
 * The page is rendered once per style change, never per frame; filtering only toggles
 * visibility, so a query can be cleared without rebuilding any control.</p>
 */
public final class CenterShell extends TgGroup {

    /** Destination index that shows the settings page. */
    public static final int DESTINATION_SETTINGS = 0;

    /** Supplies the page tree and the rail anchors when the shell first renders. */
    public interface PageSource {
        @NonNull
        SettingsPage build(@NonNull Context context);

        @NonNull
        List<PageMeta> meta();
    }

    private static final long FIRST_FRAME_HOLD_MS = 48L;
    private static final long FIRST_FRAME_FADE_MS = 120L;

    private final FrameLayout mHeader;
    private final SearchField mSearch;
    private final TextView mVersion;
    private final StyleButton mStyleButton;
    private final ResetButton mResetButton;
    private final FrameLayout mBody;
    private final IndexRail mRail;
    private final ScrollView mScroll;
    private final RowList mRows;
    private final FrameLayout mContent;
    private final TextView mEmptyState;
    private final View mRailGutter;

    private final List<SettingsRenderer.Anchor> mAnchors = new ArrayList<>();

    private PageSource mPageSource;
    private SettingsPage mPage;
    private List<PageMeta> mMeta;
    private int mDestination = DESTINATION_SETTINGS;
    private boolean mRendered;
    private boolean mRailEnabled = true;
    private Runnable mResetListener;

    private long mFirstFrameAt;
    private float mCoverAlpha = 1f;
    private boolean mCoverStarted;
    private boolean mCoverDone;

    public CenterShell(@NonNull Context context) {
        super(context);
        setClipChildren(false);

        // ------------------------------------------------------------- header
        mHeader = new FrameLayout(context);
        var headerParams = new LayoutParams(MATCH_PARENT, Math.round(dp(64f)));
        headerParams.gravity = Gravity.TOP;
        addView(mHeader, headerParams);

        mStyleButton = new StyleButton(context);
        mStyleButton.setOnClickListener(v -> showSkinPicker());

        var styleParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        styleParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        styleParams.leftMargin = Math.round(dp(16f));
        mHeader.addView(mStyleButton, styleParams);

        mResetButton = new ResetButton(context);
        mResetButton.setOnClickListener(v -> confirmResetAll());
        var resetParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        resetParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        resetParams.leftMargin = Math.round(dp(16f));
        // sits right after the style button; the offset is re-applied once it is measured
        mHeader.addView(mResetButton, resetParams);
        mStyleButton.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                ((FrameLayout.LayoutParams) mResetButton.getLayoutParams()).leftMargin =
                        r + Math.round(dp(8f)));

        mVersion = new TextView(context);
        mVersion.setSingleLine(true);
        mVersion.setClickable(false);
        var versionParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        versionParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        versionParams.rightMargin = Math.round(dp(16f));
        mHeader.addView(mVersion, versionParams);

        mSearch = new SearchField(context);
        var searchParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        searchParams.gravity = Gravity.CENTER_VERTICAL;
        searchParams.leftMargin = Math.round(dp(190f));
        searchParams.rightMargin = Math.round(dp(110f));
        mHeader.addView(mSearch, searchParams);
        mSearch.setOnQueryChanged(this::applyFilter);

        // --------------------------------------------------------------- body
        mBody = new FrameLayout(context);
        var bodyParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        bodyParams.topMargin = Math.round(dp(64f));
        addView(mBody, bodyParams);

        mRailGutter = new View(context);
        var gutterParams = new FrameLayout.LayoutParams(Math.round(dp(40f)), MATCH_PARENT);
        gutterParams.gravity = Gravity.START;
        mBody.addView(mRailGutter, gutterParams);

        mRail = new IndexRail(context);
        var railParams = new FrameLayout.LayoutParams(Math.round(dp(24f)), MATCH_PARENT);
        railParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        railParams.leftMargin = Math.round(dp(8f));
        mBody.addView(mRail, railParams);
        mRail.setOnGroupSelected((index, groupId) -> scrollToGroup(index));

        mScroll = new ScrollView(context);
        var scrollParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        scrollParams.leftMargin = Math.round(dp(40f));
        mBody.addView(mScroll, scrollParams);

        mRows = new RowList(context);
        mScroll.addView(mRows, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        mScroll.setOnScrollChangeListener((v, x, y, oldX, oldY) -> onScrolled());

        mEmptyState = new TextView(context);
        mEmptyState.setText(I18n.get("mui.v2.shell.search.empty"));
        mEmptyState.setSingleLine(false);
        mEmptyState.setGravity(Gravity.CENTER);
        mEmptyState.setVisibility(GONE);
        var emptyParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        emptyParams.gravity = Gravity.CENTER;
        mBody.addView(mEmptyState, emptyParams);

        // ------------------------------------------------------------ content
        mContent = new FrameLayout(context);
        mContent.setVisibility(GONE);
        var contentParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        contentParams.topMargin = Math.round(dp(64f));
        addView(mContent, contentParams);

        syncChrome();
    }

    // ------------------------------------------------------------------ page

    /**
     * Supplies the tree the shell renders; rendering is deferred until the view is attached,
     * so a shell may be built before the config has loaded.
     */
    public void setPageSource(@Nullable PageSource source) {
        mPageSource = source;
        mPage = null;
        mMeta = null;
        mRendered = false;
        renderPage();
    }

    /**
     * Sets the page tree directly. Rail anchors come from {@code meta}; when it is null the
     * shell derives them from the page groups.
     */
    public void setPage(@NonNull SettingsPage page, @Nullable List<PageMeta> meta) {
        mPageSource = null;
        mPage = page;
        mMeta = meta;
        mRendered = false;
        renderPage();
    }

    /** Renders the page again; called once per style change and on first attach. */
    public void renderPage() {
        if (!isAttachedToWindow()) {
            return;
        }
        var page = mPage;
        if (page == null && mPageSource != null) {
            page = mPageSource.build(getContext());
            mPage = page;
            mMeta = mPageSource.meta();
        }
        if (page == null) {
            return;
        }

        mRows.clearEntries();
        mAnchors.clear();
        mAnchors.addAll(SettingsRenderer.render(getContext(), page, mRows));

        rebuildRail();
        applyFilter(mSearch.getQuery());
        mRendered = true;
        mScroll.scrollTo(0, 0);

        // one line per render so a broken page is diagnosable from the log alone
        final var builtPage = page;
        mRows.post(() -> {
            int children = mRows.getChildCount();
            int visible = 0;
            int tall = 0;
            for (int i = 0; i < children; i++) {
                var child = mRows.getChildAt(i);
                if (child.getVisibility() != GONE) {
                    visible++;
                    if (child.getMeasuredHeight() > Math.round(dp(200f))) {
                        tall++;
                    }
                }
            }
            icyllis.modernui.mc.ModernUIMod.LOGGER.info(
                    icyllis.modernui.mc.ModernUIMod.MARKER,
                    "Settings page: {} groups, {} anchors, {} entries ({} visible, {} over 200dp), column {}x{}",
                    builtPage.groups().size(), mAnchors.size(), children, visible, tall,
                    mRows.getWidth(), mRows.getMeasuredHeight());
        });
    }

    private void rebuildRail() {
        // anchors are stable across renders, so the rail is only built once
        if (mRail.size() == 0) {
            for (var anchor : mAnchors) {
                mRail.addGroup(anchor.id(), anchorGlyph(anchor.id()));
            }
        }
        mRail.setVisibility(mRailEnabled ? VISIBLE : GONE);
        mRailGutter.setVisibility(mRailEnabled ? VISIBLE : GONE);
    }

    private int anchorGlyph(@NonNull String groupId) {
        var meta = mMeta;
        if (meta != null) {
            for (var item : meta) {
                if (item.groupId().equals(groupId)) {
                    return item.glyph();
                }
            }
        }
        var page = mPage;
        if (page != null) {
            for (var group : page.groups()) {
                if (group.id.equals(groupId)) {
                    return group.glyph;
                }
            }
        }
        return Skin.GLYPH_NONE;
    }

    // ------------------------------------------------------------ navigation

    /** Switches between the settings page and the caller filled content area. */
    public void setDestination(int destination) {
        if (mDestination == destination) {
            return;
        }
        mDestination = destination;
        boolean settings = destination == DESTINATION_SETTINGS;
        mBody.setVisibility(settings ? VISIBLE : GONE);
        mHeader.setVisibility(settings ? VISIBLE : GONE);
        mContent.setVisibility(settings ? GONE : VISIBLE);
        if (settings && !mRendered) {
            renderPage();
        }
    }

    public int getDestination() {
        return mDestination;
    }

    /** Area the caller fills for the other destinations; the shell never adds to it. */
    @NonNull
    public FrameLayout content() {
        return mContent;
    }

    /** Scrolls the long page to one group anchor. */
    public void scrollToGroup(int index) {
        if (index < 0 || index >= mAnchors.size()) {
            return;
        }
        var header = mAnchors.get(index).header();
        int y = header.getTop();
        if (y >= 0) {
            mScroll.smoothScrollTo(Math.max(0, y - Math.round(dp(6f))));
        }
        mRail.setActive(index);
    }

    /** Highlights the rail anchor of whichever group the page is scrolled into. */
    private void onScrolled() {
        if (!mRailEnabled || mAnchors.isEmpty()) {
            return;
        }
        int scrollY = mScroll.getScrollY();
        int offset = Math.round(dp(24f));
        int active = 0;
        for (int i = 0; i < mAnchors.size(); i++) {
            if (mAnchors.get(i).header().getTop() - offset <= scrollY) {
                active = i;
            } else {
                break;
            }
        }
        mRail.setActive(active);
    }

    // --------------------------------------------------------------- search

    private void applyFilter(@NonNull String query) {
        Set<String> matches = mRows.filter(query);
        boolean searching = !query.strip().isEmpty();
        if (searching) {
            mRail.setEnabledGroups(matches);
            mRail.clearActive();
        } else {
            var all = new LinkedHashSet<String>();
            for (var anchor : mAnchors) {
                all.add(anchor.id());
            }
            mRail.setEnabledGroups(all);
            onScrolled();
        }
        boolean any = mRows.hasVisibleRows();
        mEmptyState.setVisibility(any ? GONE : VISIBLE);
        mScroll.setVisibility(any ? VISIBLE : INVISIBLE);
    }

    /** The header search box, so a screen can focus it from a key binding. */
    @NonNull
    public SearchField search() {
        return mSearch;
    }

    /** Shows or hides the index rail; the appearance group drives it. */
    public void setRailEnabled(boolean enabled) {
        if (mRailEnabled != enabled) {
            mRailEnabled = enabled;
            mRail.setVisibility(enabled ? VISIBLE : GONE);
            mRailGutter.setVisibility(enabled ? VISIBLE : GONE);
            requestLayout();
        }
    }

    public boolean isRailEnabled() {
        return mRailEnabled;
    }

    /** Opens the style picker; the page's style row calls this through the host. */
    public void showSkinPicker() {
        new SkinPicker(mStyleButton, id -> {
            SkinRegistry.setActive(id);
            syncChrome();
        }).show(mStyleButton);
    }

    // ---------------------------------------------------------------- chrome

    /** Re-resolves every text and colour the header owns from the active skin. */
    private void syncChrome() {
        var tk = tokens();
        mStyleButton.refreshLabel();
        mVersion.setTextSize(tk.descSize);
        mVersion.setTextColor(tk.muted);
        if (tk.monoTypeface != null) {
            mVersion.setTypeface(tk.monoTypeface);
        }
        mEmptyState.setTextSize(tk.titleSize);
        mEmptyState.setTextColor(tk.muted);
        if (tk.typeface != null) {
            mEmptyState.setTypeface(tk.typeface);
        }
    }

    @Override
    public void onSkinChanged() {
        super.onSkinChanged();
        syncChrome();
        // widgets re-resolve their own skin, but the header text and the headings were built
        // from the old tokens, so the column is rebuilt once per style change
        renderPage();
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstFrameAt = System.nanoTime();
        // a detached shell had its cover step removed, so the whole sequence restarts
        mCoverStarted = false;
        mCoverDone = false;
        mCoverAlpha = 1f;
        startFirstFrameCover();
        if (!mRendered) {
            renderPage();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mCoverStep);
        super.onDetachedFromWindow();
    }

    // ----------------------------------------------------------- first frame

    /**
     * The first frame can arrive before the text layouts are ready. An opaque cover is held
     * for one short interval and then faded out, so nothing half drawn is ever visible.
     */
    private void startFirstFrameCover() {
        if (mCoverStarted) {
            return;
        }
        mCoverStarted = true;
        postOnAnimationDelayed(mCoverStep, FIRST_FRAME_HOLD_MS);
    }

    private final Runnable mCoverStep = new Runnable() {
        @Override
        public void run() {
            if (mCoverDone) {
                return;
            }
            mCoverDone = true;
            TgAnim.run(CenterShell.this, FIRST_FRAME_FADE_MS, TgAnim.OUT_CUBIC, t -> {
                mCoverAlpha = 1f - t;
                invalidate();
            });
        }
    };

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        skin().drawBackdrop(canvas, getWidth(), getHeight());
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!mCoverDone && mFirstFrameAt > 0L) {
            // a frame driven fallback in case the posted step is delayed by a slow first frame
            long elapsed = (System.nanoTime() - mFirstFrameAt) / 1_000_000L;
            if (elapsed >= FIRST_FRAME_HOLD_MS) {
                mCoverAlpha = Math.max(0f, 1f - (elapsed - FIRST_FRAME_HOLD_MS)
                        / (float) FIRST_FRAME_FADE_MS);
            }
        }
        if (mCoverAlpha > 0.001f) {
            var paint = Paint.obtain();
            paint.setColor(TgTheme.alpha(tokens().bg, mCoverAlpha));
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.recycle();
        }
    }

    // ---------------------------------------------------------------- reset

    /**
     * Restores every setting of the mod, not just the visible ones. Destructive, so it asks
     * first: the confirm sheet names what will happen and offers a cancel.
     */
    private void confirmResetAll() {
        var context = getContext();
        var column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(dp(18f));
        column.setPadding(pad, pad, pad, pad);
        column.setBackground(icyllis.modernui.mc.ui.v2.TgDrawable.rounded(context,
                tokens().panel, tokens().radiusPanel));

        var title = new TextView(context);
        title.setText(I18n.get("mui.v2.shell.resetAll"));
        title.setTextSize(tokens().titleSize + 1f);
        title.setTextColor(tokens().text);
        column.addView(title, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        var body = new TextView(context);
        body.setText(I18n.get("mui.v2.shell.resetAll.desc"));
        body.setTextSize(tokens().descSize);
        body.setTextColor(tokens().muted);
        body.setSingleLine(false);
        var bodyParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        bodyParams.setMargins(0, Math.round(dp(6f)), 0, Math.round(dp(14f)));
        column.addView(body, bodyParams);

        var actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        int gap = Math.round(dp(8f));

        var cancel = icyllis.modernui.mc.ui.v2.page.Rows.button(context,
                I18n.get("gui.cancel"), () -> {
                });
        var cancelParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        cancelParams.setMargins(0, 0, gap / 2, 0);
        actions.addView(cancel, cancelParams);

        var confirm = icyllis.modernui.mc.ui.v2.page.Rows.button(context,
                I18n.get("mui.v2.shell.resetAll.confirm"), () -> {
                });
        var confirmParams = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
        confirmParams.setMargins(gap / 2, 0, 0, 0);
        actions.addView(confirm, confirmParams);

        column.addView(actions, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        var popup = new icyllis.modernui.widget.PopupWindow(column);
        popup.setFocusable(true);
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(12f));
        popup.setWidth(Math.round(dp(320f)));

        cancel.setOnAction(popup::dismiss);
        confirm.setOnAction(() -> {
            popup.dismiss();
            resetEverything();
        });
        popup.showAsDropDown(mResetButton, 0, Math.round(dp(6f)), Gravity.TOP);
    }

    /** Writes the defaults, drops the accent override, and re-renders in place. */
    private void resetEverything() {
        int changed = icyllis.modernui.mc.Config.resetAllToDefaults();
        // the accent lives outside the config specs, in the bootstrap properties
        icyllis.modernui.mc.ui.AccentPalette.setSeed(0);
        SkinRegistry.setActive(SkinRegistry.DEFAULT_ID);
        SkinRegistry.invalidate();
        renderPage();
        if (mResetListener != null) {
            mResetListener.run();
        }
        icyllis.modernui.mc.ModernUIMod.LOGGER.info(
                icyllis.modernui.mc.ModernUIMod.MARKER,
                "Reset {} settings to their defaults", changed);
        icyllis.modernui.widget.Toast.makeText(getContext(),
                I18n.get("mui.v2.shell.resetAll.done", changed),
                icyllis.modernui.widget.Toast.LENGTH_SHORT).show();
    }

    /** Called after a global reset so the host can mark every config dirty. */
    public void setOnResetListener(@Nullable Runnable listener) {
        mResetListener = listener;
    }

    // ---------------------------------------------------------------- misc

    /** Version label of the header; the screen that owns the shell sets it. */
    public void setVersionText(@NonNull CharSequence text) {
        mVersion.setText(text);
    }

    @NonNull
    public ScrollView scroll() {
        return mScroll;
    }

    @NonNull
    public IndexRail rail() {
        return mRail;
    }

    @NonNull
    public RowList rows() {
        return mRows;
    }

    /** Top of every group anchor inside the column; empty until the page is rendered. */
    @NonNull
    public List<Integer> anchorOffsets() {
        var offsets = new ArrayList<Integer>();
        for (var anchor : mAnchors) {
            offsets.add(anchor.header().getTop());
        }
        return offsets;
    }

    /** Id of the group the rail currently marks; empty when nothing is marked. */
    @NonNull
    public String activeGroupId() {
        int index = mRail.getActive();
        return index >= 0 && index < mAnchors.size() ? mAnchors.get(index).id() : "";
    }
}
