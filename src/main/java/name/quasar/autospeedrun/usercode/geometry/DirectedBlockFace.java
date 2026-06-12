package name.quasar.autospeedrun.usercode.geometry;

import name.quasar.autospeedrun.usercode.Dimension;

/**
 * BlockFace but in any direction
 */
public class DirectedBlockFace {
    public enum Direction {
        POS_X,  // east
        POS_Y,
        POS_Z,  // south
        NEG_X,  // west
        NEG_Y,
        NEG_Z   // north
    }

    // todo change these to longs
    private final int x;
    private final int y;
    private final int z;
    private final Direction dir;

    public DirectedBlockFace(int x, int y, int z, Direction dir) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dir = dir;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Direction getDir() {
        return dir;
    }

    public String toString() {
        return String.format("DBF<%d, %d, %d, %s>", getX(), getY(), getZ(), dir.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        DirectedBlockFace other = (DirectedBlockFace) obj;
        return other.x == this.x && other.y == this.y && other.z == this.z && other.dir == this.dir;
    }

    public BlockFace toBlockFace() {
        if (getDir() == Direction.NEG_X) { return new BlockFace(x-1, y, z, BlockFace.Direction.POS_X); }
        if (getDir() == Direction.NEG_Y) { return new BlockFace(x, y-1, z, BlockFace.Direction.POS_Y); }
        if (getDir() == Direction.NEG_Z) { return new BlockFace(x, y, z-1, BlockFace.Direction.POS_Z); }
        if (getDir() == Direction.POS_X) { return new BlockFace(x, y, z, BlockFace.Direction.POS_X); }
        if (getDir() == Direction.POS_Y) { return new BlockFace(x, y, z, BlockFace.Direction.POS_Y); }
        return new BlockFace(x, y, z, BlockFace.Direction.POS_Z);
    }

    public Vector3 getNormal() {
        if (getDir() == Direction.NEG_X) { return new Vector3(-1, 0, 0); }
        if (getDir() == Direction.NEG_Y) { return new Vector3(0, -1, 0); }
        if (getDir() == Direction.NEG_Z) { return new Vector3(0, 0, -1); }
        if (getDir() == Direction.POS_X) { return new Vector3(1, 0, 0); }
        if (getDir() == Direction.POS_Y) { return new Vector3(0, 1, 0); }
        return new Vector3(0, 0, 1);
    }

    public BlockLocation getInsideBlock(Dimension dim) {
        return new BlockLocation(dim, x, y, z);
    }

    public BlockLocation getOutsideBlock(Dimension dim) {
        if (dir == Direction.POS_X) { return new BlockLocation(dim, x+1, y, z); }
        else if (dir == Direction.POS_Y) { return new BlockLocation(dim, x, y+1, z); }
        else if (dir == Direction.POS_Z) { return new BlockLocation(dim, x, y, z+1); }
        else if (dir == Direction.NEG_X) { return new BlockLocation(dim, x-1, y, z); }
        else if (dir == Direction.NEG_Y) { return new BlockLocation(dim, x, y-1, z); }
        else { return new BlockLocation(dim, x, y, z-1); }
    }

    public Vector3 getCenter() {
        if (getDir() == Direction.NEG_X) { return new Vector3(getX()+0.0, getY()+0.5, getZ()+0.5); }
        if (getDir() == Direction.NEG_Y) { return new Vector3(getX()+0.5, getY()+0.0, getZ()+0.5); }
        if (getDir() == Direction.NEG_Z) { return new Vector3(getX()+0.5, getY()+0.5, getZ()+0.0); }
        if (getDir() == Direction.POS_X) { return new Vector3(getX()+1.0, getY()+0.5, getZ()+0.5); }
        if (getDir() == Direction.POS_Y) { return new Vector3(getX()+0.5, getY()+1.0, getZ()+0.5); }
        return new Vector3(getX()+0.5, getY()+0.5, getZ()+1.0);
    }
}
