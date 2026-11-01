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

import icyllis.modernui.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Declarative description of the settings page: groups, sections and the rows in them.
 * The shell renders this tree; the content binding never touches views directly.
 */
public final class SettingsPage {

    /** One anchor of the index rail. */
    public static final class Group {
        public final String id;
        public final String titleKey;
        public final int glyph;
        public final List<Section> sections = new ArrayList<>();

        Group(String id, String titleKey, int glyph) {
            this.id = id;
            this.titleKey = titleKey;
            this.glyph = glyph;
        }
    }

    /** A titled run of rows inside a group. */
    public static final class Section {
        public final Group group;
        public final String titleKey;
        public final List<Row> rows = new ArrayList<>();

        Section(Group group, String titleKey) {
            this.group = group;
            this.titleKey = titleKey;
        }
    }

    /** One row, or a full width block such as an accordion body. */
    public static final class Row {
        public final Section section;
        /** Translation key of the row label, or null for a block. */
        public final String key;
        /** Builds the control; called once when the shell materialises the page. */
        public final RowFactory factory;
        /** Block rows span the column instead of pairing with a control. */
        public final boolean block;

        Row(Section section, String key, RowFactory factory, boolean block) {
            this.section = section;
            this.key = key;
            this.factory = factory;
            this.block = block;
        }
    }

    public interface RowFactory {
        @NonNull
        icyllis.modernui.view.View create(@NonNull icyllis.modernui.core.Context context);
    }

    private final List<Group> mGroups = new ArrayList<>();
    private Group mCurrentGroup;
    private Section mCurrentSection;

    public SettingsPage() {
    }

    @NonNull
    public List<Group> groups() {
        return mGroups;
    }

    @NonNull
    public SettingsPage group(@NonNull String id, @NonNull String titleKey, int glyph) {
        mCurrentGroup = new Group(id, titleKey, glyph);
        mGroups.add(mCurrentGroup);
        mCurrentSection = null;
        return this;
    }

    @NonNull
    public SettingsPage section(@NonNull String titleKey) {
        if (mCurrentGroup == null) {
            throw new IllegalStateException("section() before group()");
        }
        mCurrentSection = new Section(mCurrentGroup, titleKey);
        mCurrentGroup.sections.add(mCurrentSection);
        return this;
    }

    /** Adds a row built by {@code factory} with the given label key. */
    @NonNull
    public SettingsPage row(@NonNull String key, @NonNull RowFactory factory) {
        requireSection().rows.add(new Row(mCurrentSection, key, factory, false));
        return this;
    }

    /** Adds a full width block, e.g. the tooltip border editor. */
    @NonNull
    public SettingsPage block(@NonNull RowFactory factory) {
        requireSection().rows.add(new Row(mCurrentSection, null, factory, true));
        return this;
    }

    @NonNull
    private Section requireSection() {
        if (mCurrentSection == null) {
            throw new IllegalStateException("row() before section()");
        }
        return mCurrentSection;
    }
}
