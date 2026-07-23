package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Criticals;
import net.aetheris.client.modules.impl.combat.Reach;
import net.aetheris.client.modules.impl.world.FastBreak;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow
    private float destroyProgress;

    /**
     * Reach — estende la distanza di interazione.
     */
    @Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
    private void onGetPickRange(CallbackInfoReturnable<Float> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Reach r && r.isEnabled()) {
                cir.setReturnValue(r.getReachDistance());
            }
        }
    }

    /**
     * FastBreak — accelera il progresso di rottura blocchi.
     */
    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof FastBreak fb && fb.isEnabled()) {
                if (destroyProgress > 0f) {
                    float extra = fb.getSpeedMultiplier() * 0.015f;
                    destroyProgress += extra;
                    if (destroyProgress >= 1.0f) {
                        destroyProgress = 1.0f;
                    }
                }
            }
        }
    }

    /**
     * Criticals — mini-salto prima di attaccare per forzare colpo critico.
     */
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Player player, Entity target, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Criticals crit && crit.isEnabled()) {
                if (crit.shouldForceCritical(target)) {
                    if (player.onGround()) {
                        player.push(0, 0.11, 0);
                    }
                }
            }
        }
    }
}
