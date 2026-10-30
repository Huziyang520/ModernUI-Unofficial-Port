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

package icyllis.modernui.mc.ui.studio;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.view.View;

/**
 * Minimal time based animator driven by the frame callback. Avoids property animators for
 * floats that are not view properties.
 */
public final class StudioAnim {

    public interface Step {
        /** @param t progress in [0, 1] */
        void onStep(float t);
    }

    private StudioAnim() {
    }

    public static void run(@NonNull View host, long durationMs, @NonNull Step step) {
        if (durationMs <= 0) {
            step.onStep(1f);
            host.invalidate();
            return;
        }
        final long start = System.nanoTime();
        final long duration = durationMs * 1_000_000L;
        host.postOnAnimation(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.nanoTime() - start;
                float t = Math.min(1f, elapsed / (float) duration);
                // ease-out cubic keeps the motion smooth without an interpolator dependency
                float eased = 1f - (1f - t) * (1f - t) * (1f - t);
                step.onStep(eased);
                host.invalidate();
                if (t < 1f) {
                    host.postOnAnimation(this);
                }
            }
        });
    }
}
