package gg.dsmedia.megafloaters.api;

import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

/**
 * Contract for externally-defined island archetypes.
 *
 * <p>In v0.1 the built-in archetypes (disc, cone, mesa, cluster, spire) are
 * baked into the mod; external registrations via
 * {@link MegaFloatersAPI#registerArchetype} are accepted but not yet wired
 * into the placement flow. The interface ships now so mods can target the
 * stable contract and light up as soon as v0.2's dispatch machinery lands.
 */
public interface ArchetypeBuilder {

    /** Build the island shape into {@code level} around {@code center}. */
    void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
               float edgeChance, SurfacePalette palette, RandomSource rng);

    /** Multiplier applied to the rolled radius before passing to {@link #build}. */
    default double radiusMult() {
        return 1.0;
    }

    /** Multiplier applied to the rolled thickness before passing to {@link #build}. */
    default double thicknessMult() {
        return 1.0;
    }

    /** Whether a tree from the palette's vegetation list should be placed on top. */
    default boolean placesTree() {
        return true;
    }
}
