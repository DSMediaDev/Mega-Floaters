package gg.dsmedia.megafloaters.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final Map<ResourceLocation, ArchetypeBuilder> EXTERNAL = new ConcurrentHashMap<>();

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
     * Register an externally-defined archetype under the given id. Other mods
     * or script runtimes can use this to add new island shapes that
     * {@code /megafloaters spawn <id>} can drive. Externals are not yet part
     * of the biome-weighted natural-spawn pool — they participate only in
     * explicit placement (commands, the public generate helper, scripting).
     */
    public static void registerArchetype(ResourceLocation id, ArchetypeBuilder builder) {
        ArchetypeBuilder prior = EXTERNAL.put(id, builder);
        if (prior != null) {
            MegaFloatersMod.LOGGER.warn("Archetype {} was registered twice — newer entry wins.", id);
        }
    }

    /** Look up a previously-registered external archetype. */
    public static Optional<ArchetypeBuilder> getArchetype(ResourceLocation id) {
        return Optional.ofNullable(EXTERNAL.get(id));
    }

    /** Snapshot of currently-registered external archetype ids. */
    public static Map<ResourceLocation, ArchetypeBuilder> externalArchetypes() {
        return Collections.unmodifiableMap(EXTERNAL);
    }
}
