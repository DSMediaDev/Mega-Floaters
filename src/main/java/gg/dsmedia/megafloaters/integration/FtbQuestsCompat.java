package gg.dsmedia.megafloaters.integration;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import net.neoforged.fml.ModList;

/**
 * Soft integration with FTB Quests ({@code ftbquests} mod id).
 *
 * <p>Mega Floaters doesn't bundle or depend on FTB Quests' API. Quest packs
 * should watch {@code gg.dsmedia.megafloaters.api.IslandDiscoveredEvent} via
 * FTB Quests' NeoForge-event task type (or via KubeJS if scripted). This
 * compat class exists so operators see a confirmation log line when the
 * integration "lights up".
 */
public final class FtbQuestsCompat {

    public static final String MOD_ID = "ftbquests";

    private static boolean loaded;
    private static boolean initialized;

    private FtbQuestsCompat() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        loaded = ModList.get().isLoaded(MOD_ID);
        if (loaded) {
            MegaFloatersMod.LOGGER.info(
                    "FTB Quests detected — quest packs can watch IslandDiscoveredEvent for "
                            + "first-discovery tasks.");
        }
    }

    public static boolean isActive() {
        return loaded;
    }
}
