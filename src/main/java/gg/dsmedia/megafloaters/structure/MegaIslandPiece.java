package gg.dsmedia.megafloaters.structure;

import gg.dsmedia.megafloaters.ModRegistries;
import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.api.palette.SurfacePaletteRegistry;
import gg.dsmedia.megafloaters.structure.shape.ColumnSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
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
        Holder<Biome> biome = level.getBiome(params.center());
        SurfacePalette palette = SurfacePaletteRegistry.select(
                biome, params.shape().getSerializedName());

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        for (int dx = 0; dx < 16; dx++) {
            int worldX = chunkMinX + dx;
            if (worldX < writeBounds.minX() || worldX > writeBounds.maxX()) continue;

            for (int dz = 0; dz < 16; dz++) {
                int worldZ = chunkMinZ + dz;
                if (worldZ < writeBounds.minZ() || worldZ > writeBounds.maxZ()) continue;

                ColumnSpec col = params.shape().columnAt(params, worldX, worldZ);
                if (col == null) continue;
                int columnThickness = col.thickness();
                if (columnThickness <= 0) continue;

                for (int dyFromTop = 0; dyFromTop < columnThickness; dyFromTop++) {
                    int worldY = col.topY() - dyFromTop;
                    if (worldY < writeBounds.minY() || worldY > writeBounds.maxY()) continue;

                    BlockState block = layerBlock(palette, dyFromTop, columnThickness, col.surface());
                    mut.set(worldX, worldY, worldZ);
                    level.setBlock(mut, block, 2);
                }
            }
        }
    }

    /**
     * Picks the layer block for a column position. Shared with
     * {@link gg.dsmedia.megafloaters.worldgen.IslandBuilder}'s small-island
     * convention: bottom {@code undersideDepth} layers use the underside block,
     * the very top is the top block, second is sub, the rest is core. Basin
     * floors use the underside palette so a later water fill reads as a basin
     * floor, not a grass block under water.
     */
    private static BlockState layerBlock(SurfacePalette palette, int dyFromTop,
                                         int columnThickness, ColumnSpec.Surface surface) {
        int dyFromBottom = columnThickness - 1 - dyFromTop;
        if (dyFromBottom < palette.undersideDepth()) return palette.undersideBlock();
        if (surface == ColumnSpec.Surface.BASIN) return palette.undersideBlock();
        if (dyFromTop == 0) return palette.topBlock();
        if (dyFromTop == 1) return palette.subBlock();
        return palette.coreBlock();
    }
}
