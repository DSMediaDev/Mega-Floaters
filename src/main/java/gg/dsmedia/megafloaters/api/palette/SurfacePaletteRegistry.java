package gg.dsmedia.megafloaters.api.palette;

import java.util.List;

import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
import gg.dsmedia.megafloaters.data.SurfacePaletteManager;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Resolves a {@link SurfacePalette} for a given biome + archetype.
 *
 * <p>Resolution priority:
 * <ol>
 *   <li>Per-biome + per-archetype override (none shipped in v0.1 defaults).</li>
 *   <li>Per-biome default.</li>
 *   <li>Global fallback ({@link #FALLBACK}).</li>
 * </ol>
 */
public final class SurfacePaletteRegistry {

    private SurfacePaletteRegistry() {}

    // Tree keys (aliases suffixed with _TREE so they don't collide with palette names).
    private static final ResourceKey<ConfiguredFeature<?, ?>> OAK_TREE      = TreeFeatures.OAK;
    private static final ResourceKey<ConfiguredFeature<?, ?>> BIRCH_TREE    = TreeFeatures.BIRCH;
    private static final ResourceKey<ConfiguredFeature<?, ?>> SPRUCE_TREE   = TreeFeatures.SPRUCE;
    private static final ResourceKey<ConfiguredFeature<?, ?>> JUNGLE_TREE   = TreeFeatures.JUNGLE_TREE;
    private static final ResourceKey<ConfiguredFeature<?, ?>> ACACIA_TREE   = TreeFeatures.ACACIA;
    private static final ResourceKey<ConfiguredFeature<?, ?>> MANGROVE_TREE = TreeFeatures.MANGROVE;
    private static final ResourceKey<ConfiguredFeature<?, ?>> DARK_OAK_TREE = TreeFeatures.DARK_OAK;

    // Single-block ground cover.
    private static final BlockState SHORT_GRASS = Blocks.SHORT_GRASS.defaultBlockState();
    private static final BlockState FERN = Blocks.FERN.defaultBlockState();
    private static final BlockState DANDELION = Blocks.DANDELION.defaultBlockState();
    private static final BlockState POPPY = Blocks.POPPY.defaultBlockState();
    private static final BlockState DEAD_BUSH = Blocks.DEAD_BUSH.defaultBlockState();

    // Water specs.
    private static final WaterFeatureSpec WET = new WaterFeatureSpec(0.20f, 0.15f);
    private static final WaterFeatureSpec TAIGA_WATER = new WaterFeatureSpec(0.15f, 0.15f);
    private static final WaterFeatureSpec MANGROVE_WATER = new WaterFeatureSpec(0.35f, 0.30f);

    public static final SurfacePalette FALLBACK = SurfacePalette.of(
            Blocks.GRASS_BLOCK.defaultBlockState(),
            Blocks.DIRT.defaultBlockState(),
            Blocks.STONE.defaultBlockState())
            .withVegetation(new VegetationSpec(List.of(OAK_TREE), 3, List.of(SHORT_GRASS, DANDELION, POPPY), 0.12f))
            .withWater(WET);

    private static final SurfacePalette FOREST = FALLBACK.withVegetation(
            new VegetationSpec(List.of(OAK_TREE, BIRCH_TREE), 3, List.of(SHORT_GRASS, DANDELION, POPPY), 0.18f));

    private static final SurfacePalette DESERT = SurfacePalette.of(
            Blocks.SAND.defaultBlockState(),
            Blocks.SANDSTONE.defaultBlockState(),
            Blocks.STONE.defaultBlockState())
            .withVegetation(new VegetationSpec(List.of(), 0, List.of(DEAD_BUSH), 0.05f));

    private static final SurfacePalette BADLANDS = SurfacePalette.of(
            Blocks.RED_SAND.defaultBlockState(),
            Blocks.RED_SANDSTONE.defaultBlockState(),
            Blocks.TERRACOTTA.defaultBlockState())
            .withVegetation(new VegetationSpec(List.of(), 0, List.of(DEAD_BUSH), 0.05f));

    private static final SurfacePalette SNOWY = SurfacePalette.of(
            Blocks.SNOW_BLOCK.defaultBlockState(),
            Blocks.STONE.defaultBlockState(),
            Blocks.STONE.defaultBlockState())
            .withVegetation(new VegetationSpec(List.of(SPRUCE_TREE), 2, List.of(), 0.0f));

    private static final SurfacePalette TAIGA = SurfacePalette.of(
            Blocks.COARSE_DIRT.defaultBlockState(),
            Blocks.DIRT.defaultBlockState(),
            Blocks.STONE.defaultBlockState())
            .withVegetation(new VegetationSpec(List.of(SPRUCE_TREE), 3, List.of(FERN), 0.12f))
            .withWater(TAIGA_WATER);

    private static final SurfacePalette MANGROVE = SurfacePalette.of(
            Blocks.MUD.defaultBlockState(),
            Blocks.DIRT.defaultBlockState(),
            Blocks.STONE.defaultBlockState())
            .withVegetation(new VegetationSpec(List.of(MANGROVE_TREE, OAK_TREE), 3, List.of(FERN), 0.08f))
            .withWater(MANGROVE_WATER);

    private static final SurfacePalette SAVANNA = FALLBACK.withVegetation(
            new VegetationSpec(List.of(ACACIA_TREE, OAK_TREE), 2, List.of(SHORT_GRASS), 0.15f));

    private static final SurfacePalette JUNGLE = FALLBACK.withVegetation(
            new VegetationSpec(List.of(JUNGLE_TREE, OAK_TREE), 3, List.of(SHORT_GRASS, FERN), 0.25f));

    private static final SurfacePalette DARK_FOREST = FALLBACK.withVegetation(
            new VegetationSpec(List.of(DARK_OAK_TREE, OAK_TREE), 3, List.of(SHORT_GRASS), 0.15f));

    private static final SurfacePalette END = SurfacePalette.of(
            Blocks.END_STONE.defaultBlockState(),
            Blocks.END_STONE.defaultBlockState(),
            Blocks.END_STONE.defaultBlockState());

    public static SurfacePalette select(Holder<Biome> biome, FloaterArchetype archetype) {
        return select(biome, archetype.getSerializedName());
    }

    /**
     * Path-keyed variant used for external archetypes that aren't part of the
     * built-in {@link FloaterArchetype} enum.
     */
    public static SurfacePalette select(Holder<Biome> biome, String archetypePath) {
        // Datapack overrides take priority (biome+archetype first, then biome).
        ResourceLocation biomeId = biome.unwrapKey().map(k -> k.location()).orElse(null);
        if (biomeId != null) {
            var override = SurfacePaletteManager.lookup(biomeId, archetypePath);
            if (override.isPresent()) return override.get();
        }

        if (biome.is(BiomeTags.IS_END)) return END;
        if (biome.is(BiomeTags.IS_BADLANDS)) return BADLANDS;
        if (biome.is(BiomeTags.IS_JUNGLE)) return JUNGLE;
        if (biome.is(BiomeTags.IS_SAVANNA)) return SAVANNA;

        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key != null) {
            String path = key.location().getPath();
            if (path.equals("desert")) return DESERT;
            if (path.contains("mangrove")) return MANGROVE;
            if (path.startsWith("snowy_") || path.startsWith("frozen_") || path.equals("ice_spikes")) return SNOWY;
            if (path.equals("dark_forest") || path.equals("old_growth_birch_forest")) return DARK_FOREST;
            if (path.contains("forest") || path.equals("birch_forest") || path.equals("grove")) return FOREST;
        }

        if (biome.is(BiomeTags.IS_TAIGA)) return TAIGA;

        return FALLBACK;
    }
}
