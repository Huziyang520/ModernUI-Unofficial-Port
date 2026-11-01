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

/**
 * One anchor of the index rail. The shell needs the group id to scroll to the matching
 * group, the label to draw it and the glyph to draw it as an icon; the page itself only
 * carries the same three values inside {@link SettingsPage.Group}.
 *
 * @param groupId  matches {@link SettingsPage.Group#id}
 * @param titleKey translation key of the rail label
 * @param glyph    one of {@code icyllis.modernui.mc.ui.v2.skin.Skin.GLYPH_*}
 */
public record PageMeta(@NonNull String groupId, @NonNull String titleKey, int glyph) {
}
