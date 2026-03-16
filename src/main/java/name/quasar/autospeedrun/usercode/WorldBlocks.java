package name.quasar.autospeedrun.usercode;

import com.mojang.math.Vector3f;
import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.DebugRenderLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* collect information about all the blocks seen in the world */
public class WorldBlocks {
    private static WorldBlocks instance = null;

    public static WorldBlocks getInstance() {
        if (instance == null) {
            instance = new WorldBlocks();
        }
        return instance;
    }

    public WorldBlocks() {
        // idk nothing
    }

    public HashMap<BlockLocation, BlockType> knownBlocks = new HashMap<>();

    public static void reset() {
        instance = null;
    }

    public void collectFacingBlockInformation() {
        // targetted block
        BlockLocation targetted = F3Information.getTargettedBlockLocation();
        if (targetted != null) {
            knownBlocks.put(targetted, new BlockType(
                    F3Information.getTargettedBlockName()
            ));
        }
        // use angles to find all air in the way, 3D DDA algorithm that finds all intersected faces
        double yaw = Math.toRadians(F3Information.getYaw());
        double pitch = Math.toRadians(F3Information.getPitch());
        double px = F3Information.getPosition().getX();
        double py = F3Information.getPosition().getY() + Util.PLAYER_STANDING_EYE_HEIGHT;  // todo crouching stuff
        double pz = F3Information.getPosition().getZ();
        double opx = px;
        double opy = py;
        double opz = pz;
        double dx = Math.cos(yaw + Math.PI/2)*Math.cos(-pitch);
        double dy = Math.sin(-pitch);
        double dz = Math.sin(yaw + Math.PI/2)*Math.cos(-pitch);
        double maxIterations = 200;
        int iterations = 0;
        boolean hit = false;
        ArrayList<BlockFace> bfsTotal = new ArrayList<>();
        while (iterations++ < maxIterations) {
            double sx = dx > 0 ? Math.floor(px)+1 : Math.ceil(px)-1;
            double sy = dy > 0 ? Math.floor(py)+1 : Math.ceil(py)-1;
            double sz = dz > 0 ? Math.floor(pz)+1 : Math.ceil(pz)-1;
            double xt = 99999;
            double yt = 99999;
            double zt = 99999;
            if (dx != 0) { xt = (sx - px) / dx; }
            if (dy != 0) { yt = (sy - py) / dy; }
            if (dz != 0) { zt = (sz - pz) / dz; }
            double mintime = Math.min(Math.min(xt, yt), zt);
            px += dx * mintime;
            py += dy * mintime;
            pz += dz * mintime;
            // 20 is distance that eye ray max is, 20 squared is 400
            if ((px-opx)*(px-opx)+(py-opy)*(py-opy)+(pz-opz)*(pz-opz) >= 400) { break; }
            ArrayList<BlockFace> bfs = new ArrayList<>();
            if (mintime == xt) {
                bfs.add(new BlockFace(
                        (int) (Math.round(px) - 1), (int) Math.floor(py), (int) Math.floor(pz),
                        BlockFace.Direction.POS_X
                ));
            }
            if (mintime == yt) {
                bfs.add(new BlockFace(
                        (int) Math.floor(px), (int) (Math.round(py) - 1), (int) Math.floor(pz),
                        BlockFace.Direction.POS_Y
                ));
            }
            if (mintime == zt) {
                bfs.add(new BlockFace(
                        (int) Math.floor(px), (int) Math.floor(py), (int) (Math.round(pz) - 1),
                        BlockFace.Direction.POS_Z
                ));
            }
            for (BlockFace bf : bfs) {
                if (bf.getAdjacentA(F3Information.getDimension()).equals(targetted)) {
                    hit = true;
                    break;
                }
                if (bf.getAdjacentB(F3Information.getDimension()).equals(targetted)) {
                    hit = true;
                    break;
                }
                bfsTotal.add(bf);
            }
            if (hit) {
                break;
            }
        }
        if (!hit && targetted != null) {
            // unlucky angle on eye ray air detection or something
            return;
        }
        for (BlockFace bf : bfsTotal) {
            knownBlocks.put(bf.getAdjacentA(F3Information.getDimension()), BlockType.AIR);
            knownBlocks.put(bf.getAdjacentB(F3Information.getDimension()), BlockType.AIR);
        }
        if (iterations == maxIterations) {
            AutoSpeedrunApi.chatMessage("Max iterations reached for eye ray air detection");
        }
//        if (targetted != null) {
//            AutoSpeedrunApi.chatMessage(targetted.toString());
//        }
    }

    public void debugDraw() {
        for (Map.Entry<BlockLocation, BlockType> entry : knownBlocks.entrySet()) {
            BlockLocation loc = entry.getKey();
            BlockType blockType = entry.getValue();
            if (blockType.getValue().equals("air")) {
                // draw black X through air blocks
//                AutoSpeedrunApi.renderLine(new DebugRenderLine(
//                        new Vector3f(loc.getX() + 0.4f, loc.getY() + 0.5f, loc.getZ() + 0.4f),
//                        new Vector3f(loc.getX() + 0.6f, loc.getY() + 0.5f, loc.getZ() + 0.6f),
//                        0f, 0f, 0f
//                ));
//                AutoSpeedrunApi.renderLine(new DebugRenderLine(
//                        new Vector3f(loc.getX() + 0.6f, loc.getY() + 0.5f, loc.getZ() + 0.4f),
//                        new Vector3f(loc.getX() + 0.4f, loc.getY() + 0.5f, loc.getZ() + 0.6f),
//                        0f, 0f, 0f
//                ));
            } else {
                // draw green / on top of all known blocks
                AutoSpeedrunApi.renderLine(new DebugRenderLine(
                        new Vector3f(loc.getX(), loc.getY() + 1.02f, loc.getZ()),
                        new Vector3f(loc.getX() + 1f, loc.getY() + 1.02f, loc.getZ() + 1f),
                        0f, 1f, 0f
                ));
            }
        }
    }
}
