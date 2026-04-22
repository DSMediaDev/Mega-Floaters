package gg.dsmedia.megafloaters;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data attachments attached to level chunks.
 *
 * <p>{@link #NO_HOSTILES} is a per-chunk boolean set by {@code FloaterFeature}
 * when an island is placed. {@code SpawnSuppression} checks it at
 * {@code FinalizeSpawnEvent} time and cancels natural hostile spawns inside
 * flagged chunks — passive mobs and player-triggered spawns (spawners, eggs,
 * commands) are unaffected.
 */
public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MegaFloatersMod.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> NO_HOSTILES =
            ATTACHMENT_TYPES.register("no_hostiles",
                    () -> AttachmentType.builder(() -> Boolean.FALSE)
                            .serialize(Codec.BOOL)
                            .build());

    private ModAttachments() {}

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
