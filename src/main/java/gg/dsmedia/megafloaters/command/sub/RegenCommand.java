package gg.dsmedia.megafloaters.command.sub;

import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.dsmedia.megafloaters.registry.IslandRecord;
import gg.dsmedia.megafloaters.registry.IslandRegistry;
import gg.dsmedia.megafloaters.worldgen.FloaterFeature;
import gg.dsmedia.megafloaters.worldgen.FloaterFeatureConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Destructive: re-run the feature at each known island center within the
 * radius. Terrain is not cleared first, so re-generated islands stack on
 * top of their originals. The {@code confirm} subnode guards the dispatch.
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
        int chunkRadius = IntegerArgumentType.getInteger(ctx, "chunks");
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        BlockPos center = BlockPos.containing(src.getPosition());

        List<IslandRecord> islands = IslandRegistry.get(level)
                .getIslandsNear(center, chunkRadius * 16);
        for (IslandRecord r : islands) {
            FloaterFeature.generate(level, level.getChunkSource().getGenerator(), r.center(),
                    DEFAULT_CFG, null, 0, 0, level.getRandom());
        }

        final int finalCount = islands.size();
        src.sendSuccess(() -> Component.literal(
                "Regenerated " + finalCount + " island(s). Terrain was overlaid, not replaced.")
                .withStyle(ChatFormatting.YELLOW), true);
        return finalCount;
    }
}
