package gg.dsmedia.megafloaters.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import gg.dsmedia.megafloaters.ModAttachments;
import gg.dsmedia.megafloaters.ModRegistries;
import gg.dsmedia.megafloaters.api.IslandPlacedEvent;
import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.api.palette.SurfacePaletteRegistry;
import gg.dsmedia.megafloaters.api.palette.VegetationSpec;
import gg.dsmedia.megafloaters.integration.AeronauticsCompat;
import gg.dsmedia.megafloaters.integration.BddCompat;
import gg.dsmedia.megafloaters.loot.MegaFloatersLootTables;
import gg.dsmedia.megafloaters.registry.IslandRecord;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import gg.dsmedia.megafloaters.structure.shape.ColumnSpec;
import gg.dsmedia.megafloaters.structure.shape.MegaShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;

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

    /** Fraction of eligible rim columns that grow a hanging vine chain. */
    private static final float VINE_DENSITY = 0.5f;

    /** Per-island probability that a ruin or nest is generated. */
    private static final float RUIN_CHANCE = 0.10f;
    private static final float NEST_CHANCE = 0.15f;

    /** Probability that an eligible-biome mega island gets a pond. High because the
     *  surface area is vast and a single pond reads as incidental, not dominant. */
    private static final float POND_CHANCE = 0.85f;

    // Independent seed keys for each feature slot — XOR'ed with the island seed
    // so every decision uses its own independent RNG stream.
    private static final long RUIN_PRESENT_SEED = 0xA5A5_0001L;
    private static final long RUIN_CHUNK_SEED   = 0xA5A5_0002L;
    private static final long NEST_PRESENT_SEED = 0xB6B6_0001L;
    private static final long NEST_CHUNK_SEED   = 0xB6B6_0002L;
    private static final long POND_PRESENT_SEED = 0xC7C7_0001L;
    private static final long POND_CHUNK_SEED   = 0xC7C7_0002L;

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

        BlockPos biomeProbe = new BlockPos(chunkPos.getMiddleBlockX(),
                params.center().getY(), chunkPos.getMiddleBlockZ());
        Holder<Biome> biome = level.getBiome(biomeProbe);
        SurfacePalette palette = SurfacePaletteRegistry.select(biome, shape.getSerializedName());
        boolean vinesEligible = LayerBlocks.supportsHangingVines(palette);
        boolean craterFill = (shape == MegaShape.CRATER) && AeronauticsCompat.isActive();

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        // Pad the levitite fill ceiling by the rim's top-noise amplitude so the
        // pool reaches the highest noisy rim spot rather than dipping below it
        // by 1–2 blocks. Reads as "brim full" everywhere.
        int craterBrimY = params.center().getY() + MegaShape.TOP_AMP;

        // Solid top-surface positions in this chunk, collected during the block loop
        // and reused for vegetation, ponds, ruins, and nests below.
        List<BlockPos> chunkTops    = new ArrayList<>();
        List<BlockPos> interiorTops = new ArrayList<>();

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

                // Sample the four cardinal neighbours once per column and reuse
                // for both rim detection (vines, exposure flag bootstrap) and
                // per-block exposure (ore hiding).
                ColumnSpec nE = shape.columnAt(params, worldX + 1, worldZ);
                ColumnSpec nW = shape.columnAt(params, worldX - 1, worldZ);
                ColumnSpec nS = shape.columnAt(params, worldX, worldZ + 1);
                ColumnSpec nN = shape.columnAt(params, worldX, worldZ - 1);
                boolean isRim = (nE == null) || (nW == null) || (nS == null) || (nN == null);

                for (int dyFromTop = 0; dyFromTop < columnThickness; dyFromTop++) {
                    int worldY = col.topY() - dyFromTop;
                    if (worldY < writeBounds.minY() || worldY > writeBounds.maxY()) continue;

                    boolean exposed = isExposedAt(worldY, nE, nW, nS, nN);
                    BlockState block = LayerBlocks.pick(seed, palette,
                            worldX, worldY, worldZ, dyFromTop, columnThickness,
                            col.surface(), exposed);
                    mut.set(worldX, worldY, worldZ);
                    level.setBlock(mut, block, 2);
                }

                // Collect solid top-surface positions for sub-feature placement.
                if (col.surface() == ColumnSpec.Surface.SOLID) {
                    int topY = col.topY();
                    if (topY >= writeBounds.minY() && topY <= writeBounds.maxY()) {
                        BlockPos topPos = new BlockPos(worldX, topY, worldZ);
                        chunkTops.add(topPos);
                        if (!isRim) interiorTops.add(topPos);
                    }
                }

                if (craterFill && col.surface() == ColumnSpec.Surface.BASIN) {
                    fillBasinWithLevitite(level, writeBounds, mut,
                            worldX, worldZ, col.topY(), craterBrimY);
                }

                if (isRim && vinesEligible && col.surface() == ColumnSpec.Surface.SOLID
                        && rng.nextFloat() < VINE_DENSITY) {
                    hangVines(level, writeBounds, mut, worldX, col.bottomY(), worldZ, rng);
                }
            }
        }

        // --- Sub-features ---------------------------------------------------

        if (!chunkTops.isEmpty()) {
            scatterGroundCover(level, chunkTops, palette.vegetation(), rng);
        }

        if (!interiorTops.isEmpty()) {
            placeTrees(level, gen, interiorTops, palette.vegetation(), rng);

            // Single-placement features land in a deterministic chunk derived
            // from the island seed. Only the piece whose chunkPos matches places it.
            ChunkPos ruinChunk = featureChunk(seed, RUIN_CHUNK_SEED, params);
            ChunkPos nestChunk = featureChunk(seed, NEST_CHUNK_SEED, params);
            ChunkPos pondChunk = featureChunk(seed, POND_CHUNK_SEED, params);

            if (chunkPos.equals(pondChunk)
                    && palette.waterFeatures().pondChance() > 0
                    && islandHasFeature(seed, POND_PRESENT_SEED, POND_CHANCE)) {
                int pondRadius = Math.max(3, params.radius() / 12);
                int idx = seededIndex(seed ^ POND_CHUNK_SEED, interiorTops.size());
                carvePond(level, interiorTops.get(idx), pondRadius);
            }

            if (chunkPos.equals(ruinChunk)
                    && islandHasFeature(seed, RUIN_PRESENT_SEED, RUIN_CHANCE)) {
                int idx = seededIndex(seed ^ RUIN_CHUNK_SEED, interiorTops.size());
                AncientRuin.place(level, interiorTops.get(idx),
                        MegaFloatersLootTables.pickTier(rng), rng);
            }

            if (chunkPos.equals(nestChunk)
                    && islandHasFeature(seed, NEST_PRESENT_SEED, NEST_CHANCE)) {
                int idx = seededIndex(seed ^ NEST_CHUNK_SEED, interiorTops.size());
                BlockPos nestPos = interiorTops.get(idx);
                DragonNest.place(level, nestPos, rng);
                if (BddCompat.isActive()) BddCompat.populateNest(level, nestPos, biome, rng);
            }
        }

        // --- A.4: spawn suppression + registry ------------------------------

        // Flag this chunk so SpawnSuppression cancels natural hostile spawns here.
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        ((IAttachmentHolder) chunk).setData(ModAttachments.NO_HOSTILES, Boolean.TRUE);

        // Register exactly one record per island, from the anchor chunk's piece.
        if (chunkPos.equals(anchor)) {
            registerIsland(level, params, biome);
        }
    }

    // --- Sub-feature helpers ------------------------------------------------

    private static void scatterGroundCover(WorldGenLevel level, List<BlockPos> tops,
                                           VegetationSpec veg, RandomSource rng) {
        if (veg.groundCover().isEmpty() || veg.groundCoverDensity() <= 0.0f) return;
        for (BlockPos top : tops) {
            if (rng.nextFloat() >= veg.groundCoverDensity()) continue;
            BlockPos above = top.above();
            if (!level.isEmptyBlock(above)) continue;
            BlockState cover = veg.groundCover().get(rng.nextInt(veg.groundCover().size()));
            level.setBlock(above, cover, 2);
        }
    }

    private static void placeTrees(WorldGenLevel level, ChunkGenerator gen,
                                   List<BlockPos> interiorTops, VegetationSpec veg,
                                   RandomSource rng) {
        if (veg.trees().isEmpty() || veg.maxTrees() <= 0 || interiorTops.isEmpty()) return;
        // Per-chunk attempt count scales with palette's tree density, capped at 3 per
        // piece. A 70-chunk footprint island with maxTrees=3 gets ~210 attempts
        // (~100 actual placements after feature rejections) — appropriate for a forest.
        int attempts = Math.min(veg.maxTrees(), 3);
        for (int i = 0; i < attempts; i++) {
            BlockPos top = interiorTops.get(rng.nextInt(interiorTops.size()));
            var treeKey = veg.trees().get(rng.nextInt(veg.trees().size()));
            var holder = level.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(treeKey).orElse(null);
            if (holder == null) continue;
            holder.value().place(level, gen, rng, top.above());
        }
    }

    /** Carve a circular water pond centred on {@code center}'s top surface. */
    private static void carvePond(WorldGenLevel level, BlockPos center, int pondRadius) {
        BlockState water = Blocks.WATER.defaultBlockState();
        int rSq = pondRadius * pondRadius;
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int dx = -pondRadius; dx <= pondRadius; dx++) {
            for (int dz = -pondRadius; dz <= pondRadius; dz++) {
                if (dx * dx + dz * dz > rSq) continue;
                mut.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                level.setBlock(mut, water, 2);
            }
        }
    }

    /** Write one {@link IslandRecord} for this mega island into the level's registry. */
    private static void registerIsland(WorldGenLevel level, MegaIslandParams params,
                                       Holder<Biome> biome) {
        boolean hasRuin = islandHasFeature(params.seed(), RUIN_PRESENT_SEED, RUIN_CHANCE);
        boolean hasNest = islandHasFeature(params.seed(), NEST_PRESENT_SEED, NEST_CHANCE);
        boolean hasLevitite = params.shape() == MegaShape.CRATER && AeronauticsCompat.isActive();

        ResourceLocation biomeId = biome.unwrapKey()
                .map(k -> k.location())
                .orElseGet(() -> ResourceLocation.fromNamespaceAndPath("minecraft", "plains"));
        ResourceLocation archetypeId = ResourceLocation.fromNamespaceAndPath(
                MegaFloatersMod.MOD_ID, "mega_" + params.shape().getSerializedName());

        // Deterministic UUID from seed + anchor so the same island gets the same
        // ID on every world load — prevents players rediscovering the same island.
        UUID id = new UUID(params.seed(), params.anchor().toLong());

        IslandRecord record = new IslandRecord(
                id, archetypeId, params.center(),
                params.radius(), params.thickness(),
                biomeId, hasRuin, hasNest, hasLevitite,
                level.getLevel().getGameTime());

        IslandRegistry.get(level.getLevel()).add(record);
        NeoForge.EVENT_BUS.post(new IslandPlacedEvent(level.getLevel(), record));
    }

    // --- Deterministic feature-slot helpers ---------------------------------

    /** True if the island's seed rolls a hit for this feature slot at the given probability. */
    private static boolean islandHasFeature(long seed, long featureSeed, float chance) {
        return RandomSource.create(seed ^ featureSeed).nextFloat() < chance;
    }

    /**
     * Deterministic target chunk for a single-placement feature. The offset is
     * ~65% of island radius from the center in one of four cardinal directions —
     * this band is solid ground in all six mega shapes (past hollow centers,
     * inside ring areas, within disc bodies).
     */
    private static ChunkPos featureChunk(long seed, long chunkSeed, MegaIslandParams params) {
        RandomSource r = RandomSource.create(seed ^ chunkSeed);
        int dir = r.nextInt(4); // 0=+X  1=−X  2=+Z  3=−Z
        int offset = (int) (params.radius() * 0.65);
        int fx = params.center().getX() + (dir == 0 ? offset : dir == 1 ? -offset : 0);
        int fz = params.center().getZ() + (dir == 2 ? offset : dir == 3 ? -offset : 0);
        return new ChunkPos(fx >> 4, fz >> 4);
    }

    /** Converts a 64-bit seed to a valid list index, safe against Long.MIN_VALUE. */
    private static int seededIndex(long seed, int size) {
        if (size <= 0) return 0;
        return (int) ((seed & Long.MAX_VALUE) % size);
    }

    // --- Block-placement helpers (geometry pass) ----------------------------

    /**
     * A column block is exposed to air when at least one cardinal neighbour at
     * the same Y is either outside the footprint or sits above/below this Y in
     * its own column. Used to suppress ores at exposed rim positions so they
     * don't read as "free diamonds visible from a thousand blocks away."
     */
    private static boolean isExposedAt(int worldY, ColumnSpec nE, ColumnSpec nW,
                                       ColumnSpec nS, ColumnSpec nN) {
        return isExposedDir(worldY, nE)
            || isExposedDir(worldY, nW)
            || isExposedDir(worldY, nS)
            || isExposedDir(worldY, nN);
    }

    private static boolean isExposedDir(int worldY, ColumnSpec n) {
        return n == null || worldY > n.topY() || worldY < n.bottomY();
    }

    /**
     * Fill a CRATER basin column with levitite_blend source blocks from one
     * block above the basin surface up to {@code brimY}. Every fluid block is
     * a source so adjacent chunks stitch into one continuous pool without
     * needing fluid-flow propagation across chunk boundaries.
     */
    private static void fillBasinWithLevitite(WorldGenLevel level, BoundingBox writeBounds,
                                              BlockPos.MutableBlockPos mut,
                                              int wx, int wz, int basinTopY, int brimY) {
        BlockState blend = AeronauticsCompat.levititeBlend();
        if (blend == null) return;
        for (int y = basinTopY + 1; y <= brimY; y++) {
            if (y < writeBounds.minY() || y > writeBounds.maxY()) continue;
            mut.set(wx, y, wz);
            level.setBlock(mut, blend, 2);
        }
    }

    /**
     * Hang {@value VINE_MIN_LENGTH}–{@value VINE_MAX_LENGTH} vines directly
     * beneath the island's underside. Every vine in the chain shares the same
     * cardinal face (NORTH/SOUTH/EAST/WEST), rolled once per chain — the
     * resulting block reads as a vertical sheet of vine, and stacked chain
     * blocks render as a continuous curtain instead of detached horizontal
     * patches (which is what setting {@code UP} would have produced).
     */
    private static void hangVines(WorldGenLevel level, BoundingBox writeBounds,
                                  BlockPos.MutableBlockPos mut,
                                  int wx, int columnBottomY, int wz, RandomSource rng) {
        BooleanProperty face = vineFace(rng);
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(face, Boolean.TRUE);
        int length = VINE_MIN_LENGTH + rng.nextInt(VINE_MAX_LENGTH - VINE_MIN_LENGTH + 1);
        for (int i = 1; i <= length; i++) {
            int worldY = columnBottomY - i;
            if (worldY < writeBounds.minY() || worldY > writeBounds.maxY()) break;
            mut.set(wx, worldY, wz);
            if (!level.isEmptyBlock(mut)) break;
            level.setBlock(mut, vine, 2);
        }
    }

    private static BooleanProperty vineFace(RandomSource rng) {
        Direction d = Direction.from2DDataValue(rng.nextInt(4));
        return switch (d) {
            case NORTH -> VineBlock.NORTH;
            case SOUTH -> VineBlock.SOUTH;
            case EAST  -> VineBlock.EAST;
            case WEST  -> VineBlock.WEST;
            default    -> VineBlock.NORTH;
        };
    }
}
