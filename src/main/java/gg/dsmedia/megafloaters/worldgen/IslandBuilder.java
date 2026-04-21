package gg.dsmedia.megafloaters.worldgen;

import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class IslandBuilder {

    private IslandBuilder() {}

    /** Flat pancake with palette top layer, sub layer, then core. */
    public static void buildDisc(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, SurfacePalette palette, RandomSource rng) {
        int rSq = radius * radius;
        int innerSq = (radius - 1) * (radius - 1);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > rSq) continue;
                if (distSq > innerSq && rng.nextFloat() >= edgeChance) continue;

                for (int dy = 0; dy < thickness; dy++) {
                    mut.set(center.getX() + dx, center.getY() - dy, center.getZ() + dz);
                    level.setBlock(mut, layerBlock(palette, dy, thickness), 2);
                }
            }
        }
    }

    /** Disc top + core-block taper below that narrows to a point ~2×radius deep. */
    public static void buildCone(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, SurfacePalette palette, RandomSource rng) {
        buildDisc(level, center, radius, thickness, edgeChance, palette, rng);

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

    /** 3–5 small discs scattered within ±radius of `center`, each ~1/3 the base radius. */
    public static void buildCluster(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                    float edgeChance, SurfacePalette palette, RandomSource rng) {
        int subCount = 3 + rng.nextInt(3);
        int subRadius = Math.max(2, radius / 3);
        int subThickness = Math.max(2, thickness / 2);
        int spread = Math.max(1, radius);

        for (int i = 0; i < subCount; i++) {
            int ox = rng.nextInt(spread * 2 + 1) - spread;
            int oz = rng.nextInt(spread * 2 + 1) - spread;
            int oy = rng.nextInt(5) - 2;
            BlockPos sub = center.offset(ox, oy, oz);
            buildDisc(level, sub, subRadius, subThickness, edgeChance, palette, rng);
        }
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
