package gg.dsmedia.megafloaters.command.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.dsmedia.megafloaters.ModAttachments;
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
 * Reports the count of floater chunks within a chunk radius of the caller.
 *
 * <p>Uses the {@code no_hostiles} attachment as a proxy for "contains a
 * floater island". Once the island registry (step 13) lands this will list
 * individual islands with their archetype, radius, and center coordinates.
 */
public final class ListCommand {

    private ListCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("list")
                .executes(ctx -> run(ctx, 16))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64))
                        .executes(ctx -> run(ctx, IntegerArgumentType.getInteger(ctx, "radius"))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, int radius) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        BlockPos center = BlockPos.containing(src.getPosition());
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;

        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkAccess chunk = level.getChunk(cx + dx, cz + dz, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                if (((IAttachmentHolder) chunk).getData(ModAttachments.NO_HOSTILES)) count++;
            }
        }

        final int finalCount = count;
        src.sendSuccess(() -> Component.literal(
                "Floater chunks within " + radius + ": " + finalCount)
                .withStyle(ChatFormatting.AQUA), false);
        return count;
    }
}
