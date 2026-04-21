package gg.dsmedia.megafloaters.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A 5×5×3 crumbling stone ruin on the island's top surface.
 *
 * <p>Step 7 placeholder — the loot chest is added in step 8, which is why
 * this class just sketches the stone silhouette for now.
 */
public final class AncientRuin {

    private static final BlockState COBBLESTONE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState MOSSY = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private AncientRuin() {}

    public static void place(WorldGenLevel level, BlockPos floorCenter, RandomSource rng) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int baseY = floorCenter.getY() + 1;

        // Solid floor one block above the island's grass layer.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                mut.set(floorCenter.getX() + dx, baseY, floorCenter.getZ() + dz);
                level.setBlock(mut, pickFloor(rng), 2);
            }
        }

        // Two-high perimeter walls with gaps for the "crumbling" look. Every
        // position on the outermost ring rolls 70% to exist.
        for (int dy = 1; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean onEdge = dx == -2 || dx == 2 || dz == -2 || dz == 2;
                    if (!onEdge) continue;
                    if (rng.nextFloat() >= 0.7f) continue;
                    mut.set(floorCenter.getX() + dx, baseY + dy, floorCenter.getZ() + dz);
                    level.setBlock(mut, pickWall(rng), 2);
                }
            }
        }

        // Doorway: clear two blocks on one side so the chest (step 8) is reachable.
        int side = rng.nextInt(4);
        int doorX = floorCenter.getX(), doorZ = floorCenter.getZ();
        switch (side) {
            case 0 -> doorX -= 2;
            case 1 -> doorX += 2;
            case 2 -> doorZ -= 2;
            default -> doorZ += 2;
        }
        mut.set(doorX, baseY + 1, doorZ);
        level.setBlock(mut, AIR, 2);
        mut.set(doorX, baseY + 2, doorZ);
        level.setBlock(mut, AIR, 2);
    }

    private static BlockState pickFloor(RandomSource rng) {
        float r = rng.nextFloat();
        if (r < 0.4f) return MOSSY;
        if (r < 0.8f) return COBBLESTONE;
        return CRACKED;
    }

    private static BlockState pickWall(RandomSource rng) {
        return rng.nextFloat() < 0.5f ? MOSSY : COBBLESTONE;
    }
}
