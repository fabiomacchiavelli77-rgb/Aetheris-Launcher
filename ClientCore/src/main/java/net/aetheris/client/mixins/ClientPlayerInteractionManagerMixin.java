package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Criticals;
import net.aetheris.client.modules.impl.world.FastBreak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
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
     * Criticals — impone colpi critici all'attacco.
     */
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Player player, Entity target, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Criticals crit && crit.isEnabled()) {
                if (crit.shouldForceCritical(target)) {
                    Minecraft mc = Minecraft.getInstance();
                    if (player.onGround() && mc.getConnection() != null) {
                        double x = player.getX();
                        double y = player.getY();
                        double z = player.getZ();

                        switch (crit.getMode()) {
                            case PACKET -> {
                                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
                                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
                                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.011, z, false, false));
                                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
                            }
                            case JUMP -> player.jumpFromGround();
                            case MINI_JUMP -> player.setDeltaMovement(player.getDeltaMovement().x, 0.25, player.getDeltaMovement().z);
                        }
                    }

                    if (mc.particleEngine != null) {
                        mc.particleEngine.createTrackingEmitter(target, ParticleTypes.CRIT);
                        mc.particleEngine.createTrackingEmitter(target, ParticleTypes.ENCHANTED_HIT);
                    }
                }
            }
        }
    }
}
