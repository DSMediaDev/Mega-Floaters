package gg.dsmedia.megafloaters.api.palette;

import gg.dsmedia.megafloaters.archetype.FloaterArchetype;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

/**
 * Resolves a {@link SurfacePalette} for a given biome + archetype.
 *
 * <p>Resolution priority:
 * <ol>
 *   <li>Per-biome + per-archetype override (none shipped in v0.1 defaults).</li>
 *   <li>Per-biome default.</li>
 *   <li>Global fallback ({@link #FALLBACK}).</li>
 * </ol>
 *
 * <p>Datapack-driven overrides are planned for a later step. For now the
 * defaults below cover vanilla biomes; all others inherit the grassy fallback.
 */
public final class SurfacePaletteRegistry {

    private SurfacePaletteRegistry() {}

    public static final SurfacePalette FALLBACK = SurfacePalette.of(
            Blocks.GRASS_BLOCK.defaultBlockState(),
            Blocks.DIRT.defaultBlockState(),
            Blocks.STONE.defaultBlockState());

    private static final SurfacePalette DESERT = SurfacePalette.of(
            Blocks.SAND.defaultBlockState(),
            Blocks.SANDSTONE.defaultBlockState(),
            Blocks.STONE.defaultBlockState());

    private static final SurfacePalette BADLANDS = SurfacePalette.of(
            Blocks.RED_SAND.defaultBlockState(),
            Blocks.RED_SANDSTONE.defaultBlockState(),
            Blocks.TERRACOTTA.defaultBlockState());

    private static final SurfacePalette SNOWY = SurfacePalette.of(
            Blocks.SNOW_BLOCK.defaultBlockState(),
            Blocks.STONE.defaultBlockState(),
            Blocks.STONE.defaultBlockState());

    private static final SurfacePalette TAIGA = SurfacePalette.of(
            Blocks.COARSE_DIRT.defaultBlockState(),
            Blocks.DIRT.defaultBlockState(),
            Blocks.STONE.defaultBlockState());

    private static final SurfacePalette MANGROVE = SurfacePalette.of(
            Blocks.MUD.defaultBlockState(),
            Blocks.DIRT.defaultBlockState(),
            Blocks.STONE.defaultBlockState());

    private static final SurfacePalette END = SurfacePalette.of(
            Blocks.END_STONE.defaultBlockState(),
            Blocks.END_STONE.defaultBlockState(),
            Blocks.END_STONE.defaultBlockState());

    public static SurfacePalette select(Holder<Biome> biome, FloaterArchetype archetype) {
        // Specific tags first.
        if (biome.is(BiomeTags.IS_END)) return END;
        if (biome.is(BiomeTags.IS_BADLANDS)) return BADLANDS;

        // Name-based checks for biomes that lack a clean-enough tag.
        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key != null) {
            String path = key.location().getPath();
            if (path.equals("desert")) return DESERT;
            if (path.contains("mangrove")) return MANGROVE;
            if (path.startsWith("snowy_") || path.startsWith("frozen_") || path.equals("ice_spikes")) return SNOWY;
        }

        // Broader tag-based fallback.
        if (biome.is(BiomeTags.IS_TAIGA)) return TAIGA;

        return FALLBACK;
    }
}
