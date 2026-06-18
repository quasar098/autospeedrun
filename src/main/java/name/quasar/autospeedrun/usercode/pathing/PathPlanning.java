package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.DebugRenderLine;
import name.quasar.autospeedrun.usercode.*;
import name.quasar.autospeedrun.usercode.geometry.*;
import name.quasar.autospeedrun.usercode.world.BlockType;
import name.quasar.autospeedrun.usercode.world.WorldBlocks;

import java.util.*;

/**
 * The purpose of {@code PathPlanning} is to plan movement paths.
 * Provides input to both {@code Exploration} and {@code Navigation}.
 */
public class PathPlanning {

    private static PathPlanning instance = null;

    public static PathPlanning getInstance() {
        if (instance == null) {
            instance = new PathPlanning();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private Vector3 goalPosition = null;

    public Vector3 getGoalPosition() {
        return goalPosition;
    }

    public void setGoalPosition(Vector3 goalPosition) {
        this.goalPosition = goalPosition;
    }

    public void setGoalPosition(double x, double z) {
        goalPosition = new Vector3(x, -1, z);
    }

    private ArrayList<Vector3> cachedPath = null;

    public ArrayList<Vector3> getPath() {
        return cachedPath;
    }

    private BlockLocation nextBlockToExplore = null;

    public BlockLocation getNextBlockToExplore() {
        return nextBlockToExplore;
    }

    /** also sets nextBlockToExplore and cachedPath */
    public PathPlanningResult perform() {

        // hack to detect jumping.
        // >>> 64 % 0.0005
        // 0.0004999999999986677  # insane btw
//        if (Math.abs(((F3Information.getPosition().getY() + 0.00025) % 0.0005) - 0.00025) > 0.0000001) {
//            AutoSpeedrunAPI.chatMessage("currently jumping " + Math.abs(((F3Information.getPosition().getY() + 0.00025) % 0.0005) - 0.00025));
//            return PathPlanningResult.CURRENTLY_JUMPING;
//        }

        cachedPath = null;
        nextBlockToExplore = null;
        if (goalPosition == null) { return PathPlanningResult.NO_GOAL_POSITION; }

        // debug draw tall vertical line at goal
        AutoSpeedrunAPI.renderLine(new DebugRenderLine(
            goalPosition.withY(0.0).toVector3f(),
            goalPosition.withY(256.0).toVector3f(),
            0.0f, 0.0f, 0.0f
        ));

        // do the path planning, overwrite last node to be actual goal instead of nearest block
        ArrayList<BlockLocation> path = planPath();
        if (path == null) { return PathPlanningResult.NO_VALID_PATH; }
        ArrayList<Vector3> pathNodes = new ArrayList<>();
        for (BlockLocation bl : path) {
            pathNodes.add(bl.getCenter().offsetY(-0.5));
        }
        Vector3 correctedGoal = goalPosition;
        if (goalPosition.getY() <= 0) {
            correctedGoal = new Vector3(correctedGoal.getX(), F3Information.getPosition().getY(), correctedGoal.getZ());
        }
        if (canReachGoalPosition) {
            pathNodes.set(0, correctedGoal);
        }
        cachedPath = pathNodes;

        return PathPlanningResult.SUCCESS;
    }

    // A* heuristic function
    private double h(BlockLocation bl) {
        return (goalPosition.getY() > 0 ? Math.abs(goalPosition.getY() - bl.getY()) : 0)
             + Math.sqrt(Math.pow(bl.getX() - goalPosition.getX(), 2)
                       + Math.pow(bl.getZ() - goalPosition.getZ(), 2));
    }

    private ArrayList<BlockLocation> reconstructPath(HashMap<BlockLocation, BlockLocation> cameFrom, BlockLocation current) {
        ArrayList<BlockLocation> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        return totalPath;
    }

    public boolean canReachGoalPosition = false;

    /**
     * sets navCanReachGoalPosition
     */
    private ArrayList<BlockLocation> planPath() {
        canReachGoalPosition = false;
        Vector3 goal = goalPosition;
        assert(goal != null);
        Vector3 playerPos = Navigation.getInstance().getPredictedStablePosition();
        if (playerPos == null) { playerPos = F3Information.getPosition(); }
        BlockLocation lookAtBlock = null;
        Dimension dim = F3Information.getDimension();
        // rip off of wikipedia a* article
        BlockLocation start = new BlockLocation(dim, playerPos.withY(Navigation.getInstance().getPredictedPeakY()));
        HashMap<BlockLocation, BlockLocation> cameFrom = new HashMap<>();
        HashMap<BlockLocation, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);
        HashMap<BlockLocation, Double> fScore = new HashMap<>();
        fScore.put(start, h(start));
        PriorityQueue<BlockLocation> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(o -> fScore.getOrDefault(o, Double.MAX_VALUE))
        );
        openSet.add(start);
        HashMap<BlockLocation, Boolean> canJumpFrom = new HashMap<>();
        canJumpFrom.put(start, WorldBlocks.getInstance().isAirKnown(start) &&
            !WorldBlocks.getInstance().isAirKnown(start.below()));
        int iterations = 0;
        int maxIterations = 2000;
        ArrayList<BlockLocation> path = null;
        aStarWhileLoop : while (!openSet.isEmpty()) {
            if (++iterations == maxIterations) {
                AutoSpeedrunAPI.chatMessage("Max iterations reached on A* pathfinding");
                break;
            }
            BlockLocation current = openSet.peek();
            if (current.getX() == Math.floor(goal.getX()) && current.getZ() == Math.floor(goal.getZ()) &&
                    (goal.getY() <= 0 || current.getY() == Math.floor(goal.getY()))) {
                canReachGoalPosition = true;
                return reconstructPath(cameFrom, current);
            }
            openSet.remove(current);
            for (BlockLocation neighbor : current.getNeighbors()) {
                // maybe refactor
                if (!WorldBlocks.getInstance().isKnown(neighbor)) {
                    lookAtBlock = neighbor;
                    path = reconstructPath(cameFrom, current);
                    break aStarWhileLoop;
                }
                double d = 1.0;
                // maybe use nonsolid instead?
                if (!WorldBlocks.getInstance().isAirOrUnknown(neighbor)) {
                    d += 99999.0;  // have to break through a block, bad
                }
                if (!WorldBlocks.getInstance().isAirOrUnknown(neighbor.offsetY(1))) {
                    d += 99999.0;  // have to break through a block, bad
                }
                // same y level, no jumping
                if (neighbor.getY() == current.getY()) {
                    if (!WorldBlocks.getInstance().isKnown(current.below())) {
                        lookAtBlock = current.below();
                        path = reconstructPath(cameFrom, current);
                        break aStarWhileLoop;
                    } else {
                        boolean walkingOnAir = false;
                        if (canJumpFrom.containsKey(current.below())) {
                            if (!canJumpFrom.get(current.below())) {
                                walkingOnAir = true;
                            }
                        } else if (WorldBlocks.getInstance().isAirOrUnknown(current.below())) {
                            walkingOnAir = true;
                        }
                        if (Navigation.getInstance().getCurrentVelocity().getY() > 0.0 && current.equals(start)) {
                            walkingOnAir = false;
                        }
                        if (walkingOnAir) {
                            d += 99999.0;
                        }
                    }
                }
                if (neighbor.getY() > current.getY()) {
                    canJumpFrom.put(neighbor, false);
                    if (!canJumpFrom.containsKey(current)) {
                        if (!WorldBlocks.getInstance().isKnown(current.below())) {
                            lookAtBlock = current.offsetY(-1);
                            path = reconstructPath(cameFrom, current);
                            break aStarWhileLoop;
                        }
                        canJumpFrom.put(current, !WorldBlocks.getInstance().isAirKnown(current.below()));
                    }
                    if (canJumpFrom.get(current) == false) {
                        d += 99999.0;  // have to jump up 2+ blocks, bad
                    }
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
        if (lookAtBlock != null && lookAtBlock.getCenter().distanceTo(Util.getEyePosition()) < 19.0) {
            nextBlockToExplore = lookAtBlock;
        }
        return path;
    }
}
