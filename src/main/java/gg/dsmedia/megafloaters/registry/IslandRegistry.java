package gg.dsmedia.megafloaters.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-level registry of generated floater islands. Persisted via
 * {@link SavedData} under {@code megafloaters_island_registry.dat}.
 *
 * <p>Queries are served from a simple grid-bucket spatial index keyed on
 * chunk coordinate — per the plan's §6.3 recommendation for v0.1.
 */
public class IslandRegistry extends SavedData {

    public static final String DATA_NAME = "megafloaters_island_registry";

    private final Map<UUID, IslandRecord> byId = new HashMap<>();
    private final Map<Long, List<UUID>> byChunk = new HashMap<>();

    public IslandRegistry() {}

    public static Factory<IslandRegistry> factory() {
        return new Factory<>(IslandRegistry::new, IslandRegistry::load, null);
    }

    public static IslandRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static IslandRegistry load(CompoundTag tag, HolderLookup.Provider provider) {
        IslandRegistry registry = new IslandRegistry();
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            IslandRecord.CODEC.parse(NbtOps.INSTANCE, list.getCompound(i))
                    .result()
                    .ifPresent(registry::addInternal);
        }
        return registry;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (IslandRecord record : byId.values()) {
            IslandRecord.CODEC.encodeStart(NbtOps.INSTANCE, record)
                    .result()
                    .ifPresent(list::add);
        }
        tag.put("entries", list);
        return tag;
    }

    public void add(IslandRecord record) {
        addInternal(record);
        setDirty();
    }

    private void addInternal(IslandRecord record) {
        byId.put(record.id(), record);
        long hash = chunkHash(record.center().getX() >> 4, record.center().getZ() >> 4);
        byChunk.computeIfAbsent(hash, k -> new ArrayList<>()).add(record.id());
    }

    /** All islands whose center is within {@code radiusBlocks} of {@code pos}. */
    public List<IslandRecord> getIslandsNear(BlockPos pos, int radiusBlocks) {
        int chunkRadius = (radiusBlocks >> 4) + 1;
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        long maxDistSq = (long) radiusBlocks * radiusBlocks;
        List<IslandRecord> results = new ArrayList<>();
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                List<UUID> ids = byChunk.get(chunkHash(cx + dx, cz + dz));
                if (ids == null) continue;
                for (UUID id : ids) {
                    IslandRecord r = byId.get(id);
                    if (r != null && pos.distSqr(r.center()) <= maxDistSq) {
                        results.add(r);
                    }
                }
            }
        }
        return results;
    }

    /** Closest island whose center is within 64 blocks of {@code pos}, if any. */
    public Optional<IslandRecord> getIslandAt(BlockPos pos) {
        List<IslandRecord> nearby = getIslandsNear(pos, 64);
        return nearby.stream().min(Comparator.comparingDouble(r -> pos.distSqr(r.center())));
    }

    public Collection<IslandRecord> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() {
        return byId.size();
    }

    private static long chunkHash(int cx, int cz) {
        return (((long) cx) << 32) | (cz & 0xFFFFFFFFL);
    }
}
