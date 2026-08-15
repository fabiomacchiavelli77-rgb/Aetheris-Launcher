package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.movement.NoClip;
import net.aetheris.client.modules.impl.movement.NoSlowdown;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    // NoSlowdown - Impedisce al client di accorgersi che stiamo usando un oggetto (mangiando, arco, ecc)
    // così non applica il moltiplicatore x0.2 alla velocità in LocalPlayer.aiStep()
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        NoSlowdown noSlowdown = ModuleManager.getModule(NoSlowdown.class);
        if (noSlowdown != null && noSlowdown.isEnabled() && noSlowdown.getItems()) {
            cir.setReturnValue(false);
        }
    }

    // NoClip - Mantiene noPhysics = true durante aiStep per consentire di attraversare tutti i blocchi senza collisioni
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStepHead(CallbackInfo ci) {
        NoClip noClip = ModuleManager.getModule(NoClip.class);
        if (noClip != null && noClip.isEnabled()) {
            ((LocalPlayer) (Object) this).noPhysics = true;
        }
    }
}
