package name.quasar.autospeedrun.usercode.geometry;

/**
 * axis aligned bounding box. largely similar to net.minecraft.world.phys.AABB
 */
public class AABB {

    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;

    public AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public AABB(Vector3 min, Vector3 max) {
        this(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public Vector3 min() { return new Vector3(minX, minY, minZ); }
    public Vector3 max() { return new Vector3(maxX, maxY, maxZ); }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof AABB)) {
            return false;
        } else {
            AABB other = (AABB) (object);
            return getMinX() == other.getMinX()
                && getMinY() == other.getMinY()
                && getMinZ() == other.getMinZ()
                && getMaxX() == other.getMaxX()
                && getMaxY() == other.getMaxY()
                && getMaxZ() == other.getMaxZ();
        }
    }

    public int hashCode() {
        int i = Double.hashCode(this.minX);
        i = 31 * i + Double.hashCode(this.minY);
        i = 31 * i + Double.hashCode(this.minZ);
        i = 31 * i + Double.hashCode(this.maxX);
        i = 31 * i + Double.hashCode(this.maxY);
        return 31 * i + Double.hashCode(this.maxZ);
    }
}
