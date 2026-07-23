package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Velocity;
import net.aetheris.client.modules.impl.movement.NoFall;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {



    /**
     * NoFall — azzera distanza di caduta e invia pacchetti onGround.
     */
    @Inject(method = "sendPosition()V", at = @At("HEAD"))
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
