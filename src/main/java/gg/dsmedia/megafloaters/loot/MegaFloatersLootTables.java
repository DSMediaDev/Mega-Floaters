package gg.dsmedia.megafloaters.loot;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootTable;

public final class MegaFloatersLootTables {

    public static final ResourceKey<LootTable> RUIN_IRON    = key("ruin_iron");
    public static final ResourceKey<LootTable> RUIN_DIAMOND = key("ruin_diamond");
    public static final ResourceKey<LootTable> RUIN_END     = key("ruin_end");

    private MegaFloatersLootTables() {}

    /** True if the given loot table key is one of the three ruin tiers. */
    public static boolean isRuinTable(ResourceKey<LootTable> key) {
        return key != null
                && (key == RUIN_IRON || key == RUIN_DIAMOND || key == RUIN_END
                        || key.equals(RUIN_IRON) || key.equals(RUIN_DIAMOND) || key.equals(RUIN_END));
    }

    /** Weighted tier roll per the plan: iron 60, diamond 30, end 10. */
    public static ResourceKey<LootTable> pickTier(RandomSource rng) {
        float r = rng.nextFloat();
        if (r < 0.6f) return RUIN_IRON;
        if (r < 0.9f) return RUIN_DIAMOND;
        return RUIN_END;
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(MegaFloatersMod.MOD_ID, path));
    }
}
