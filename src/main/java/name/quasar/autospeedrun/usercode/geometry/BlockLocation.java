package name.quasar.autospeedrun.usercode.geometry;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.usercode.Dimension;

import java.util.ArrayList;

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

    public BlockLocation below() { return new BlockLocation(getDimension(), getX(), getY()-1, getZ()); }

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

    public BlockFace[] getFaces() {
        return new BlockFace[]{
            new BlockFace((int) x, (int) y, (int) z, BlockFace.Direction.POS_X),
            new BlockFace((int) x, (int) y, (int) z, BlockFace.Direction.POS_Y),
            new BlockFace((int) x, (int) y, (int) z, BlockFace.Direction.POS_Z),
            new BlockFace((int) x-1, (int) y, (int) z, BlockFace.Direction.POS_X),
            new BlockFace((int) x, (int) y-1, (int) z, BlockFace.Direction.POS_Y),
            new BlockFace((int) x, (int) y, (int) z-1, BlockFace.Direction.POS_Z),
        };
    }

    public DirectedBlockFace[] getDirectedFaces() {
        return new DirectedBlockFace[]{
            new DirectedBlockFace((int) x, (int) y, (int) z, DirectedBlockFace.Direction.POS_X),
            new DirectedBlockFace((int) x, (int) y, (int) z, DirectedBlockFace.Direction.POS_Y),
            new DirectedBlockFace((int) x, (int) y, (int) z, DirectedBlockFace.Direction.POS_Z),
            new DirectedBlockFace((int) x, (int) y, (int) z, DirectedBlockFace.Direction.NEG_X),
            new DirectedBlockFace((int) x, (int) y, (int) z, DirectedBlockFace.Direction.NEG_Y),
            new DirectedBlockFace((int) x, (int) y, (int) z, DirectedBlockFace.Direction.NEG_Z),
        };
    }

    public void debugDrawGreatCircles(Vector3 camera) {
        AutoSpeedrunAPI.chatMessage("great circles");
        ArrayList<GreatCircle> gcs = new ArrayList<>();
        for (Vector3 gcPosition : new Vector3[] { new Vector3(x, y, z),     new Vector3(x+1, y+1, z),
                                                  new Vector3(x+1, y, z+1), new Vector3(x, y+1, z+1) }) {
            double[] ypRadians = gcPosition.sub(camera).toYawAndPitchRadians();
            double gcYaw = ypRadians[0];
            double gcPitch = ypRadians[1];
            Vector3 lookingVector = Vector3.fromRadians(gcYaw, gcPitch).normalized();
            gcs.add(new GreatCircle(Vector3.fromRadians(gcYaw + Math.PI / 2, 0)));
            if (Math.abs(lookingVector.dot(Vector3.POS_X)) > 0.000001) {
                gcs.add(new GreatCircle(lookingVector.cross(Vector3.POS_X).normalized()));
            }
            if (Math.abs(lookingVector.dot(Vector3.POS_Z)) > 0.000001) {
                gcs.add(new GreatCircle(lookingVector.cross(Vector3.POS_Z).normalized()));
            }
        }
        for (GreatCircle gc : gcs) {
            gc.debugDraw(camera);
        }
    }
}
