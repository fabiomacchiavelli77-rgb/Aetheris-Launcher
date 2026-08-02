package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.world.Timer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ModuleManager.onTick();
    }

    @Inject(method = "getDeltaTracker", at = @At("RETURN"), cancellable = true)
    private void onGetFrameTime(CallbackInfoReturnable<Float> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Timer timer && timer.isEnabled()) {
                cir.setReturnValue(cir.getReturnValue() * timer.getTimerSpeed());
            }
        }
    }
}
