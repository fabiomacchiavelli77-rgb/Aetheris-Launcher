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
                    cir.setReturnValue(true);  // Salta/Nascondi tutti i blocchi non Xray
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
            if ((mod instanceof Xray xray && xray.isEnabled()) || (mod instanceof FullBright fb && fb.isEnabled())) {
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
}
