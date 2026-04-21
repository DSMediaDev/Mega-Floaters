package gg.dsmedia.megafloaters.worldgen;

import net.minecraft.util.RandomSource;

public final class Distributions {

    private Distributions() {}

    /** Symmetric-triangular int in [min, max] — averages two uniforms for a cheap mode-at-midpoint bell. */
    public static int triangularInt(RandomSource rng, int min, int max) {
        if (max <= min) return min;
        int range = max - min;
        int sum = rng.nextInt(range + 1) + rng.nextInt(range + 1);
        return min + sum / 2;
    }
}
