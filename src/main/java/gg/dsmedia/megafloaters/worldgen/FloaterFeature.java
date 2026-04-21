package gg.dsmedia.megafloaters.worldgen;

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

        int baseRadius = Distributions.triangularInt(rng, cfg.minRadius(), cfg.maxRadius());
        int baseThickness = Distributions.triangularInt(rng, cfg.minThickness(), cfg.maxThickness());
        int radius = Math.max(2, (int) Math.round(baseRadius * archetype.radiusMult()));
        int thickness = Math.max(2, (int) Math.round(baseThickness * archetype.thicknessMult()));

        archetype.build(level, origin, radius, thickness, cfg.edgeChance(), rng);

        if (cfg.placeTree() && archetype.placesTree()) {
            ChunkGenerator gen = ctx.chunkGenerator();
            Holder<ConfiguredFeature<?, ?>> oak = level.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(TreeFeatures.OAK)
                    .orElseThrow();
            oak.value().place(level, gen, rng, origin.above());
        }
        return true;
    }
}
