package gg.dsmedia.megafloaters.worldgen.placement;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.dsmedia.megafloaters.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Replacement for vanilla {@code minecraft:rarity_filter} that adds
 * region-scale archipelago clustering.
 *
 * <p>Each {@code region_size × region_size} chunk region is deterministically
 * classified per world seed:
 *
 * <ul>
 *   <li>{@code void_chance} → emit nothing for this region.</li>
 *   <li>{@code archipelago_chance} → rarity divided by {@code archipelago_multiplier}
 *       (more islands).</li>
 *   <li>otherwise → normal rarity.</li>
 * </ul>
 *
 * Produces visible grouping at the world-map scale without any additional
 * per-island work.
 */
public class ArchipelagoPlacement extends PlacementModifier {

    public static final MapCodec<ArchipelagoPlacement> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            com.mojang.serialization.Codec.INT.optionalFieldOf("region_size", 32)
                    .forGetter(p -> p.regionSize),
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("archipelago_chance", 0.4f)
                    .forGetter(p -> p.archipelagoChance),
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("void_chance", 0.2f)
                    .forGetter(p -> p.voidChance),
            com.mojang.serialization.Codec.INT.optionalFieldOf("base_rarity", 5)
                    .forGetter(p -> p.baseRarity),
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("archipelago_multiplier", 3.0f)
                    .forGetter(p -> p.archipelagoMultiplier)
    ).apply(inst, ArchipelagoPlacement::new));

    private final int regionSize;
    private final float archipelagoChance;
    private final float voidChance;
    private final int baseRarity;
    private final float archipelagoMultiplier;

    public ArchipelagoPlacement(int regionSize, float archipelagoChance, float voidChance,
                                int baseRarity, float archipelagoMultiplier) {
        this.regionSize = Math.max(4, regionSize);
        this.archipelagoChance = archipelagoChance;
        this.voidChance = voidChance;
        this.baseRarity = Math.max(1, baseRarity);
        this.archipelagoMultiplier = Math.max(1.0f, archipelagoMultiplier);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource rng, BlockPos pos) {
        int regionX = Math.floorDiv(pos.getX() >> 4, regionSize);
        int regionZ = Math.floorDiv(pos.getZ() >> 4, regionSize);
        // Derivation matches Minecraft's constants for random per-position seeds —
        // gives us a stable float per (rx, rz) without needing the world seed.
        long regionHash = ((long) regionX * 341873128712L) ^ ((long) regionZ * 132897987541L);
        RandomSource regionRng = RandomSource.create(regionHash);
        float roll = regionRng.nextFloat();

        float effectiveRarity;
        if (roll < voidChance) {
            return Stream.empty();
        } else if (roll < voidChance + archipelagoChance) {
            effectiveRarity = baseRarity / archipelagoMultiplier;
        } else {
            effectiveRarity = baseRarity;
        }

        if (rng.nextFloat() < 1.0f / effectiveRarity) {
            return Stream.of(pos);
        }
        return Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModRegistries.ARCHIPELAGO_PLACEMENT.get();
    }
}
