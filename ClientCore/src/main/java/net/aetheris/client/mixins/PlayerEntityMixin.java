package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.player.NoHunger;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {

    @Shadow
    protected FoodData foodData;

    /**
     * NoHunger — blocca il consumo di fame.
     */
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void onCauseFoodExhaustion(float exhaustion, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof NoHunger && mod.isEnabled()) {
                ci.cancel();
                return;
            }
        }
    }
}
