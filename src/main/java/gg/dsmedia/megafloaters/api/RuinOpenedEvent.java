package gg.dsmedia.megafloaters.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.Event;

/**
 * Fired when a player right-clicks an ancient-ruin loot chest for the first
 * time (before its loot table has resolved). Fires exactly once per chest
 * because opening the chest consumes its loot-table reference.
 */
public class RuinOpenedEvent extends Event {

    private final ServerPlayer player;
    private final IslandInfo island;
    private final ResourceKey<LootTable> tier;

    public RuinOpenedEvent(ServerPlayer player, IslandInfo island, ResourceKey<LootTable> tier) {
        this.player = player;
        this.island = island;
        this.tier = tier;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public IslandInfo getIsland() {
        return island;
    }

    /** The loot table tier on the chest — one of the three ruin_* keys. */
    public ResourceKey<LootTable> getTier() {
        return tier;
    }
}
