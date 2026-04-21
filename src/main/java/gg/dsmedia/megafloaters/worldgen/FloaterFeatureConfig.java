package gg.dsmedia.megafloaters.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record FloaterFeatureConfig(int minRadius, int maxRadius,
                                   int minThickness, int maxThickness,
                                   float edgeChance, boolean placeTree)
        implements FeatureConfiguration {

    public static final Codec<FloaterFeatureConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("min_radius").forGetter(FloaterFeatureConfig::minRadius),
            Codec.INT.fieldOf("max_radius").forGetter(FloaterFeatureConfig::maxRadius),
            Codec.INT.fieldOf("min_thickness").forGetter(FloaterFeatureConfig::minThickness),
            Codec.INT.fieldOf("max_thickness").forGetter(FloaterFeatureConfig::maxThickness),
            Codec.FLOAT.fieldOf("edge_chance").forGetter(FloaterFeatureConfig::edgeChance),
            Codec.BOOL.fieldOf("place_tree").forGetter(FloaterFeatureConfig::placeTree)
    ).apply(inst, FloaterFeatureConfig::new));
}
