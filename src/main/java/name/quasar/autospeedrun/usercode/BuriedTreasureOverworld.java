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
        performingScan = false;
        scanAngle = 0;
    }

    /* scanning for bt */

    public static boolean performingScan = false;
    public static double scanAngle = 0;

    public static void performScan() {
        // force pie chart open
        if (!F3Information.isPieChartShown()) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
            performingScan = true;
            return;
        }
        // force correct path
        int pathTravNum = F3Information.getRecommendedNumForPiePathTraversal("root.gameRenderer.level.entities");
        if (pathTravNum != -1) {
            AutoSpeedrunApi.tapKey(pathTravNum + '0');
            if (pathTravNum == 2) {  // inventory key :(
                // todo fix make it next tick or something
            }
            if (pathTravNum == 3) {  // f5/perspective key :(
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_3);
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_3);
            }
            return;
        }
        // do scan
        if (scanAngle < 720) {
            AutoSpeedrunApi.chatMessage("Scan angle: " + scanAngle);
            MouseInputManager.setPlayerAngle(scanAngle, 10.0);

            Double globalPerc = F3Information.getPieDirectoryGlobalPercentage("blockentities");
            Double relPerc = F3Information.getPieDirectoryRelativePercentage("blockentities");
            if (globalPerc == null) {
                AutoSpeedrunApi.chatMessage("gloabl percentage of blockentities null on scan");
                AutoSpeedrunApi.emergencyStopUserCode();
                return;
            }
            if (relPerc == null) {
                AutoSpeedrunApi.chatMessage("rel percentage of blockentities null on scan");
                AutoSpeedrunApi.emergencyStopUserCode();
                return;
            }
            System.out.println("gp=" + globalPerc + "/rp=" + relPerc);

            // dw pie chart will reenable because of the beginning of this func
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
            scanAngle += 5;
            return;
        }
        // done with scan
        AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
        subsection = Subsection.MOVING_TO_9_9;
    }

    /* overall/misc */

    public static void perform() {
        if (subsection == Subsection.SCANNING) {
            performScan();
        }
    }
}

