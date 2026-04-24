package gg.dsmedia.megafloaters.structure;

import gg.dsmedia.megafloaters.ModRegistries;
import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.api.palette.SurfacePaletteRegistry;
import gg.dsmedia.megafloaters.integration.AeronauticsCompat;
import gg.dsmedia.megafloaters.structure.shape.ColumnSpec;
import gg.dsmedia.megafloaters.structure.shape.MegaShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * One chunk-aligned slice of a mega island. Holds only the structure {@code seed}
 * and {@code anchor} chunk; everything else (center, radius, thickness, shape)
 * is re-derived via {@link MegaIslandParams#fromSeed} in postProcess. All pieces
 * of the same island arrive at identical params without any cross-piece lookup.
 */
public class MegaIslandPiece extends StructurePiece {

    private static final String TAG_SEED   = "Seed";
    private static final String TAG_ANCHOR = "Anchor";

    /** Hanging-vine chain length. Rolled per rim column for organic variation. */
    private static final int VINE_MIN_LENGTH = 2;
    private static final int VINE_MAX_LENGTH = 6;

    private final long seed;
    private final ChunkPos anchor;

    public MegaIslandPiece(int genDepth, BoundingBox boundingBox, long seed, ChunkPos anchor) {
        super(ModRegistries.MEGA_ISLAND_PIECE.get(), genDepth, boundingBox);
        this.seed = seed;
        this.anchor = anchor;
    }

    public MegaIslandPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModRegistries.MEGA_ISLAND_PIECE.get(), tag);
        this.seed = tag.getLong(TAG_SEED);
        this.anchor = new ChunkPos(tag.getLong(TAG_ANCHOR));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putLong(TAG_SEED, seed);
        tag.putLong(TAG_ANCHOR, anchor.toLong());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator gen,
                            RandomSource rng, BoundingBox writeBounds, ChunkPos chunkPos,
                            BlockPos pos) {
        MegaIslandParams params = MegaIslandParams.fromSeed(seed, anchor);
        MegaShape shape = params.shape();

        // Sample biome at THIS piece's chunk, not the island center. Querying a
        // far-away column throws "Requested chunk unavailable" (WorldGenRegion only
        // exposes chunks near the one being generated) and aborts generation.
        // Sampling locally also gives mega islands that straddle a biome boundary
        // a natural palette transition.
        BlockPos biomeProbe = new BlockPos(chunkPos.getMiddleBlockX(),
                params.center().getY(), chunkPos.getMiddleBlockZ());
        Holder<Biome> biome = level.getBiome(biomeProbe);
        SurfacePalette palette = SurfacePaletteRegistry.select(biome, shape.getSerializedName());
        boolean vinesEligible = LayerBlocks.supportsHangingVines(palette);

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        for (int dx = 0; dx < 16; dx++) {
            int worldX = chunkMinX + dx;
            if (worldX < writeBounds.minX() || worldX > writeBounds.maxX()) continue;

            for (int dz = 0; dz < 16; dz++) {
                int worldZ = chunkMinZ + dz;
                if (worldZ < writeBounds.minZ() || worldZ > writeBounds.maxZ()) continue;

                ColumnSpec col = shape.columnAt(params, worldX, worldZ);
                if (col == null) continue;
                int columnThickness = col.thickness();
                if (columnThickness <= 0) continue;

                for (int dyFromTop = 0; dyFromTop < columnThickness; dyFromTop++) {
                    int worldY = col.topY() - dyFromTop;
                    if (worldY < writeBounds.minY() || worldY > writeBounds.maxY()) continue;

                    BlockState block = LayerBlocks.pick(seed, palette,
                            worldX, worldY, worldZ, dyFromTop, columnThickness, col.surface());
                    mut.set(worldX, worldY, worldZ);
                    level.setBlock(mut, block, 2);
                }

                // CRATER basins are the only natural source of levitite blend in
                // the pack. Fill the depression from the basin surface up to rim
                // level so the pool reads as "this crater contains a lake of
                // levitite" rather than a 1-block wet floor.
                if (col.surface() == ColumnSpec.Surface.BASIN
                        && shape == MegaShape.CRATER
                        && AeronauticsCompat.isActive()) {
                    fillBasinWithLevitite(level, writeBounds, mut,
                            worldX, worldZ, col.topY(), params.center().getY());
                }

                if (vinesEligible && col.surface() == ColumnSpec.Surface.SOLID
                        && isRim(shape, params, worldX, worldZ)) {
                    hangVines(level, writeBounds, mut, worldX, col.bottomY(), worldZ, rng);
                }
            }
        }
    }

    /**
     * A column is a rim if any of its four cardinal XZ neighbours falls outside
     * the shape's footprint. Works for every shape — circular, ringed, elongated
     * — without needing shape-specific geometry.
     */
    private static boolean isRim(MegaShape shape, MegaIslandParams params, int wx, int wz) {
        return shape.columnAt(params, wx + 1, wz) == null
            || shape.columnAt(params, wx - 1, wz) == null
            || shape.columnAt(params, wx, wz + 1) == null
            || shape.columnAt(params, wx, wz - 1) == null;
    }

    /**
     * Fill a CRATER basin column with levitite_blend source blocks from one
     * block above the basin surface up to rim level. Adjacent pieces do the
     * same for their columns; the pool reads as a single continuous lake
     * across chunk boundaries because every fluid block is a source block.
     */
    private static void fillBasinWithLevitite(WorldGenLevel level, BoundingBox writeBounds,
                                              BlockPos.MutableBlockPos mut,
                                              int wx, int wz, int basinTopY, int rimY) {
        BlockState blend = AeronauticsCompat.levititeBlend();
        if (blend == null) return;
        for (int y = basinTopY + 1; y <= rimY; y++) {
            if (y < writeBounds.minY() || y > writeBounds.maxY()) continue;
            mut.set(wx, y, wz);
            level.setBlock(mut, blend, 2);
        }
    }

    /**
     * Hang {@value VINE_MIN_LENGTH}–{@value VINE_MAX_LENGTH} vines directly
     * beneath the island's underside. Every vine in the chain is placed with
     * the {@code up} face attached — functionally survives a block update and
     * renders as a continuous hanging curtain.
     */
    private static void hangVines(WorldGenLevel level, BoundingBox writeBounds,
                                  BlockPos.MutableBlockPos mut,
                                  int wx, int columnBottomY, int wz, RandomSource rng) {
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.UP, Boolean.TRUE);
        int length = VINE_MIN_LENGTH + rng.nextInt(VINE_MAX_LENGTH - VINE_MIN_LENGTH + 1);
        for (int i = 1; i <= length; i++) {
            int worldY = columnBottomY - i;
            if (worldY < writeBounds.minY() || worldY > writeBounds.maxY()) break;
            mut.set(wx, worldY, wz);
            if (!level.isEmptyBlock(mut)) break;
            level.setBlock(mut, vine, 2);
        }
    }
}
