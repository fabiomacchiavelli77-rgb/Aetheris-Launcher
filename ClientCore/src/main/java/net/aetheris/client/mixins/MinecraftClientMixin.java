package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Reach;
import net.aetheris.client.modules.impl.world.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @org.spongepowered.asm.mixin.Shadow
    private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof net.aetheris.client.modules.impl.player.FastPlace fp && fp.isEnabled()) {
                rightClickDelay = fp.getDelay();
            }
        }
        ModuleManager.onTick();
    }



    @Inject(method = "getTickTargetMillis", at = @At("HEAD"), cancellable = true)
    private void onGetTickTargetMillis(float f, CallbackInfoReturnable<Float> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Timer timer && timer.isEnabled()) {
                float normal = f;
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.level.tickRateManager().runsNormally()) {
                    normal = Math.max(f, mc.level.tickRateManager().millisecondsPerTick());
                }
                cir.setReturnValue(normal / timer.getTimerSpeed());
                return;
            }
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Reach reach && reach.isEnabled()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.gameMode != null) {
                    Entity target = reach.getTargetInLookVector(reach.getReachDistance());
                    if (target != null) {
                        mc.gameMode.attack(mc.player, target);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof net.aetheris.client.modules.impl.render.ESP esp && esp.isEnabled()) {
                if (esp.shouldGlow(entity)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}
