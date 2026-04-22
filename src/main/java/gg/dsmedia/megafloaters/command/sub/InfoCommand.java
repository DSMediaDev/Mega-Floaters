package gg.dsmedia.megafloaters.command.sub;

import java.util.Optional;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.dsmedia.megafloaters.registry.IslandRecord;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Info about the nearest floater island to the caller (within 64 blocks),
 * as tracked in the island registry.
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

        Optional<IslandRecord> maybe = IslandRegistry.get(level).getIslandAt(pos);
        if (maybe.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No floater within 64 blocks.")
                    .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }

        IslandRecord r = maybe.get();
        src.sendSuccess(() -> Component.literal(
                r.archetype().getPath() + " r=" + r.radius() + " t=" + r.thickness()
                        + " biome=" + r.biome().getPath()
                        + " @ " + r.center().toShortString())
                .withStyle(ChatFormatting.GREEN), false);
        if (r.hasRuin() || r.hasNest() || r.hasLevitite()) {
            StringBuilder features = new StringBuilder("  features:");
            if (r.hasRuin())     features.append(" ruin");
            if (r.hasNest())     features.append(" nest");
            if (r.hasLevitite()) features.append(" levitite");
            final String out = features.toString();
            src.sendSuccess(() -> Component.literal(out).withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }
}
