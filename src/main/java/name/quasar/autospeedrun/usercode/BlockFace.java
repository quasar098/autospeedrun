package name.quasar.autospeedrun.usercode;

import com.mojang.math.Vector3f;
import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.DebugRenderLine;

/** rip off of scripts/3dvis/main.py BlockFace */
public class BlockFace {
    public enum Direction {
        POS_X,  // east
        POS_Y,
        POS_Z  // south
    }

    // todo change these to longs
    private int x;
    private int y;
    private int z;
    private Direction dir;

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

    public void debugDraw() {
        if (dir == Direction.POS_X) {
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x+1f, y, z), new Vector3f(x+1f, y+1f, z), 1.0f, 0.68f, 0.77f
            ));
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x+1f, y+1f, z), new Vector3f(x+1f, y+1f, z+1f), 1.0f, 0.68f, 0.77f
            ));
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x+1f, y+1f, z+1f), new Vector3f(x+1f, y, z+1f), 1.0f, 0.68f, 0.77f
            ));
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x+1f, y, z+1f), new Vector3f(x+1f, y, z), 1.0f, 0.68f, 0.77f
            ));
        } else if (dir == Direction.POS_Y) {
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x, y+1f, z), new Vector3f(x+1f, y+1f, z), 1.0f, 0.68f, 0.77f
            ));
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x+1f, y+1f, z), new Vector3f(x+1f, y+1f, z+1f), 1.0f, 0.68f, 0.77f
            ));
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x+1f, y+1f, z+1f), new Vector3f(x, y+1f, z+1f), 1.0f, 0.68f, 0.77f
            ));
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    new Vector3f(x, y+1f, z+1f), new Vector3f(x, y+1f, z), 1.0f, 0.68f, 0.77f
            ));
        } else {
            if (dir == Direction.POS_Z) {
                AutoSpeedrunApi.renderLine(new DebugRenderLine(
                        new Vector3f(x, y, z+1f), new Vector3f(x+1f, y, z+1f), 1.0f, 0.68f, 0.77f
                ));
                AutoSpeedrunApi.renderLine(new DebugRenderLine(
                        new Vector3f(x+1f, y, z+1f), new Vector3f(x+1f, y+1f, z+1f), 1.0f, 0.68f, 0.77f
                ));
                AutoSpeedrunApi.renderLine(new DebugRenderLine(
                        new Vector3f(x+1f, y+1f, z+1f), new Vector3f(x, y+1f, z+1f), 1.0f, 0.68f, 0.77f
                ));
                AutoSpeedrunApi.renderLine(new DebugRenderLine(
                        new Vector3f(x, y+1f, z+1f), new Vector3f(x, y, z+1f), 1.0f, 0.68f, 0.77f
                ));
            }
        }
    }
}
