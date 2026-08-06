package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.world.AirPlace;
import net.aetheris.client.modules.impl.world.LiquidInteract;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void onCanPlace(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof LiquidInteract li && li.isEnabled()) {
                cir.setReturnValue(true);
                return;
            }
            if (mod instanceof AirPlace ap && ap.isEnabled()) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
