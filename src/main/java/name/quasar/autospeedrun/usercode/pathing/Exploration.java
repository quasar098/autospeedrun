package name.quasar.autospeedrun.usercode.pathing;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.debug.DebugWorldLine;
import name.quasar.autospeedrun.usercode.F3Information;
import name.quasar.autospeedrun.usercode.MouseInputManager;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.world.WorldBlocks;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.DirectedBlockFace;
import name.quasar.autospeedrun.usercode.geometry.Vector3;

import java.util.ArrayList;
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

    public void perform() {
        DirectedBlockFace toExplore = PathPlanning.getInstance().getNextDBFToExplore();
        Vector3 lookAt;
        if (toExplore == null) {
            ArrayList<Vector3> pathNodes = PathPlanning.getInstance().getPath();
            Vector3 nextNode = pathNodes.get(pathNodes.size() - 1);
            if (pathNodes.size() >= 2) {
                nextNode = pathNodes.get(pathNodes.size() - 2);
            }
            lookAt = nextNode.withY(Util.getEyePosition().getY());
        } else {
            lookAt = toExplore.getCenter();
        }
        AutoSpeedrunAPI.render(new DebugWorldLine(
            lookAt.offsetY(-0.01).toVector3f(), lookAt.offsetY(0.01).toVector3f(), 1.0f, 0.0f, 0.0f
        ));
        AutoSpeedrunAPI.chatMessage("Look at point: " + lookAt.toString(4));
        MouseInputManager.lookAtPoint(lookAt);
    }
}
