package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.FullBright;
import net.aetheris.client.modules.impl.render.Xray;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow public abstract Block getBlock();

    @Inject(method = "skipRendering", at = @At("HEAD"), cancellable = true)
    private void onSkipRendering(BlockState neighborState, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Xray xray = ModuleManager.getModule(Xray.class);
        if (xray != null && xray.isEnabled()) {
            if (Xray.isXrayBlock(this.getBlock())) {
                cir.setReturnValue(false); // Mai saltare le facce dei blocchi minerale visibile
            } else {
                // If opacity > 0, don't skip (render dimmed), else skip completely
                cir.setReturnValue(Xray.getOpacity() == 0);
            }
        }
    }

    @Inject(method = "canOcclude", at = @At("HEAD"), cancellable = true)
    private void onCanOcclude(CallbackInfoReturnable<Boolean> cir) {
        Xray xray = ModuleManager.getModule(Xray.class);
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getShadeBrightness", at = @At("HEAD"), cancellable = true)
    private void onGetShadeBrightness(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Xray xray = ModuleManager.getModule(Xray.class);
        if (xray != null && xray.isEnabled()) {
            if (Xray.isXrayBlock(this.getBlock())) {
                // Xray blocks: full brightness so they stand out
                cir.setReturnValue(1.0f);
            } else {
                // Non-xray blocks: use opacity to dim them
                // opacity 0 = hidden (handled by renderBatched cancel)
                // opacity 1-100 = dim to full brightness
                cir.setReturnValue(Xray.getOpacityFactor() * 0.4f); // max 40% brightness so ores always stand out
            }
            return;
        }
        FullBright fullBright = ModuleManager.getModule(FullBright.class);
        if (fullBright != null && fullBright.isEnabled()) {
            cir.setReturnValue(1.0f); // 100% luminosità uniforme senza ombre scure sotterranee
        }
    }

    @Inject(method = "getLightBlock", at = @At("HEAD"), cancellable = true)
    private void onGetLightBlock(CallbackInfoReturnable<Integer> cir) {
        Xray xray = ModuleManager.getModule(Xray.class);
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(0); // I blocchi non assorbono luce in modalità Xray
        }
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.phys.shapes.VoxelShape> cir) {
        net.aetheris.client.modules.impl.movement.NoClip noClip =
                ModuleManager.getModule(net.aetheris.client.modules.impl.movement.NoClip.class);
        net.aetheris.client.modules.impl.render.FreeCam freeCam =
                ModuleManager.getModule(net.aetheris.client.modules.impl.render.FreeCam.class);
        if ((noClip != null && noClip.isEnabled()) || (freeCam != null && freeCam.isEnabled())) {
            cir.setReturnValue(net.minecraft.world.phys.shapes.Shapes.empty());
        }
    }
}
