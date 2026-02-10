package name.quasar.autospeedrun.usercode;

public class BlockLocation {
    private final Dimension dimension;
    private final long x;
    private final long y;
    private final long z;

    public BlockLocation(Dimension dimension, long x, long y, long z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        long h = 2203L * Long.hashCode(x);
        h = 2281 * h + Long.hashCode(y);
        h = 3217 * h + Long.hashCode(z);
        return (int) (h << 2) | (dimension == Dimension.OVERWORLD ? 0b00 : dimension == Dimension.NETHER ? 0b01 : 0b10);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (this.getClass() != other.getClass()) return false;
        BlockLocation that = (BlockLocation) other;
        return (this.x == that.x) && (this.y == that.y) && (this.z == that.z) && (this.dimension == that.dimension);
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getZ() {
        return z;
    }

    public String toString() {
        return String.format("BL<%d, %d, %d>", getX(), getY(), getZ());
    }

    public Dimension getWorld() {
        return dimension;
    }
}
