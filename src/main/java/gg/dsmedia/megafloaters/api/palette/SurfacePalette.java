package gg.dsmedia.megafloaters.api.palette;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Surface block palette for a generated floating island.
 *
 * <p>{@code topBlock} is the single grass-height layer. {@code subBlock} is the
 * shallow layer directly beneath it (one block thick in v0.1). {@code coreBlock}
 * is everything between the sub layer and the underside. {@code undersideBlock}
 * is the last {@code undersideDepth} layers at the bottom of the island. The
 * {@code vegetation} and {@code waterFeatures} fields describe the sub-features
 * placed after the base shape is generated.
 */
public record SurfacePalette(BlockState topBlock, BlockState subBlock, BlockState coreBlock,
                             BlockState undersideBlock, int undersideDepth,
                             VegetationSpec vegetation, WaterFeatureSpec waterFeatures) {

    /** Convenience constructor — underside defaults to the core block, no subfeatures. */
    public static SurfacePalette of(BlockState top, BlockState sub, BlockState core) {
        return new SurfacePalette(top, sub, core, core, 2, VegetationSpec.NONE, WaterFeatureSpec.DRY);
    }

    /** Return a copy with the supplied vegetation spec. */
    public SurfacePalette withVegetation(VegetationSpec vegetation) {
        return new SurfacePalette(topBlock, subBlock, coreBlock, undersideBlock, undersideDepth,
                vegetation, waterFeatures);
    }

    /** Return a copy with the supplied water-feature spec. */
    public SurfacePalette withWater(WaterFeatureSpec waterFeatures) {
        return new SurfacePalette(topBlock, subBlock, coreBlock, undersideBlock, undersideDepth,
                vegetation, waterFeatures);
    }
}
