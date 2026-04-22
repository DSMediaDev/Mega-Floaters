package gg.dsmedia.megafloaters.registry;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.dsmedia.megafloaters.api.IslandInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

/**
 * Per-island metadata tracked in the {@link IslandRegistry}. One record is
 * written per generated floater, persisted to the level's SavedData, and
 * queried by commands, the public API, and integration hooks.
 *
 * <p>Implements {@link IslandInfo} directly so the registry can return
 * records from the public API without wrapping.
 */
public record IslandRecord(UUID id, ResourceLocation archetype, BlockPos center,
                           int radius, int thickness, ResourceLocation biome,
                           boolean hasRuin, boolean hasNest, boolean hasLevitite,
                           long placedAtTick) implements IslandInfo {

    public static final Codec<IslandRecord> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(IslandRecord::id),
            ResourceLocation.CODEC.fieldOf("archetype").forGetter(IslandRecord::archetype),
            BlockPos.CODEC.fieldOf("center").forGetter(IslandRecord::center),
            Codec.INT.fieldOf("radius").forGetter(IslandRecord::radius),
            Codec.INT.fieldOf("thickness").forGetter(IslandRecord::thickness),
            ResourceLocation.CODEC.fieldOf("biome").forGetter(IslandRecord::biome),
            Codec.BOOL.fieldOf("has_ruin").forGetter(IslandRecord::hasRuin),
            Codec.BOOL.fieldOf("has_nest").forGetter(IslandRecord::hasNest),
            Codec.BOOL.fieldOf("has_levitite").forGetter(IslandRecord::hasLevitite),
            Codec.LONG.fieldOf("placed_at_tick").forGetter(IslandRecord::placedAtTick)
    ).apply(inst, IslandRecord::new));
}
