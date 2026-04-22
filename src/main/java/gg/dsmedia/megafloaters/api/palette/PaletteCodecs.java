package gg.dsmedia.megafloaters.api.palette;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * JSON codecs for datapack-driven palette overrides under
 * {@code data/<ns>/megafloaters/palettes/}.
 *
 * <p>Blocks are encoded as their registry id — {@code "minecraft:grass_block"} —
 * and loaded as the block's default state. Palette authors who need a
 * non-default state can still put BlockState properties in code, but the JSON
 * format covers the overwhelmingly common case.
 */
public final class PaletteCodecs {

    private PaletteCodecs() {}

    /** Codec that maps between BlockState default states and block ids. */
    public static final Codec<BlockState> BLOCK_STATE_ID = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Block::defaultBlockState, BlockState::getBlock);

    /** Codec for ConfiguredFeature resource keys (tree lists). */
    public static final Codec<ResourceKey<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_KEY =
            ResourceKey.codec(Registries.CONFIGURED_FEATURE);

    public static final Codec<VegetationSpec> VEGETATION = RecordCodecBuilder.create(inst -> inst.group(
            CONFIGURED_FEATURE_KEY.listOf().optionalFieldOf("trees", java.util.List.of())
                    .forGetter(VegetationSpec::trees),
            Codec.INT.optionalFieldOf("max_trees", 0).forGetter(VegetationSpec::maxTrees),
            BLOCK_STATE_ID.listOf().optionalFieldOf("ground_cover", java.util.List.of())
                    .forGetter(VegetationSpec::groundCover),
            Codec.FLOAT.optionalFieldOf("ground_cover_density", 0.0f)
                    .forGetter(VegetationSpec::groundCoverDensity)
    ).apply(inst, VegetationSpec::new));

    public static final Codec<WaterFeatureSpec> WATER = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("pond_chance", 0.0f).forGetter(WaterFeatureSpec::pondChance),
            Codec.FLOAT.optionalFieldOf("waterfall_chance", 0.0f).forGetter(WaterFeatureSpec::waterfallChance)
    ).apply(inst, WaterFeatureSpec::new));

    public static final Codec<SurfacePalette> PALETTE = RecordCodecBuilder.create(inst -> inst.group(
            BLOCK_STATE_ID.fieldOf("top_block").forGetter(SurfacePalette::topBlock),
            BLOCK_STATE_ID.fieldOf("sub_block").forGetter(SurfacePalette::subBlock),
            BLOCK_STATE_ID.fieldOf("core_block").forGetter(SurfacePalette::coreBlock),
            BLOCK_STATE_ID.optionalFieldOf("underside_block").forGetter(p -> java.util.Optional.of(p.undersideBlock())),
            Codec.INT.optionalFieldOf("underside_depth", 2).forGetter(SurfacePalette::undersideDepth),
            VEGETATION.optionalFieldOf("vegetation", VegetationSpec.NONE).forGetter(SurfacePalette::vegetation),
            WATER.optionalFieldOf("water", WaterFeatureSpec.DRY).forGetter(SurfacePalette::waterFeatures)
    ).apply(inst, (top, sub, core, underside, undersideDepth, veg, water) ->
            new SurfacePalette(top, sub, core, underside.orElse(core), undersideDepth, veg, water)));
}
