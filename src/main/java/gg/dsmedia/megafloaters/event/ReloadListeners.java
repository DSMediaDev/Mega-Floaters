package gg.dsmedia.megafloaters.event;

import gg.dsmedia.megafloaters.data.SurfacePaletteManager;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * Registers mod resource reload listeners on {@link AddReloadListenerEvent}.
 */
public final class ReloadListeners {

    private ReloadListeners() {}

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SurfacePaletteManager());
    }
}
