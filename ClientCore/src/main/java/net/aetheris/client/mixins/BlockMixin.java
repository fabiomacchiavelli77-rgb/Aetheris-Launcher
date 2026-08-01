package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.Xray;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void onShouldRenderFace(BlockState state, BlockState neighborState, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof Xray xray && xray.isEnabled()) {
                if (Xray.isXrayBlock(state.getBlock())) {
                    cir.setReturnValue(true); // Always render xray ore faces
                } else {
                    // If opacity > 0, still render the face (dimmed via shade brightness)
                    // If opacity == 0, hide it completely
                    cir.setReturnValue(Xray.getOpacity() > 0);
                }
                return;
            }
        }
    }
}
