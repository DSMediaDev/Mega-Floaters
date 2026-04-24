package gg.dsmedia.megafloaters.structure;

import gg.dsmedia.megafloaters.structure.shape.MegaShape;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;

/**
 * Per-mega-island parameters. Pieces persist only the {@code seed} and the
 * anchor {@link ChunkPos}; everything else is deterministically re-derived
 * via {@link #fromSeed} so the same params reconstruct on every world load
 * regardless of which piece's postProcess runs first.
 */
public record MegaIslandParams(long seed, ChunkPos anchor, BlockPos center,
                               int radius, int thickness, MegaShape shape, long shapeSeed) {

    /** Mega islands are an order of magnitude larger than satellite floaters. */
    public static final int MIN_RADIUS    = 60;
    public static final int MAX_RADIUS    = 100;
    public static final int MIN_THICKNESS = 28;
    public static final int MAX_THICKNESS = 56;

    /** Top of the island sits in this band — keeps mega islands clear of mountain peaks. */
    public static final int MIN_ALTITUDE  = 240;
    public static final int MAX_ALTITUDE  = 290;

    public static MegaIslandParams fromSeed(long seed, ChunkPos anchor) {
        RandomSource rng = RandomSource.create(seed);
        int centerX = anchor.getMinBlockX() + rng.nextInt(16);
        int centerZ = anchor.getMinBlockZ() + rng.nextInt(16);
        int centerY = MIN_ALTITUDE + rng.nextInt(MAX_ALTITUDE - MIN_ALTITUDE + 1);
        int radius = MIN_RADIUS + rng.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int thickness = MIN_THICKNESS + rng.nextInt(MAX_THICKNESS - MIN_THICKNESS + 1);
        MegaShape shape = MegaShape.pick(rng);
        long shapeSeed = rng.nextLong();
        return new MegaIslandParams(seed, anchor, new BlockPos(centerX, centerY, centerZ),
                radius, thickness, shape, shapeSeed);
    }

    /**
     * XZ extent of the island's footprint in blocks from the center.
     * Shapes that are non-circular (ridge) report their longer axis here.
     */
    public int xzExtent() {
        return shape.xzExtent(radius);
    }

    /** Shallow check used by the structure to decide whether a chunk holds any blocks. */
    public boolean affectsChunk(ChunkPos chunkPos) {
        int ext = xzExtent();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxZ = chunkMinZ + 15;
        return chunkMaxX >= center.getX() - ext && chunkMinX <= center.getX() + ext
            && chunkMaxZ >= center.getZ() - ext && chunkMinZ <= center.getZ() + ext;
    }
}
