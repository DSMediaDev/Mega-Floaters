package gg.dsmedia.megafloaters.structure.shape;

/**
 * Deterministic value noise used to break up the perfect-CSG look of mega-island
 * shapes. Inputs are absolute world coords so adjacent pieces produce continuous
 * noise across chunk boundaries — no visible seams.
 */
public final class ShapeNoise {

    private ShapeNoise() {}

    /**
     * Smoothed 2D value noise in [0, 1). Same (seed, x, z, scale) → same value
     * across calls and across chunk boundaries.
     */
    public static float noise2D(long seed, int x, int z, double scale) {
        double sx = x * scale;
        double sz = z * scale;
        int ix = (int) Math.floor(sx);
        int iz = (int) Math.floor(sz);
        double tx = sx - ix;
        double tz = sz - iz;
        // Smoothstep ease so adjacent cells don't show grid lines.
        double ex = tx * tx * (3.0 - 2.0 * tx);
        double ez = tz * tz * (3.0 - 2.0 * tz);
        float a = hash01(seed, ix,     iz);
        float b = hash01(seed, ix + 1, iz);
        float c = hash01(seed, ix,     iz + 1);
        float d = hash01(seed, ix + 1, iz + 1);
        double ab = a + ex * (b - a);
        double cd = c + ex * (d - c);
        return (float) (ab + ez * (cd - ab));
    }

    /** Convenience: same as {@link #noise2D} but rescaled to [-1, 1]. */
    public static float signed(long seed, int x, int z, double scale) {
        return noise2D(seed, x, z, scale) * 2.0f - 1.0f;
    }

    /**
     * 2-octave fractional Brownian motion in [-1, 1]. Cheap; the second octave
     * is only used to break up the smoothness of the first.
     */
    public static float fbm(long seed, int x, int z, double scale) {
        float a = signed(seed,             x, z, scale);
        float b = signed(seed ^ 0x9E37L,   x, z, scale * 2.0);
        return (a * 0.6667f + b * 0.3333f);
    }

    private static float hash01(long seed, int x, int z) {
        long h = seed ^ ((long) x * 0xC4CEB9FE1A85EC53L);
        h ^= ((long) z * 0x9E3779B97F4A7C15L);
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return ((h >>> 40)) / (float) (1 << 24);
    }
}
