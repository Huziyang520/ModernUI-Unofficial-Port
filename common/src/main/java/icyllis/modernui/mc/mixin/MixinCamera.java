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
