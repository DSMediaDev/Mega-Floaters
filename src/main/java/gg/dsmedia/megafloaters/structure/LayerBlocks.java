package gg.dsmedia.megafloaters.structure;

import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.integration.AeronauticsCompat;
import gg.dsmedia.megafloaters.structure.shape.ColumnSpec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Picks the block for a given (column, layer) position on a mega island. Unlike
 * the small-island {@code IslandBuilder.layerBlock} (three layers and done),
 * this mimics overworld stratigraphy: top → sub → core-with-stone-variants →
 * ore pockets → deepslate transition in thick cores → underside. Pearlescent
 * levitite scatters in stone cores when Aeronautics is loaded.
 *
 * <p>All variation is deterministic from {@code seed} + absolute world coords,
 * so adjacent pieces produce a continuous field and the same island always
 * renders the same way.
 */
public final class LayerBlocks {

    private LayerBlocks() {}

    // --- Stone variants -------------------------------------------------

    private static final BlockState GRANITE  = Blocks.GRANITE.defaultBlockState();
    private static final BlockState DIORITE  = Blocks.DIORITE.defaultBlockState();
    private static final BlockState ANDESITE = Blocks.ANDESITE.defaultBlockState();
    private static final BlockState TUFF     = Blocks.TUFF.defaultBlockState();

    // --- Ores (regular) -------------------------------------------------

    private static final BlockState COAL     = Blocks.COAL_ORE.defaultBlockState();
    private static final BlockState IRON     = Blocks.IRON_ORE.defaultBlockState();
    private static final BlockState COPPER   = Blocks.COPPER_ORE.defaultBlockState();
    private static final BlockState REDSTONE = Blocks.REDSTONE_ORE.defaultBlockState();
    private static final BlockState GOLD     = Blocks.GOLD_ORE.defaultBlockState();
    private static final BlockState LAPIS    = Blocks.LAPIS_ORE.defaultBlockState();
    private static final BlockState DIAMOND  = Blocks.DIAMOND_ORE.defaultBlockState();

    // --- Ores (deepslate) -----------------------------------------------

    private static final BlockState DS_COAL     = Blocks.DEEPSLATE_COAL_ORE.defaultBlockState();
    private static final BlockState DS_IRON     = Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
    private static final BlockState DS_COPPER   = Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
    private static final BlockState DS_REDSTONE = Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
    private static final BlockState DS_GOLD     = Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
    private static final BlockState DS_LAPIS    = Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
    private static final BlockState DS_DIAMOND  = Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();

    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();

