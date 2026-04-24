package gg.dsmedia.megafloaters.structure.shape;

/**
 * Y range and surface kind for a single column in a mega island.
 *
 * <p>{@link Surface#SOLID} columns get top/sub/core/underside layers based on
 * {@code dy} from {@link #topY}. {@link Surface#BASIN} columns are the floor of
 * an interior depression — their top block uses the underside palette so a
 * sub-feature pass (A.3) can flood the basin with water without sitting on grass.
 */
public record ColumnSpec(int topY, int bottomY, Surface surface) {

    public enum Surface {
        SOLID,
        BASIN
    }

    public static ColumnSpec solid(int topY, int bottomY) {
        return new ColumnSpec(topY, bottomY, Surface.SOLID);
    }

    public static ColumnSpec basin(int topY, int bottomY) {
        return new ColumnSpec(topY, bottomY, Surface.BASIN);
    }

    public int thickness() {
        return topY - bottomY + 1;
    }
}
