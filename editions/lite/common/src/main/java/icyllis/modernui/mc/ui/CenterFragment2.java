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
import icyllis.modernui.mc.ModernUIMod;
import icyllis.modernui.mc.ui.lite.YaclBridge;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import net.minecraft.client.Minecraft;

/**
 * Stand-in for the entry points that still construct this fragment: the lite edition has
 * no screen of its own, so it forwards straight to the config library.
 */
public class CenterFragment2 extends Fragment {

    @Override
    public void onCreate(@Nullable DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The screen swap has to happen on the client thread, not on the UI thread.
        Minecraft.getInstance().execute(() -> {
            var minecraft = Minecraft.getInstance();
            var screen = YaclBridge.open(null);
            if (screen != null) {
                minecraft.setScreenAndShow(screen);
                return;
            }
            // A blank page would look like a broken screen, so try to drop it.
            try {
                minecraft.setScreenAndShow(null);
            } catch (Throwable t) {
                ModernUIMod.LOGGER.warn("Cannot close the empty page", t);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        return new FrameLayout(requireContext());
    }
}