    /**
     * Pick the block for a column position.
     *
     * @param seed            Seed key — should differ per island so ore patterns
     *                        don't repeat. The island's structure seed works well.
     * @param palette         Biome-derived surface palette.
     * @param wx, wy, wz      Absolute world coordinates of the block being placed.
     * @param dyFromTop       Layer distance from the column's top (0 = top block).
     * @param columnThickness Total number of layers in this column.
     * @param surface         Whether the column is a solid top or a basin floor.
     * @param exposedToAir    True when at least one cardinal neighbour at this
     *                        Y is air (rim or vertical step). Suppresses ores
     *                        so they don't appear visible from outside the
     *                        island; stone-variant texture and pearlescent
     *                        scatter still apply.
     */
    public static BlockState pick(long seed, SurfacePalette palette,
                                  int wx, int wy, int wz,
                                  int dyFromTop, int columnThickness,
                                  ColumnSpec.Surface surface,
                                  boolean exposedToAir) {
        int dyFromBottom = columnThickness - 1 - dyFromTop;

        // Underside always wins so the island bottom reads as one continuous skin.
        if (dyFromBottom < palette.undersideDepth()) return palette.undersideBlock();

        // Basin floors (CRATER, ATOLL) use the underside block so a fluid fill
        // doesn't sit on grass.
        if (surface == ColumnSpec.Surface.BASIN) return palette.undersideBlock();

        if (dyFromTop == 0) return palette.topBlock();
        if (dyFromTop <= 3) return palette.subBlock();

        // Non-stone cores (badlands → terracotta, end → end_stone) keep their
        // palette core block. Variants, ores, and deepslate only make sense on
        // stone-cored biomes.
        if (!palette.coreBlock().is(Blocks.STONE)) return palette.coreBlock();

        // Deepslate zone: bottom 8 layers of cores ≥20 blocks thick. Thin cores
        // skip the transition entirely so small-end-of-range islands don't become
        // deepslate sandwiches with three core layers.
        boolean deep = columnThickness >= 20 && dyFromBottom < 8 + palette.undersideDepth();

        float oreRoll  = hash3D(seed ^ 0xA1L, wx, wy, wz);
        BlockState ore = pickOre(oreRoll, dyFromTop, deep);
        if (ore != null && !exposedToAir) return ore;

        // Pearlescent sprinkle — flagged "why it floats" in the lore, cheaply
        // visible when strip-mining. Only in the stone core, not in deepslate.
        if (!deep && AeronauticsCompat.isActive()) {
            float lvRoll = hash3D(seed ^ 0xC3L, wx, wy, wz);
            if (lvRoll < 0.003f) return AeronauticsCompat.pearlescentLevitite();
        }

        // Stone-variant speckles. In the deepslate zone, variants give way to
        // tuff — same visual role, correct underlayer.
        float varRoll = hash3D(seed ^ 0xB2L, wx, wy, wz);
        BlockState variant = pickStoneVariant(varRoll);
        if (variant != null) {
            return deep ? TUFF : variant;
        }

        return deep ? DEEPSLATE : palette.coreBlock();
    }

    /**
     * Target density ~4% (~2× overworld baseline). Bands are non-overlapping,
     * so a single hash roll stratifies into the right ore. Each band also gates
     * on {@code dyFromTop} so coal doesn't appear in the underside and diamond
     * doesn't appear 4 blocks below the grass.
     */
    private static BlockState pickOre(float r, int dyFromTop, boolean deep) {
        if (r < 0.016f) return (dyFromTop >= 4  && dyFromTop <= 14) ? (deep ? DS_COAL     : COAL)     : null;
        if (r < 0.028f) return (dyFromTop >= 4)                     ? (deep ? DS_IRON     : IRON)     : null;
        if (r < 0.032f) return (dyFromTop >= 4)                     ? (deep ? DS_COPPER   : COPPER)   : null;
        if (r < 0.037f) return (dyFromTop >= 10)                    ? (deep ? DS_REDSTONE : REDSTONE) : null;
        if (r < 0.040f) return (dyFromTop >= 12)                    ? (deep ? DS_GOLD     : GOLD)     : null;
        if (r < 0.042f) return (dyFromTop >= 8  && dyFromTop <= 22) ? (deep ? DS_LAPIS    : LAPIS)    : null;
        if (r < 0.043f) return (dyFromTop >= 20)                    ? (deep ? DS_DIAMOND  : DIAMOND)  : null;
        return null;
    }

    /** 18% of core positions are a stone variant — visible texture variety. */
    private static BlockState pickStoneVariant(float r) {
        if (r < 0.06f) return GRANITE;
        if (r < 0.12f) return DIORITE;
        if (r < 0.18f) return ANDESITE;
        return null;
    }

    /** True if this palette's top block indicates a grass-topped biome where
     *  hanging vines look natural (jungle, forest, swamp, plains). Filters out
     *  desert/badlands/snow/end where vines would read as out of place. */
    public static boolean supportsHangingVines(SurfacePalette palette) {
        return palette.topBlock().is(Blocks.GRASS_BLOCK);
    }

    /** Deterministic 3D hash in [0, 1). */
    private static float hash3D(long seed, int x, int y, int z) {
        long h = seed;
        h ^= (long) x * 0xC4CEB9FE1A85EC53L;
        h ^= (long) y * 0x9E3779B97F4A7C15L;
        h ^= (long) z * 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return ((h >>> 40)) / (float) (1 << 24);
    }
}
