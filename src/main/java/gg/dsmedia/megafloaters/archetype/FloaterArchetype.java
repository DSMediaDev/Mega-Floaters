package gg.dsmedia.megafloaters.archetype;

import com.mojang.serialization.Codec;
import gg.dsmedia.megafloaters.api.palette.SurfacePalette;
import gg.dsmedia.megafloaters.worldgen.IslandBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;

public enum FloaterArchetype implements StringRepresentable {

    // DISC archetype removed in v0.3.0 — the flat-pancake look landed too uniform
    // once the rarity settled in-world; mesa + cone + cluster + spire cover the
    // visual range without it. External mods can still register a disc-shaped
    // ArchetypeBuilder via MegaFloatersAPI.registerArchetype if they want one.

    MESA("mesa") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, SurfacePalette palette, RandomSource rng) {
            // Vertical cliff — skip rim thinning entirely.
            IslandBuilder.buildDisc(level, center, radius, thickness, 1.0f, palette, rng);
        }
    },
    CONE("cone") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, SurfacePalette palette, RandomSource rng) {
            IslandBuilder.buildCone(level, center, radius, thickness, edgeChance, palette, rng);
        }
    },
    CLUSTER("cluster") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, SurfacePalette palette, RandomSource rng) {
            IslandBuilder.buildCluster(level, center, radius, thickness, edgeChance, palette, rng);
        }
        @Override public boolean placesTree() { return false; }
    },
    SPIRE("spire") {
        @Override
        public void build(WorldGenLevel level, BlockPos center, int radius, int thickness,
                          float edgeChance, SurfacePalette palette, RandomSource rng) {
            IslandBuilder.buildSpire(level, center, radius, thickness, edgeChance, palette, rng);
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
                               float edgeChance, SurfacePalette palette, RandomSource rng);

    public double radiusMult()    { return 1.0; }
    public double thicknessMult() { return 1.0; }
    public boolean placesTree()   { return true; }

    @Override
    public String getSerializedName() {
        return serialized;
    }
}
