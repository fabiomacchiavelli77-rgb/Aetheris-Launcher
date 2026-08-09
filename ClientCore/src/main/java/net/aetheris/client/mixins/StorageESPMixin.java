package net.aetheris.client.mixins;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.StorageESP;
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
public class StorageESPMixin {

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void onRenderLevel(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker,
                               boolean arg3, Camera camera, Matrix4f arg5, Matrix4f arg6, Matrix4f arg7,
                               com.mojang.blaze3d.buffers.GpuBufferSlice arg8, org.joml.Vector4f arg9, boolean arg10, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof StorageESP se && se.isEnabled()) {
                se.render(camera, deltaTracker);
                return;
            }
        }
    }
}
