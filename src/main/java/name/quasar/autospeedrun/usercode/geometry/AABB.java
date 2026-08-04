package name.quasar.autospeedrun.usercode.geometry;

import net.minecraft.world.phys.Vec3;

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

    public AABB(Vector3 bottomCenter, double horizW, double vertH) {
        this(
            bottomCenter.offsetX(-horizW/2.0).offsetZ(-horizW/2.0),
            bottomCenter.offsetX(horizW/2.0).offsetZ(horizW/2.0).offsetY(vertH)
        );
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

    public AABB move(Vector3 delta) {
        return new AABB(min().add(delta), max().add(delta));
    }

    public AABB move(double dx, double dy, double dz) {
        return move(new Vector3(dx, dy, dz));
    }

    /**
     * get the smallest box that encloses both boxes
     */
    public AABB minmax(AABB other) {
		return new AABB(
            Math.min(this.minX, other.minX), Math.min(this.minY, other.minY), Math.min(this.minZ, other.minZ),
            Math.max(this.maxX, other.maxX), Math.max(this.maxY, other.maxY), Math.max(this.maxZ, other.maxZ)
        );
    }

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
        int i = Double.hashCode(minX);
        i = 31 * i + Double.hashCode(minY);
        i = 31 * i + Double.hashCode(minZ);
        i = 31 * i + Double.hashCode(maxX);
        i = 31 * i + Double.hashCode(maxY);
        return 31 * i + Double.hashCode(maxZ);
    }

    public AABB inflate(double x, double y, double z) {
        return new AABB(
            minX - x, minY - y, minZ - z,
            maxX + x, maxY + y, maxZ + z
        );
    }

    public AABB inflate(double d) {
        return inflate(d, d, d);
    }

    public AABB deflate(double d) {
        return inflate(-d);
    }

    public AABB expandTowards(Vector3 vector3) {
        return expandTowards(vector3.getX(), vector3.getY(), vector3.getZ());
    }

    public AABB expandTowards(double vx, double vy, double vz) {
        double newMinX = this.minX;
        double newMinY = this.minY;
        double newMinZ = this.minZ;
        double newMaxX = this.maxX;
        double newMaxY = this.maxY;
        double newMaxZ = this.maxZ;
        if (vx < 0.0) {
            newMinX += vx;
        } else if (vx > 0.0) {
            newMaxX += vx;
        }

        if (vy < 0.0) {
            newMinY += vy;
        } else if (vy > 0.0) {
            newMaxY += vy;
        }

        if (vz < 0.0) {
            newMinZ += vz;
        } else if (vz > 0.0) {
            newMaxZ += vz;
        }

        return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }
}
