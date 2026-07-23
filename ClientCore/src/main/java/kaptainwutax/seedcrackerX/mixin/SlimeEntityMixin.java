package kaptainwutax.seedcrackerX.mixin;

import kaptainwutax.seedcrackerX.finder.SlimeChunkFinder;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to detect natural slime spawns and record them as slime chunk data.
 * When a slime passes its spawn rules check, the chunk is confirmed
 * as a slime chunk.
 */
@Mixin(Slime.class)
public class SlimeEntityMixin {

    @Inject(method = "checkSlimeSpawnRules", at = @At("RETURN"))
    private static void onCheckSlimeSpawnRules(
            Slime slime, LevelAccessor level, BlockPos pos,
            RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && level instanceof net.minecraft.world.level.Level world) {
            SlimeChunkFinder.onSlimeSpawn(world, pos);
        }
    }
}
