package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.usercode.F3Information;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.DirectedBlockFace;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import name.quasar.autospeedrun.usercode.world.WorldBlocks;

import java.util.ArrayList;

public class PathPlanningResult {
    public PathPlanningStatusCode getStatus() {
        return status;
    }

    private final PathPlanningStatusCode status;

    public ArrayList<Vector3> getPath() {
        return path;
    }

    private final ArrayList<Vector3> path;
    private final ArrayList<BlockLocation> blocksToBreak;

    public Vector3 getLookLocation() {
        return lookLocation;
    }

    private final Vector3 lookLocation;

    // non-success path
    public PathPlanningResult(PathPlanningStatusCode status) {
        this.status = status;
        this.path = null;
        this.lookLocation = null;
        this.blocksToBreak = null;
    }

    // success path
    public PathPlanningResult(ArrayList<Vector3> path, Vector3 lookLocation, ArrayList<BlockLocation> blocksToBreak) {
        this.status = PathPlanningStatusCode.SUCCESS;
        this.path = path;
        if (lookLocation == null) {
            Vector3 playerPos = F3Information.getPosition();
            Vector3 newLookAt = path.get(path.size() - 1);
            double lookAheadDistance = 4.0;
            for (int i = path.size() - 2; i >= 0; i--) {
                Vector3 next = path.get(i);
                double nextD = next.distanceTo(playerPos);
                double currD = newLookAt.distanceTo(playerPos);
                if (nextD > lookAheadDistance) {
                    double clampedAmt = Math.max(0.0, Math.min(1.0, (lookAheadDistance-currD)/(nextD-currD)));
                    newLookAt = path.get(i).interpTo(next, clampedAmt);
                    break;
                } else {
                    newLookAt = path.get(i);
                }
            }
            this.lookLocation = newLookAt.withY(Util.getEyePosition().getY());
        } else {
            this.lookLocation = lookLocation;
        }
        this.blocksToBreak = blocksToBreak;
    }

    public static PathPlanningResult fromBlockLocations(ArrayList<BlockLocation> pathBlocks, Vector3 lookLocation,
                                                        ArrayList<BlockLocation> blocksToBreak) {
        ArrayList<Vector3> path = new ArrayList<>();
        for (BlockLocation bl : pathBlocks) {
            path.add(bl.getCenter().offsetY(-0.5));
        }
        return new PathPlanningResult(path, lookLocation, blocksToBreak);
    }

    /** returns null if there is nothing to break within reach */
    public DirectedBlockFace getDBFToBreak() {
        if (blocksToBreak == null) { return null; }
        if (blocksToBreak.isEmpty()) { return null; }
        BlockLocation blockToBreak = blocksToBreak.get(0);
        DirectedBlockFace dbf = WorldBlocks.getInstance().getVisibleFace(blockToBreak);
        if (dbf == null) {
            return null;
        }
        if (dbf.getCenter().distanceTo(Util.getEyePosition()) > 4.5) {
            return null;
        }
        return dbf;
    }
}
