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
import icyllis.modernui.mc.ui.v2.TgTheme;
import icyllis.modernui.mc.ui.v2.widget.TgGroup;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Vertical index of the settings groups: one glyph per anchor. The active marker comes from
 * the skin, so every style gives it its own shape. Groups with no row left after a filter
 * are dimmed rather than removed, which keeps the rail from reflowing while typing.
 */
public final class IndexRail extends TgGroup {

    public interface OnGroupSelected {
        void onGroupSelected(int index, @NonNull String groupId);
    }

    private static final class Item {
        final String id;
        final int glyph;
        final View view;
        boolean enabled = true;

        Item(String id, int glyph, View view) {
            this.id = id;
            this.glyph = glyph;
            this.view = view;
        }
    }

    private final LinearLayout mColumn;
    private final List<Item> mItems = new ArrayList<>();
    private OnGroupSelected mListener;
    private int mActive = -1;

    public IndexRail(@NonNull Context context) {
        super(context);
        setClickable(true);
        mColumn = new LinearLayout(context);
        mColumn.setOrientation(LinearLayout.VERTICAL);
        mColumn.setGravity(Gravity.CENTER_HORIZONTAL);
        var params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        addView(mColumn, params);
    }

    public void setOnGroupSelected(OnGroupSelected listener) {
        mListener = listener;
    }

    /** Adds one anchor; call before the rail is measured. */
    public void addGroup(@NonNull String groupId, int glyph) {
        final int index = mItems.size();
        var cell = new View(getContext()) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                if (index >= mItems.size()) {
                    return;
                }
                var item = mItems.get(index);
                boolean active = index == mActive;
                var tk = tokens();
                skin().drawRailMarker(canvas, 0, 0, getWidth(), getHeight(), active);
                int color;
                if (active) {
                    color = tk.accent;
                } else if (!item.enabled) {
                    color = TgTheme.alpha(tk.muted, 0.35f);
                } else {
                    color = tk.muted;
                }
                float size = Math.min(getWidth(), getHeight()) * 0.52f;
                skin().drawGlyph(canvas, item.glyph, getWidth() * 0.5f, getHeight() * 0.5f,
                        size, color);
            }
        };
        cell.setWillNotDraw(false);
        cell.setClickable(true);
        cell.setOnClickListener(v -> select(index, true));

        var params = new LinearLayout.LayoutParams(MATCH_PARENT, Math.round(dp(30f)));
        params.topMargin = Math.round(dp(5f));
        params.bottomMargin = Math.round(dp(5f));
        mColumn.addView(cell, params);
        mItems.add(new Item(groupId, glyph, cell));
    }

    public int size() {
        return mItems.size();
    }

    public int getActive() {
        return mActive;
    }

    @NonNull
    public String groupIdAt(int index) {
        return mItems.get(index).id;
    }

    /** Moves the marker without notifying; used while the page scrolls. */
    public void setActive(int index) {
        select(index, false);
    }

    private void select(int index, boolean notify) {
        if (index < 0 || index >= mItems.size() || index == mActive) {
            return;
        }
        mActive = index;
        for (var item : mItems) {
            item.view.invalidate();
        }
        if (notify && mListener != null) {
            mListener.onGroupSelected(index, mItems.get(index).id);
        }
    }

    /** Dims the anchors whose group has no row left after a filter. */
    public void setEnabledGroups(@NonNull Set<String> withMatches) {
        for (var item : mItems) {
            boolean enabled = withMatches.contains(item.id);
            if (item.enabled != enabled) {
                item.enabled = enabled;
                item.view.invalidate();
            }
        }
    }

    /** Top of one anchor relative to this rail, in pixels; -1 when out of range. */
    public int topOf(int index) {
        if (index < 0 || index >= mItems.size()) {
            return -1;
        }
        return mColumn.getTop() + mItems.get(index).view.getTop();
    }

    /** Clears the marker, e.g. while a search hides every group. */
    public void clearActive() {
        if (mActive != -1) {
            mActive = -1;
            for (var item : mItems) {
                item.view.invalidate();
            }
        }
    }
}
