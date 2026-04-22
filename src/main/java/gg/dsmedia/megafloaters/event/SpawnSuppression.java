package gg.dsmedia.megafloaters.event;

import gg.dsmedia.megafloaters.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Cancels natural hostile mob spawns in chunks flagged with
 * {@link ModAttachments#NO_HOSTILES}. Player-triggered spawns (spawners,
 * spawn eggs, commands, dispensers, buckets, mob summons, structures) pass
 * through unchanged so farms and mechanics keep working.
 */
public final class SpawnSuppression {

    private SpawnSuppression() {}

    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) return;

        MobSpawnType reason = event.getSpawnType();
        if (reason != MobSpawnType.NATURAL
                && reason != MobSpawnType.CHUNK_GENERATION
                && reason != MobSpawnType.PATROL) {
            return;
        }

        ServerLevelAccessor level = event.getLevel();
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        ChunkAccess chunk = level.getChunk(pos);

        // ChunkAccess implements IAttachmentHolder at runtime via NeoForge's patch;
        // the compile-time MC jar doesn't expose the interface, hence the cast.
        boolean flagged = ((IAttachmentHolder) chunk).getData(ModAttachments.NO_HOSTILES);
        if (flagged) {
            event.setSpawnCancelled(true);
        }
    }
}
