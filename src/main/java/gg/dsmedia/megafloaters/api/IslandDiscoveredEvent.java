package gg.dsmedia.megafloaters.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Fired on {@code NeoForge.EVENT_BUS} the first time a player comes within
 * discovery range of a given island. Quest mods (FTB Quests etc.) can watch
 * this to award first-discovery bonuses.
 */
public class IslandDiscoveredEvent extends Event {

    private final ServerPlayer player;
    private final IslandInfo island;

    public IslandDiscoveredEvent(ServerPlayer player, IslandInfo island) {
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
