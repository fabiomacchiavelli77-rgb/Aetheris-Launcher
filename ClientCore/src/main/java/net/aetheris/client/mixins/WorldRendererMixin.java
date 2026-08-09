package net.aetheris.client.mixins;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    protected abstract net.minecraft.client.renderer.entity.state.EntityRenderState extractEntity(Entity entity, float partialTick);

    @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
    private void onExtractVisibleEntities(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState state, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof net.aetheris.client.modules.impl.render.FreeCam fc && fc.isEnabled()) {
                Entity dummy = fc.getDummyEntity();
                if (dummy != null) {
                    float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
                    state.entityRenderStates.add(extractEntity(dummy, partialTick));
                }
            }
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void onRenderLevel(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker,
                               boolean arg3, Camera camera, Matrix4f arg5, Matrix4f arg6, Matrix4f arg7,
                               com.mojang.blaze3d.buffers.GpuBufferSlice arg8, org.joml.Vector4f arg9, boolean arg10, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod.isEnabled() && mod instanceof net.aetheris.client.modules.impl.render.Trajectories trajectories) {
                trajectories.render(camera, deltaTracker);
            }
        }
    }
}
