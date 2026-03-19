package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.DebugRenderLine;

import java.util.*;

public class Navigation {

    // player hitbox 0.6 wide, so ~0.2 remaining on each side
    private static final double AXIS_ALIGNMENT_BOUND = 0.15;

    // player hitbox 0.6 wide, so ~0.2 remaining on each side
    private static final double ARRIVED_AT_DEST_POSITION_BOUND = 0.16;
    private static final double ARRIVED_AT_DEST_MAX_VELO = 0.16;

    private static Navigation instance = null;

    public static Navigation getInstance() {
        if (instance == null) {
            instance = new Navigation();
        }
        return instance;
    }

    public Navigation() {

    }

    public static void reset() {
        instance = null;
    }

    /* the idea with "axis alignment" is if we are on bridge bastion and going up that one side railing with lava on
       either side we should not be touching the lava by going outside of a bound. we set an axis alignment to
       prioritize staying on a specific axis */

    public enum AxisAlignment {
        PRIORITY_X,
        PRIORITY_Z,
        INDIFFERENT
    }

    private AxisAlignment alignment = AxisAlignment.INDIFFERENT;

    public AxisAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(AxisAlignment alignment) {
        this.alignment = alignment;
    }

    private Vector3 goalPosition = null;

    public Vector3 getGoalPosition() {
        return goalPosition;
    }

    public void setGoalPosition(Vector3 goalPosition) {
        this.goalPosition = goalPosition;
    }

    public void setGoalPosition(double x, double y, double z) {
        goalPosition = new Vector3(x, y, z);
    }

    public void setGoalPosition(double x, double z) {
        goalPosition = new Vector3(x, -1, z);
    }

    public boolean perform() {
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
        ArrayList<BlockLocation> path = navigate();
        if (path == null) { return false; }
        ArrayList<Vector3> pathVectors = optimizePath(path);
        debugDrawPath(pathVectors);
        if (pathVectors.isEmpty()) { return false; }
        Vector3 nextNode = pathVectors.get(pathVectors.size()-1);
        return false;
    }

    private ArrayList<Vector3> optimizePath(ArrayList<BlockLocation> inputPath) {
        ArrayList<Vector3> newPath1 = new ArrayList<>(inputPath.size());
        for (BlockLocation bl0 : inputPath) {
            Vector3 vec0 = bl0.getCenter().offsetY(-0.5);
            if (newPath1.size() >= 2) {
                Vector3 vec1 = newPath1.get(newPath1.size()-1);
                Vector3 vec2 = newPath1.get(newPath1.size()-2);
                // l-shape corner node removal
                if (Math.abs(vec1.sub(vec0).normalized().dot(vec1.sub(vec2).normalized())) < 0.0001
                        && vec0.getY() == vec1.getY() && vec1.getY() == vec2.getY()) {
                    Vector3 diagCollidePos = vec0.add(vec2.sub(vec1));
                    BlockLocation diagBl = new BlockLocation(bl0.getDimension(), diagCollidePos);
                    if (!WorldBlocks.getInstance().knownBlocks.containsKey(diagBl)
                            || WorldBlocks.getInstance().knownBlocks.get(diagBl).getValue().equals("air")) {
                        newPath1.set(newPath1.size()-1, vec0);
                        continue;
                    }
                }
            }
            newPath1.add(vec0);
        }
        ArrayList<Vector3> newPath2 = new ArrayList<>(inputPath.size());
        for (Vector3 vec0 : newPath1) {
            if (newPath1.size() >= 2) {
                Vector3 vec1 = newPath1.get(newPath1.size()-1);
                Vector3 vec2 = newPath1.get(newPath1.size()-2);
                // straight line middle node removal
                if (Vector3.inStraightLine(vec0, vec1, vec2)) {
                    newPath1.set(newPath1.size()-1, vec0);
                    continue;
                }
            }
            newPath2.add(vec0);
        }
        return newPath2;
    }

