package gg.dsmedia.megafloaters.api.palette;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Surface block palette for a generated floating island.
 *
 * <p>{@code topBlock} is the single grass-height layer. {@code subBlock} is the
 * shallow layer directly beneath it (one block thick in v0.1). {@code coreBlock}
 * is everything between the sub layer and the underside. {@code undersideBlock}
 * is the last {@code undersideDepth} layers at the bottom of the island, which
 * may differ from the core for biomes where an exposed underside should look
 * distinct.
 */
public record SurfacePalette(BlockState topBlock, BlockState subBlock, BlockState coreBlock,
                             BlockState undersideBlock, int undersideDepth) {

    /** Convenience constructor — underside defaults to the core block, depth 2. */
    public static SurfacePalette of(BlockState top, BlockState sub, BlockState core) {
        return new SurfacePalette(top, sub, core, core, 2);
    }
}
