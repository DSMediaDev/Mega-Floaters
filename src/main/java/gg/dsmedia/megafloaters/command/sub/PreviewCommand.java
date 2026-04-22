package gg.dsmedia.megafloaters.command.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * One-shot particle outline of what an island of the given archetype would
 * look like at the caller's position. Server sends END_ROD particles to
 * clients in view range — purely cosmetic, no blocks change.
 */
public final class PreviewCommand {

    private PreviewCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("preview")
                .then(Commands.argument("archetype", StringArgumentType.word())
                        .suggests(ARCHETYPE_SUGGESTER)
                        .executes(ctx -> run(ctx, 12))
                        .then(Commands.argument("size", IntegerArgumentType.integer(3, 32))
                                .executes(ctx -> run(ctx, IntegerArgumentType.getInteger(ctx, "size")))));
    }

    private static final SuggestionProvider<CommandSourceStack> ARCHETYPE_SUGGESTER = (ctx, builder) -> {
        String prefix = builder.getRemaining().toLowerCase();
        for (FloaterArchetype a : FloaterArchetype.values()) {
            if (a.getSerializedName().startsWith(prefix)) {
                builder.suggest(a.getSerializedName());
            }
        }
        return builder.buildFuture();
    };

    private static int run(CommandContext<CommandSourceStack> ctx, int size) {
        CommandSourceStack src = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "archetype");
        FloaterArchetype arch = parse(name);
        if (arch == null) {
            src.sendFailure(Component.literal("Unknown archetype: " + name));
            return 0;
        }

        ServerLevel level = src.getLevel();
        BlockPos center = BlockPos.containing(src.getPosition());
        int radius = (int) Math.round(size * arch.radiusMult());
        int thickness = (int) Math.round((size / 2) * arch.thicknessMult());
        radius = Math.max(2, radius);
        thickness = Math.max(2, thickness);

        // Circular rim at top Y
        for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 24) {
            double px = center.getX() + 0.5 + Math.cos(angle) * radius;
            double pz = center.getZ() + 0.5 + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.END_ROD, px, center.getY() + 0.5, pz,
                    2, 0.05, 0.05, 0.05, 0.0);
        }
        // Vertical line at center showing thickness
        for (int dy = 0; dy < thickness; dy++) {
            level.sendParticles(ParticleTypes.END_ROD,
                    center.getX() + 0.5, center.getY() - dy + 0.5, center.getZ() + 0.5,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        final int finalR = radius;
        final int finalT = thickness;
        src.sendSuccess(() -> Component.literal(
                "Preview: " + arch.getSerializedName()
                        + " radius=" + finalR + " thickness=" + finalT)
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }

    private static FloaterArchetype parse(String name) {
        for (FloaterArchetype a : FloaterArchetype.values()) {
            if (a.getSerializedName().equalsIgnoreCase(name)) return a;
        }
        return null;
    }
}
