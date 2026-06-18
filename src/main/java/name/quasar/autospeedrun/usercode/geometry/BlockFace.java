package name.quasar.autospeedrun.usercode.geometry;

import com.mojang.math.Vector3f;
import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.DebugRenderLine;
import name.quasar.autospeedrun.usercode.Dimension;

/** rip off of scripts/3dvis/main.py BlockFace */
public class BlockFace {
    public enum Direction {
        POS_X,  // east
        POS_Y,
        POS_Z  // south
    }

    // todo change these to longs
    private final int x;
    private final int y;
    private final int z;
    private final Direction dir;

    public BlockFace(int x, int y, int z, Direction dir) {
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
        return String.format("BF<%d, %d, %d, %s>", getX(), getY(), getZ(), dir.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BlockFace other = (BlockFace) obj;
        return other.x == this.x && other.y == this.y && other.z == this.z && other.dir == this.dir;
    }

    public BlockLocation getAdjacentA(Dimension dim) {
        return new BlockLocation(dim, x, y, z);
    }

    public BlockLocation getAdjacentB(Dimension dim) {
        if (dir == Direction.POS_X) { return new BlockLocation(dim, x+1, y, z); }
        else if (dir == Direction.POS_Y) { return new BlockLocation(dim, x, y+1, z); }
        else { return new BlockLocation(dim, x, y, z+1); }
    }

    public DirectedBlockFace toDirectedBlockFace(boolean negative) {
        if (negative) {
            if (getDir() == Direction.POS_X) {
                return new DirectedBlockFace(x+1, y, z, DirectedBlockFace.Direction.NEG_X);
            } else if (getDir() == Direction.POS_Y) {
                return new DirectedBlockFace(x, y+1, z, DirectedBlockFace.Direction.NEG_Y);
            } else {
                return new DirectedBlockFace(x, y, z+1, DirectedBlockFace.Direction.NEG_Z);
            }
        } else {
            if (getDir() == Direction.POS_X) {
                return new DirectedBlockFace(x, y, z, DirectedBlockFace.Direction.POS_X);
            } else if (getDir() == Direction.POS_Y) {
                return new DirectedBlockFace(x, y, z, DirectedBlockFace.Direction.POS_Y);
            } else {
                return new DirectedBlockFace(x, y, z, DirectedBlockFace.Direction.POS_Z);
            }
        }
    }

    public void debugDraw() { debugDraw(0.0f, 1.0f, 0.68f, 0.77f); }
    
    public void debugDraw(float s, float r, float g, float b) {
        // s is the shrink amount
        if (dir == Direction.POS_X) {
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+s, z+s), new Vector3f(x+1f-s, y+1f-s, z+s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+1f-s, z+s), new Vector3f(x+1f-s, y+1f-s, z+1f-s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+1f-s, z+1f-s), new Vector3f(x+1f-s, y+s, z+1f-s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+s, z+1f-s), new Vector3f(x+1f-s, y+s, z+s), r, g, b
            ));
        } else if (dir == Direction.POS_Y) {
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+s, y+1f-s, z+s), new Vector3f(x+1f-s, y+1f-s, z+s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+1f-s, z+s), new Vector3f(x+1f-s, y+1f-s, z+1f-s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+1f-s, z+1f-s), new Vector3f(x+s, y+1f-s, z+1f-s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+s, y+1f-s, z+1f-s), new Vector3f(x+s, y+1f-s, z+s), r, g, b
            ));
        } else {  // (dir == Direction.POS_Z)
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+s, y+s, z+1f-s), new Vector3f(x+1f-s, y+s, z+1f-s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+s, z+1f-s), new Vector3f(x+1f-s, y+1f-s, z+1f-s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+1f-s, y+1f-s, z+1f-s), new Vector3f(x+s, y+1f-s, z+1f-s), r, g, b
            ));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                new Vector3f(x+s, y+1f-s, z+1f-s), new Vector3f(x+s, y+s, z+1f-s), r, g, b
            ));
        }
    }
}
