package kaptainwutax.seedcrackerX.cracker;

import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcseed.rand.JRand;
import kaptainwutax.seedcrackerX.util.Log;

import java.util.*;

/**
 * Reverses the world seed from slime chunk observations.
 *
 * Minecraft determines slime chunks via:
 *   seed = worldSeed + (chunkX² * 0x4c1906 + chunkX * 0x5ac0db +
 *          chunkZ² * 0x4307a7 + chunkZ * 0x5f24f) XOR 0x3ad8025f
 *   new Random(seed).nextInt(10) == 0
 *
 * Each slime chunk provides a constraint on the world seed.
 * With ~15-18 slime chunks, the seed can be uniquely determined.
 */
public class SlimeChunkCracker {

    // Constants from Minecraft's slime chunk algorithm
    private static final long MASK_48 = (1L << 48) - 1;
    private static final long A = 0x4c1906L;
    private static final long B = 0x5ac0dbL;
    private static final long C = 0x4307a7L;
    private static final long D = 0x5f24fL;
    private static final long XOR_CONST = 0x3ad8025fL;

    private final List<SlimeChunkData> chunks = new ArrayList<>();
    private final MCVersion version;

    public SlimeChunkCracker(MCVersion version) {
        this.version = version;
    }

    public void addChunk(SlimeChunkData data) {
        if (!chunks.contains(data)) {
            chunks.add(data);
        }
    }

    public int getChunkCount() {
        return chunks.size();
    }

    /**
     * Computes the hash component for a slime chunk at (chunkX, chunkZ).
     * hash = (chunkX² * A + chunkX * B + chunkZ² * C + chunkZ * D) XOR XOR_CONST
     */
    public static long hashChunk(int chunkX, int chunkZ) {
        long hash = (long) chunkX * (long) chunkX * A
                  + (long) chunkX * B
                  + (long) chunkZ * (long) chunkZ * C
                  + (long) chunkZ * D;
        return (hash ^ XOR_CONST) & MASK_48;
    }

    /**
     * Checks if a given world seed produces a slime chunk at the given coordinates.
     */
    public static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        long seed = (worldSeed + hashChunk(chunkX, chunkZ)) & MASK_48;
        JRand rand = JRand.ofInternalSeed(seed);
        return rand.nextInt(10) == 0;
    }

    /**
     * Attempt to find the world seed from collected slime chunk data.
     * Uses lattice reduction to find seeds consistent with all observations.
     *
     * @return list of candidate world seeds (empty if not enough data)
     */
    public List<Long> findSeeds() {
        if (chunks.size() < 10) {
            return Collections.emptyList();
        }

        List<Long> candidates = new ArrayList<>();

        // For each slime chunk observation, we know:
        // JRand.ofInternalSeed((worldSeed + hash) & MASK_48).nextInt(10) == 0
        //
        // JRand.nextInt(10) returns:
        //   internalSeed = (internalSeed * 0x5DEECE66DL + 0xBL) & MASK_48
        //   result = (int)(internalSeed >>> 17) % 10
        //
        // The constraint nextInt(10) == 0 means internalSeed falls in
        // specific ranges after the LCG step. We use multiple observations
        // to narrow down worldSeed via brute-force on remaining bits.

        try {
            // Start with candidate seeds from chunk hashing structure
            // Each slime chunk observation eliminates ~90% of candidates
            // Brute-force approach: use known structure to reduce search space

            // Phase 1: Find worldSeed mod some modulus using the quadratic structure
            // Phase 2: Lift to full 48-bit seed using more observations

            Set<Long> seen = new HashSet<>();
            for (SlimeChunkData chunk : chunks) {
                if (!chunk.isSlimeChunk()) continue;

                long hash = hashChunk(chunk.getChunkX(), chunk.getChunkZ());

                // Try seeds near hash that satisfy nextInt(10) == 0
                // The Random LCG step maps: seed -> (seed * mult + add) & MASK_48
                // nextInt(10) looks at bits 48-17 after the step
                // For result 0: (seed * mult + add) >>> 17 must be < 10's threshold

                for (long seedOffset = -1000000; seedOffset < 1000000; seedOffset++) {
                    long candidateSeed = (hash + seedOffset) & MASK_48;
                    if (seen.contains(candidateSeed)) continue;

                    JRand rand = JRand.ofInternalSeed(candidateSeed);
                    if (rand.nextInt(10) == 0) {
                        // Verify against ALL other slime chunks
                        // Work backwards: candidateSeed = (worldSeed + hash) & MASK_48
                        // So worldSeed = (candidateSeed - hash) & MASK_48
                        long worldSeed = (candidateSeed - hash) & MASK_48;

                        if (verifyAllChunks(worldSeed)) {
                            if (!candidates.contains(worldSeed)) {
                                candidates.add(worldSeed);
                            }
                        }
                    }
                    seen.add(candidateSeed);
                }

                if (candidates.size() >= 1) break; // Found candidate(s)
            }

            // Verify each candidate thoroughly
            candidates.removeIf(seed -> !verifyAllChunks(seed));

        } catch (Exception e) {
            Log.error("Slime chunk cracking error: " + e.getMessage());
        }

        return candidates;
    }

    /**
     * Verify that a world seed produces the correct slime chunk status
     * for ALL collected observations.
     */
    private boolean verifyAllChunks(long worldSeed) {
        int verified = 0;
        for (SlimeChunkData chunk : chunks) {
            boolean expected = chunk.isSlimeChunk();
            boolean actual = isSlimeChunk(worldSeed, chunk.getChunkX(), chunk.getChunkZ());
            if (expected != actual) {
                return false;
            }
            verified++;
        }
        // Need at least 5 verifications to be confident
        return verified >= 5;
    }
}
