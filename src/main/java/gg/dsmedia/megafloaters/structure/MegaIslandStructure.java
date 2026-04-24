package gg.dsmedia.megafloaters.structure;

import com.mojang.serialization.MapCodec;
import gg.dsmedia.megafloaters.ModRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Rolls a mega island and emits one {@link MegaIslandPiece} per chunk the island
 * touches. The full set of params is derived from {@code seed} via
 * {@link MegaIslandParams#fromSeed}; the structure stores only {@code seed} and
 * the anchor chunk in each piece, and the same derivation re-runs at postProcess
 * time on every load.
 */
public class MegaIslandStructure extends Structure {

    public static final MapCodec<MegaIslandStructure> CODEC = simpleCodec(MegaIslandStructure::new);

    /** Y margin above/below the rolled island top/bottom for the piece bounding box. */
    private static final int Y_PIECE_MARGIN = 4;

    public MegaIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos anchor = ctx.chunkPos();
        long seed = ctx.seed() ^ anchor.toLong();
        MegaIslandParams params = MegaIslandParams.fromSeed(seed, anchor);

        // Walk every chunk inside the footprint and add a piece for each.
        // Per-piece bounding box is the chunk's XZ extent plus the island's Y range.
        int ext = params.xzExtent();
        int minChunkX = (params.center().getX() - ext) >> 4;
        int maxChunkX = (params.center().getX() + ext) >> 4;
        int minChunkZ = (params.center().getZ() - ext) >> 4;
        int maxChunkZ = (params.center().getZ() + ext) >> 4;

        int yTop    = params.center().getY() + Y_PIECE_MARGIN;
        int yBottom = params.center().getY() - params.thickness() - Y_PIECE_MARGIN;

        return Optional.of(new GenerationStub(params.center(), builder -> {
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    ChunkPos cp = new ChunkPos(cx, cz);
                    if (!params.affectsChunk(cp)) continue;
                    BoundingBox bb = new BoundingBox(
                            cp.getMinBlockX(), yBottom, cp.getMinBlockZ(),
                            cp.getMinBlockX() + 15, yTop, cp.getMinBlockZ() + 15);
                    builder.addPiece(new MegaIslandPiece(0, bb, seed, anchor));
                }
            }
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModRegistries.MEGA_ISLAND_STRUCTURE.get();
    }
}
