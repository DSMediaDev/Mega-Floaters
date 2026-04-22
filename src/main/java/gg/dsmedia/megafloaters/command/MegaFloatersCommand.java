package gg.dsmedia.megafloaters.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gg.dsmedia.megafloaters.command.sub.InfoCommand;
import gg.dsmedia.megafloaters.command.sub.ListCommand;
import gg.dsmedia.megafloaters.command.sub.PreviewCommand;
import gg.dsmedia.megafloaters.command.sub.RegenCommand;
import gg.dsmedia.megafloaters.command.sub.ReloadCommand;
import gg.dsmedia.megafloaters.command.sub.SpawnCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MegaFloatersCommand {
    public static final String ROOT = "megafloaters";

    private MegaFloatersCommand() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT)
                .requires(src -> src.hasPermission(2))
                .then(ReloadCommand.build())
                .then(SpawnCommand.build())
                .then(ListCommand.build())
                .then(InfoCommand.build())
                .then(RegenCommand.build())
                .then(PreviewCommand.build());
        event.getDispatcher().register(root);
    }
}
