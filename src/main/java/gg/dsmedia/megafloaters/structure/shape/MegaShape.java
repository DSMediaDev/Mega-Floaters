package gg.dsmedia.megafloaters.structure.shape;

import gg.dsmedia.megafloaters.structure.MegaIslandParams;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

/**
 * Top-level shape menu for mega islands. Each shape implements its own column
 * geometry — given world coords (wx, wz), return the Y range of blocks for that
 * column, or {@code null} if the column lies outside the island footprint.
 *
 * <p>All math is deterministic from {@link MegaIslandParams}. Pieces re-derive
 * params from a stored seed and call into the shape independently per chunk;
 * shapes therefore must not hold state across calls.
 *
 * <p>Edge irregularity, top-surface roll, and inter-sub blending all come from
 * {@link ShapeNoise} keyed on {@code shapeSeed} and absolute world coords —
 * adjacent chunk pieces sample the same noise field at their shared boundary,
 * so seams never show up.
 */
public enum MegaShape implements StringRepresentable {

    /**
     * Multi-tier mesa. Three concentric tiers step the top down toward the rim.
     * Tier boundaries and rim outline are perturbed by 2D noise so the steps
     * read as eroded terraces rather than CSG cylinders.
     */
    PLATEAU("plateau", 2.0f) {
        @Override public int xzExtent(int radius) { return radius + EDGE_AMP; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;

            float edgeJitter = ShapeNoise.fbm(p.shapeSeed(), wx, wz, EDGE_SCALE) * EDGE_AMP;
            int effRadius = p.radius() + (int) edgeJitter;
            if (effRadius <= 0) return null;
            if (distSq > effRadius * (long) effRadius) return null;

            // Perturb the *normalized* distance used to pick a tier so the tier
            // rings stop reading as perfect circles.
            double distNorm = Math.sqrt(distSq) / effRadius;
            float tierNoise = ShapeNoise.fbm(p.shapeSeed() ^ 0x71L, wx, wz, EDGE_SCALE * 1.4) * 0.07f;
            double tieredNorm = distNorm + tierNoise;
            int tierStep = Math.max(1, p.thickness() / 8);
            int tier = tieredNorm < 0.40 ? 0 : (tieredNorm < 0.75 ? 1 : 2);

            int topY = p.center().getY() - tier * tierStep;
            topY += topYNoise(p, wx, wz);

            int columnThickness = p.thickness() - tier * tierStep;
            if (distNorm > 0.85) {
                double rimT = (distNorm - 0.85) / 0.15;
                columnThickness = (int) Math.round(columnThickness * (1.0 - 0.5 * rimT));
            }
            columnThickness = Math.max(2, columnThickness);
            return ColumnSpec.solid(topY, topY - columnThickness + 1);
        }
    },

    /**
     * Disc with a central depression. Outer rim, top surface, and crater lip
     * all carry noise; the basin floor stays smooth so a sub-feature pass can
     * flood it cleanly.
     */
    CRATER("crater", 2.2f) {
        @Override public int xzExtent(int radius) { return radius + EDGE_AMP; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;

            float edgeJitter = ShapeNoise.fbm(p.shapeSeed(), wx, wz, EDGE_SCALE) * EDGE_AMP;
            int effRadius = p.radius() + (int) edgeJitter;
            if (effRadius <= 0) return null;
            if (distSq > effRadius * (long) effRadius) return null;

            double distNorm = Math.sqrt(distSq) / effRadius;
            int topY = p.center().getY();

            ColumnSpec.Surface surface = ColumnSpec.Surface.SOLID;
            // Crater lip is also noisy so it isn't a perfect ring.
            float lipJitter = ShapeNoise.fbm(p.shapeSeed() ^ 0xC1L, wx, wz, EDGE_SCALE * 1.2) * 0.05f;
            double basinThreshold = 0.55 + lipJitter;
            if (distNorm < basinThreshold) {
                double dipT = 1.0 - (distNorm / basinThreshold);
                int dip = (int) Math.round(dipT * p.thickness() * 0.6);
                topY -= dip;
                surface = ColumnSpec.Surface.BASIN;
            } else {
                topY += topYNoise(p, wx, wz);
            }

            int rimBottomY = p.center().getY() - p.thickness() + 1;
            int bottomY = rimBottomY;
            if (distNorm > 0.75) {
                double rimT = (distNorm - 0.75) / 0.25;
                bottomY = rimBottomY + (int) Math.round(rimT * p.thickness() * 0.5);
            }
            if (bottomY > topY) bottomY = topY;
            return new ColumnSpec(topY, bottomY, surface);
        }
    },

