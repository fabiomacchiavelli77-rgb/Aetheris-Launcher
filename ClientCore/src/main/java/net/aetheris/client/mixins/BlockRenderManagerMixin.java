package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.Xray;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderManagerMixin {

    @Inject(method = "renderBatched", at = @At("HEAD"), cancellable = true)
    private void onRenderBatched(BlockState state, BlockPos pos, BlockAndTintGetter level,
                                  PoseStack poseStack, VertexConsumer consumer,
                                  boolean checkSides, java.util.List<?> someList, CallbackInfo ci) {
        Xray xray = ModuleManager.getModule(Xray.class);
        if (xray != null && xray.isEnabled()) {
            if (!Xray.isXrayBlock(state.getBlock())) {
                // If opacity is 0, hide the block completely (original behavior)
                if (Xray.getOpacity() == 0) {
                    ci.cancel();
                    return;
                }
            }
            // If opacity > 0, let it render — brightness is controlled
            // via BlockStateBaseMixin.getShadeBrightness using the opacity factor
        }
    }
}
