package gg.dsmedia.megafloaters.event;

import gg.dsmedia.megafloaters.ModAttachments;
import gg.dsmedia.megafloaters.integration.BddCompat;
import net.minecraft.core.BlockPos;
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
 *   <li>When Bluedude Dragons is loaded, blocks all NATURAL, CHUNK_GENERATION,
 *       and PATROL spawns of {@code bdd:*} entities. This is a v0.4.1
 *       workaround for a BDD v1.3.0-alpha bug where {@code BddAbilityDragon.fireProjectile}
 *       loads the client-only {@code BddKeybinds} class on dedicated servers
 *       and crashes the tick. Egg hatches, breeding, and command spawns still
 *       go through — only unexpected wild dragons are blocked until upstream
 *       fixes the projectile path.</li>
 * </ol>
 */
public final class SpawnSuppression {

    private SpawnSuppression() {}

    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        MobSpawnType reason = event.getSpawnType();
        boolean natural = reason == MobSpawnType.NATURAL
                || reason == MobSpawnType.CHUNK_GENERATION
                || reason == MobSpawnType.PATROL;

        ServerLevelAccessor levelAccessor = event.getLevel();
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

        // BDD v1.3.0-alpha has a server-unsafe fire-projectile path that loads a
        // client-only keybinds class at runtime, crashing the server tick. Until
        // upstream fixes it, we block all natural/patrol BDD spawns so new wild
        // dragons stop appearing. Egg-hatches, command spawns, spawn eggs, and
        // breeding still go through — players who hatch or tame dragons keep
        // them, and the crash is only when a wild dragon engages unexpectedly.
        if (BddCompat.isActive() && BddCompat.isDragonEntity(event.getEntity()) && natural) {
            event.setSpawnCancelled(true);
            return;
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
