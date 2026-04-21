package gg.dsmedia.megafloaters.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gg.dsmedia.megafloaters.command.sub.ReloadCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MegaFloatersCommand {
    public static final String ROOT = "megafloaters";

    private MegaFloatersCommand() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT)
                .requires(src -> src.hasPermission(2))
                .then(ReloadCommand.build());
        event.getDispatcher().register(root);
    }
}
