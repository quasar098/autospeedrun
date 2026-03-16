package name.quasar.autospeedrun.usercode;

import com.mojang.math.Vector3f;

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

    public Vector3f toVector3f() {
        return new Vector3f((float) getX(), (float) getY(), (float) getZ());
    }

    public Vector3 withX(double x) { return new Vector3(x, getY(), getZ()); }

    public Vector3 withY(double y) { return new Vector3(getX(), y, getZ()); }

    public Vector3 withZ(double z) { return new Vector3(getX(), getY(), z); }

    public Vector3 offsetX(double x) { return new Vector3(x+getX(), getY(), getZ()); }

    public Vector3 offsetY(double y) { return new Vector3(getX(), y+getY(), getZ()); }

    public Vector3 offsetZ(double z) { return new Vector3(getX(), getY(), z+getZ()); }
}
