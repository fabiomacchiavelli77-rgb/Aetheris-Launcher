package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Reach;
import net.aetheris.client.modules.impl.render.NoHurtCam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * NoHurtCam — disabilita l'effetto di danno (bob della visuale).
     */
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof NoHurtCam && mod.isEnabled()) {
                ci.cancel();
                return;
            }
        }
    }

    /**
     * Reach & TP-Reach — Estende il Raycast visivo del mirino (crosshairTarget / entity picking)
     * fino alla portata impostata nel modulo Reach (es. 15 blocchi), consentendo di evidenziare
     * ed agganciare col click sinistro le entità lontane.
     */
    @Inject(method = "pick", at = @At("RETURN"))
    private void onPick(float partialTick, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Reach reach && reach.isEnabled()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.level == null) return;

                double reachDist = reach.getReachDistance();
                Vec3 eyePos = mc.player.getEyePosition(partialTick);
                Vec3 viewVec = mc.player.getViewVector(partialTick);
                Vec3 reachEnd = eyePos.add(viewVec.scale(reachDist));

                AABB searchBox = mc.player.getBoundingBox().expandTowards(viewVec.scale(reachDist)).inflate(2.0);

                EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                        mc.player,
                        eyePos,
                        reachEnd,
                        searchBox,
                        e -> e.isAlive() && !e.isSpectator(),
                        reachDist * reachDist
                );

                if (entityHit != null) {
                    mc.hitResult = entityHit;
                    mc.crosshairPickEntity = entityHit.getEntity();
                }
            }
        }
    }
}
