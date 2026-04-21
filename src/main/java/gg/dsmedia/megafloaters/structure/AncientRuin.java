package gg.dsmedia.megafloaters.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * 5×5×3 crumbling stone ruin with a single loot chest at the center.
 *
 * <p>The loot table is picked by the caller (see {@code MegaFloatersLootTables.pickTier}).
 */
public final class AncientRuin {

    private static final BlockState COBBLESTONE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState MOSSY = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState CHEST = Blocks.CHEST.defaultBlockState();

    private AncientRuin() {}

    public static void place(WorldGenLevel level, BlockPos floorCenter,
                             ResourceKey<LootTable> lootTable, RandomSource rng) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int baseY = floorCenter.getY() + 1;

        // Solid floor one block above the island's grass layer.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                mut.set(floorCenter.getX() + dx, baseY, floorCenter.getZ() + dz);
                level.setBlock(mut, pickFloor(rng), 2);
            }
        }

        // Two-high perimeter walls with gaps for the crumbling look.
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

        // Doorway on one random side so the chest is reachable.
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

        // Chest at center of floor, one above the floor surface.
        BlockPos chestPos = new BlockPos(floorCenter.getX(), baseY + 1, floorCenter.getZ());
        level.setBlock(chestPos, CHEST, 2);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(lootTable);
            chest.setLootTableSeed(rng.nextLong());
        }
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
