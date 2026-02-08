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
        return String.format("Vector3<%f, %f, %f>", getX(), getY(), getZ());
    }

    public String toString(int prec) {
        return String.format(String.format("Vector3<%%.%df, %%.%df, %%.%df>", prec, prec, prec), getX(), getY(), getZ());
    }
}
