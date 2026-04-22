package gg.dsmedia.megafloaters.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Fired when a player fills a bucket from the levitite pool on a floater
 * island. Only fires when Create Aeronautics is installed (otherwise no
 * pool exists to harvest).
 */
public class LevititeHarvestedEvent extends Event {

    private final ServerPlayer player;
    private final IslandInfo island;
    private final BlockPos pos;

    public LevititeHarvestedEvent(ServerPlayer player, IslandInfo island, BlockPos pos) {
        this.player = player;
        this.island = island;
        this.pos = pos;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public IslandInfo getIsland() {
        return island;
    }

    /** Block position the bucket was filled from. */
    public BlockPos getPos() {
        return pos;
    }
}
