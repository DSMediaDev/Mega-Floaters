package gg.dsmedia.megafloaters.api.palette;

import java.util.List;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Vegetation palette for a biome.
 *
 * @param trees             Tree {@link ConfiguredFeature} keys to pick from when placing trees.
 *                          Empty list disables trees entirely.
 * @param maxTrees          Upper bound on trees placed on a single island.
 * @param groundCover       Single-block plants (short grass, flowers, dead bush, etc.) placed
 *                          above the palette top block. Empty list disables ground cover.
 * @param groundCoverDensity Chance per top-surface tile that a ground-cover block is placed.
 */
public record VegetationSpec(List<ResourceKey<ConfiguredFeature<?, ?>>> trees,
                             int maxTrees,
                             List<BlockState> groundCover,
                             float groundCoverDensity) {

    public static final VegetationSpec NONE = new VegetationSpec(List.of(), 0, List.of(), 0.0f);
}
