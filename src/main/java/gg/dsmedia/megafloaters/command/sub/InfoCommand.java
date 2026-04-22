package gg.dsmedia.megafloaters.command.sub;

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
import net.neoforged.neoforge.attachment.IAttachmentHolder;

/**
 * Reports whether the caller's chunk is a floater chunk. Once the island
 * registry lands this will include archetype, radius, biome, and subfeatures.
 */
public final class InfoCommand {

    private InfoCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("info").executes(InfoCommand::run);
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        BlockPos pos = BlockPos.containing(src.getPosition());
        ChunkAccess chunk = level.getChunk(pos);
        boolean flagged = ((IAttachmentHolder) chunk).getData(ModAttachments.NO_HOSTILES);

        if (flagged) {
            src.sendSuccess(() -> Component.literal(
                    "This chunk is a floater chunk. Hostile spawns are suppressed.")
                    .withStyle(ChatFormatting.GREEN), false);
            return 1;
        }
        src.sendSuccess(() -> Component.literal("No floater in this chunk.")
                .withStyle(ChatFormatting.GRAY), false);
        return 0;
    }
}
