package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.debug.DebugWorldLine;
import name.quasar.autospeedrun.usercode.F3Information;
import name.quasar.autospeedrun.usercode.MovementInputManager;
import name.quasar.autospeedrun.usercode.geometry.Vector3;

import java.util.*;

/**
 * {@code Navigation} controls the player movements to follow the path set by {@code PathPlanning}.
 * Also handles movement prediction.
 */
public class Navigation {
    private static Navigation instance = null;

    public static Navigation getInstance() {
        if (instance == null) {
            instance = new Navigation();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    /* movement prediction */

    private final LinkedList<Vector3> previousPositions = new LinkedList<>();

    public void logCurrentPosition(Vector3 playerPosition) {
        previousPositions.addFirst(playerPosition);
        int PREV_POSITIONS_STORED_AMT = 5;
        if (previousPositions.size() > PREV_POSITIONS_STORED_AMT) {
            previousPositions.removeLast();
        }
    }

    public Vector3 prevPosition() {
        return previousPositions.size() > 1 ? previousPositions.get(1) : null;
    }

    /**
     * has the potential to return null
     */
    public Vector3 getCurrentVelocity() {
        if (previousPositions.size() < 2) { return null; }
        return previousPositions.getFirst().sub(previousPositions.get(1));
    }

    /**
     * has the potential to return null
     */
    public Vector3 getPredictedStablePosition() {
        double slipperyMult = 0.6;  // assume 0.6 slippery multiplier, maybe change later?
        if (previousPositions.isEmpty()) { return null; }
        Vector3 predicted = F3Information.getPosition();
        Vector3 velo = getCurrentVelocity();
        if (velo == null) { return null; }
        while (velo.length() > 0.003) {
            velo = velo.mult(slipperyMult * 0.91);
            if (Math.abs(velo.getX()) < 0.003) {
                velo = new Vector3(0, velo.getY(), velo.getZ());
            }
            if (Math.abs(velo.getZ()) < 0.003) {
                velo = new Vector3(velo.getX(), velo.getY(), 0);
            }
            predicted = predicted.add(velo);
        }
        return new Vector3(predicted.getX(), F3Information.getPosition().getY(), predicted.getZ());
    }

    /**
     * has the potential to return null
     */
    public double getPredictedPeakY() {
        if (getCurrentVelocity() == null) { return F3Information.getPosition().getY(); }
        double vy = getCurrentVelocity().getY();
        double predictedY = F3Information.getPosition().getY();
        while (vy > 0.003) {
            vy = (vy - 0.08) * 0.98;
            predictedY += vy;
        }
        return predictedY;
    }

    public void drawPredictedPlayerBox() {
        Vector3 predictedStablePos = getPredictedStablePosition();
        if (predictedStablePos == null) { return; }
        predictedStablePos = predictedStablePos.withY(getPredictedPeakY());
        Vector3 aaa = predictedStablePos.offsetX(-0.3).offsetZ(-0.3);
        Vector3 baa = aaa.offsetX(0.6);
        Vector3 aab = aaa.offsetZ(0.6);
        Vector3 bab = baa.offsetZ(0.6);
        Vector3 aba = aaa.offsetY(1.8);
        Vector3 bba = baa.offsetY(1.8);
        Vector3 abb = aab.offsetY(1.8);
        Vector3 bbb = bab.offsetY(1.8);
        AutoSpeedrunAPI.render(new DebugWorldLine(aaa.toVector3f(), baa.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(aaa.toVector3f(), aba.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(aaa.toVector3f(), aab.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(baa.toVector3f(), bba.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(baa.toVector3f(), bab.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(aba.toVector3f(), bba.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(aba.toVector3f(), abb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(aab.toVector3f(), bab.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(aab.toVector3f(), abb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(bba.toVector3f(), bbb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(bab.toVector3f(), bbb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunAPI.render(new DebugWorldLine(abb.toVector3f(), bbb.toVector3f(), 1.0f, 0.0f, 1.0f));
    }

    /* path following */

    public void perform() {
        // player hitbox 0.6 wide, so ~0.2 remaining on each side
        final double ARRIVED_AT_DEST_POSITION_BOUND = 0.16;

        ArrayList<Vector3> pathNodes = PathPlanning.getInstance().getPath();

        // debug draw path and vertical red line at next node
        Vector3 nextNode = pathNodes.get(pathNodes.size() - 1);
        if (pathNodes.size() >= 2) {
            nextNode = pathNodes.get(pathNodes.size() - 2);
        }
        AutoSpeedrunAPI.render(new DebugWorldLine(
            nextNode.toVector3f(), nextNode.offsetY(1.0).toVector3f(), 1.0f, 0.0f, 0.0f
        ));

        // move to next node
        Vector3 vecToNextNode = nextNode.sub(getPredictedStablePosition());
        moveInBestDirection(vecToNextNode);
        AutoSpeedrunAPI.render(new DebugWorldLine(
            getPredictedStablePosition().offsetY(0.1).toVector3f(), nextNode.offsetY(0.1).toVector3f(),
            0.3f, 0.3f, 0.3f
        ));

        // check for arrival at destination
        Vector3 goalPosition = PathPlanning.getInstance().getGoalPosition();
        double distance = goalPosition.getY() <= 0
            ? goalPosition.distanceTo2d(Navigation.getInstance().getPredictedStablePosition())
            : goalPosition.distanceTo(Navigation.getInstance().getPredictedStablePosition());
        if (distance < ARRIVED_AT_DEST_POSITION_BOUND) {
            AutoSpeedrunAPI.chatMessage("Arrived at your destination");
            PathPlanning.getInstance().setGoalPosition(null);
            MovementInputManager.cancelWASD();
        }
    }

    // todo CHANGE to account for path
    public void moveInBestDirection(Vector3 vecToDirec) {
        Vector3 predStablePos = getPredictedStablePosition();
        if (predStablePos == null) {
            return;
        }
        double yawRad = Math.toRadians(F3Information.getYaw());
        double slipperyMult = 0.6;  // assume 0.6 slippery multiplier, maybe change later?
        double walkMult = 1.0;
        double runMult = 1.3;
        double effectsMult = 1.0;
        double movement = 0.1 * effectsMult * Math.pow((0.6 / slipperyMult), 3);
        double displacementLength = 1.0/(1.0 - slipperyMult * 0.91) * walkMult * movement * 0.98;
        double displacement45Length = 1.0/(1.0 - slipperyMult * 0.91) * walkMult * movement * 1.0;
        double displacementRunLength = 1.0/(1.0 - slipperyMult * 0.91) * runMult * movement * 0.98;
        double displacementRun45Length = 1.0/(1.0 - slipperyMult * 0.91) * runMult * movement * 1.0;
        Vector3 wVec = new Vector3(-Math.sin(yawRad), 0, Math.cos(yawRad)).mult(displacementRunLength);
        Vector3 aVec = new Vector3(Math.cos(yawRad), 0, Math.sin(yawRad)).mult(displacementLength);
        Vector3 sVec = new Vector3(Math.sin(yawRad), 0, -Math.cos(yawRad)).mult(displacementLength);
        Vector3 dVec = new Vector3(-Math.cos(yawRad), 0, -Math.sin(yawRad)).mult(displacementLength);
        Vector3 waVec = wVec.add(aVec).normalized().mult(displacementRun45Length);
        Vector3 asVec = aVec.add(sVec).normalized().mult(displacementRun45Length);
        Vector3 sdVec = sVec.add(dVec).normalized().mult(displacement45Length);
        Vector3 dwVec = dVec.add(wVec).normalized().mult(displacement45Length);
        List<Vector3> vectors = Arrays.asList(wVec, aVec, sVec, dVec, waVec, asVec, sdVec, dwVec);
        Vector3 bestVec = vectors.stream().max(Comparator.comparingDouble(vecToDirec::dot)).get();
        // todo: i need to do this in a better way bruh
        MovementInputManager.setSprinting(true);
        if (bestVec.equals(wVec)) {
            MovementInputManager.planPressKeyW();
        } else if (bestVec.equals(aVec)) {
            MovementInputManager.planPressKeyA();
        } else if (bestVec.equals(sVec)) {
            MovementInputManager.planPressKeyS();
        } else if (bestVec.equals(dVec)) {
            MovementInputManager.planPressKeyD();
        } else if (bestVec.equals(waVec)) {
            MovementInputManager.planPressKeyW();
            MovementInputManager.planPressKeyA();
        } else if (bestVec.equals(asVec)) {
            MovementInputManager.planPressKeyA();
            MovementInputManager.planPressKeyS();
        } else if (bestVec.equals(sdVec)) {
            MovementInputManager.planPressKeyS();
            MovementInputManager.planPressKeyD();
        } else if (bestVec.equals(dwVec)) {
            MovementInputManager.planPressKeyD();
            MovementInputManager.planPressKeyW();
        }
        // todo change to preemptively jump
        if (vecToDirec.getY() > 0.25 && vecToDirec.normalized().getY() > 0.5) {
//            AutoSpeedrunAPI.emergencyStopUserCode();
            MovementInputManager.planPressKeySpace();
        }
    }
}
