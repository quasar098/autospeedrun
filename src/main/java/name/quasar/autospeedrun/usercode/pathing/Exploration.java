package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.debug.DebugWorldLine;
import name.quasar.autospeedrun.usercode.MouseInputManager;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.DirectedBlockFace;
import name.quasar.autospeedrun.usercode.geometry.Vector3;

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

    public void perform() {
        //noinspection AssertWithSideEffects
        assert PathPlanning.getInstance().getResult().getStatus() == PathPlanningStatusCode.SUCCESS;
        Vector3 lookAt = PathPlanning.getInstance().getResult().getLookLocation();

        // breaking blocks
        DirectedBlockFace nextDBFToBreak = PathPlanning.getInstance().getResult().getDBFToBreak();
        if (nextDBFToBreak != null) {
            lookAt = nextDBFToBreak.getCenter();
            MouseInputManager.planPressLeftButton();
        }

        AutoSpeedrunAPI.render(new DebugWorldLine(
            lookAt.offsetY(-0.01).toVector3f(), lookAt.offsetY(0.01).toVector3f(), 1.0f, 0.0f, 0.0f
        ));
        AutoSpeedrunAPI.chatMessage("Look at point: " + lookAt.toString(4));
        MouseInputManager.lookAtPoint(lookAt);
    }
}
