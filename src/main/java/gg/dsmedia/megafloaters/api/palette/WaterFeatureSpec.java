package gg.dsmedia.megafloaters.api.palette;

/**
 * Per-palette pond and waterfall probabilities.
 *
 * @param pondChance      Chance per island that a pond is carved into the top surface.
 * @param waterfallChance Chance (conditional on a pond being placed) that a second
 *                        water source is placed at a rim tile, producing a visual waterfall.
 */
public record WaterFeatureSpec(float pondChance, float waterfallChance) {

    public static final WaterFeatureSpec DRY = new WaterFeatureSpec(0.0f, 0.0f);
}
