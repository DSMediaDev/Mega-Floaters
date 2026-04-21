package gg.dsmedia.megafloaters.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record FloaterFeatureConfig(int radius, int thickness, float edgeChance, boolean placeTree)
        implements FeatureConfiguration {

    public static final Codec<FloaterFeatureConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("radius").forGetter(FloaterFeatureConfig::radius),
            Codec.INT.fieldOf("thickness").forGetter(FloaterFeatureConfig::thickness),
            Codec.FLOAT.fieldOf("edge_chance").forGetter(FloaterFeatureConfig::edgeChance),
            Codec.BOOL.fieldOf("place_tree").forGetter(FloaterFeatureConfig::placeTree)
    ).apply(inst, FloaterFeatureConfig::new));
}
