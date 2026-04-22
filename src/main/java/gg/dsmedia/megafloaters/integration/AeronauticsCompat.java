package gg.dsmedia.megafloaters.integration;

import java.util.List;

import gg.dsmedia.megafloaters.MegaFloatersMod;
import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.worldgen.SurfaceScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

/**
 * Optional integration with Create Aeronautics ({@code aeronautics} mod id).
 *
 * <p>When the mod is present, each floater gets a small pool of
 * {@code aeronautics:levitite_blend} on its top surface and a scattering of
 * {@code aeronautics:pearlescent_levitite} blocks on the underside — both
 * cosmetic, with the lore that they are the reason the island floats.
 *
 * <p>If the mod is absent, {@link #isActive()} returns false and every hook
 * becomes a no-op — no errors, no log spam.
 */
public final class AeronauticsCompat {

    public static final String MOD_ID = "aeronautics";

    private static boolean loaded;
    private static boolean initialized;
    private static BlockState levititeBlend;
    private static BlockState pearlescentLevitite;

    private AeronauticsCompat() {}

    /**
     * Resolve the aeronautics block ids. Call from FMLCommonSetupEvent after all
     * mods have registered their blocks.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        loaded = ModList.get().isLoaded(MOD_ID);
        if (!loaded) return;

        Block blend = lookup("levitite_blend");
        Block pearlescent = lookup("pearlescent_levitite");
        if (blend == Blocks.AIR || pearlescent == Blocks.AIR) {
            MegaFloatersMod.LOGGER.warn(
                    "Aeronautics is loaded but expected block ids did not resolve — "
                    + "disabling levitite compat. Expected: aeronautics:levitite_blend, "
                    + "aeronautics:pearlescent_levitite.");
            loaded = false;
            return;
        }
        levititeBlend = blend.defaultBlockState();
        pearlescentLevitite = pearlescent.defaultBlockState();
        MegaFloatersMod.LOGGER.info("Aeronautics detected — levitite pools enabled.");
    }

    public static boolean isActive() {
        return loaded;
    }

    /** True if {@code state}'s block is the resolved levitite_blend fluid block. */
    public static boolean isLevititeBlock(net.minecraft.world.level.block.state.BlockState state) {
        return loaded && state.is(levititeBlend.getBlock());
    }

    /**
     * Carve a circular pool of levitite_blend on the island's top surface. The
     * caller supplies the already-computed top-surface list so we don't rescan.
     *
     * @param tops   Top surface positions from {@link SurfaceScanner#topSurface}.
     * @param radius The island's radius — pool diameter is {@code max(3, radius * 0.4)}.
     */
    public static void placePool(WorldGenLevel level, List<BlockPos> tops, int radius,
                                 RandomSource rng) {
        if (!loaded || tops.isEmpty()) return;

        BlockPos center = pickInterior(level, tops, rng);
        if (center == null) return;

        int poolRadius = Math.max(2, (int) Math.round(Math.max(3, radius * 0.4) / 2.0));
        int rSq = poolRadius * poolRadius;
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int dx = -poolRadius; dx <= poolRadius; dx++) {
            for (int dz = -poolRadius; dz <= poolRadius; dz++) {
                if (dx * dx + dz * dz > rSq) continue;
                mut.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                level.setBlock(mut, levititeBlend, 2);
            }
        }
    }

    /**
     * Scatter pearlescent_levitite blocks on the island's exposed underside at
     * roughly {@code density} of visible-bottom columns.
     */
    public static void embedUnderside(WorldGenLevel level, BlockPos origin, int radius,
                                      int thickness, SurfacePalette palette,
                                      float density, RandomSource rng) {
        if (!loaded) return;

        int searchRadius = radius + 3;
        // Cones/spires extend below the main thickness; search a generous window.
        int searchBottom = origin.getY() - thickness * 3;
        int searchTop = origin.getY() - 1;
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        Block core = palette.coreBlock().getBlock();
        Block underside = palette.undersideBlock().getBlock();

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int y = searchBottom; y <= searchTop; y++) {
                    mut.set(origin.getX() + dx, y, origin.getZ() + dz);
                    Block here = level.getBlockState(mut).getBlock();
                    if (here != core && here != underside) continue;

                    below.set(origin.getX() + dx, y - 1, origin.getZ() + dz);
                    if (!level.isEmptyBlock(below)) break;

                    if (rng.nextFloat() < density) {
                        level.setBlock(mut, pearlescentLevitite, 2);
                    }
                    break;
                }
            }
        }
    }

    private static BlockPos pickInterior(WorldGenLevel level, List<BlockPos> tops, RandomSource rng) {
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos candidate = tops.get(rng.nextInt(tops.size()));
            if (!SurfaceScanner.isRim(level, candidate)) return candidate;
        }
        return tops.isEmpty() ? null : tops.get(rng.nextInt(tops.size()));
    }

    private static Block lookup(String path) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
        Block block = BuiltInRegistries.BLOCK.get(loc);
        if (block == Blocks.AIR) {
            MegaFloatersMod.LOGGER.warn("Aeronautics block {} not found in block registry.", loc);
        }
        return block;
    }
}