    private double h(BlockLocation bl) {
        if (goalPosition.getY() == -1) {
            return Math.sqrt(Math.pow(bl.getX() - goalPosition.getX(), 2) + Math.pow(bl.getZ() - goalPosition.getZ(), 2));
        }
        return Math.sqrt(Math.pow(bl.getX() - goalPosition.getX(), 2) +
                Math.pow(bl.getY() - goalPosition.getY(), 2) +
                Math.pow(bl.getZ() - goalPosition.getZ(), 2));
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

    private void debugDrawPath(ArrayList<Vector3> totalPath) {
        Vector3 prevP = null;
        for (Vector3 currentP : totalPath) {
            if (prevP != null) {
                AutoSpeedrunApi.renderLine(new DebugRenderLine(
                        prevP.offsetY(0.2).toVector3f(), currentP.offsetY(0.2).toVector3f(), 0.0f, 0.0f, 1.0f
                ));
            }
            prevP = currentP;
        }
    }

    private Vector3 findPointToLookAtToLookAtBlock(BlockLocation bl) {
        Vector3 lookAt = bl.getCenter();
        Vector3 playerPos = F3Information.getPosition();
        // todo account for all faces and blocks in the way and stuff
        if (bl.getY() < playerPos.getY()) {
            lookAt = lookAt.offsetY(0.5);
        } else if (bl.getY() > Util.PLAYER_STANDING_EYE_HEIGHT + playerPos.getY()) {
            lookAt = lookAt.offsetY(-0.5);
        }
        return lookAt;
    }

    private ArrayList<BlockLocation> navigate() {
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
        HashMap<BlockLocation, Boolean> canJumpFrom = new HashMap<>();
        canJumpFrom.put(start, true);
        int iterations = 0;
        int maxIterations = 2000;
        ArrayList<BlockLocation> path = null;
        aStarWhileLoop : while (!openSet.isEmpty()) {
            if (++iterations == maxIterations) {
                AutoSpeedrunApi.chatMessage("Max iterations reached on A* pathfinding");
                break;
            }
            BlockLocation current = openSet.peek();
            if (current.getX() == goal.getX() && current.getZ() == goal.getZ() &&
                    (goal.getY() == -1 || current.getY() == goal.getY())) {
                return reconstructPath(cameFrom, current);
            }
            openSet.remove(current);
            for (BlockLocation neighbor : current.getNeighbors()) {
                if (!kb.containsKey(neighbor)) {
                    lookAt = findPointToLookAtToLookAtBlock(neighbor);
                    path = reconstructPath(cameFrom, current);
                    break aStarWhileLoop;
                }
                double d = 1.0;
                if (!kb.get(neighbor).getValue().equals("air")) {
                    d += 999999.0;  // have to break through a block, bad
                }
                if (neighbor.getY() == current.getY()) {
                    if (!kb.containsKey(current.offsetY(-1))) {
                        lookAt = findPointToLookAtToLookAtBlock(current.offsetY(-1));
                        path = reconstructPath(cameFrom, current);
                        break aStarWhileLoop;
                    } else {
                        if (canJumpFrom.containsKey(current.offsetY(-1))) {
                            if (!canJumpFrom.get(current.offsetY(-1))) {
                                d += 999999.0;  // bad to walk on air lmao
                            }
                        } else if (kb.get(current.offsetY(-1)).getValue().equals("air")) {
                            d += 999999.0;  // bad to walk on air lmao
                        }
                    }
                }
                if (neighbor.getY() > current.getY()) {
                    canJumpFrom.put(neighbor, false);
                    if (!canJumpFrom.containsKey(current)) {
                        if (kb.containsKey(current.offsetY(-1))) {
                            canJumpFrom.put(current, !kb.get(current.offsetY(-1)).getValue().equals("air"));
                        } else {
                            lookAt = findPointToLookAtToLookAtBlock(current.offsetY(-1));
                            path = reconstructPath(cameFrom, current);
                            break aStarWhileLoop;
                        }
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
        if (lookAt != null && lookAt.distanceTo(playerPos.offsetY(Util.PLAYER_STANDING_EYE_HEIGHT)) < 20.0) {
            AutoSpeedrunApi.renderLine(new DebugRenderLine(
                    lookAt.offsetY(-0.1).toVector3f(), lookAt.offsetY(0.1).toVector3f(), 1.0f, 0.0f, 0.0f
            ));
            MouseInputManager.lookAtPoint(lookAt);
        }
        return path;
    }
}