    /**
     * Three to seven sub-mesas at staggered altitudes. Each sub contributes a
     * bell-curve height field; the column's top Y is the max across all
     * contributing subs. Where two subs overlap, the bells blend instead of
     * step-cutting — joins read as smooth saddles.
     */
    ARCHIPELAGO("archipelago", 1.4f) {
        @Override public int xzExtent(int radius) { return radius + EDGE_AMP; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            RandomSource rng = RandomSource.create(p.shapeSeed());
            int subCount = 3 + rng.nextInt(5);                // 3–7
            int spread   = (int) (p.radius() * 0.55);
            int subRadius   = Math.max(8, (int) (p.radius() * 0.45));
            int subThickness = Math.max(8, p.thickness() * 2 / 3);

            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();

            int bestTopY = Integer.MIN_VALUE;
            int bestThickness = 0;

            for (int i = 0; i < subCount; i++) {
                int sx = rng.nextInt(spread * 2 + 1) - spread;
                int sz = rng.nextInt(spread * 2 + 1) - spread;
                int sy = rng.nextInt(21) - 10;
                int ldx = dx - sx;
                int ldz = dz - sz;
                int sDistSq = ldx * ldx + ldz * ldz;

                // Per-sub edge noise — different seed per sub so subs don't share
                // the same fuzz pattern.
                long subSeed = p.shapeSeed() ^ ((long) i * 0x9E37L);
                float edgeJitter = ShapeNoise.fbm(subSeed, wx, wz, EDGE_SCALE) * EDGE_AMP;
                int effSubR = subRadius + (int) edgeJitter;
                if (effSubR <= 0) continue;
                if (sDistSq > effSubR * (long) effSubR) continue;

                double sDistNorm = Math.sqrt(sDistSq) / effSubR;
                int subTop = p.center().getY() + sy;

                // Bell-curve drop toward sub rim. Quadratic falloff means the centre
                // stays at full height while the rim falls smoothly into neighbours.
                int bellDrop = (int) Math.round(sDistNorm * sDistNorm * 6.0);
                int contributedTopY = subTop - bellDrop;

                int contributedThickness = subThickness;
                if (sDistNorm > 0.6) {
                    contributedThickness = (int) Math.round(subThickness * (1.0 - (sDistNorm - 0.6) * 1.4));
                }
                contributedThickness = Math.max(2, contributedThickness);

                if (contributedTopY > bestTopY) {
                    bestTopY = contributedTopY;
                    bestThickness = contributedThickness;
                }
            }

            if (bestTopY == Integer.MIN_VALUE) return null;
            bestTopY += topYNoise(p, wx, wz);
            return ColumnSpec.solid(bestTopY, bestTopY - bestThickness + 1);
        }
    },

    /**
     * C-shape (annulus with one quadrant removed). Outer rim, inner rim, and
     * the wedge cut are all perturbed so the opening doesn't read as a clean
     * machined slice.
     */
    HORSESHOE("horseshoe", 1.0f) {
        @Override public int xzExtent(int radius) { return radius + EDGE_AMP; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            RandomSource rng = RandomSource.create(p.shapeSeed());
            int openDir = rng.nextInt(4);  // 0=+x  1=-x  2=+z  3=-z

            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;

            float outerJitter = ShapeNoise.fbm(p.shapeSeed(), wx, wz, EDGE_SCALE) * EDGE_AMP;
            float innerJitter = ShapeNoise.fbm(p.shapeSeed() ^ 0x33L, wx, wz, EDGE_SCALE) * INNER_AMP;
            int effOuterR = p.radius() + (int) outerJitter;
            int effInnerR = (int) (p.radius() * 0.45) + (int) innerJitter;
            if (effOuterR <= 0 || effInnerR < 0) return null;
            if (distSq > effOuterR * (long) effOuterR) return null;
            if (distSq < effInnerR * (long) effInnerR) return null;

            // Wedge cut with a noisy boundary — instead of a clean 90° slice, the
            // opening edge wobbles ±WEDGE_NOISE blocks along its length.
            float wedgeJitter = ShapeNoise.fbm(p.shapeSeed() ^ 0x55L, wx, wz, EDGE_SCALE) * WEDGE_NOISE;
            int aDx = Math.abs(dx);
            int aDz = Math.abs(dz);
            boolean inOpening = switch (openDir) {
                case 0  -> dx > 0 && aDz < dx + wedgeJitter;
                case 1  -> dx < 0 && aDz < -dx + wedgeJitter;
                case 2  -> dz > 0 && aDx < dz + wedgeJitter;
                case 3  -> dz < 0 && aDx < -dz + wedgeJitter;
                default -> false;
            };
            if (inOpening) return null;

            double dist = Math.sqrt(distSq);
            double centerline = effOuterR * 0.725;
            double bandHalfWidth = Math.max(1.0, effOuterR * 0.275);
            double offRing = Math.min(1.0, Math.abs(dist - centerline) / bandHalfWidth);

            int topY = p.center().getY() + topYNoise(p, wx, wz);
            int columnThickness = (int) Math.round(p.thickness() * (1.0 - 0.5 * offRing));
            columnThickness = Math.max(2, columnThickness);
            return ColumnSpec.solid(topY, topY - columnThickness + 1);
        }
    },

