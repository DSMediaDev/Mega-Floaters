package gg.dsmedia.megafloaters.worldgen;

import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class IslandBuilder {

    /** How narrow (as a fraction of full radius) the bottom of a tapered disc gets. */
    private static final double TAPER_MIN_FRACTION = 0.5;

    private IslandBuilder() {}

    /** Flat-topped disc; default taperDepth 0 keeps the old flat-bottom geometry. */
    public static void buildDisc(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, SurfacePalette palette, RandomSource rng) {
        buildDisc(level, center, radius, thickness, edgeChance, 0, palette, rng);
    }

    /**
     * Disc with an optional bottom taper. When {@code taperDepth > 0}, the lower
     * {@code taperDepth} layers shrink linearly from full radius at the taper's
     * top edge to {@link #TAPER_MIN_FRACTION} of full radius at the very bottom.
     * Gives small mesa-shaped islands a natural underside instead of a cliff cut.
     */
    public static void buildDisc(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, int taperDepth,
                                 SurfacePalette palette, RandomSource rng) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int effectiveTaper = Math.min(taperDepth, Math.max(0, thickness - 1));

        for (int dy = 0; dy < thickness; dy++) {
            int fromBottom = thickness - 1 - dy;
            int r = radius;
            if (effectiveTaper > 0 && fromBottom < effectiveTaper) {
                double t = effectiveTaper == 0 ? 1.0 : (double) fromBottom / effectiveTaper;
                r = Math.max(1, (int) Math.round(radius * (TAPER_MIN_FRACTION
                        + (1.0 - TAPER_MIN_FRACTION) * t)));
            }
            int rSq = r * r;
            int innerSq = Math.max(0, (r - 1) * (r - 1));
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int distSq = dx * dx + dz * dz;
                    if (distSq > rSq) continue;
                    if (distSq > innerSq && rng.nextFloat() >= edgeChance) continue;
                    mut.set(center.getX() + dx, center.getY() - dy, center.getZ() + dz);
                    level.setBlock(mut, layerBlock(palette, dy, thickness), 2);
                }
            }
        }
    }

    /** Disc top + core-block taper below that narrows to a point ~2×radius deep. */
    public static void buildCone(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, SurfacePalette palette, RandomSource rng) {
        // Cone builds a flat-bottomed disc first, then adds its own stone taper below.
        // Taper depth 0 keeps the disc bottom flat so the stone cone attaches cleanly.
        buildDisc(level, center, radius, thickness, edgeChance, 0, palette, rng);

        int taperDepth = Math.max(4, radius * 2);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int bottomY = center.getY() - thickness + 1;
        BlockState core = palette.coreBlock();

        for (int dy = 1; dy <= taperDepth; dy++) {
            int r = (int) (radius * (1.0 - (double) dy / taperDepth));
            if (r <= 0) break;
            int rSq = r * r;
            int y = bottomY - dy;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > rSq) continue;
                    mut.set(center.getX() + dx, y, center.getZ() + dz);
                    level.setBlock(mut, core, 2);
                }
            }
        }
    }

    /** 3–5 small discs scattered within ±radius/2 of `center`, each ~1/3 the base radius. */
    public static void buildCluster(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                    float edgeChance, SurfacePalette palette, RandomSource rng) {
        int subCount = 3 + rng.nextInt(3);
        int subRadius = Math.max(2, radius / 3);
        int subThickness = Math.max(2, thickness / 2);
        int subTaperDepth = Math.max(1, subThickness / 2);
        int spread = Math.max(1, radius / 2);

        for (int i = 0; i < subCount; i++) {
            int ox = rng.nextInt(spread * 2 + 1) - spread;
            int oz = rng.nextInt(spread * 2 + 1) - spread;
            int oy = rng.nextInt(5) - 2;
            BlockPos sub = center.offset(ox, oy, oz);
            buildDisc(level, sub, subRadius, subThickness, edgeChance, subTaperDepth, palette, rng);
        }
    }

    /** Mesa: cliff-edged disc with a tapered bottom. */
    public static void buildMesa(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 SurfacePalette palette, RandomSource rng) {
        int taperDepth = Math.max(2, thickness / 2);
        buildDisc(level, center, radius, thickness, 1.0f, taperDepth, palette, rng);
    }

    /** Tall narrow cylinder with a slightly wider top-layer cap. */
    public static void buildSpire(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                  float edgeChance, SurfacePalette palette, RandomSource rng) {
        int r = Math.max(2, radius);
        int rSq = r * r;
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy < thickness; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > rSq) continue;
                    mut.set(center.getX() + dx, center.getY() - dy, center.getZ() + dz);
                    level.setBlock(mut, layerBlock(palette, dy, thickness), 2);
                }
            }
        }

        // Cap ring: one extra radius of palette top around the spire's cap.
        int capR = r + 2;
        int capRSq = capR * capR;
        BlockState top = palette.topBlock();
        for (int dx = -capR; dx <= capR; dx++) {
            for (int dz = -capR; dz <= capR; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > capRSq || distSq <= rSq) continue;
                if (rng.nextFloat() >= edgeChance) continue;
                mut.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                level.setBlock(mut, top, 2);
            }
        }
    }

    /**
     * Picks the block for layer {@code dy} from the top. Bottom {@code undersideDepth}
     * layers come from {@link SurfacePalette#undersideBlock()}; above that, the first
     * layer is top, the second is sub, the rest are core.
     */
    private static BlockState layerBlock(SurfacePalette palette, int dy, int thickness) {
        int bottomIndex = thickness - 1 - dy;
        if (bottomIndex < palette.undersideDepth()) return palette.undersideBlock();
        if (dy == 0) return palette.topBlock();
        if (dy == 1) return palette.subBlock();
        return palette.coreBlock();
    }
}
