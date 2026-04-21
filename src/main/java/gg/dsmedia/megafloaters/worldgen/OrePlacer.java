package gg.dsmedia.megafloaters.worldgen;

import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class OrePlacer {

    private OrePlacer() {}

    // Weights roughly match vanilla overworld ore availability. Coal and iron are
    // the base layer; diamond stays rare. Order of `KEYS` must match `WEIGHTS`.
    private static final ResourceKey<ConfiguredFeature<?, ?>>[] KEYS = weightedKeys();
    private static final int[] WEIGHTS = { 40, 30, 10, 8, 8, 4 };
    private static final int WEIGHT_TOTAL;
    static {
        int sum = 0;
        for (int w : WEIGHTS) sum += w;
        WEIGHT_TOTAL = sum;
    }

    @SuppressWarnings("unchecked")
    private static ResourceKey<ConfiguredFeature<?, ?>>[] weightedKeys() {
        return (ResourceKey<ConfiguredFeature<?, ?>>[]) new ResourceKey<?>[] {
                OreFeatures.ORE_COAL,
                OreFeatures.ORE_IRON,
                OreFeatures.ORE_REDSTONE,
                OreFeatures.ORE_GOLD,
                OreFeatures.ORE_LAPIS,
                OreFeatures.ORE_DIAMOND_SMALL
        };
    }

    /**
     * Scatter ore veins through the core layers of an island. End-palette islands
     * are skipped (end stone has its own logic; no vanilla ores belong there).
     *
     * @param attemptMultiplier Applied on top of the radius²-proportional base count.
     */
    public static void scatter(WorldGenLevel level, ChunkGenerator gen, RandomSource rng,
                               BlockPos center, int radius, int thickness,
                               SurfacePalette palette, float attemptMultiplier) {
        if (palette.coreBlock().getBlock() == net.minecraft.world.level.block.Blocks.END_STONE) return;

        int baseAttempts = Math.max(1, (radius * radius) / 30);
        int attempts = Math.max(1, (int) Math.round(baseAttempts * attemptMultiplier));

        // Core extent: top two layers are grass + sub; bottom `undersideDepth` are
        // the underside. Ore goes between those, if there's room.
        int coreDepth = thickness - 2 - palette.undersideDepth();
        if (coreDepth <= 0) return;
        int coreTopY = center.getY() - 2;
        int coreBottomY = center.getY() - thickness + 1 + palette.undersideDepth();

        var registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);

        for (int i = 0; i < attempts; i++) {
            int dx = rng.nextInt(radius * 2 + 1) - radius;
            int dz = rng.nextInt(radius * 2 + 1) - radius;
            int y = coreBottomY + rng.nextInt(coreTopY - coreBottomY + 1);
            BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);

            ResourceKey<ConfiguredFeature<?, ?>> oreKey = pickOre(rng);
            Holder<ConfiguredFeature<?, ?>> ore = registry.getHolder(oreKey).orElse(null);
            if (ore == null) continue;
            ore.value().place(level, gen, rng, pos);
        }
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> pickOre(RandomSource rng) {
        int roll = rng.nextInt(WEIGHT_TOTAL);
        int cum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            cum += WEIGHTS[i];
            if (roll < cum) return KEYS[i];
        }
        return KEYS[0];
    }
}
