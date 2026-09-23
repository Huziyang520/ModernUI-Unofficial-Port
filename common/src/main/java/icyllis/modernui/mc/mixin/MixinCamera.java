/*
 * Modern UI.
 * Copyright (C) 2019-2024 BloCamLimb. All rights reserved.
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

package icyllis.modernui.mc.mixin;

import icyllis.modernui.mc.UIManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applies the C-key zoom to the world projection FOV, once per frame.
 * <p>
 * {@code Camera.setupPerspective} bakes the FOV into
 * {@code Projection.setupPerspective(zNear, zFar, fov, width, height)} every frame, and the third
 * float is the FOV ({@code javap} verified on both 26.2 and 26.3). This is the world projection
 * only; {@code Camera.getFov()} is not used by it.
 * <p>
 * Both loaders register this mixin, so the zoom is implemented exactly once for both of them.
 */
@Mixin(Camera.class)
public class MixinCamera {

    @ModifyArg(method = "setupPerspective",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V"),
            index = 2)
    private float modernui$applyZoom(float fov) {
        return UIManager.applyZoomFrame(fov);
    }
}
