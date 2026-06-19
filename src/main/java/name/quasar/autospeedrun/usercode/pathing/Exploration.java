package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.debug.DebugWorldLine;
import name.quasar.autospeedrun.usercode.F3Information;
import name.quasar.autospeedrun.usercode.MouseInputManager;
import name.quasar.autospeedrun.usercode.world.WorldBlocks;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.DirectedBlockFace;
import name.quasar.autospeedrun.usercode.geometry.Vector3;

import java.util.Arrays;
import java.util.Comparator;

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

    public static void reset() {
        instance = null;
    }

    private Vector3 findPointToLookAtToLookAtBlock(BlockLocation bl) {
//        bl.debugDrawGreatCircles(Util.getEyePosition());
        // this is such an annoying problem to solve im just going to use a heuristic for now
        // solution is perspective projection + vatti polygon boolean operations + reverse perspective projection btw
        DirectedBlockFace[] dbfs = bl.getDirectedFaces();
        double yaw = Math.toRadians(F3Information.getYaw());
        double pitch = Math.toRadians(F3Information.getPitch());
        Vector3 eyeVec = Vector3.fromRadians(yaw, pitch);
        Arrays.sort(dbfs, Comparator.comparingDouble(a -> eyeVec.dot(a.getNormal())));
        for (DirectedBlockFace dbf : dbfs) {
            if (eyeVec.dot(dbf.getNormal()) >= 0) { break; }
            BlockLocation outsideBlockLoc = dbf.getOutsideBlock(F3Information.getDimension());
            if (WorldBlocks.getInstance().isAirOrUnknown(outsideBlockLoc)) {
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
        AutoSpeedrunAPI.render(new DebugWorldLine(
            lookAt.offsetY(-0.01).toVector3f(), lookAt.offsetY(0.01).toVector3f(), 1.0f, 0.0f, 0.0f
        ));
        AutoSpeedrunAPI.chatMessage("Look at point: " + lookAt.toString(4));
        MouseInputManager.lookAtPoint(lookAt);
    }
}
