package gg.dsmedia.megafloaters.event;

import java.util.List;
import java.util.UUID;

import gg.dsmedia.megafloaters.ModAttachments;
import gg.dsmedia.megafloaters.api.IslandDiscoveredEvent;
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
 * <p>Discovery range: 32 blocks from the island's center. The per-player
 * discovered-list is persisted via the {@code DISCOVERED_ISLANDS} attachment
 * and survives death and save/load.
 */
public final class DiscoveryTracker {

    private static final int DISCOVERY_RADIUS = 32;
    private static final int CHECK_INTERVAL_TICKS = 20;

    private DiscoveryTracker() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        List<IslandRecord> nearby = IslandRegistry.get(level).getIslandsNear(pos, DISCOVERY_RADIUS);
        if (nearby.isEmpty()) return;

        List<UUID> discovered = ((IAttachmentHolder) player).getData(ModAttachments.DISCOVERED_ISLANDS);
        for (IslandRecord r : nearby) {
            if (discovered.contains(r.id())) continue;
            discovered.add(r.id());
            NeoForge.EVENT_BUS.post(new IslandDiscoveredEvent(player, r));
        }
    }
}
