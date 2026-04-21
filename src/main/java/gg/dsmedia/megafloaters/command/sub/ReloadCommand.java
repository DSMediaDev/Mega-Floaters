package gg.dsmedia.megafloaters.command.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class ReloadCommand {
    private ReloadCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("reload").executes(ctx -> {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Mega Floaters config reloaded.")
                            .withStyle(ChatFormatting.GREEN),
                    true);
            return 1;
        });
    }
}
