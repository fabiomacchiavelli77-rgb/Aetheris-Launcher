package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Criticals;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /**
     * Criticals — forza critico quando il player è a terra facendo credere
     * al server che sia in volo (onGround = false).
     */
    @Inject(method = "onGround", at = @At("RETURN"), cancellable = true)
    private void onIsOnGround(CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Criticals && mod.isEnabled()) {
                if ((Object) this instanceof net.minecraft.client.player.LocalPlayer) {
                    // Lo spoof viene gestito dall'attacco in MultiPlayerGameMode
                }
            }
        }
    }
}
