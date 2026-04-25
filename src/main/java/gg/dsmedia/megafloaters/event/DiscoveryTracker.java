package gg.dsmedia.megafloaters.event;

import java.util.List;
import java.util.UUID;

import gg.dsmedia.megafloaters.ModAttachments;
import gg.dsmedia.megafloaters.api.IslandDiscoveredEvent;
import gg.dsmedia.megafloaters.api.NestEncounteredEvent;
import gg.dsmedia.megafloaters.registry.IslandRecord;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Fires {@link IslandDiscoveredEvent} the first time a player enters
 * discovery range of an island they've never been near before.
 *
 * <p>Discovery range: {@code max(32, island.radius())} blocks from the island's
 * center. For small satellite islands this stays at 32 blocks; for mega islands
 * it scales to the island's radius so the event fires when the player first
 * reaches the rim rather than requiring them to walk to the center.
 */
public final class DiscoveryTracker {

    private static final int MIN_DISCOVERY_RADIUS = 32;
    private static final int CHECK_INTERVAL_TICKS = 20;

    // Query radius must cover the largest possible per-island discovery radius.
    // MegaIslandParams.MAX_RADIUS = 100; add a small margin.
    private static final int QUERY_RADIUS = 128;

    private DiscoveryTracker() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        List<IslandRecord> nearby = IslandRegistry.get(level).getIslandsNear(pos, QUERY_RADIUS);
        if (nearby.isEmpty()) return;

        IAttachmentHolder holder = (IAttachmentHolder) player;
        java.util.ArrayList<UUID> discovered =
                new java.util.ArrayList<>(holder.getData(ModAttachments.DISCOVERED_ISLANDS));
        java.util.ArrayList<UUID> encounteredNests =
                new java.util.ArrayList<>(holder.getData(ModAttachments.ENCOUNTERED_NESTS));
        boolean changed = false;

        for (IslandRecord r : nearby) {
            // Per-island threshold: mega islands fire at rim approach, small ones at 32 blocks.
            int discoveryRadius = Math.max(MIN_DISCOVERY_RADIUS, r.radius());
            if (pos.distSqr(r.center()) > (long) discoveryRadius * discoveryRadius) continue;

            if (!discovered.contains(r.id())) {
                discovered.add(r.id());
                changed = true;
                NeoForge.EVENT_BUS.post(new IslandDiscoveredEvent(player, r));
            }
            if (r.hasNest() && !encounteredNests.contains(r.id())) {
                encounteredNests.add(r.id());
                changed = true;
                NeoForge.EVENT_BUS.post(new NestEncounteredEvent(player, r));
            }
        }

        if (changed) {
            holder.setData(ModAttachments.DISCOVERED_ISLANDS, discovered);
            holder.setData(ModAttachments.ENCOUNTERED_NESTS, encounteredNests);
        }
    }
}
