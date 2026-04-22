package gg.dsmedia.megafloaters.api;

import java.util.List;
import java.util.Optional;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import gg.dsmedia.megafloaters.registry.IslandRecord;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Public entry point for other mods and scripts that want to read floater
 * state or react to floater events.
 *
 * <p>The {@code api} package is held to a stricter semver contract than the
 * rest of the mod: breaking changes bump the minor version pre-1.0 and the
 * major version post-1.0.
 */
public final class MegaFloatersAPI {

    private MegaFloatersAPI() {}

    /**
     * The closest floater island whose center is within 64 blocks of
     * {@code pos}, if any.
     */
    public static Optional<IslandInfo> getIslandAt(ServerLevel level, BlockPos pos) {
        return IslandRegistry.get(level).getIslandAt(pos).map(r -> (IslandInfo) r);
    }

    /**
     * All floater islands whose center is within {@code radiusBlocks} of
     * {@code pos}. The returned list is a snapshot; safe to iterate but not
     * back-synchronised with the registry.
     */
    public static List<IslandInfo> getIslandsNear(ServerLevel level, BlockPos pos, int radiusBlocks) {
        List<IslandRecord> hits = IslandRegistry.get(level).getIslandsNear(pos, radiusBlocks);
        return hits.stream().map(r -> (IslandInfo) r).toList();
    }

    /**
     * The event bus {@link IslandPlacedEvent} is posted on. For convenience;
     * functionally identical to {@code NeoForge.EVENT_BUS}.
     */
    public static IEventBus islandPlacedEvent() {
        return NeoForge.EVENT_BUS;
    }

    /**
     * Reserved — external archetypes are not yet dispatched into the
     * placement flow in v0.1. The call is accepted for forward compatibility
     * and a warning is logged so authors know the registration is a no-op
     * until a later release.
     */
    public static void registerArchetype(ResourceLocation id, ArchetypeBuilder builder) {
        MegaFloatersMod.LOGGER.warn(
                "MegaFloatersAPI.registerArchetype({}) called — external archetypes are not "
                        + "wired into placement in v0.1. The call is accepted but no-op.", id);
    }

    /**
     * Reserved companion to {@link #registerArchetype}. Returns empty in
     * v0.1 since no external archetypes can be registered yet.
     */
    public static Optional<ArchetypeBuilder> getArchetype(ResourceLocation id) {
        return Optional.empty();
    }
}
