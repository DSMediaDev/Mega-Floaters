package gg.dsmedia.megafloaters.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Empty dragon nest — a low cobblestone rim around a sand floor.
 *
 * <p>Step 7 placeholder. BDD egg blocks are added by the BDD integration in
 * step 10, which looks for this structure via the island registry and drops
 * 1–3 biome-appropriate eggs onto the sand.
 */
public final class DragonNest {

    private static final BlockState COBBLESTONE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();

    private DragonNest() {}

    public static void place(WorldGenLevel level, BlockPos nestCenter, RandomSource rng) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int y = nestCenter.getY() + 1;
        int outer = 3;
        int outerSq = outer * outer;
        int innerSq = (outer - 1) * (outer - 1);

        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > outerSq) continue;

                mut.set(nestCenter.getX() + dx, y, nestCenter.getZ() + dz);
                if (distSq > innerSq) {
                    // Outer ring: cobblestone rim, occasional gap.
                    if (rng.nextFloat() < 0.85f) {
                        level.setBlock(mut, COBBLESTONE, 2);
                    }
                } else {
                    // Interior: sand bed for eggs.
                    level.setBlock(mut, SAND, 2);
                }
            }
        }
    }
}
