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
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.mc.ui.v2.skin.Skin;
import icyllis.modernui.mc.ui.v2.skin.SkinRegistry;
import icyllis.modernui.mc.ui.v2.skin.SkinTokens;
import icyllis.modernui.mc.ui.v2.widget.TgRow;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;
/**
 * The single vertical column that holds the whole settings page: group headings, section
 * headings, rows and full width blocks. It also owns the row filter and the dividers.
 *
 * <p>Filtering hides rows rather than removing them, so a query can be cleared without
 * rebuilding any control and without losing a half typed value.</p>
 */
public final class RowList extends LinearLayout implements SkinRegistry.Listener {

    private static final int KIND_GROUP_HEADER = 0;
    private static final int KIND_SECTION_HEADER = 1;
    private static final int KIND_ROW = 2;
    private static final int KIND_BLOCK = 3;

    private static final class Entry {
        final View view;
        final int kind;
        final String groupId;
        final String sectionKey;
        final TgRow row;
        boolean visible = true;

        Entry(@NonNull View view, int kind, String groupId, String sectionKey, TgRow row) {
            this.view = view;
            this.kind = kind;
            this.groupId = groupId;
            this.sectionKey = sectionKey;
            this.row = row;
        }
    }

    private final List<Entry> mEntries = new ArrayList<>();
    private Skin mSkin;
    private boolean mDividers = true;

    public RowList(@NonNull Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        setWillNotDraw(false);
    }

    @NonNull
    private Skin skin() {
        var skin = mSkin;
        if (skin == null) {
            skin = SkinRegistry.get(getContext());
            mSkin = skin;
        }
        return skin;
    }

    @NonNull
    private SkinTokens tokens() {
        return skin().tokens();
    }

    public void addGroupHeader(@NonNull String groupId, @NonNull View header) {
        addEntry(new Entry(header, KIND_GROUP_HEADER, groupId, groupId, null));
    }

    public void addSectionHeader(@NonNull String groupId, @NonNull String sectionKey,
                                 @NonNull View header) {
        addEntry(new Entry(header, KIND_SECTION_HEADER, groupId, sectionId(groupId, sectionKey), null));
    }

    public void addRow(@NonNull String groupId, @NonNull String sectionKey, @NonNull TgRow row) {
        addEntry(new Entry(row, KIND_ROW, groupId, sectionId(groupId, sectionKey), row));
    }

    public void addBlock(@NonNull String groupId, @NonNull String sectionKey, @NonNull View block) {
        addEntry(new Entry(block, KIND_BLOCK, groupId, sectionId(groupId, sectionKey), null));
    }

    /** Section titles are only unique inside a group, so the id is qualified by it. */
    @NonNull
    private static String sectionId(@NonNull String groupId, @NonNull String sectionKey) {
        return groupId + '/' + sectionKey;
    }

    private void addEntry(@NonNull Entry entry) {
        mEntries.add(entry);
        addView(entry.view, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

    /** Drops every child; the caller is expected to render the page again. */
    public void clearEntries() {
        mEntries.clear();
        removeAllViews();
        requestLayout();
        invalidate();
    }

    /** Every row currently in the column, in display order. */
    @NonNull
    public List<TgRow> rows() {
        var rows = new ArrayList<TgRow>();
        for (var entry : mEntries) {
            if (entry.row != null) {
                rows.add(entry.row);
            }
        }
        return rows;
    }

    public void setDividers(boolean enabled) {
        if (mDividers != enabled) {
            mDividers = enabled;
            invalidate();
        }
    }

    /**
     * Hides the rows that do not match {@code query} and every heading left without a
     * visible row under it.
     *
     * @return the group ids that still have at least one match, so the rail can dim the
     * rest; every group id is returned for an empty query
     */
    @NonNull
    public Set<String> filter(@NonNull String query) {
        final String trimmed = query == null ? "" : query.strip();
        final boolean active = !trimmed.isEmpty();

        var groupsWithMatch = new LinkedHashSet<String>();
        var sectionsWithMatch = new LinkedHashSet<String>();
        for (var entry : mEntries) {
            boolean visible;
            switch (entry.kind) {
                case KIND_ROW -> visible = !active || entry.row.matches(trimmed);
                // a block is not searchable, so it only survives an empty query
                case KIND_BLOCK -> visible = !active;
                default -> visible = false;
            }
            entry.visible = visible;
            if (visible) {
                groupsWithMatch.add(entry.groupId);
                sectionsWithMatch.add(entry.sectionKey);
            }
        }
        if (!active) {
            for (var entry : mEntries) {
                entry.visible = true;
                groupsWithMatch.add(entry.groupId);
                sectionsWithMatch.add(entry.sectionKey);
            }
        }

        for (var entry : mEntries) {
            if (entry.kind == KIND_GROUP_HEADER) {
                entry.visible = groupsWithMatch.contains(entry.groupId);
            } else if (entry.kind == KIND_SECTION_HEADER) {
                entry.visible = sectionsWithMatch.contains(entry.sectionKey);
            }
            entry.view.setVisibility(entry.visible ? VISIBLE : GONE);
        }

        markPositions();
        requestLayout();
        invalidate();
        return groupsWithMatch;
    }

    /**
     * Gives every visible run of rows its own first/last pair. A group header or a block
     * breaks the run, a section header does not: the rows under it still read as one card.
     *
     * <p>Every row of a run has to be marked, not only its last one, because a
     * {@link TgRow} starts out as both first and last.</p>
     */
    private void markPositions() {
        var run = new ArrayList<TgRow>();
        for (var entry : mEntries) {
            if (!entry.visible) {
                continue;
            }
            if (entry.row != null) {
                run.add(entry.row);
            } else if (entry.kind == KIND_GROUP_HEADER || entry.kind == KIND_BLOCK) {
                closeRun(run);
                run.clear();
            }
        }
        closeRun(run);
    }

    private static void closeRun(@NonNull List<TgRow> run) {
        for (int i = 0; i < run.size(); i++) {
            run.get(i).setPosition(i == 0, i == run.size() - 1);
        }
    }

    @Override
    public void onSkinChanged() {
        mSkin = null;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        SkinRegistry.addListener(this);
        mSkin = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        SkinRegistry.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!mDividers) {
            return;
        }
        var skin = skin();
        float inset = tokens().padH * tokens().density;
        TgRow previous = null;
        for (var entry : mEntries) {
            if (!entry.visible || entry.view.getVisibility() != VISIBLE) {
                continue;
            }
            if (entry.row != null) {
                if (previous != null) {
                    skin.drawDivider(canvas, inset, entry.view.getTop(),
                            getWidth() - inset);
                }
                previous = entry.row;
            } else {
                // a heading or a block already separates the two rows
                previous = null;
            }
        }
    }

    /** True when at least one row is showing; the shell uses it for the empty state. */
    public boolean hasVisibleRows() {
        for (var entry : mEntries) {
            if (entry.visible && entry.row != null) {
                return true;
            }
        }
        return false;
    }

    /** Utility for skins that want the row column inset; also used by the shell. */
    public int columnInset() {
        return Math.round(tokens().padH * tokens().density);
    }
}
