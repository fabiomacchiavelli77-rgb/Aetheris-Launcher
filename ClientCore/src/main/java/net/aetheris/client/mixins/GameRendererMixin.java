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
     * NoHurtCam - disabilita l'effetto di danno (bob della visuale).
     */
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        NoHurtCam noHurtCam = ModuleManager.getModule(NoHurtCam.class);
        if (noHurtCam != null && noHurtCam.isEnabled()) {
            ci.cancel();
        }
    }

    /**
     * Prevents crash when mc.player is null but level is not null.
     */
    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void onRenderLevel(net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null && mc.getCameraEntity() == null) {
            ci.cancel();
        }
    }
}
