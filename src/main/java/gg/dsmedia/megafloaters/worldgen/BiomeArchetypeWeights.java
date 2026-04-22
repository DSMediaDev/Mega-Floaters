package gg.dsmedia.megafloaters.worldgen;

import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public final class BiomeArchetypeWeights {

    // Weight order: MESA, CONE, CLUSTER, SPIRE (matches FloaterArchetype declaration order
    // post-v0.3.0 DISC removal). Any disc weight was folded into mesa + cluster.
    private static final int[] PLAINS   = {25, 35, 25, 15};
    private static final int[] DESERT   = {60, 10, 10, 20};
    private static final int[] JUNGLE   = { 5, 55, 30, 10};
    private static final int[] TAIGA    = {10, 25, 20, 45};
    private static final int[] OCEAN    = {10, 10, 60, 20};
    private static final int[] END      = {40, 10, 15, 35};
    private static final int[] FALLBACK = {30, 30, 20, 20};

    private BiomeArchetypeWeights() {}

    public static FloaterArchetype select(Holder<Biome> biome, RandomSource rng) {
        int[] weights = weightsFor(biome);
        int total = 0;
        for (int w : weights) total += w;
        if (total <= 0) return FloaterArchetype.MESA;

        int roll = rng.nextInt(total);
        int cum = 0;
        FloaterArchetype[] values = FloaterArchetype.values();
        for (int i = 0; i < weights.length; i++) {
            cum += weights[i];
            if (roll < cum) return values[i];
        }
        return FloaterArchetype.MESA;
    }

    private static int[] weightsFor(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_END)) return END;
        if (biome.is(BiomeTags.IS_JUNGLE)) return JUNGLE;
        if (biome.is(BiomeTags.IS_BADLANDS)) return DESERT;
        if (biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_OCEAN)) return OCEAN;
        if (biome.is(BiomeTags.IS_TAIGA)) return TAIGA;
        if (biome.is(BiomeTags.IS_SAVANNA)) return PLAINS;

        // Tags don't cover desert / swamp / snowy / plains cleanly — fall back to
        // resource-location path matching for those.
        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key != null) {
            String path = key.location().getPath();
            if (path.equals("desert")) return DESERT;
            if (path.contains("swamp") || path.contains("mangrove")) return JUNGLE;
            if (path.equals("plains") || path.equals("sunflower_plains") || path.equals("meadow")) return PLAINS;
            if (path.startsWith("snowy_") || path.startsWith("frozen_") || path.equals("ice_spikes")) return TAIGA;
        }
        return FALLBACK;
    }
}
