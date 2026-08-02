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
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Xray xray && xray.isEnabled()) {
                if (Xray.isXrayBlock(this.getBlock())) {
                    cir.setReturnValue(false); // Mai saltare le facce dei blocchi minerale visibile
                } else {
                    // If opacity > 0, don't skip (render dimmed), else skip completely
                    cir.setReturnValue(Xray.getOpacity() == 0);
                }
                return;
            }
        }
    }

    @Inject(method = "canOcclude", at = @At("HEAD"), cancellable = true)
    private void onCanOcclude(CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Xray xray && xray.isEnabled()) {
                cir.setReturnValue(false);
                return;
            }
        }
    }

    @Inject(method = "getShadeBrightness", at = @At("HEAD"), cancellable = true)
    private void onGetShadeBrightness(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Xray xray && xray.isEnabled()) {
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
            if (mod instanceof FullBright fb && fb.isEnabled()) {
                cir.setReturnValue(1.0f); // 100% luminosità uniforme senza ombre scure sotterranee
                return;
            }
        }
    }

    @Inject(method = "getLightBlock", at = @At("HEAD"), cancellable = true)
    private void onGetLightBlock(CallbackInfoReturnable<Integer> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Xray xray && xray.isEnabled()) {
                cir.setReturnValue(0); // I blocchi non assorbono luce in modalità Xray
                return;
            }
        }
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context, CallbackInfoReturnable<net.minecraft.world.phys.shapes.VoxelShape> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof net.aetheris.client.modules.impl.movement.NoClip nc && nc.isEnabled()) {
                cir.setReturnValue(net.minecraft.world.phys.shapes.Shapes.empty());
                return;
            }
        }
    }
}
