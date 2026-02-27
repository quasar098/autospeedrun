package name.quasar.autospeedrun.usercode;

public class Vector3 {
    private final double x;
    private final double y;
    private final double z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String toString() {
        return String.format("V3<%f, %f, %f>", getX(), getY(), getZ());
    }

    public String toString(int prec) {
        return String.format(String.format("V3<%%.%df, %%.%df, %%.%df>", prec, prec, prec), getX(), getY(), getZ());
    }

    public Double distanceToSquared(Vector3 other) {
        return (other.getZ() - getZ()) * (other.getZ() - getZ())
            + (other.getY() - getY()) * (other.getY() - getY())
            + (other.getX() - getX()) * (other.getX() - getX());
    }

    public Double distanceTo2dSquared(Vector3 other) {
        return (other.getZ() - getZ()) * (other.getZ() - getZ())
            + (other.getX() - getX()) * (other.getX() - getX());
    }

    public Double distanceTo2d(Vector3 other) {
        return Math.sqrt(distanceTo2dSquared(other));
    }

    public Double distanceTo(Vector3 other) {
        return Math.sqrt(distanceToSquared(other));
    }
}
