package name.quasar.autospeedrun.usercode.geometry;

import name.quasar.autospeedrun.usercode.simulation.Direction;

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

    public double getXSize() { return this.maxX - this.minX; }
    public double getYSize() { return this.maxY - this.minY; }
    public double getZSize() { return this.maxZ - this.minZ; }

    /**
     * beware: may return null
     */
    public AABB intersection(AABB other) {
        double maxX = Math.min(other.maxX, this.maxX);
        double maxY = Math.min(other.maxY, this.maxY);
        double maxZ = Math.min(other.maxZ, this.maxZ);
        double minX = Math.max(other.minX, this.minX);
        double minY = Math.max(other.minY, this.minY);
        double minZ = Math.max(other.minZ, this.minZ);
        return maxX < minX && maxY < minY && maxZ < minZ
            ? new AABB(minX, minY, minZ, maxX, maxY, maxZ) : null;
    }

    public boolean intersects2d(Direction.Axis ignoredAxis, AABB other) {
        Direction.Axis perp1 = ignoredAxis.perp1();
        Direction.Axis perp2 = ignoredAxis.perp2();
        double minOfUpper1 = Math.min(other.max(perp1), this.max(perp1));
        double minOfUpper2 = Math.min(other.max(perp2), this.max(perp2));
        double maxOfLower1 = Math.max(other.min(perp1), this.min(perp1));
        double maxOfLower2 = Math.max(other.min(perp2), this.min(perp2));
        return maxOfLower1 < minOfUpper1 && maxOfLower2 < minOfUpper2;
    }

    public boolean intersects1d(Direction.Axis axis, AABB other) {
        return Math.min(other.max(axis), this.max(axis)) < Math.max(other.min(axis), this.min(axis));
    }

    public double distanceUntilCollision(Direction.Axis axis, AABB other, double d) {
        if (intersects2d(axis, other)) {
            if (d > 0) {
                return Math.min(d, Math.max(0, other.max(axis) - this.min(axis)));
            } else {
                return Math.max(d, Math.min(0, this.max(axis) - other.min(axis)));
            }
        } else {
            return d;
        }
    }

    public double min(Direction.Axis axis) {
        switch (axis) {
            case X:
                return minX;
            case Y:
                return minY;
            case Z:
                return minZ;
        }
        return 0.0;
    }

    public double max(Direction.Axis axis) {
        switch (axis) {
            case X:
                return maxX;
            case Y:
                return maxY;
            case Z:
            default:
                return maxZ;
        }
    }

    public String toString() {
        return String.format(
            "V3<%.3f, %.3f, %.3f, %.3f, %.3f, %.3f>",
            getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ()
        );
    }
}
