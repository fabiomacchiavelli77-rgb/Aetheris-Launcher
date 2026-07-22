package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.ESP;
import net.aetheris.client.modules.impl.render.Xray;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lnet/minecraft/client/Camera;)V"))
    private void afterRenderEntities(DeltaTracker deltaTracker, boolean renderBlockOutline,
                                     Camera camera, GameRenderer gameRenderer,
                                     LightTexture lightTexture, Matrix4f matrix4f,
                                     Matrix4f matrix4f2, CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof ESP esp && esp.isEnabled()) {
                esp.renderESP(matrix4f, camera);
            }
        }
    }
}
