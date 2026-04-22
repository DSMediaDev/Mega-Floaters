package gg.dsmedia.megafloaters.integration;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import net.neoforged.fml.ModList;

/**
 * Soft integration with KubeJS ({@code kubejs} mod id).
 *
 * <p>Mega Floaters does not bundle KubeJS's (Rhino-based) API, but all of
 * {@link gg.dsmedia.megafloaters.api.MegaFloatersAPI} consists of plain static
 * methods on public classes — KubeJS server scripts can call them directly
 * by resource-resolving the class. See the README for example snippets.
 *
 * <p>Listeners for {@link gg.dsmedia.megafloaters.api.IslandPlacedEvent} can
 * be registered from KubeJS via its {@code NeoForgeEvents.onEvent(...)}
 * wrapper on the NeoForge event bus.
 */
public final class KubeJSCompat {

    public static final String MOD_ID = "kubejs";

    private static boolean loaded;
    private static boolean initialized;

    private KubeJSCompat() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        loaded = ModList.get().isLoaded(MOD_ID);
        if (loaded) {
            MegaFloatersMod.LOGGER.info(
                    "KubeJS detected — MegaFloatersAPI is callable from server_scripts. "
                            + "See README for examples.");
        }
    }

    public static boolean isActive() {
        return loaded;
    }
}
