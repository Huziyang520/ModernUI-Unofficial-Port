/*
 * Modern UI Lite.
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
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.fragment.FragmentContainerView;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * The lite build only has an info page: no navigation rail, no pages, no settings entries.
 * The configuration belongs to the config library and is opened from the mod list directly.
 */
public class CenterFragment2 extends Fragment {

    /** Referenced by the info page for its log output. */
    public static final Marker MARKER = MarkerManager.getMarker("Center");

    private static final int id_content = 0x2002;

    @Override
    public void onCreate(@Nullable DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .replace(id_content, DashboardFragment.class, null, "dashboard")
                .setReorderingAllowed(true)
                .commit();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        var content = new FragmentContainerView(requireContext());
        content.setId(id_content);
        content.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return content;
    }
}