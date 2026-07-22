package kaptainwutax.seedcrackerX.finder;

import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.cracker.DataAddedEvent;
import kaptainwutax.seedcrackerX.cracker.SlimeChunkData;
import kaptainwutax.seedcrackerX.render.Cuboid;
import kaptainwutax.seedcrackerX.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects slime chunk observations for seed cracking.
 * Slime chunks are determined by the world seed's RNG.
 * Observing ~15-18 slime chunks can uniquely identify the world seed
 * via ChunkRandomReversal, without needing any structures.
 */
public class SlimeChunkFinder extends Finder {

    public SlimeChunkFinder(Level world, ChunkPos chunkPos) {
        super(world, chunkPos);
    }

    public static List<Finder> create(Level world, ChunkPos chunkPos) {
        List<Finder> finders = new ArrayList<>();
        finders.add(new SlimeChunkFinder(world, chunkPos));
        return finders;
    }

    /**
     * Called externally when a slime actually spawns (via Mixin).
     * This provides definitive proof that a chunk is a slime chunk.
     */
    public static void onSlimeSpawn(Level world, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        SlimeChunkData data = new SlimeChunkData(chunkX, chunkZ, true);
        if (SeedCracker.get().getDataStorage().addSlimeChunkData(data,
                DataAddedEvent.POKE_STRUCTURES)) {
            if (Config.get().debug) {
                Log.warn("Slime chunk confirmed at " + chunkX + ", " + chunkZ);
            }
        }
    }

    @Override
    public List<BlockPos> findInChunk() {
        List<BlockPos> result = new ArrayList<>();

        // Slime chunks are determined by:
        // new Random(seed + chunkX * 341873128712L + chunkZ * 132897987541L + ...).nextInt(10) == 0
        // We cannot check this without the seed, so we rely on actual spawn
        // observations via the Mixin. This finder marks the chunk for tracking.

        BlockPos centerPos = this.chunkPos.getWorldPosition().offset(8, 0, 8);
        result.add(centerPos);
        result.forEach(pos -> this.cuboids.add(new Cuboid(pos, ARGB.color(128, 179, 255))));

        return result;
    }

    @Override
    public boolean isValidDimension(DimensionType dimension) {
        return this.isOverworld(dimension);
    }
}
