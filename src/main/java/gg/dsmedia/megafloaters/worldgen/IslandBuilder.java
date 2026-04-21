package gg.dsmedia.megafloaters.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class IslandBuilder {

    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();

    private IslandBuilder() {}

    /** Flat pancake with grass top, one layer of dirt, then stone. */
    public static void buildDisc(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, RandomSource rng) {
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
                    level.setBlock(mut, layerBlock(dy), 2);
                }
            }
        }
    }

    /** Disc top + stone taper below that narrows to a point ~2×radius deep. */
    public static void buildCone(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, RandomSource rng) {
        buildDisc(level, center, radius, thickness, edgeChance, rng);

        int taperDepth = Math.max(4, radius * 2);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int bottomY = center.getY() - thickness + 1;

        for (int dy = 1; dy <= taperDepth; dy++) {
            int r = (int) (radius * (1.0 - (double) dy / taperDepth));
            if (r <= 0) break;
            int rSq = r * r;
            int y = bottomY - dy;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > rSq) continue;
                    mut.set(center.getX() + dx, y, center.getZ() + dz);
                    level.setBlock(mut, STONE, 2);
                }
            }
        }
    }

    /** 3–5 small discs scattered within ±radius of `center`, each ~1/3 the base radius. */
    public static void buildCluster(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                    float edgeChance, RandomSource rng) {
        int subCount = 3 + rng.nextInt(3);
        int subRadius = Math.max(2, radius / 3);
        int subThickness = Math.max(2, thickness / 2);
        int spread = Math.max(1, radius);

        for (int i = 0; i < subCount; i++) {
            int ox = rng.nextInt(spread * 2 + 1) - spread;
            int oz = rng.nextInt(spread * 2 + 1) - spread;
            int oy = rng.nextInt(5) - 2;
            BlockPos sub = center.offset(ox, oy, oz);
            buildDisc(level, sub, subRadius, subThickness, edgeChance, rng);
        }
    }

    /** Tall narrow cylinder with a slightly wider grass cap on top. */
    public static void buildSpire(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                  float edgeChance, RandomSource rng) {
        int r = Math.max(2, radius);
        int rSq = r * r;
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        for (int dy = 0; dy < thickness; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > rSq) continue;
                    mut.set(center.getX() + dx, center.getY() - dy, center.getZ() + dz);
                    level.setBlock(mut, layerBlock(dy), 2);
                }
            }
        }

        // Cap ring: one extra radius of grass around the top to flare it outward.
        int capR = r + 2;
        int capRSq = capR * capR;
        for (int dx = -capR; dx <= capR; dx++) {
            for (int dz = -capR; dz <= capR; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > capRSq || distSq <= rSq) continue;
                if (rng.nextFloat() >= edgeChance) continue;
                mut.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                level.setBlock(mut, GRASS, 2);
            }
        }
    }

    private static BlockState layerBlock(int dy) {
        if (dy == 0) return GRASS;
        if (dy == 1) return DIRT;
        return STONE;
    }
}
