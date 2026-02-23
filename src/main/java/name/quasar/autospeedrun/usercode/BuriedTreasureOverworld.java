package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import org.lwjgl.glfw.GLFW;

public class BuriedTreasureOverworld {
    public enum Subsection {
        SCANNING,
        MOVING_TO_9_9,
        DIGGING_FOR_CHEST,
        LOOTING_CHEST,
        GOING_BACK_TO_ISLAND,
        FINDING_ISLAND_TREE,
        MINING_ISLAND_TREE,
        LOCATING_RAVINE,  // impossible level difficulty
        OBTAINING_FLINT,
        GOING_TO_RAVINE_FLOOR,
        CREATING_PORTAL,  // probably needs to be expanded to multiple subtasks or something
        DONE
    }

    public static Subsection subsection = Subsection.SCANNING;

    public static void init() {
        subsection = Subsection.SCANNING;

        // scanning
        openingPieChartDelay = 0;
        scanAngle = 0;
    }

    public static int openingPieChartDelay = 0;
    public static double scanAngle = 0;

    public static void performScan() {
        if (!F3Information.isPieChartShown()) {
            if (openingPieChartDelay == 0) {
                AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            }
            if (openingPieChartDelay == 1) {
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
            }
            if (openingPieChartDelay < 4) {
                openingPieChartDelay++;
                return;
            }
            if (openingPieChartDelay == 4) {
                openingPieChartDelay = 0;
                AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
                return;
            }
        }

        if (scanAngle < 360) {
            AutoSpeedrunApi.chatMessage("Scan angle: " + scanAngle);
            scanAngle += 10;
            return;
        }
    }

    public static void perform() {
        if (subsection == Subsection.SCANNING) {
            performScan();
        }
    }
}
