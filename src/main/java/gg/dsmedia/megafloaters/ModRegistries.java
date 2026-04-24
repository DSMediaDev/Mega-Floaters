package gg.dsmedia.megafloaters;

import gg.dsmedia.megafloaters.structure.MegaIslandPiece;
import gg.dsmedia.megafloaters.structure.MegaIslandStructure;
import gg.dsmedia.megafloaters.worldgen.FloaterFeature;
import gg.dsmedia.megafloaters.worldgen.FloaterFeatureConfig;
import gg.dsmedia.megafloaters.worldgen.placement.ArchipelagoPlacement;
import gg.dsmedia.megafloaters.worldgen.placement.MountainPeakAvoidance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistries {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MegaFloatersMod.MOD_ID);

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, MegaFloatersMod.MOD_ID);

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MegaFloatersMod.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MegaFloatersMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, FloaterFeature> FLOATER =
            FEATURES.register("floater", () -> new FloaterFeature(FloaterFeatureConfig.CODEC));

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ArchipelagoPlacement>> ARCHIPELAGO_PLACEMENT =
            PLACEMENT_MODIFIERS.register("archipelago", () -> () -> ArchipelagoPlacement.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<MountainPeakAvoidance>> MOUNTAIN_PEAK_AVOIDANCE =
            PLACEMENT_MODIFIERS.register("mountain_peak_avoidance", () -> () -> MountainPeakAvoidance.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<MegaIslandStructure>> MEGA_ISLAND_STRUCTURE =
            STRUCTURE_TYPES.register("mega_island", () -> () -> MegaIslandStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> MEGA_ISLAND_PIECE =
            STRUCTURE_PIECE_TYPES.register("mega_island_piece",
                    () -> (StructurePieceType) MegaIslandPiece::new);

    private ModRegistries() {}

    public static void register(IEventBus modEventBus) {
        PLACEMENT_MODIFIERS.register(modEventBus);
        FEATURES.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECE_TYPES.register(modEventBus);
        ModAttachments.register(modEventBus);
    }
}
