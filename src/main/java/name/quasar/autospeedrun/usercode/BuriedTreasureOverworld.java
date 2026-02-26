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
        scanCurrentType = 0;
        scanLeftScreenEdgeMin = null;
        scanLeftScreenEdgeMax = null;
        scanRightScreenEdgeMin = null;
        scanRightScreenEdgeMax = null;
    }

    /* scanning for bt */

    public static boolean performingScan = false;
    public static int scanPause = 3;  // 0=no pause, positive=pause for that many ticks
    public static double scanAngle = 0;
    public static double scanCurrentType = 0;  // 0=360 coverage w/ scanAngle | 1=deduce l/r
    // the left screen edge angle is the angle that if i turn epsilon to the left then it shows up
    public static Double scanLeftScreenEdgeMin = null;
    public static Double scanLeftScreenEdgeMax = null;
    // the right screen edge angle is the angle that if i turn epsilon to the right then it shows up
    public static Double scanRightScreenEdgeMin = null;
    public static Double scanRightScreenEdgeMax = null;

    public static void performScan() {
        // force pie chart open
        if (!F3Information.isPieChartShown()) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
            performingScan = true;
            return;
        }
        // force close inventory/whatever other bullshit
        if (F3Information.f3HasBackgroundDim) {
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_ESCAPE);
            return;
        }
        // force correct path
        int pathTravNum = F3Information.getRecommendedNumForPiePathTraversal("root.gameRenderer.level.entities");
        if (pathTravNum != -1) {
            AutoSpeedrunApi.tapKey(pathTravNum + '0');
            if (pathTravNum == 3) {  // f5/perspective key :(
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_3);
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_3);
            }
            return;
        }
        double screen_ratio = (double) Util.SCREEN_W/(double) Util.SCREEN_H;
        double vfov = Math.toRadians(Util.OPTIONS_TXT_FOV * 40 + 70);
        double hfov = 2*Math.atan(Math.tan(vfov/2)*screen_ratio);
        // do scan
        if (scanPause > 0) {
            if (--scanPause == 0) {
                AutoSpeedrunApi.chatMessage("Scan angle: " + (((scanAngle + 180) % 360) - 180));
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
                double leftScreenEdge = (scanAngle - hfov / 2 + 360) % 360;
                double rightScreenEdge = (scanAngle + hfov / 2 + 360) % 360;
                // todo fix to be ranges that handle periodicness of angles or whatever
                // https://stackoverflow.com/questions/55270058/calculate-overlap-of-two-angle-intervals
                if (globalPerc > 1.3) {  // present in (h)fov
                    if (scanLeftScreenEdgeMin == null || scanLeftScreenEdgeMin < rightScreenEdge) {
                        scanLeftScreenEdgeMin = rightScreenEdge;
                    }
                    if (scanRightScreenEdgeMax == null || scanRightScreenEdgeMax > leftScreenEdge) {
                        scanRightScreenEdgeMax = leftScreenEdge;
                    }
                } else {  // not present in (h)fov
                    if (scanLeftScreenEdgeMax == null || scanLeftScreenEdgeMax > leftScreenEdge) {
                        scanLeftScreenEdgeMax = leftScreenEdge;
                    }
                    if (scanRightScreenEdgeMin == null || scanRightScreenEdgeMin < rightScreenEdge) {
                        scanRightScreenEdgeMin = rightScreenEdge;
                    }
                }
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
                scanAngle += Math.toDegrees(hfov) / 3;
            }
            return;
        }
        if (scanCurrentType == 0) {
            if (scanAngle < 720) {
                MouseInputManager.setPlayerAngle(scanAngle, 0.0);

                // dw pie chart will reenable because of the beginning of this func
                scanPause = 3;
                return;
            }
        } else if (scanCurrentType == 1) {
            // todo
        } else {
            AutoSpeedrunApi.chatMessage("invalid scanCurrentType");
            AutoSpeedrunApi.emergencyStopUserCode();
        }
        // done with scan
        System.out.printf("scanLeftScreenEdgeMin=%f\nscanLeftScreenEdgeMax=%f\n" +
            "scanRightScreenEdgeMin=%f\nscanRightScreenEdgeMax=%f\n", scanLeftScreenEdgeMin, scanLeftScreenEdgeMax,
            scanRightScreenEdgeMin, scanRightScreenEdgeMax);
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

