package gg.dsmedia.megafloaters;

import com.mojang.logging.LogUtils;
import gg.dsmedia.megafloaters.command.MegaFloatersCommand;
import gg.dsmedia.megafloaters.integration.AeronauticsCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(MegaFloatersMod.MOD_ID)
public class MegaFloatersMod {
    public static final String MOD_ID = "megafloaters";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MegaFloatersMod(IEventBus modEventBus) {
        ModRegistries.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(MegaFloatersCommand::onRegisterCommands);
        LOGGER.info("Mega Floaters initialising.");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // Optional-mod integrations resolve their block/item ids here, after all
        // mods have finished registering content.
        AeronauticsCompat.init();
    }
}
