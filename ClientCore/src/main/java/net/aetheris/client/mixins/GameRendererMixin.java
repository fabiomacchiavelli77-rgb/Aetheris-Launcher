package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.NoHurtCam;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * NoHurtCam — disabilita l'effetto di danno (bob della visuale).
     */
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof NoHurtCam && mod.isEnabled()) {
                ci.cancel();
                return;
            }
        }
    }
}
