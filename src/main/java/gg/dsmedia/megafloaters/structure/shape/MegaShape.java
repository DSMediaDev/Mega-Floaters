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
 */
public enum MegaShape implements StringRepresentable {

    /**
     * Multi-tier mesa. Three concentric tiers step the top down toward the rim
     * (full height in the central 40%, −thickness/8 in the 40–75% band,
     * −thickness/4 in the outer 25%). Bottom tapers in the outermost ring so
     * the rim doesn't read as a vertical cliff.
     */
    PLATEAU("plateau", 2.0f) {
        @Override public int xzExtent(int radius) { return radius; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;
            int rSq = p.radius() * p.radius();
            if (distSq > rSq) return null;

            double distNorm = Math.sqrt(distSq) / p.radius();
            int tierStep = Math.max(1, p.thickness() / 8);
            int tier = distNorm < 0.40 ? 0 : (distNorm < 0.75 ? 1 : 2);
            int topY = p.center().getY() - tier * tierStep;

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
     * Disc with a central depression. The top dips by up to {@code thickness × 0.6}
     * in the inner half, leaving a basin a sub-feature pass can fill with water.
     * Outer rim still tapers underneath.
     */
    CRATER("crater", 1.5f) {
        @Override public int xzExtent(int radius) { return radius; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;
            int rSq = p.radius() * p.radius();
            if (distSq > rSq) return null;

            double distNorm = Math.sqrt(distSq) / p.radius();
            int topY = p.center().getY();

            ColumnSpec.Surface surface = ColumnSpec.Surface.SOLID;
            if (distNorm < 0.55) {
                double dipT = 1.0 - (distNorm / 0.55);
                int dip = (int) Math.round(dipT * p.thickness() * 0.6);
                topY -= dip;
                surface = ColumnSpec.Surface.BASIN;
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
     * Three to seven sub-mesas at staggered altitudes inside the footprint. Each
     * column reports the highest sub it falls inside, so overlapping sub-discs
     * read as merged terraces.
     */
    ARCHIPELAGO("archipelago", 1.4f) {
        @Override public int xzExtent(int radius) { return radius; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            // Reconstruct sub-positions deterministically. Same RNG seed → same subs every call.
            RandomSource rng = RandomSource.create(p.shapeSeed());
            int subCount = 3 + rng.nextInt(5);                // 3–7
            int spread   = (int) (p.radius() * 0.55);
            int subRadius   = Math.max(8, (int) (p.radius() * 0.45));
            int subThickness = Math.max(8, p.thickness() * 2 / 3);

            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int subRSq = subRadius * subRadius;

            ColumnSpec best = null;
            for (int i = 0; i < subCount; i++) {
                int sx = rng.nextInt(spread * 2 + 1) - spread;
                int sz = rng.nextInt(spread * 2 + 1) - spread;
                int sy = rng.nextInt(21) - 10;          // ±10 altitude jitter
                int ldx = dx - sx;
                int ldz = dz - sz;
                int sDistSq = ldx * ldx + ldz * ldz;
                if (sDistSq > subRSq) continue;

                double sDistNorm = Math.sqrt(sDistSq) / subRadius;
                int sTopY = p.center().getY() + sy;
                int sThick = subThickness;
                if (sDistNorm > 0.7) {
                    sThick = (int) Math.round(subThickness * (1.0 - (sDistNorm - 0.7) * 1.6));
                }
                sThick = Math.max(2, sThick);
                ColumnSpec spec = ColumnSpec.solid(sTopY, sTopY - sThick + 1);
                if (best == null || sTopY > best.topY()) best = spec;
            }
            return best;
        }
    },

    /**
     * C-shape (annulus with one quadrant removed). The opening direction is
     * rolled from the shape seed.
     */
    HORSESHOE("horseshoe", 1.0f) {
        @Override public int xzExtent(int radius) { return radius; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            RandomSource rng = RandomSource.create(p.shapeSeed());
            int openDir = rng.nextInt(4);  // 0=+x  1=-x  2=+z  3=-z

            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;
            int outerSq = p.radius() * p.radius();
            int innerR  = (int) (p.radius() * 0.45);
            int innerSq = innerR * innerR;
            if (distSq > outerSq || distSq < innerSq) return null;

            // Cut a 90° wedge facing openDir.
            boolean inOpening = switch (openDir) {
                case 0  -> dx > 0 && Math.abs(dz) < dx;
                case 1  -> dx < 0 && Math.abs(dz) < -dx;
                case 2  -> dz > 0 && Math.abs(dx) < dz;
                case 3  -> dz < 0 && Math.abs(dx) < -dz;
                default -> false;
            };
            if (inOpening) return null;

            // Bottom taper based on distance from the ring's centerline.
            double dist = Math.sqrt(distSq);
            double centerline = p.radius() * 0.725;
            double bandHalfWidth = p.radius() * 0.275;
            double offRing = Math.min(1.0, Math.abs(dist - centerline) / bandHalfWidth);

            int topY = p.center().getY();
            int columnThickness = (int) Math.round(p.thickness() * (1.0 - 0.5 * offRing));
            columnThickness = Math.max(2, columnThickness);
            return ColumnSpec.solid(topY, topY - columnThickness + 1);
        }
    },

    /**
     * Long narrow island, 3:1 aspect ratio, randomly rotated about the center.
     * Footprint extent is the longer axis (1.5 × radius).
     */
    RIDGE("ridge", 1.5f) {
        @Override public int xzExtent(int radius) { return (int) (radius * 1.5); }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            RandomSource rng = RandomSource.create(p.shapeSeed());
            double angle = rng.nextDouble() * Math.PI;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            double localX =  dx * cos + dz * sin;
            double localZ = -dx * sin + dz * cos;

            double halfLength = p.radius() * 1.5;
            double halfWidth  = p.radius() * 0.4;
            double normLen = localX / halfLength;
            double normWid = localZ / halfWidth;
            double dist = normLen * normLen + normWid * normWid;
            if (dist > 1.0) return null;

            int topY = p.center().getY();
            int columnThickness = (int) Math.round(p.thickness() * (1.0 - 0.45 * dist));
            columnThickness = Math.max(2, columnThickness);
            return ColumnSpec.solid(topY, topY - columnThickness + 1);
        }
    },

    /**
     * Ring with a shallow basin in the middle. The basin floor (BASIN surface)
     * sits {@code thickness/4} below the rim — A.3 will fill it with water.
     */
    ATOLL("atoll", 0.8f) {
        @Override public int xzExtent(int radius) { return radius; }
        @Override public ColumnSpec columnAt(MegaIslandParams p, int wx, int wz) {
            int dx = wx - p.center().getX();
            int dz = wz - p.center().getZ();
            int distSq = dx * dx + dz * dz;
            int outerSq = p.radius() * p.radius();
            int innerR  = (int) (p.radius() * 0.50);
            int innerSq = innerR * innerR;
            if (distSq > outerSq) return null;

            int rimTopY = p.center().getY();

            if (distSq < innerSq) {
                int basinTopY = rimTopY - p.thickness() / 4;
                int basinBottomY = basinTopY - 3;
                return ColumnSpec.basin(basinTopY, basinBottomY);
            }

            double dist = Math.sqrt(distSq);
            double distNorm = dist / p.radius();
            int columnThickness = p.thickness();
            if (distNorm > 0.7) {
                columnThickness = (int) Math.round(p.thickness() * (1.0 - (distNorm - 0.7) * 1.6));
            }
            columnThickness = Math.max(2, columnThickness);
            return ColumnSpec.solid(rimTopY, rimTopY - columnThickness + 1);
        }
    };

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
}
