package gg.dsmedia.megafloaters.event;

import java.util.Optional;

import gg.dsmedia.megafloaters.api.LevititeHarvestedEvent;
import gg.dsmedia.megafloaters.api.RuinOpenedEvent;
import gg.dsmedia.megafloaters.integration.AeronauticsCompat;
import gg.dsmedia.megafloaters.loot.MegaFloatersLootTables;
import gg.dsmedia.megafloaters.registry.IslandRecord;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Two of the three remaining observation events from §5.3:
 *
 * <ul>
 *   <li>{@link RuinOpenedEvent} — fires on right-clicking an unopened ruin
 *       loot chest. Once per chest, because opening a chest consumes its
 *       loot-table reference and subsequent clicks have no table to match.</li>
 *   <li>{@link LevititeHarvestedEvent} — fires when the player right-clicks
 *       a levitite_blend block with an empty bucket near a known floater.</li>
 * </ul>
 *
 * The third event ({@code NestEncounteredEvent}) piggybacks on
 * {@link DiscoveryTracker} and is fired there.
 */
public final class ObservationHooks {

    private ObservationHooks() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        tryRuinOpened(player, level, pos);
        tryLevititeHarvested(player, level, pos, event.getItemStack());
    }

    private static void tryRuinOpened(ServerPlayer player, ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RandomizableContainerBlockEntity chest)) return;
        ResourceKey<LootTable> table = chest.getLootTable();
        if (table == null) return;
        if (!MegaFloatersLootTables.isRuinTable(table)) return;

        Optional<IslandRecord> island = IslandRegistry.get(level).getIslandAt(pos);
        if (island.isEmpty()) return;
        NeoForge.EVENT_BUS.post(new RuinOpenedEvent(player, island.get(), table));
    }

    private static void tryLevititeHarvested(ServerPlayer player, ServerLevel level, BlockPos pos,
                                             ItemStack held) {
        if (!AeronauticsCompat.isActive()) return;
        if (held.getItem() != Items.BUCKET && !(held.getItem() instanceof BucketItem)) return;
        // Empty-bucket only: filled buckets are BucketItem too, so rule them out.
        if (held.getItem() != Items.BUCKET) return;

        BlockState state = level.getBlockState(pos);
        if (!AeronauticsCompat.isLevititeBlock(state)) return;

        Optional<IslandRecord> island = IslandRegistry.get(level).getIslandAt(pos);
        if (island.isEmpty()) return;
        NeoForge.EVENT_BUS.post(new LevititeHarvestedEvent(player, island.get(), pos));
    }
}
