package gg.dsmedia.megafloaters.archetype;

import com.mojang.serialization.Codec;
import gg.dsmedia.megafloaters.worldgen.IslandBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;

public enum FloaterArchetype implements StringRepresentable {

    DISC("disc") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, RandomSource rng) {
            IslandBuilder.buildDisc(level, center, radius, thickness, edgeChance, rng);
        }
    },
    MESA("mesa") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, RandomSource rng) {
            // Vertical cliff — skip rim thinning entirely.
            IslandBuilder.buildDisc(level, center, radius, thickness, 1.0f, rng);
        }
    },
    CONE("cone") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, RandomSource rng) {
            IslandBuilder.buildCone(level, center, radius, thickness, edgeChance, rng);
        }
    },
    CLUSTER("cluster") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, RandomSource rng) {
            IslandBuilder.buildCluster(level, center, radius, thickness, edgeChance, rng);
        }
        @Override public boolean placesTree() { return false; }
    },
    SPIRE("spire") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, RandomSource rng) {
            IslandBuilder.buildSpire(level, center, radius, thickness, edgeChance, rng);
        }
        // Tall and narrow — multipliers compound with the rolled base size.
        @Override public double radiusMult()    { return 0.35; }
        @Override public double thicknessMult() { return 2.5;  }
        @Override public boolean placesTree()    { return false; }
    };

    public static final Codec<FloaterArchetype> CODEC = StringRepresentable.fromEnum(FloaterArchetype::values);

    private final String serialized;

    FloaterArchetype(String serialized) {
        this.serialized = serialized;
    }

    public abstract void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                               float edgeChance, RandomSource rng);

    public double radiusMult()    { return 1.0; }
    public double thicknessMult() { return 1.0; }
    public boolean placesTree()   { return true; }

    @Override
    public String getSerializedName() {
        return serialized;
    }
}
