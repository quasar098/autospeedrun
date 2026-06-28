package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.debug.DebugWorldLine;
import name.quasar.autospeedrun.debug.DebugWorldText;
import name.quasar.autospeedrun.usercode.*;
import name.quasar.autospeedrun.usercode.geometry.*;
import name.quasar.autospeedrun.usercode.world.WorldBlocks;

import java.util.*;
import java.util.function.ToDoubleFunction;

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

    private boolean debugDraw = false;
    private HashMap<BlockLocation, Integer> dYRiseDebugSave = null;
    private HashMap<BlockLocation, Integer> dYFallDebugSave = null;
    private HashMap<BlockLocation, Double> fDebugSave = null;
    private PriorityQueue<BlockLocation> exploredDebugSave = null;
    private PriorityQueue<BlockLocation> unexploredDebugSave = null;
    private ToDoubleFunction<BlockLocation> bruhTodoDelete = null;

    public void toggleDebugDraw() {
        debugDraw = !debugDraw;
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

    private PathPlanningResult result;

    public PathPlanningResult getResult() {
        return result;
    }

    /** sets result */
    public void perform() {
        if (goalPosition == null) {
            result = new PathPlanningResult(PathPlanningStatusCode.NO_GOAL_POSITION);
            return;
        }

        // debug draw tall vertical line at goal
        AutoSpeedrunAPI.render(new DebugWorldLine(
            goalPosition.withY(0.0).toVector3f(),
            goalPosition.withY(256.0).toVector3f(),
            0.0f, 0.0f, 0.0f
        ));

        // do the path planning, overwrite last node to be actual goal instead of nearest block
        PathPlanningResult path = planPath();
        if (path.getStatus() == PathPlanningStatusCode.SUCCESS) {
            debugDrawPath(path.getPath());
        }
        result = path;
    }

    public void debugDrawPath(ArrayList<Vector3> totalPath) {
        Vector3 prevP = null;
        for (Vector3 currentP : totalPath) {
            if (prevP != null) {
                AutoSpeedrunAPI.render(new DebugWorldLine(
                    prevP.offsetY(0.2).toVector3f(), currentP.offsetY(0.2).toVector3f(), 0.0f, 0.0f, 1.0f
                ));
            }
            prevP = currentP;
        }
    }

    private ArrayList<BlockLocation> reconstructPath(HashMap<BlockLocation, BlockLocation> cameFrom, BlockLocation current) {
        ArrayList<BlockLocation> totalPath = new ArrayList<>();
        totalPath.add(current);
        int iterations = 0;
        while (cameFrom.containsKey(current)) {
            iterations++;
            if (iterations > 10000) {
                AutoSpeedrunAPI.chatMessage("too many cameFrom iterations!");
                break;
            }
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        return totalPath;
    }

    // A* heuristic function
    private double h(BlockLocation bl) {
        double horiz = Math.sqrt(Math.pow(bl.getX() - goalPosition.getX(), 2)
            + Math.pow(bl.getZ() - goalPosition.getZ(), 2));
        if (goalPosition.getY() <= 0) {
            return horiz;
        }
        double vert = goalPosition.getY() - bl.getY();
        if (vert < 0) {  // we are above the goal, so it's easy to drop down
            vert *= -0.2;
        }
        double interp = vert/(vert+horiz);
        return (vert * interp + horiz * (1 - interp));
    }

    /**
     * sets nextBlockFaceToExplore
     */
    private PathPlanningResult planPath() {
        Vector3 goal = goalPosition;
        assert(goal != null);
        Vector3 playerPos = Navigation.getInstance().getPredictedStablePosition();
        if (playerPos == null) { playerPos = F3Information.getPosition(); }
        Dimension dim = F3Information.getDimension();
        WorldBlocks wb = WorldBlocks.getInstance();
        BlockLocation start = new BlockLocation(dim, playerPos.withY(Navigation.getInstance().getPredictedPeakY()));

        // A* specific stuff
        HashMap<BlockLocation, BlockLocation> cameFrom = new HashMap<>();
        HashMap<BlockLocation, Integer> dYRise = new HashMap<>();  // rising information
        dYRiseDebugSave = dYRise;
        HashMap<BlockLocation, Integer> dYFall = new HashMap<>();  // falling information
        dYFallDebugSave = dYFall;
        // distance from player to each block
        HashMap<BlockLocation, Double> g = new HashMap<>();
        HashMap<BlockLocation, Double> f = new HashMap<>();
        fDebugSave = f;
        PriorityQueue<BlockLocation> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(o -> f.getOrDefault(o, Double.MAX_VALUE))
        );
        // set of candidates to decide where to look next
        Vector3 finalPlayerPos = playerPos;
        bruhTodoDelete = o -> -100.0/(100.0+o.getCenter().distanceToSquared(finalPlayerPos));
//        bruhTodoDelete = o -> o.getCenter().distanceToSquared(finalPlayerPos);
        PriorityQueue<BlockLocation> unexplored = new PriorityQueue<>(
            Comparator.comparingDouble(
                bruhTodoDelete
            )
        );
        unexploredDebugSave = unexplored;
        PriorityQueue<BlockLocation> explored = new PriorityQueue<>(
            Comparator.comparingDouble(o -> f.getOrDefault(o, Double.MAX_VALUE)));
        exploredDebugSave = explored;

        openSet.add(start);
        g.put(start, 0.0);
        f.put(start, h(start));
        // hack to detect if player is in the air
        // >>> 64 % 0.0005
        // 0.0004999999999986677  # insane btw
        boolean inTheAir = Math.abs(((F3Information.getPosition().getY() + 0.00025) % 0.0005) - 0.00025) > 0.0000001;
        dYRise.put(start, inTheAir ? 1 : 0);
        dYFall.put(start, 0);

        // todo rework to account for block place/break costs per block

        int iterations = 0;

        while (!openSet.isEmpty()) {
            iterations++;
            if (iterations > 10000) {
                AutoSpeedrunAPI.chatMessage("max iterations!");
                break;
            }
            BlockLocation current = openSet.peek();
            if (current.getX() == Math.floor(goal.getX()) && current.getZ() == Math.floor(goal.getZ()) &&
                (goal.getY() <= 0 || current.getY() == Math.floor(goal.getY()))) {
                // find path to walk along
                ArrayList<BlockLocation> path = reconstructPath(cameFrom, current);
                unexplored.removeIf(bl -> !(path.contains(bl) || path.contains(bl.above()) || path.contains(bl.below())));
                // find best place to look at
                BlockLocation bestBlockToExplore;
                DirectedBlockFace visibleFace = null;
                do {
                    bestBlockToExplore = unexplored.poll();
                    if (bestBlockToExplore == null) { break; }
                    visibleFace = wb.getVisibleFace(bestBlockToExplore);
                } while (visibleFace == null);
                if (bestBlockToExplore != null) {
                    bestBlockToExplore.debugDraw();
                }
                AutoSpeedrunAPI.chatMessage("best (c):" + bestBlockToExplore);
                Vector3 lookAtLocation = null;
                if (bestBlockToExplore != null) {
                    lookAtLocation = visibleFace.getCenter();
                    visibleFace.toBlockFace().debugDraw(-0.04f, 0.3f, 0.1f, 0.3f);
                    AutoSpeedrunAPI.chatMessage("visible:" + visibleFace);
                }
                // get blocks to break
                ArrayList<BlockLocation> blocksToBreak = new ArrayList<>();
                for (BlockLocation pathBlock : path) {
                    if (wb.isSolidKnown(pathBlock)) {
                        if (!blocksToBreak.contains(pathBlock)) {
                            blocksToBreak.add(pathBlock);
                        }
                    }
                    if (wb.isSolidKnown(pathBlock.above())) {
                        if (!blocksToBreak.contains(pathBlock.above())) {
                            blocksToBreak.add(pathBlock.above());
                        }
                    }
                }
                PathPlanningResult res = PathPlanningResult.fromBlockLocations(path, lookAtLocation, blocksToBreak);
                // set last node to be actual goal position
                res.getPath().set(0, goalPosition.getY() <= 0 ? goalPosition.withY(F3Information.getPosition().getY())
                                                              : goalPosition);
                return res;
            }
            openSet.remove(current);
            explored.add(current);
            if (!wb.isKnown(current)) {
                unexplored.add(current);
            }
            for (BlockLocation neighbor : current.getNeighbors()) {
                if (wb.isKnown(current)) {
                    if (!wb.isKnown(neighbor)) {
                        unexplored.add(neighbor);
                    }
                    if (!wb.isKnown(neighbor.above())) {
                        unexplored.add(neighbor.above());
                    }
                    if (!wb.isKnown(neighbor.below())) {
                        unexplored.add(neighbor.below());
                    }
                }
                double d = 1.0;
                if (wb.isSolidKnown(neighbor)) {
                    d += 8.0;  // have to break through a block, bad
                }
                if (wb.isSolidKnown(neighbor.above())) {
                    d += 8.0;  // have to break through a block, bad
                }
                if (neighbor.getY() < current.getY()) {  // falling
                    if (wb.isSolidKnown(neighbor.below())) {
                        dYRise.put(neighbor, 0);
                        dYFall.put(neighbor, 0);
                    } else {
                        dYFall.merge(neighbor, dYFall.getOrDefault(neighbor.above(), 0) - 1, Math::max);
                    }
                    if (dYFall.get(neighbor) < 0) {
                        d += 6.0;  // falling without knowing where you land, bad
                    }
                    // todo better dYfall logic for places we know we can fall safely
                } else if (neighbor.getY() == current.getY()) {  // same y
                    if (dYFall.getOrDefault(current, 0) < 0) {
                        d += 0.0;  // bad to go from falling to going sideways (maybe change later?)
                    }
                    if (wb.isSolidKnown(neighbor.below())) {
                        dYRise.put(neighbor, 0);
                        dYFall.put(neighbor, 0);
                    } else {
                        if (dYRise.getOrDefault(current, 0) > 0) {
                            d += 9.0;  // bad to go from jumping in the air to sideways supported by nothing
                        }
                        if (dYFall.get(current) != null && dYFall.get(current) == 0) {
                            dYFall.put(neighbor, -1);
                        }
                    }
                } else if (neighbor.getY() > current.getY()) {  // jumping
                    if (dYRise.containsKey(current)) {
                        dYRise.put(neighbor, dYRise.get(current) + 1);
                    }
                    if (dYRise.getOrDefault(neighbor, 999) >= 2) {
                        d += 7.0;  // can't jump up two blocks or more
                    }
                }
                // update g,f for neighbor
                double newGScore = g.getOrDefault(current, Double.MAX_VALUE) + d;
                if (newGScore < g.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    g.put(neighbor, newGScore);
                    f.put(neighbor, newGScore + h(neighbor));
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        // todo fix up this path because its totally broken lol
        if (unexplored.isEmpty()) {
            AutoSpeedrunAPI.chatMessage("no path 1!");
            return new PathPlanningResult(PathPlanningStatusCode.NO_VALID_PATH);
        }
        return new PathPlanningResult(PathPlanningStatusCode.NO_VALID_PATH);
    }

    public void debugDraw() {
        if (debugDraw) {
            Vector3 slightUp = new Vector3(0, 0.18, 0);
            Vector3 slightDown = new Vector3(0, -0.18, 0);
            if (exploredDebugSave != null) {
                for (BlockLocation bl : exploredDebugSave) {
                    if (bl.getCenter().distanceTo(F3Information.getPosition()) > 7.0) {
                        continue;
                    }
                    Double fScore = fDebugSave.get(bl);
                    AutoSpeedrunAPI.render(
                        new DebugWorldText(
                            bl.getCenter().add(slightUp).toVector3f(),
                            fScore != null ? String.format("%.1f", fScore) : "?", 0xffffff)
                    );
                    if (dYRiseDebugSave != null && dYFallDebugSave != null) {
                        Integer r = dYRiseDebugSave.get(bl);
                        Integer f = dYFallDebugSave.get(bl);
                        String dYStr = String.format("R:%s F:%s", r == null ? "?" : r, f == null ? "?" : f);
                        AutoSpeedrunAPI.render(
                            new DebugWorldText(bl.getCenter().add(slightDown).toVector3f(), dYStr, 0xffffff)
                        );
                    }
                }
            }
            if (unexploredDebugSave != null && bruhTodoDelete != null) {
                for (BlockLocation bl : unexploredDebugSave) {
                    double exploreScore = bruhTodoDelete.applyAsDouble(bl);
                    AutoSpeedrunAPI.render(
                        new DebugWorldText(
                            bl.getCenter().toVector3f(),
                            String.format("%.1f", exploreScore), 0xff8888)
                    );
                }
            }
        }
    }
}
