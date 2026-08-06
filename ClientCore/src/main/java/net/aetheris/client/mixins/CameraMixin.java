package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.CameraClip;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "getMaxZoom(F)F", at = @At("HEAD"), cancellable = true)
    private void onGetMaxZoom(float startingDistance, CallbackInfoReturnable<Float> cir) {
        ModuleManager.getModule("CameraClip").ifPresent(mod -> {
            if (mod instanceof CameraClip cameraClip && cameraClip.isEnabled()) {
                cir.setReturnValue(cameraClip.getDistance());
            }
        });
    }
}
