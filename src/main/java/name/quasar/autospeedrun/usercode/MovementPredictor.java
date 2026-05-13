package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.DebugRenderLine;
import name.quasar.autospeedrun.usercode.geometry.Vector3;

import java.util.*;

public class MovementPredictor {
    private static MovementPredictor movementPredictor = null;

    public static MovementPredictor getInstance() {
        if (movementPredictor == null) {
            movementPredictor = new MovementPredictor();
        }
        return movementPredictor;
    }

    public static void reset() {
        movementPredictor = null;
    }

    // previousPositions[0] is the most recent position
    // maybe change this to a linked list? or store fewer?
    private final Vector3[] previousPositions = { null, null, null, null, null };

    public void logCurrentPosition(Vector3 playerPosition) {
        if (previousPositions[0] == null) {
            Arrays.fill(previousPositions, playerPosition);
        } else {
            for (int i = 0; i < previousPositions.length - 1; i++) {
                previousPositions[i + 1] = previousPositions[i];
            }
            previousPositions[0] = playerPosition;
        }
    }

    public Vector3 prevPosition() {
        return previousPositions[1];
    }

    /**
     * has the potential to return null
     */
    public Vector3 getCurrentVelocity() {
        if (previousPositions[0] == null || previousPositions[1] == null) { return null; }
        return previousPositions[0].sub(previousPositions[1]);
    }

    /**
     * has the potential to return null
     */
    public Vector3 getPredictedStablePosition() {
        double slipperyMult = 0.6;  // assume 0.6 slippery multiplier, maybe change later?
        /*
        velocity is completely additive to position; there is no interference between separate tick's velocity.
        this results in the recurrence relation Δp = v + v*0.546 + v*0.546^2 + v*0.546^3 + ... = v/(1-0.546) where
        0.546 = slipperyMult(==0.546) * 0.91(==constant from mc movement code).
        see scripts/movement/simulate.py for mc movement code reimpl.
         */
        if (previousPositions[0] == null) { return null; }
        Vector3 velo = getCurrentVelocity();
        if (velo == null) { return null; }
        double veloMult = 1.0/(1.0 - slipperyMult * 0.91);
        // todo change this to simulate tick per tick instead of making the assumption that there is no block or other
        veloMult *= (slipperyMult * 0.91);  // we remove this because it already happen (measured as p0 - p1)
//        System.out.println("velo: " + velo.toString(6));
//        System.out.println("p0, p1: " + previousPositions[0].toString(6) + ", " + previousPositions[1].toString(6));
//        System.out.println("movement amount: " + velo.mult(veloMult).length());
//        System.out.println("veloMult: " + veloMult);
//        System.out.println("velo multiplied: " + velo.mult(veloMult));
        Vector3 predicted = previousPositions[0].add(velo.mult(veloMult));
        return new Vector3(predicted.getX(), previousPositions[0].getY(), predicted.getZ());
    }

    public void doBestMovementInDirection(Vector3 vecToDirec) {
        Vector3 predStablePos = getPredictedStablePosition();
        if (predStablePos == null) {
            return;
        }
        double yawRad = Math.toRadians(F3Information.getYaw());
        // see comment in getPredictedStablePosition for more info
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
//        Vector3 playerPos = F3Information.getPosition();
//        AutoSpeedrunApi.renderLine(new DebugRenderLine(
//                playerPos.toVector3f(), playerPos.add(aVec).toVector3f(), 1.0f, 1.0f, 0.5f
//        ));
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
        // maybe change to preemptively jump
        if (vecToDirec.getY() > 0.25 && vecToDirec.normalized().getY() > 0.5) {
            MovementInputManager.planPressKeySpace();
        }
    }

    public void drawVelocityVector() {
        double scaleArrow = 1.0;
        Vector3 velo = getCurrentVelocity();
        if (velo == null) { return; }
        if (velo.length() < 0.001) { return; }
        Vector3 startPos = F3Information.getPosition().offsetY(0.5);
        Vector3 endPos = startPos.add(velo.mult(scaleArrow));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(
                startPos.toVector3f(), endPos.toVector3f(), 1.0f, 0.5f, 1.0f)
        );
    }

    public void drawPredictedStablePositionBox() {
        Vector3 predictedStablePos = getPredictedStablePosition();
        if (predictedStablePos == null) { return; }
        Vector3 aaa = predictedStablePos.offsetX(-0.3).offsetZ(-0.3);
        Vector3 baa = aaa.offsetX(0.6);
        Vector3 aab = aaa.offsetZ(0.6);
        Vector3 bab = baa.offsetZ(0.6);
        Vector3 aba = aaa.offsetY(1.8);
        Vector3 bba = baa.offsetY(1.8);
        Vector3 abb = aab.offsetY(1.8);
        Vector3 bbb = bab.offsetY(1.8);
        AutoSpeedrunApi.renderLine(new DebugRenderLine(aaa.toVector3f(), baa.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(aaa.toVector3f(), aba.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(aaa.toVector3f(), aab.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(baa.toVector3f(), bba.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(baa.toVector3f(), bab.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(aba.toVector3f(), bba.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(aba.toVector3f(), abb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(aab.toVector3f(), bab.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(aab.toVector3f(), abb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(bba.toVector3f(), bbb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(bab.toVector3f(), bbb.toVector3f(), 1.0f, 0.0f, 1.0f));
        AutoSpeedrunApi.renderLine(new DebugRenderLine(abb.toVector3f(), bbb.toVector3f(), 1.0f, 0.0f, 1.0f));
    }
}
