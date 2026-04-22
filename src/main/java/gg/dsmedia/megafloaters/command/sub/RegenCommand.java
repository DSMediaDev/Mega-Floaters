package gg.dsmedia.megafloaters.command.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.dsmedia.megafloaters.ModAttachments;
import gg.dsmedia.megafloaters.worldgen.FloaterFeature;
import gg.dsmedia.megafloaters.worldgen.FloaterFeatureConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

/**
 * Destructive: forces a new floater to generate at the center of every
 * floater chunk in a radius around the caller. Existing islands are not
 * cleared first, so the results will overlap visually. The {@code confirm}
 * subnode is required so this isn't run by accident.
 */
public final class RegenCommand {

    private static final FloaterFeatureConfig DEFAULT_CFG = new FloaterFeatureConfig(
            6, 24, 4, 14, 0.6f, true, 0.02f, 0.05f, 1.5f);

    private RegenCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("regen")
                .then(Commands.argument("chunks", IntegerArgumentType.integer(1, 32))
                        .then(Commands.literal("confirm").executes(RegenCommand::run)));
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        int radius = IntegerArgumentType.getInteger(ctx, "chunks");
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        BlockPos center = BlockPos.containing(src.getPosition());
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;

        int regenerated = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkAccess chunk = level.getChunk(cx + dx, cz + dz, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                if (!((IAttachmentHolder) chunk).getData(ModAttachments.NO_HOSTILES)) continue;

                int worldX = (cx + dx) * 16 + 8;
                int worldZ = (cz + dz) * 16 + 8;
                BlockPos origin = new BlockPos(worldX, 220, worldZ);
                FloaterFeature.generate(level, level.getChunkSource().getGenerator(), origin,
                        DEFAULT_CFG, null, 0, 0, level.getRandom());
                regenerated++;
            }
        }

        final int finalCount = regenerated;
        src.sendSuccess(() -> Component.literal(
                "Regenerated " + finalCount + " floater chunk(s). Existing terrain was overlaid, "
                        + "not replaced.")
                .withStyle(ChatFormatting.YELLOW), true);
        return regenerated;
    }
}
