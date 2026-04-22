package gg.dsmedia.megafloaters.event;

import gg.dsmedia.megafloaters.ModAttachments;
import gg.dsmedia.megafloaters.integration.BddCompat;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Two-job spawn listener:
 *
 * <ol>
 *   <li>Cancels natural hostile mob spawns in chunks flagged with
 *       {@link ModAttachments#NO_HOSTILES}. Player-triggered spawns
 *       (spawners, spawn eggs, commands, dispensers, buckets, mob summons,
 *       structures) pass through unchanged so farms and mechanics keep
 *       working.</li>
 *   <li>When Bluedude Dragons is loaded, a dragon attempting to spawn
 *       naturally within 48 blocks of any registered floater island has its
 *       spawn forced through even if it would otherwise have been cancelled
 *       by another listener. Gives players extra dragon encounters near
 *       their sky-bases without flooding the rest of the world.</li>
 * </ol>
 */
public final class SpawnSuppression {

    private static final int DRAGON_BUFF_RADIUS = 48;

    private SpawnSuppression() {}

    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        MobSpawnType reason = event.getSpawnType();
        boolean natural = reason == MobSpawnType.NATURAL
                || reason == MobSpawnType.CHUNK_GENERATION
                || reason == MobSpawnType.PATROL;

        ServerLevelAccessor levelAccessor = event.getLevel();
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

        // Dragon buff first — overrides the hostile suppression for dragon spawns.
        if (BddCompat.isActive() && BddCompat.isDragonEntity(event.getEntity()) && natural) {
            ServerLevel level = levelAccessor.getLevel();
            if (!IslandRegistry.get(level).getIslandsNear(pos, DRAGON_BUFF_RADIUS).isEmpty()) {
                event.setSpawnCancelled(false);
                return;
            }
        }

        // Hostile suppression: only natural spawns of MONSTER-category mobs
        // inside flagged chunks.
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) return;
        if (!natural) return;

        ChunkAccess chunk = levelAccessor.getChunk(pos);
        // ChunkAccess implements IAttachmentHolder at runtime via NeoForge's patch;
        // the compile-time MC jar doesn't expose the interface, hence the cast.
        boolean flagged = ((IAttachmentHolder) chunk).getData(ModAttachments.NO_HOSTILES);
        if (flagged) {
            event.setSpawnCancelled(true);
        }
    }
}
