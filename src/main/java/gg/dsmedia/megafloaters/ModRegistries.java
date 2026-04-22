package gg.dsmedia.megafloaters;

import gg.dsmedia.megafloaters.worldgen.FloaterFeature;
import gg.dsmedia.megafloaters.worldgen.FloaterFeatureConfig;
import gg.dsmedia.megafloaters.worldgen.placement.ArchipelagoPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistries {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MegaFloatersMod.MOD_ID);

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, MegaFloatersMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, FloaterFeature> FLOATER =
            FEATURES.register("floater", () -> new FloaterFeature(FloaterFeatureConfig.CODEC));

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ArchipelagoPlacement>> ARCHIPELAGO_PLACEMENT =
            PLACEMENT_MODIFIERS.register("archipelago", () -> () -> ArchipelagoPlacement.CODEC);

    private ModRegistries() {}

    public static void register(IEventBus modEventBus) {
        PLACEMENT_MODIFIERS.register(modEventBus);
        FEATURES.register(modEventBus);
        ModAttachments.register(modEventBus);
    }
}
