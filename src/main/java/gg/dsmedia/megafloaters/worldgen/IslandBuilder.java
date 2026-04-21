package gg.dsmedia.megafloaters.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class IslandBuilder {

    private IslandBuilder() {}

    public static void buildDisc(WorldGenLevel level, BlockPos center, int radius, int thickness,
                                 float edgeChance, RandomSource rng) {
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();

        int rSq = radius * radius;
        int innerSq = (radius - 1) * (radius - 1);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > rSq) continue;
                // Outermost ring: rim thinning for organic edges.
                if (distSq > innerSq && rng.nextFloat() >= edgeChance) continue;

                for (int dy = 0; dy < thickness; dy++) {
                    mut.set(center.getX() + dx, center.getY() - dy, center.getZ() + dz);
                    BlockState state;
                    if (dy == 0)               state = grass;
                    else if (dy == 1)          state = dirt;
                    else                       state = stone;
                    level.setBlock(mut, state, 2);
                }
            }
        }
    }
}
