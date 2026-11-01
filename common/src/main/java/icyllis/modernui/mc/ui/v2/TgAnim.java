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

package icyllis.modernui.mc.ui.v2;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.view.View;

/**
 * Frame driven animator. Custom floats are not view properties, so property animators
 * cannot drive them; this walks the animation callback instead.
 */
public final class TgAnim {

    public interface Step {
        /**
         * @param t eased progress in [0, 1]
         */
        void onStep(float t);
    }

    public interface Easing {
        float ease(float t);
    }

    public static final Easing LINEAR = t -> t;
    public static final Easing OUT_CUBIC = t -> 1f - (1f - t) * (1f - t) * (1f - t);
    public static final Easing IN_OUT_CUBIC = t -> t < 0.5f
            ? 4f * t * t * t
            : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    public static final Easing OUT_BACK = t -> {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (t - 1f) * (t - 1f) * (t - 1f) + c1 * (t - 1f) * (t - 1f);
    };

    private TgAnim() {
    }

    public static void run(@NonNull View host, long durationMs, @NonNull Easing easing,
                           @NonNull Step step) {
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
                float raw = Math.min(1f, elapsed / (float) duration);
                step.onStep(easing.ease(raw));
                host.invalidate();
                if (raw < 1f) {
                    host.postOnAnimation(this);
                }
            }
        });
    }

    public static void run(@NonNull View host, long durationMs, @NonNull Step step) {
        run(host, durationMs, OUT_CUBIC, step);
    }
}
