package gg.dsmedia.megafloaters.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

/**
 * Optional integration with Bluedude Dragons ({@code bdd} mod id).
 *
 * <p>When BDD is installed, dragon nests on floater islands are populated with
 * 1–3 biome-appropriate dragon eggs. Biome → species mapping comes from the
 * plan (§5.2):
 *
 * <ul>
 *   <li>Snowy / frozen → nightfury</li>
 *   <li>Plains / savanna / meadow → deadly nadder</li>
 *   <li>Forest / birch → gronckle</li>
 *   <li>Swamp / mangrove → hideous zippleback</li>
 *   <li>Badlands / desert → monstrous nightmare</li>
 *   <li>Taiga / old-growth → speed stinger</li>
 *   <li>Anywhere else → terrible terror</li>
 * </ul>
 *
 * <p>The plan also specifies a dragon spawn buff near floaters. That requires
 * the island registry (step 13) to locate nearby islands at spawn time, so it
 * is left as a TODO here and wired up in step 13.
 */
public final class BddCompat {

    public static final String MOD_ID = "bdd";

    public enum Species {
        NIGHTFURY("nightfury_egg"),
        DEADLY_NADDER("deadly_nadder_egg"),
        GRONCKLE("gronckle_egg"),
        HIDEOUS_ZIPPLEBACK("hideous_zippleback_egg"),
        MONSTROUS_NIGHTMARE("monstrous_nightmare_egg"),
        SPEED_STINGER("speed_stinger_egg"),
        TERRIBLE_TERROR("terrible_terror_egg");

        private final String path;

        Species(String path) {
            this.path = path;
        }
    }

    private static boolean loaded;
    private static boolean initialized;
    private static Map<Species, BlockState> eggBlocks;

    private BddCompat() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        loaded = ModList.get().isLoaded(MOD_ID);
        if (!loaded) return;

        EnumMap<Species, BlockState> resolved = new EnumMap<>(Species.class);
        for (Species sp : Species.values()) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MOD_ID, sp.path);
            Block block = BuiltInRegistries.BLOCK.get(loc);
            if (block == Blocks.AIR) {
                MegaFloatersMod.LOGGER.warn(
                        "BDD egg block {} did not resolve — disabling BDD compat.", loc);
                loaded = false;
                return;
            }
            resolved.put(sp, block.defaultBlockState());
        }
        eggBlocks = resolved;
        MegaFloatersMod.LOGGER.info("Bluedude Dragons detected — dragon nests will spawn eggs.");
    }

    public static boolean isActive() {
        return loaded;
    }

    /**
     * Heuristic: any entity registered under the {@code bdd} namespace counts
     * as a dragon for spawn-buff purposes. Avoids binding to BDD's (alpha)
     * entity classes, which have changed between releases.
     */
    public static boolean isDragonEntity(net.minecraft.world.entity.Entity entity) {
        net.minecraft.resources.ResourceLocation key = net.minecraft.world.entity.EntityType.getKey(entity.getType());
        return loaded && MOD_ID.equals(key.getNamespace());
    }

    /**
     * Pick a dragon species appropriate to the given biome. Tag checks first,
     * with resource-key path fallbacks for biomes that lack clean tags.
     */
    public static Species speciesForBiome(Holder<Biome> biome) {
        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key != null) {
            String path = key.location().getPath();
            if (path.startsWith("snowy_") || path.startsWith("frozen_") || path.equals("ice_spikes")) {
                return Species.NIGHTFURY;
            }
            if (path.contains("swamp") || path.contains("mangrove")) {
                return Species.HIDEOUS_ZIPPLEBACK;
            }
            if (path.equals("desert")) {
                return Species.MONSTROUS_NIGHTMARE;
            }
            if (path.equals("plains") || path.equals("sunflower_plains") || path.equals("meadow")) {
                return Species.DEADLY_NADDER;
            }
            if (path.contains("old_growth")) {
                return Species.SPEED_STINGER;
            }
        }
        if (biome.is(BiomeTags.IS_SAVANNA))  return Species.DEADLY_NADDER;
        if (biome.is(BiomeTags.IS_BADLANDS)) return Species.MONSTROUS_NIGHTMARE;
        if (biome.is(BiomeTags.IS_TAIGA))    return Species.SPEED_STINGER;
        if (biome.is(BiomeTags.IS_FOREST))   return Species.GRONCKLE;
        return Species.TERRIBLE_TERROR;
    }

    /**
     * Populate an already-placed dragon nest with 1–3 eggs of the biome's
     * species on the sand bed. {@code nestCenter} is the floorCenter passed to
     * {@code DragonNest.place}; the sand bed is at {@code nestCenter.y + 1} and
     * eggs sit one block above.
     */
    public static void populateNest(WorldGenLevel level, BlockPos nestCenter,
                                    Holder<Biome> biome, RandomSource rng) {
        if (!loaded) return;

        Species species = speciesForBiome(biome);
        BlockState egg = eggBlocks.get(species);
        if (egg == null) return;

        int eggY = nestCenter.getY() + 2;
        List<BlockPos> candidates = new ArrayList<>();
        // Match the nest's sand-bed extent — distSq <= (outer-1)² with outer=3 → 4.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz > 4) continue;
                candidates.add(new BlockPos(nestCenter.getX() + dx, eggY, nestCenter.getZ() + dz));
            }
        }
        Collections.shuffle(candidates, new java.util.Random(rng.nextLong()));

        int eggCount = 1 + rng.nextInt(3);
        int placed = 0;
        for (BlockPos pos : candidates) {
            if (placed >= eggCount) break;
            if (!level.isEmptyBlock(pos)) continue;
            level.setBlock(pos, egg, 2);
            placed++;
        }
    }
}
