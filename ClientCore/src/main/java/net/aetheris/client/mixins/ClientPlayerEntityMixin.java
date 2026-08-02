package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.movement.NoFall;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    /**
     * NoFall — Inietta all'inizio di sendPosition() per forzare onGround=true
     * prima che il metodo costruisca e invii i pacchetti di movimento al server.
     * Questo è il punto esatto dove LocalPlayer decide il valore di onGround
     * per i ServerboundMovePlayerPacket (PosRot, Pos, StatusOnly).
     */
    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void onSendPositionNoFall(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof NoFall nf && nf.isEnabled()) {
                if (!self.isCreative() && !self.isSpectator()) {
                    if (self.fallDistance > nf.getFallThreshold()) {
                        self.setOnGround(true);
                        self.fallDistance = 0f;
                    }
                }
            }
        }
    }
}