    /**
     * Long narrow island, ~3:1 aspect ratio, randomly rotated about the center.
     * Footprint extent is the longer axis (1.5 × radius) plus the noise margin.
     */
    RIDGE("ridge", 1.5f) {
        @Override public int xzExtent(int radius) { return (int) (radius * 1.5) + EDGE_AMP; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            RandomSource rng = RandomSource.create(p.shapeSeed());
            double angle = rng.nextDouble() * Math.PI;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            double localX =  dx * cos + dz * sin;
            double localZ = -dx * sin + dz * cos;

            // Edge noise applied to the perpendicular axis so the ridge silhouette
            // isn't a perfect ellipse from above.
            float widthJitter = ShapeNoise.fbm(p.shapeSeed(), wx, wz, EDGE_SCALE) * EDGE_AMP;
            double halfLength = p.radius() * 1.5;
            double halfWidth  = Math.max(2.0, p.radius() * 0.4 + widthJitter);
            double normLen = localX / halfLength;
            double normWid = localZ / halfWidth;
            double dist = normLen * normLen + normWid * normWid;
            if (dist > 1.0) return null;

            int topY = p.center().getY() + topYNoise(p, wx, wz);
            int columnThickness = (int) Math.round(p.thickness() * (1.0 - 0.45 * dist));
            columnThickness = Math.max(2, columnThickness);
            return ColumnSpec.solid(topY, topY - columnThickness + 1);
        }
    },

    /**
     * Outer ring with a shallow basin in the middle. Both rims jitter; rim top
     * carries roll, basin floor stays smooth so water settles cleanly.
     */
    ATOLL("atoll", 0.8f) {
        @Override public int xzExtent(int radius) { return radius + EDGE_AMP; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;

            float outerJitter = ShapeNoise.fbm(p.shapeSeed(), wx, wz, EDGE_SCALE) * EDGE_AMP;
            float innerJitter = ShapeNoise.fbm(p.shapeSeed() ^ 0x77L, wx, wz, EDGE_SCALE) * INNER_AMP;
            int effOuterR = p.radius() + (int) outerJitter;
            int effInnerR = (int) (p.radius() * 0.50) + (int) innerJitter;
            if (effOuterR <= 0) return null;
            if (distSq > effOuterR * (long) effOuterR) return null;

            int rimTopY = p.center().getY();

            if (distSq < effInnerR * (long) effInnerR) {
                int basinTopY = rimTopY - p.thickness() / 4;
                int basinBottomY = basinTopY - 3;
                return ColumnSpec.basin(basinTopY, basinBottomY);
            }

            double dist = Math.sqrt(distSq);
            double distNorm = dist / effOuterR;
            int columnThickness = p.thickness();
            if (distNorm > 0.7) {
                columnThickness = (int) Math.round(p.thickness() * (1.0 - (distNorm - 0.7) * 1.6));
            }
            columnThickness = Math.max(2, columnThickness);
            int topY = rimTopY + topYNoise(p, wx, wz);
            return ColumnSpec.solid(topY, topY - columnThickness + 1);
        }
    };

    /**
     * Outer-edge perturbation amplitude in blocks. Footprint extent is widened
     * by this so noise that pushes the rim outward doesn't fall in a chunk
     * that didn't get assigned a piece.
     */
    static final int EDGE_AMP = 4;

    /** Inner-rim perturbation (basin/ring inner boundaries). */
    static final int INNER_AMP = 3;

    /** Top-surface vertical roll amplitude. */
    static final int TOP_AMP = 2;

    /** Wedge boundary perturbation (HORSESHOE). */
    static final int WEDGE_NOISE = 4;

    /** Spatial frequency of edge / top noise — lower scale = longer wavelength. */
    static final double EDGE_SCALE = 0.06;
    static final double TOP_SCALE  = 0.10;

    private final String serialized;
    private final float weight;

    MegaShape(String serialized, float weight) {
        this.serialized = serialized;
        this.weight = weight;
    }

    public abstract int xzExtent(int radius);

    public abstract ColumnSpec columnAt(MegaIslandParams params, int worldX, int worldZ);

    @Override
    public String getSerializedName() {
        return serialized;
    }

    public static MegaShape pick(RandomSource rng) {
        float total = 0f;
        for (MegaShape s : values()) total += s.weight;
        float roll = rng.nextFloat() * total;
        float cum = 0f;
        for (MegaShape s : values()) {
            cum += s.weight;
            if (roll < cum) return s;
        }
        return PLATEAU;
    }

    /** Top-surface noise shared by every shape's solid columns. */
    static int topYNoise(MegaIslandParams p, int wx, int wz) {
        return Math.round(ShapeNoise.fbm(p.shapeSeed() ^ 0xABCDL, wx, wz, TOP_SCALE) * TOP_AMP);
    }
}
