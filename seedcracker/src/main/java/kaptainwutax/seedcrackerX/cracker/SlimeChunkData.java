package kaptainwutax.seedcrackerX.cracker;

import java.util.Objects;

/**
 * Stores a slime chunk observation for seed cracking.
 * Each slime chunk (or non-slime chunk) observation constrains
 * the possible world seeds via the Java Random chunk hashing.
 */
public class SlimeChunkData {

    private final int chunkX;
    private final int chunkZ;
    private final boolean isSlimeChunk;

    public SlimeChunkData(int chunkX, int chunkZ, boolean isSlimeChunk) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.isSlimeChunk = isSlimeChunk;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public boolean isSlimeChunk() {
        return isSlimeChunk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlimeChunkData)) return false;
        SlimeChunkData that = (SlimeChunkData) o;
        return chunkX == that.chunkX && chunkZ == that.chunkZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return "SlimeChunk[" + chunkX + ", " + chunkZ + "]=" + isSlimeChunk;
    }
}
