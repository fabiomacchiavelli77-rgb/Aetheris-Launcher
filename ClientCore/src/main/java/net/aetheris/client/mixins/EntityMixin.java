package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Velocity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * Velocity — cancella knockback dal server.
     */
    @Inject(method = "setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    private void onSetDeltaMovement(net.minecraft.world.phys.Vec3 deltaMovement, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player) {
            for (var mod : ModuleManager.getModules()) {
                if (mod instanceof Velocity && mod.isEnabled()) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
