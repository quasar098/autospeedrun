package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.DebugRenderLine;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class Navigation {

    // player hitbox 0.6 wide, so ~0.2 remaining on each side
    private static final double AXIS_ALIGNMENT_BOUND = 0.15;

    // player hitbox 0.6 wide, so ~0.2 remaining on each side
    private static final double ARRIVED_AT_DEST_POSITION_BOUND = 0.16;
    private static final double ARRIVED_AT_DEST_MAX_VELO = 0.16;

    /* the idea with "axis alignment" is if we are on bridge bastion and going up that one side railing with lava on
       either side we should not be touching the lava by going outside of a bound. we set an axis alignment to
       prioritize staying on a specific axis */

    public enum AxisAlignment {
        PRIORITY_X,
        PRIORITY_Z,
        INDIFFERENT
    }

    private static AxisAlignment alignment = AxisAlignment.INDIFFERENT;

    public static AxisAlignment getAlignment() {
        return alignment;
    }

    public static void setAlignment(AxisAlignment alignment) {
        Navigation.alignment = alignment;
    }

    private static Vector3 goalPosition = null;

    public static Vector3 getGoalPosition() {
        return goalPosition;
    }

    public static void setGoalPosition(Vector3 goalPosition) {
        Navigation.goalPosition = goalPosition;
    }

    public static void setGoalPosition(double x, double y, double z) {
        Navigation.goalPosition = new Vector3(x, y, z);
    }

    public static void setGoalPosition(double x, double z) {
        Navigation.goalPosition = new Vector3(x, -1, z);
    }

    public static boolean perform() {
        if (goalPosition == null) {
            return false;
        }
        Vector3 goal = goalPosition;
        // debug render line
        AutoSpeedrunApi.renderLine(new DebugRenderLine(
                goal.withY(0.0).toVector3f(),
                goal.withY(256.0).toVector3f(),
                0.0f, 0.0f, 0.0f
        ));
//        if (getAlignment() == AxisAlignment.PRIORITY_X) {
//            if (Math.abs(F3Information.getPosition().getX()-goal.getX()) < AXIS_ALIGNMENT_BOUND) {
//
//            }
//        }
//        if (getAlignment() == AxisAlignment.PRIORITY_Z) {
//
//        }
        double yaw = F3Information.getYaw();
        double goalYaw = Math.atan2(
            F3Information.getPosition().getX() - goal.getX(),
            goal.getZ() - F3Information.getPosition().getZ()
        ) * 180 / Math.PI;
        double distance = goal.distanceTo(F3Information.getPosition());
        if (goal.getY() <= 0) {
            distance = goal.distanceTo2d(F3Information.getPosition());
        }
        if (distance < ARRIVED_AT_DEST_POSITION_BOUND) {
            // this means we have arrived
            AutoSpeedrunApi.chatMessage("Arrived at your destination");
            goalPosition = null;
            return false;
        }
        navigate();
        return false;
    }

    private static boolean checkIsAir(BlockLocation bl) {
        return WorldBlocks.getInstance().knownBlocks.containsKey(bl)
                && WorldBlocks.getInstance().knownBlocks.get(bl).getValue().equals("air");
    }

    private static boolean checkIsSolid(BlockLocation bl) {
        return WorldBlocks.getInstance().knownBlocks.containsKey(bl)
                && !WorldBlocks.getInstance().knownBlocks.get(bl).getValue().equals("air");
    }

    private static double h(BlockLocation bl) {
        return Math.pow(bl.getX() - goalPosition.getX(), 2) +
                Math.pow(bl.getY() - goalPosition.getY(), 2) * 4 +
                Math.pow(bl.getZ() - goalPosition.getZ(), 2);
    }

    private static void reconstructPath(HashMap<BlockLocation, BlockLocation> cameFrom, BlockLocation current) {
        ArrayList<BlockLocation> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }

        Vector3 prevP = totalPath.remove(0).getCenter();
        for (BlockLocation bl : totalPath) {
            Vector3 currentP = bl.getCenter();
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    prevP.toVector3f(), currentP.toVector3f(), 0.0f, 0.0f, 1.0f
            ));
            prevP = currentP;
        }
    }

    private static void navigate() {
        Vector3 goal = goalPosition;
        assert(goal != null);
        Vector3 playerPos = F3Information.getPosition();
        Vector3 lookAt = null;
        HashMap<BlockLocation, BlockType> kb = WorldBlocks.getInstance().knownBlocks;
        Dimension dim = F3Information.getDimension();
        // rip off of wikipedia a* article
        BlockLocation start = new BlockLocation(dim, playerPos);
        HashMap<BlockLocation, BlockLocation> cameFrom = new HashMap<>();
        HashMap<BlockLocation, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);
        HashMap<BlockLocation, Double> fScore = new HashMap<>();
        fScore.put(start, h(start));
        PriorityQueue<BlockLocation> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(o -> fScore.getOrDefault(o, Double.MAX_VALUE))
        );
        openSet.add(start);
        int iterations = 0;
        int maxIterations = 2000;
        aStarWhileLoop : while (!openSet.isEmpty()) {
            if (++iterations == maxIterations) {
                AutoSpeedrunApi.chatMessage("Max iterations reached on A* pathfinding");
                break;
            }
            BlockLocation current = openSet.peek();
            if (current.getX() == goal.getX() && current.getZ() == goal.getZ() &&
                    (goal.getY() == -1 || current.getY() == goal.getY())) {
                reconstructPath(cameFrom, current);
                return;
            }
            openSet.remove(current);
            for (BlockLocation neighbor : current.getNeighbors()) {
                if (!kb.containsKey(neighbor)) {
                    lookAt = neighbor.getCenter();
                    // todo account for all faces and blocks in the way and stuff
                    if (neighbor.getY() < playerPos.getY()) {
                        lookAt = lookAt.offsetY(0.5);
                    } else if (neighbor.getY() > Util.PLAYER_STANDING_EYE_HEIGHT + playerPos.getY()) {
                        lookAt = lookAt.offsetY(-0.5);
                    }
                    reconstructPath(cameFrom, current);
                    if (lookAt.distanceTo(playerPos.offsetY(Util.PLAYER_STANDING_EYE_HEIGHT)) >= 20.0) {
                        return;
                    }
                    break aStarWhileLoop;
                }
                double d = 1.0;
                if (!kb.get(neighbor).getValue().equals("air")) {
                    d += 9999.0;
                }
                double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + d;
                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + h(neighbor));
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        if (lookAt != null) {
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    lookAt.offsetY(-0.3).toVector3f(), lookAt.offsetY(0.3).toVector3f(), 1.0f, 0.0f, 0.0f
            ));
            MouseInputManager.lookAtPoint(lookAt);
        }
    }
}
