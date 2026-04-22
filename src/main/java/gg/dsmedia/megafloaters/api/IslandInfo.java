package gg.dsmedia.megafloaters.api;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Read-only view of a generated floater island. Returned by
 * {@link MegaFloatersAPI#getIslandAt} and {@link MegaFloatersAPI#getIslandsNear}.
 *
 * <p>All values reflect the island at placement time. Archetype and biome are
 * {@link ResourceLocation}s so external mods can match on them without
 * referencing the mod's internal enums or registries.
 */
public interface IslandInfo {

    /** Unique, world-persistent id for this island. */
    UUID id();

    /** Namespaced archetype id, e.g. {@code megafloaters:disc}. */
    ResourceLocation archetype();

    /** The center block position the feature was asked to place at. */
    BlockPos center();

    /** Radius in blocks, after archetype multipliers. */
    int radius();

    /** Thickness in blocks, after archetype multipliers. */
    int thickness();

    /** Biome id at the placement position. */
    ResourceLocation biome();

    /** True if an ancient ruin was placed on this island. */
    boolean hasRuin();

    /** True if a dragon nest was placed on this island. */
    boolean hasNest();

    /**
     * True if a levitite pool and underside embedding were placed on this
     * island by the Aeronautics integration.
     */
    boolean hasLevitite();

    /** {@code level.getGameTime()} when the island was generated. */
    long placedAtTick();
}
