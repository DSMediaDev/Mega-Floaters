package gg.dsmedia.megafloaters.worldgen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class SurfaceScanner {

    private SurfaceScanner() {}

    /**
     * Collect top-surface positions (highest matching {@code topBlock} per XZ column)
     * within a square bounding box around {@code center}.
     *
     * <p>Scans {@code searchHeight} blocks above and below {@code center.y}. Pessimistic
     * but cheap — islands in v0.1 fit comfortably in a 64-block Y envelope.
     */
    public static List<BlockPos> topSurface(WorldGenLevel level, BlockPos center, int searchRadius,
                                            int searchHeight, BlockState topBlock) {
        List<BlockPos> tops = new ArrayList<>();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int yTop = center.getY() + searchHeight;
        int yBottom = center.getY() - searchHeight;
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int y = yTop; y >= yBottom; y--) {
                    mut.set(center.getX() + dx, y, center.getZ() + dz);
                    if (level.getBlockState(mut).equals(topBlock)) {
                        tops.add(mut.immutable());
                        break;
                    }
                }
            }
        }
        return tops;
    }

    /**
     * Returns true if any of {@code pos}'s 4 horizontal neighbors is an air block in
     * {@code level} — i.e. {@code pos} is on the island's rim.
     */
    public static boolean isRim(WorldGenLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos.north())
            || level.isEmptyBlock(pos.south())
            || level.isEmptyBlock(pos.east())
            || level.isEmptyBlock(pos.west());
    }
}
