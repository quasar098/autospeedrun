package name.quasar.autospeedrun.usercode.geometry;

import name.quasar.autospeedrun.usercode.Dimension;

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

    public BlockLocation(Dimension dimension, Vector3 vec) {
        this.dimension = dimension;
        this.x = (long) Math.floor(vec.getX());
        this.y = (long) Math.floor(vec.getY());
        this.z = (long) Math.floor(vec.getZ());
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

    public Dimension getDimension() {
        return dimension;
    }

    public Vector3 getCenter() {
        return new Vector3(getX()+0.5, getY()+0.5, getZ()+0.5);
    }

    public BlockLocation offsetX(long x) { return new BlockLocation(getDimension(), x+getX(), getY(), getZ()); }

    public BlockLocation offsetY(long y) { return new BlockLocation(getDimension(), getX(), y+getY(), getZ()); }

    public BlockLocation offsetZ(long z) { return new BlockLocation(getDimension(), getX(), getY(), z+getZ()); }

    public BlockLocation[] getNeighbors() {
        return new BlockLocation[]{
                offsetX(1),
                offsetX(-1),
                offsetZ(1),
                offsetZ(-1),
                offsetY(1),
                offsetY(-1),
        };
    }
}
