package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Velocity;
import net.aetheris.client.modules.impl.movement.NoFall;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    /**
     * Velocity — cancella knockback dal server.
     * LocalPlayer riceve velocità dal server tramite i metodi di Entity.
     * Intercettiamo setDeltaMovement per annullare il knockback.
     */
    @Inject(method = "lerpMotion", at = @At("HEAD"), cancellable = true)
    private void onLerpMotion(double x, double y, double z, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Velocity && mod.isEnabled()) {
                ci.cancel();
                return;
            }
        }
    }

    /**
     * NoFall — azzera distanza di caduta e invia pacchetti onGround.
     */
    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void onSendPosition(CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof NoFall nf && nf.isEnabled()) {
                LocalPlayer self = (LocalPlayer) (Object) this;
                if (self.fallDistance > 2.5f) {
                    self.fallDistance = 0f;
                    // Il mixin forza onGround=true prima dell'invio
                }
            }
        }
    }
}
