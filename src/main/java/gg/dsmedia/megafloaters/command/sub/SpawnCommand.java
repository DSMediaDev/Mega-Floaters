package gg.dsmedia.megafloaters.command.sub;

import java.util.Optional;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import gg.dsmedia.megafloaters.MegaFloatersMod;
import gg.dsmedia.megafloaters.api.ArchetypeBuilder;
import gg.dsmedia.megafloaters.api.MegaFloatersAPI;
import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
import gg.dsmedia.megafloaters.worldgen.FloaterFeature;
import gg.dsmedia.megafloaters.worldgen.FloaterFeatureConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

/**
 * Accepts either a built-in archetype name ({@code disc}, {@code cone},
 * {@code mesa}, {@code cluster}, {@code spire}) or a fully-qualified external
 * archetype id ({@code namespace:path}) registered via
 * {@link MegaFloatersAPI#registerArchetype}.
 */
public final class SpawnCommand {

    private static final FloaterFeatureConfig DEFAULT_CFG = new FloaterFeatureConfig(
            6, 24, 4, 14, 0.6f, true, 0.02f, 0.05f, 1.5f);

    private SpawnCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("spawn")
                .then(Commands.argument("archetype", StringArgumentType.word())
                        .suggests(ARCHETYPE_SUGGESTER)
                        .executes(ctx -> run(ctx, 0))
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
        for (ResourceLocation id : MegaFloatersAPI.externalArchetypes().keySet()) {
            if (id.toString().startsWith(prefix)) {
                builder.suggest(id.toString());
            }
        }
        return builder.buildFuture();
    };

    private static int run(CommandContext<CommandSourceStack> ctx, int size) {
        CommandSourceStack src = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "archetype");

        ServerLevel level = src.getLevel();
        BlockPos pos = BlockPos.containing(src.getPosition());
        int r = size > 0 ? size : 0;
        int t = size > 0 ? Math.max(4, size / 2) : 0;

        if (name.contains(":")) {
            ResourceLocation id;
            try {
                id = ResourceLocation.parse(name);
            } catch (Exception e) {
                src.sendFailure(Component.literal("Invalid archetype id: " + name));
                return 0;
            }
            Optional<ArchetypeBuilder> external = MegaFloatersAPI.getArchetype(id);
            if (external.isEmpty()) {
                src.sendFailure(Component.literal("No archetype registered under " + id));
                return 0;
            }
            FloaterFeature.generateWithBuilder(level, level.getChunkSource().getGenerator(), pos,
                    DEFAULT_CFG, external.get(), id, r, t, level.getRandom());
            src.sendSuccess(() -> Component.literal(
                    "Spawned external " + id + " at " + pos.toShortString())
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        }

        FloaterArchetype arch = parse(name);
        if (arch == null) {
            src.sendFailure(Component.literal("Unknown archetype: " + name));
            return 0;
        }
        FloaterFeature.generate(level, level.getChunkSource().getGenerator(), pos,
                DEFAULT_CFG, arch, r, t, level.getRandom());
        final BlockPos finalPos = pos;
        src.sendSuccess(() -> Component.literal(
                "Spawned " + arch.getSerializedName() + " at " + finalPos.toShortString())
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static FloaterArchetype parse(String name) {
        for (FloaterArchetype a : FloaterArchetype.values()) {
            if (a.getSerializedName().equalsIgnoreCase(name)) return a;
        }
        return null;
    }
}
