package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.DebugRenderLine;
import name.quasar.autospeedrun.usercode.F3Information;
import name.quasar.autospeedrun.usercode.MouseInputManager;
import name.quasar.autospeedrun.usercode.world.WorldBlocks;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.world.BlockType;
import name.quasar.autospeedrun.usercode.geometry.DirectedBlockFace;
import name.quasar.autospeedrun.usercode.geometry.Vector3;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * {@code Exploration} handles the scanning of world blocks information.
 */
public class Exploration {
    private static Exploration instance = null;

    public static Exploration getInstance() {
        if (instance == null) {
            instance = new Exploration();
        }
        return instance;
    }

    public static void Exploration() {
        instance = null;
    }

    private Vector3 findPointToLookAtToLookAtBlock(BlockLocation bl) {
//        bl.debugDrawGreatCircles(Util.getEyePosition());
        // this is such a hard problem to solve im just going to use a heuristic for now
        DirectedBlockFace[] dbfs = bl.getDirectedFaces();
        double yaw = Math.toRadians(F3Information.getYaw());
        double pitch = Math.toRadians(F3Information.getPitch());
        Vector3 eyeVec = Vector3.fromRadians(yaw, pitch);
        Arrays.sort(dbfs, Comparator.comparingDouble(a -> eyeVec.dot(a.getNormal())));
        HashMap<BlockLocation, BlockType> known = WorldBlocks.getInstance().knownBlocks;
        for (DirectedBlockFace dbf : dbfs) {
            if (eyeVec.dot(dbf.getNormal()) >= 0) { break; }
            BlockLocation outsideBlockLoc = dbf.getOutsideBlock(F3Information.getDimension());
            BlockType outsideBlockType = known.getOrDefault(outsideBlockLoc, BlockType.AIR);
            if (outsideBlockType.getValue().equals("air")) {
                AutoSpeedrunAPI.chatMessage("DBF selected: " + dbf);
                return dbf.getCenter();
            }
        }
        return null;
    }

    public void perform() {
        BlockLocation toExplore = PathPlanning.getInstance().getNextBlockToExplore();
        if (toExplore == null) { return; }
        Vector3 lookAt = findPointToLookAtToLookAtBlock(toExplore);
        if (lookAt == null) { return; }
        AutoSpeedrunAPI.renderLine(new DebugRenderLine(
            lookAt.offsetY(-0.01).toVector3f(), lookAt.offsetY(0.01).toVector3f(), 1.0f, 0.0f, 0.0f
        ));
        AutoSpeedrunAPI.chatMessage("Look at point: " + lookAt.toString(4));
        MouseInputManager.lookAtPoint(lookAt);
    }
}
