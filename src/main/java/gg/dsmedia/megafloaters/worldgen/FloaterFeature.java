package gg.dsmedia.megafloaters.worldgen;

import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.api.palette.SurfacePaletteRegistry;
import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class FloaterFeature extends Feature<FloaterFeatureConfig> {

    public FloaterFeature(com.mojang.serialization.Codec<FloaterFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FloaterFeatureConfig> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource rng = ctx.random();
        FloaterFeatureConfig cfg = ctx.config();

        Holder<Biome> biome = level.getBiome(origin);
        FloaterArchetype archetype = BiomeArchetypeWeights.select(biome, rng);
        SurfacePalette palette = SurfacePaletteRegistry.select(biome, archetype);

        int baseRadius = Distributions.triangularInt(rng, cfg.minRadius(), cfg.maxRadius());
        int baseThickness = Distributions.triangularInt(rng, cfg.minThickness(), cfg.maxThickness());
        int radius = Math.max(2, (int) Math.round(baseRadius * archetype.radiusMult()));
        int thickness = Math.max(2, (int) Math.round(baseThickness * archetype.thicknessMult()));

        archetype.build(level, origin, radius, thickness, cfg.edgeChance(), palette, rng);

        if (cfg.placeTree() && archetype.placesTree() && paletteSupportsTree(palette)) {
            ChunkGenerator gen = ctx.chunkGenerator();
            Holder<ConfiguredFeature<?, ?>> oak = level.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(TreeFeatures.OAK)
                    .orElseThrow();
            oak.value().place(level, gen, rng, origin.above());
        }
        return true;
    }

    /**
     * Trees only go on grass-topped palettes for now — placing an oak on
     * end_stone or sand looks wrong and will be handled by the biome-specific
     * vegetation map in step 6.
     */
    private static boolean paletteSupportsTree(SurfacePalette palette) {
        return palette.topBlock() == SurfacePaletteRegistry.FALLBACK.topBlock();
    }
}
