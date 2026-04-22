package gg.dsmedia.megafloaters.command.sub;

import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
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
 * Lists floater islands within a chunk radius of the caller, using the
 * island registry. Shows archetype, radius, and center for each entry
 * (truncated to 10 to keep chat output readable).
 */
public final class ListCommand {

    private static final int MAX_LINES = 10;

    private ListCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("list")
                .executes(ctx -> run(ctx, 16))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64))
                        .executes(ctx -> run(ctx, IntegerArgumentType.getInteger(ctx, "radius"))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, int chunkRadius) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        BlockPos center = BlockPos.containing(src.getPosition());

        List<IslandRecord> islands = IslandRegistry.get(level).getIslandsNear(center, chunkRadius * 16);
        src.sendSuccess(() -> Component.literal(
                "Floaters within " + chunkRadius + " chunks: " + islands.size())
                .withStyle(ChatFormatting.AQUA), false);

        int shown = Math.min(islands.size(), MAX_LINES);
        for (int i = 0; i < shown; i++) {
            IslandRecord r = islands.get(i);
            String line = "  " + r.archetype().getPath()
                    + " r=" + r.radius()
                    + " @ " + r.center().toShortString();
            src.sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.GRAY), false);
        }
        if (islands.size() > MAX_LINES) {
            int hidden = islands.size() - MAX_LINES;
            src.sendSuccess(() -> Component.literal("  … and " + hidden + " more")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        }
        return islands.size();
    }
}
