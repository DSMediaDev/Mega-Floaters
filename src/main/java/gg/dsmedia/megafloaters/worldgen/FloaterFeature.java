package gg.dsmedia.megafloaters.worldgen;

import java.util.List;

import java.util.UUID;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import gg.dsmedia.megafloaters.ModAttachments;
import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.api.palette.SurfacePaletteRegistry;
import gg.dsmedia.megafloaters.api.palette.VegetationSpec;
import gg.dsmedia.megafloaters.api.palette.WaterFeatureSpec;
import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
import gg.dsmedia.megafloaters.integration.AeronauticsCompat;
import gg.dsmedia.megafloaters.integration.BddCompat;
import gg.dsmedia.megafloaters.api.IslandPlacedEvent;
import gg.dsmedia.megafloaters.loot.MegaFloatersLootTables;
import gg.dsmedia.megafloaters.registry.IslandRecord;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import gg.dsmedia.megafloaters.structure.AncientRuin;
import gg.dsmedia.megafloaters.structure.DragonNest;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class FloaterFeature extends Feature<FloaterFeatureConfig> {

    private static final BlockState WATER = Blocks.WATER.defaultBlockState();

    public FloaterFeature(com.mojang.serialization.Codec<FloaterFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FloaterFeatureConfig> ctx) {
        return generate(ctx.level(), ctx.chunkGenerator(), ctx.origin(), ctx.config(),
                null, 0, 0, ctx.random());
    }

    /**
     * Run the full island-placement pipeline. Public so the {@code /megafloaters spawn}
     * command (and future scripting hooks) can drive it directly.
     *
     * @param archetypeOverride If non-null, use this archetype instead of biome-weighted selection.
     * @param radiusOverride    If &gt; 0, use this radius instead of rolling from the config.
     * @param thicknessOverride If &gt; 0, use this thickness instead of rolling from the config.
     */
    public static boolean generate(WorldGenLevel level, ChunkGenerator gen, BlockPos origin,
                                   FloaterFeatureConfig cfg, FloaterArchetype archetypeOverride,
                                   int radiusOverride, int thicknessOverride, RandomSource rng) {
        Holder<Biome> biome = level.getBiome(origin);
        FloaterArchetype archetype = archetypeOverride != null
                ? archetypeOverride
                : BiomeArchetypeWeights.select(biome, rng);
        SurfacePalette palette = SurfacePaletteRegistry.select(biome, archetype);

        int radius, thickness;
        if (radiusOverride > 0) {
            radius = radiusOverride;
        } else {
            int base = Distributions.triangularInt(rng, cfg.minRadius(), cfg.maxRadius());
            radius = Math.max(2, (int) Math.round(base * archetype.radiusMult()));
        }
        if (thicknessOverride > 0) {
            thickness = thicknessOverride;
        } else {
            int base = Distributions.triangularInt(rng, cfg.minThickness(), cfg.maxThickness());
            thickness = Math.max(2, (int) Math.round(base * archetype.thicknessMult()));
        }
        radius = clampToSafeRadius(radius);

        archetype.build(level, origin, radius, thickness, cfg.edgeChance(), palette, rng);
        ResourceLocation archetypeId = ResourceLocation.fromNamespaceAndPath(
                MegaFloatersMod.MOD_ID, archetype.getSerializedName());
        return finalizeAfterBuild(level, gen, origin, cfg, archetypeId, radius, thickness,
                palette, biome, archetype.placesTree(), rng);
    }

    /**
     * MC's feature generation step only allows writes within origin chunk + 1 in
     * each cardinal direction. Because origin can land anywhere inside its chunk,
     * the safe extent in any direction is 16 blocks — any bigger risks the
     * "Detected setBlock in a far chunk" error. Pond adds 2 beyond radius, so
     * effective cap is 14. Structure-based generation (v0.2+) will lift this.
     */
    private static int clampToSafeRadius(int radius) {
        return Math.min(radius, 14);
    }

    /**
     * Run the pipeline with an {@link gg.dsmedia.megafloaters.api.ArchetypeBuilder}
     * supplied by an external mod or script. The builder's {@link
     * gg.dsmedia.megafloaters.api.ArchetypeBuilder#build} produces the base
     * shape; the post-build pipeline (top-surface scan, vegetation, water,
     * ores, structures, optional-mod integration, chunk flag, registry)
     * runs identically to the built-in path.
     */
    public static boolean generateWithBuilder(WorldGenLevel level, ChunkGenerator gen, BlockPos origin,
                                              FloaterFeatureConfig cfg,
                                              gg.dsmedia.megafloaters.api.ArchetypeBuilder builder,
                                              ResourceLocation archetypeId,
                                              int radiusOverride, int thicknessOverride,
                                              RandomSource rng) {
        Holder<Biome> biome = level.getBiome(origin);
        SurfacePalette palette = SurfacePaletteRegistry.select(biome, archetypeId.getPath());

        int radius, thickness;
        if (radiusOverride > 0) {
            radius = radiusOverride;
        } else {
            int base = Distributions.triangularInt(rng, cfg.minRadius(), cfg.maxRadius());
            radius = Math.max(2, (int) Math.round(base * builder.radiusMult()));
        }
        if (thicknessOverride > 0) {
            thickness = thicknessOverride;
        } else {
            int base = Distributions.triangularInt(rng, cfg.minThickness(), cfg.maxThickness());
            thickness = Math.max(2, (int) Math.round(base * builder.thicknessMult()));
        }
        radius = clampToSafeRadius(radius);

        builder.build(level, origin, radius, thickness, cfg.edgeChance(), palette, rng);
        return finalizeAfterBuild(level, gen, origin, cfg, archetypeId, radius, thickness,
                palette, biome, builder.placesTree(), rng);
    }

    /** Post-build subfeature pipeline shared by both generate paths. */
    private static boolean finalizeAfterBuild(WorldGenLevel level, ChunkGenerator gen, BlockPos origin,
                                              FloaterFeatureConfig cfg, ResourceLocation archetypeId,
                                              int radius, int thickness, SurfacePalette palette,
                                              Holder<Biome> biome, boolean archetypeWantsTree,
                                              RandomSource rng) {
        // Sub-feature tracking — what ended up on this island. Used both by the
        // registry record below and as the "has_*" hints for /info etc.
        boolean[] featureFlags = new boolean[] { false, false, false };  // ruin, nest, levitite

        // After the base shape is built, find the top-surface positions once and
        // reuse them for every sub-feature pass below.
        int searchRadius = radius + 3;
        int searchHeight = Math.max(thickness, 8) + 4;
        List<BlockPos> topPositions = SurfaceScanner.topSurface(level, origin, searchRadius,
                searchHeight, palette.topBlock());
        if (topPositions.isEmpty()) {
            return true;
        }

        if (cfg.placeTree() && archetypeWantsTree) {
            placeTrees(level, gen, rng, topPositions, palette.vegetation(), radius);
        }
        scatterGroundCover(level, rng, topPositions, palette.vegetation());
        maybeAddWater(level, rng, topPositions, palette.waterFeatures());

        OrePlacer.scatter(level, gen, rng, origin, radius, thickness, palette, cfg.oreCountMultiplier());

        featureFlags[0] = maybeAddRuin(level, rng, topPositions, cfg.ruinChance());
        featureFlags[1] = maybeAddNest(level, rng, topPositions, cfg.nestChance(), biome);

        if (AeronauticsCompat.isActive()) {
            AeronauticsCompat.placePool(level, topPositions, radius, rng);
            AeronauticsCompat.embedUnderside(level, origin, radius, thickness, palette, 0.08f, rng);
            featureFlags[2] = true;
        }

        flagChunksNoHostiles(level, origin, searchRadius);
        recordIsland(level, origin, archetypeId, radius, thickness, biome, featureFlags);

        return true;
    }

    private static void recordIsland(WorldGenLevel level, BlockPos origin, ResourceLocation archetypeId,
                                     int radius, int thickness, Holder<Biome> biome,
                                     boolean[] featureFlags) {
        ResourceLocation biomeId = biome.unwrapKey()
                .map(k -> k.location())
                .orElseGet(() -> ResourceLocation.fromNamespaceAndPath("minecraft", "plains"));
        IslandRecord record = new IslandRecord(
                UUID.randomUUID(),
                archetypeId,
                origin,
                radius,
                thickness,
                biomeId,
                featureFlags[0],
                featureFlags[1],
                featureFlags[2],
                level.getLevel().getGameTime());
        IslandRegistry.get(level.getLevel()).add(record);
        NeoForge.EVENT_BUS.post(new IslandPlacedEvent(level.getLevel(), record));
    }

    /**
     * Mark every chunk the island's XZ extent touches with the no_hostiles
     * attachment so {@code SpawnSuppression} can suppress natural monster
     * spawns there.
     */
    private static void flagChunksNoHostiles(WorldGenLevel level, BlockPos origin, int searchRadius) {
        int minChunkX = (origin.getX() - searchRadius) >> 4;
        int maxChunkX = (origin.getX() + searchRadius) >> 4;
        int minChunkZ = (origin.getZ() - searchRadius) >> 4;
        int maxChunkZ = (origin.getZ() + searchRadius) >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                ((IAttachmentHolder) level.getChunk(cx, cz)).setData(
                        ModAttachments.NO_HOSTILES, Boolean.TRUE);
            }
        }
    }

    private static void placeTrees(WorldGenLevel level, ChunkGenerator gen, RandomSource rng,
                                   List<BlockPos> tops, VegetationSpec veg, int radius) {
        if (veg.trees().isEmpty() || veg.maxTrees() <= 0) return;

        // Filter out rim positions so tree canopies don't spill off the island
        // edge into a far chunk.
        List<BlockPos> interior = new java.util.ArrayList<>(tops.size());
        for (BlockPos top : tops) {
            if (!SurfaceScanner.isRim(level, top)) interior.add(top);
        }
        if (interior.isEmpty()) return;

        int desired = Math.max(1, radius / 8);
        int numTrees = Math.min(desired, veg.maxTrees());

        for (int i = 0; i < numTrees; i++) {
            BlockPos top = interior.get(rng.nextInt(interior.size()));
            ResourceKey<ConfiguredFeature<?, ?>> treeKey = veg.trees().get(rng.nextInt(veg.trees().size()));
            Holder<ConfiguredFeature<?, ?>> tree = level.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(treeKey)
                    .orElse(null);
            if (tree == null) continue;
            tree.value().place(level, gen, rng, top.above());
        }
    }

    private static void scatterGroundCover(WorldGenLevel level, RandomSource rng,
                                           List<BlockPos> tops, VegetationSpec veg) {
        if (veg.groundCover().isEmpty() || veg.groundCoverDensity() <= 0.0f) return;

        for (BlockPos top : tops) {
            if (rng.nextFloat() >= veg.groundCoverDensity()) continue;
            BlockPos above = top.above();
            if (!level.isEmptyBlock(above)) continue;
            BlockState cover = veg.groundCover().get(rng.nextInt(veg.groundCover().size()));
            level.setBlock(above, cover, 2);
        }
    }

    private static void maybeAddWater(WorldGenLevel level, RandomSource rng, List<BlockPos> tops,
                                      WaterFeatureSpec water) {
        if (tops.isEmpty() || water.pondChance() <= 0.0f) return;
        if (rng.nextFloat() >= water.pondChance()) return;

        // Pick an interior top position (not rim) as the pond center. Fall back to
        // any position if every top is on the rim (very small islands).
        BlockPos pondCenter = pickInterior(level, tops, rng);
        if (pondCenter == null) return;
        carvePond(level, pondCenter);

        if (rng.nextFloat() < water.waterfallChance()) {
            BlockPos rim = pickRim(level, tops, rng);
            if (rim != null) {
                level.setBlock(rim, WATER, 2);
            }
        }
    }

    private static BlockPos pickInterior(WorldGenLevel level, List<BlockPos> tops, RandomSource rng) {
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos candidate = tops.get(rng.nextInt(tops.size()));
            if (!SurfaceScanner.isRim(level, candidate)) return candidate;
        }
        return tops.get(rng.nextInt(tops.size()));
    }

    private static BlockPos pickRim(WorldGenLevel level, List<BlockPos> tops, RandomSource rng) {
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos candidate = tops.get(rng.nextInt(tops.size()));
            if (SurfaceScanner.isRim(level, candidate)) return candidate;
        }
        return null;
    }

    private static boolean maybeAddRuin(WorldGenLevel level, RandomSource rng, List<BlockPos> tops,
                                        float chance) {
        if (chance <= 0.0f || rng.nextFloat() >= chance) return false;
        BlockPos floor = pickInterior(level, tops, rng);
        if (floor == null) return false;
        AncientRuin.place(level, floor, MegaFloatersLootTables.pickTier(rng), rng);
        return true;
    }

    private static boolean maybeAddNest(WorldGenLevel level, RandomSource rng, List<BlockPos> tops,
                                        float chance, Holder<Biome> biome) {
        if (chance <= 0.0f || rng.nextFloat() >= chance) return false;
        BlockPos nestFloor = pickInterior(level, tops, rng);
        if (nestFloor == null) return false;
        DragonNest.place(level, nestFloor, rng);
        if (BddCompat.isActive()) {
            BddCompat.populateNest(level, nestFloor, biome, rng);
        }
        return true;
    }

    private static void carvePond(WorldGenLevel level, BlockPos center) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                mut.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                level.setBlock(mut, WATER, 2);
            }
        }
    }
}
