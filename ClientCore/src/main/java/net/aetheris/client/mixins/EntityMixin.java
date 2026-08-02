package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Velocity;
import net.aetheris.client.modules.impl.movement.NoClip;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * NoClip — assicura che noPhysics sia true all'inizio di Entity.move()
     * per evitare che il motore di fisica controlli le collisioni dei blocchi.
     */
    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MoverType moverType, Vec3 vec3, CallbackInfo ci) {
        if ((Object) this instanceof net.minecraft.world.entity.player.Player) {
            for (var mod : ModuleManager.getModules()) {
                if (mod instanceof NoClip nc && nc.isEnabled()) {
                    ((Entity) (Object) this).noPhysics = true;
                }
            }
        }
    }

    /**
     * Previene i danni da soffocamento (inWall) e l'oscuramento della vista quando si è dentro un blocco.
     */
    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void onIsInWall(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof net.minecraft.world.entity.player.Player) {
            for (var mod : ModuleManager.getModules()) {
                if (mod instanceof NoClip nc && nc.isEnabled()) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }

    /**
     * Velocity — cancella la spinta da collisione con altre entità.
     */
    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true)
    private void onPush(double x, double y, double z, CallbackInfo ci) {
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
