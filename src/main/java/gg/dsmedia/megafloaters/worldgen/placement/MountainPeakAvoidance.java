package gg.dsmedia.megafloaters.worldgen.placement;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.dsmedia.megafloaters.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Rejects placements whose Y falls within {@code safety_gap} blocks of the
 * ground column's WORLD_SURFACE_WG heightmap. Keeps floaters from grazing
 * mountain peaks — nothing is less magical than an island fused into a
 * stone cliff.
 */
public class MountainPeakAvoidance extends PlacementModifier {

    public static final MapCodec<MountainPeakAvoidance> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.optionalFieldOf("safety_gap", 20).forGetter(p -> p.safetyGap)
    ).apply(inst, MountainPeakAvoidance::new));

    private final int safetyGap;

    public MountainPeakAvoidance(int safetyGap) {
        this.safetyGap = Math.max(0, safetyGap);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource rng, BlockPos pos) {
        int groundTop = ctx.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        if (pos.getY() - groundTop < safetyGap) {
            return Stream.empty();
        }
        return Stream.of(pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModRegistries.MOUNTAIN_PEAK_AVOIDANCE.get();
    }
}
