package gg.dsmedia.megafloaters.worldgen;

import java.util.List;

import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.api.palette.SurfacePaletteRegistry;
import gg.dsmedia.megafloaters.api.palette.VegetationSpec;
import gg.dsmedia.megafloaters.api.palette.WaterFeatureSpec;
import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
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
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource rng = ctx.random();
        FloaterFeatureConfig cfg = ctx.config();
        ChunkGenerator gen = ctx.chunkGenerator();

        Holder<Biome> biome = level.getBiome(origin);
        FloaterArchetype archetype = BiomeArchetypeWeights.select(biome, rng);
        SurfacePalette palette = SurfacePaletteRegistry.select(biome, archetype);

        int baseRadius = Distributions.triangularInt(rng, cfg.minRadius(), cfg.maxRadius());
        int baseThickness = Distributions.triangularInt(rng, cfg.minThickness(), cfg.maxThickness());
        int radius = Math.max(2, (int) Math.round(baseRadius * archetype.radiusMult()));
        int thickness = Math.max(2, (int) Math.round(baseThickness * archetype.thicknessMult()));

        archetype.build(level, origin, radius, thickness, cfg.edgeChance(), palette, rng);

        // After the base shape is built, find the top-surface positions once and
        // reuse them for every sub-feature pass below.
        int searchRadius = radius + 3;
        int searchHeight = Math.max(thickness, 8) + 4;
        List<BlockPos> topPositions = SurfaceScanner.topSurface(level, origin, searchRadius,
                searchHeight, palette.topBlock());
        if (topPositions.isEmpty()) {
            return true;
        }

        if (cfg.placeTree() && archetype.placesTree()) {
            placeTrees(level, gen, rng, topPositions, palette.vegetation(), radius);
        }
        scatterGroundCover(level, rng, topPositions, palette.vegetation());
        maybeAddWater(level, rng, topPositions, palette.waterFeatures());

        return true;
    }

    private static void placeTrees(WorldGenLevel level, ChunkGenerator gen, RandomSource rng,
                                   List<BlockPos> tops, VegetationSpec veg, int radius) {
        if (veg.trees().isEmpty() || veg.maxTrees() <= 0) return;

        int desired = Math.max(1, radius / 8);
        int numTrees = Math.min(desired, veg.maxTrees());

        for (int i = 0; i < numTrees; i++) {
            BlockPos top = tops.get(rng.nextInt(tops.size()));
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
