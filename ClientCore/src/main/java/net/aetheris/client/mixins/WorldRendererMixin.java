package net.aetheris.client.mixins;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.ESP;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "collectVisibleEntities", at = @At("RETURN"))
    private void onCollectVisibleEntities(Camera camera, net.minecraft.client.renderer.culling.Frustum frustum, java.util.List<net.minecraft.world.entity.Entity> list, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof net.aetheris.client.modules.impl.render.FreeCam fc && fc.isEnabled()) {
                net.minecraft.world.entity.Entity dummy = fc.getDummyEntity();
                if (dummy != null && !list.contains(dummy)) {
                    list.add(dummy);
                }
            }
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void onRenderLevel(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker,
                               boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                               Matrix4f projectionMatrix, Matrix4f projectionMatrix2, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod.isEnabled() && mod instanceof net.aetheris.client.modules.impl.render.Trajectories trajectories) {
                trajectories.render(camera, deltaTracker);
            }
        }
    }
}
