package gg.dsmedia.megafloaters;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data attachments used by the mod.
 *
 * <ul>
 *   <li>{@link #NO_HOSTILES} on level chunks — set by {@code FloaterFeature}
 *       when an island is placed; consumed by {@code SpawnSuppression} to
 *       cancel natural hostile spawns there.</li>
 *   <li>{@link #DISCOVERED_ISLANDS} on players — a persistent list of
 *       already-seen island UUIDs, used by the discovery tracker to fire
 *       {@code IslandDiscoveredEvent} exactly once per island per player.</li>
 * </ul>
 */
public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MegaFloatersMod.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> NO_HOSTILES =
            ATTACHMENT_TYPES.register("no_hostiles",
                    () -> AttachmentType.builder(() -> Boolean.FALSE)
                            .serialize(Codec.BOOL)
                            .build());

    public static final Supplier<AttachmentType<java.util.List<UUID>>> DISCOVERED_ISLANDS =
            ATTACHMENT_TYPES.register("discovered_islands",
                    () -> AttachmentType.<java.util.List<UUID>>builder(h -> new ArrayList<>())
                            .serialize(UUIDUtil.CODEC.listOf())
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<java.util.List<UUID>>> ENCOUNTERED_NESTS =
            ATTACHMENT_TYPES.register("encountered_nests",
                    () -> AttachmentType.<java.util.List<UUID>>builder(h -> new ArrayList<>())
                            .serialize(UUIDUtil.CODEC.listOf())
                            .copyOnDeath()
                            .build());

    private ModAttachments() {}

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
