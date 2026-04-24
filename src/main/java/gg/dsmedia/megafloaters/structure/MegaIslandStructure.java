package gg.dsmedia.megafloaters.structure;

import com.mojang.serialization.MapCodec;
import gg.dsmedia.megafloaters.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Mega-island structure entry point. v0.5.0 scaffold: rolls a fixed-altitude
 * center inside the candidate chunk and emits one zero-extent piece. No blocks
 * are placed yet — the piece's postProcess is a no-op. Phase A.2 fills the
 * geometry in by slicing the island into per-chunk pieces.
 */
public class MegaIslandStructure extends Structure {

    public static final MapCodec<MegaIslandStructure> CODEC = simpleCodec(MegaIslandStructure::new);

    /** Hard-coded scaffold altitude. Phase A.2 will roll from a config band. */
    private static final int SCAFFOLD_ALTITUDE = 220;

    public MegaIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos chunkPos = ctx.chunkPos();
        BlockPos center = new BlockPos(
                chunkPos.getMiddleBlockX(),
                SCAFFOLD_ALTITUDE,
                chunkPos.getMiddleBlockZ());

        return Optional.of(new GenerationStub(center, builder -> {
            BoundingBox bb = new BoundingBox(center.getX(), center.getY(), center.getZ(),
                    center.getX(), center.getY(), center.getZ());
            builder.addPiece(new MegaIslandPiece(0, bb));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModRegistries.MEGA_ISLAND_STRUCTURE.get();
    }
}
