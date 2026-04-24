package gg.dsmedia.megafloaters.structure;

import gg.dsmedia.megafloaters.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * One slice of a mega island. v0.5.0 scaffold places no blocks — Phase A.2 will
 * mask the island geometry to this piece's chunk and write the layered surface
 * inside {@link #postProcess}.
 */
public class MegaIslandPiece extends StructurePiece {

    public MegaIslandPiece(int genDepth, BoundingBox boundingBox) {
        super(ModRegistries.MEGA_ISLAND_PIECE.get(), genDepth, boundingBox);
    }

    public MegaIslandPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModRegistries.MEGA_ISLAND_PIECE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        // No piece-local state to persist in the scaffold.
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator gen,
                            RandomSource rng, BoundingBox writeBounds, ChunkPos chunkPos,
                            BlockPos pos) {
        // Scaffold no-op. Phase A.2 fills in per-chunk geometry here.
    }
}
