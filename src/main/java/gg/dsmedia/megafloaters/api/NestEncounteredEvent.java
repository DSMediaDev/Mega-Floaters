package gg.dsmedia.megafloaters.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Fired the first time a player comes within discovery range of a floater
 * island that carries a dragon nest. Complementary to
 * {@link IslandDiscoveredEvent} — fires once per nest-carrying island per
 * player, in addition to the discovery event for the same island.
 */
public class NestEncounteredEvent extends Event {

    private final ServerPlayer player;
    private final IslandInfo island;

    public NestEncounteredEvent(ServerPlayer player, IslandInfo island) {
        this.player = player;
        this.island = island;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public IslandInfo getIsland() {
        return island;
    }
}
