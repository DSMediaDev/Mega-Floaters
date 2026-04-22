package gg.dsmedia.megafloaters.api;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/**
 * Fired on {@code NeoForge.EVENT_BUS} after a floater island finishes
 * generating and has been recorded in the island registry. Listeners can
 * use the event to spawn additional content tied to the island or to
 * update external indexes.
 *
 * <p>Not cancellable; the island has already been placed when this fires.
 */
public class IslandPlacedEvent extends Event {

    private final ServerLevel level;
    private final IslandInfo island;

    public IslandPlacedEvent(ServerLevel level, IslandInfo island) {
        this.level = level;
        this.island = island;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public IslandInfo getIsland() {
        return island;
    }
}
