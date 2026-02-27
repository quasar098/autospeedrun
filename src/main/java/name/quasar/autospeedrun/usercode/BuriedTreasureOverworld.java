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

    /* singleton design */

    private static BuriedTreasureOverworld instance = null;

    public static void reset() {
        instance = null;
    }

    public static BuriedTreasureOverworld getInstance() {
        if (instance == null) {
            instance = new BuriedTreasureOverworld();
        }
        return instance;
    }

    /* scanning for bt */

    private int scanPause = 3;  // 0=no pause, positive=pause for that many ticks
    private double scanAngle = 0;
    private int scanTicks = 0;
    private double scanPrecision = 0;  // exponential
    private double scanDirection = 1;
    private Double scanPrevGlobalPerc = null;
    private double scanRightScreenEdgeAngle = 0;
    private double scanLeftScreenEdgeAngle = 0;

    private void performScan() {
        // force pie chart open
        if (!F3Information.isPieChartShown()) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
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
        scanTicks++;
        if (scanPause > 0) {
            scanPause--;
            return;
        }
        // todo handle case where player spawns inside of a bt chunk already
        if (scanDirection != 0) {
            Double globalPerc = F3Information.getPieDirectoryGlobalPercentage("blockentities");
            if (globalPerc == null) {
                AutoSpeedrunApi.chatMessage("gloabl percentage of blockentities null on scan");
                AutoSpeedrunApi.emergencyStopUserCode();
                return;
            }
            double scanAngleBetter = (((scanAngle + 180) % 360) - 180);
            AutoSpeedrunApi.chatMessage(String.format("(%f, %f)", scanAngleBetter, globalPerc));

            if (scanPrevGlobalPerc == null) {
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
                scanAngle += scanDirection * Math.toDegrees(hfov) / (2 * Math.pow(2, scanPrecision));
                scanPause = 3;
            } else if (scanPrevGlobalPerc > 0.6) {
                scanAngle += scanDirection * Math.toDegrees(hfov) / (2 * Math.pow(2, scanPrecision));
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
                scanPause = 3;
            } else if (globalPerc > 0.6) {  // implied: prevGlobalPerc <= 1 already
                double prevScanAngle = scanAngle;
                scanAngle -= 1.5 * scanDirection * Math.toDegrees(hfov) / (2 * Math.pow(2, scanPrecision));
                if (Math.toDegrees(hfov) / (3 * Math.pow(2, scanPrecision)) < 2.5) {
                    scanPrevGlobalPerc = null;
                    scanPrecision = 0;
                    if (scanDirection == 1) {
                        scanRightScreenEdgeAngle = (prevScanAngle + Math.toDegrees(hfov) / 2 + 3600) % 360;
                        AutoSpeedrunApi.chatMessage("Found right edge: " + scanRightScreenEdgeAngle);
                        scanDirection = -1;
                    } else {
                        scanLeftScreenEdgeAngle = (prevScanAngle - Math.toDegrees(hfov) / 2 + 3600) % 360;
                        AutoSpeedrunApi.chatMessage("Found left edge: " + scanLeftScreenEdgeAngle);
                        scanDirection = 0;
                    }
                }
                scanPrecision += 1;
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
                scanPause = 3;
            } else {
                scanAngle += scanDirection * Math.toDegrees(hfov) / (2 * Math.pow(2, scanPrecision) * 2);
                AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
                scanPause = 1;
            }

            MouseInputManager.setPlayerAngle(scanAngle, 0.0);
            scanPrevGlobalPerc = globalPerc;
            return;
        }
        // done with scan, chunk position computation
        System.out.println("done with scan");
        double leftScreenEdgeRad = Math.toRadians(scanLeftScreenEdgeAngle);
        double rightScreenEdgeRad = Math.toRadians(scanRightScreenEdgeAngle);
        double w = Math.sqrt(Math.pow(Math.cos(leftScreenEdgeRad) - Math.cos(rightScreenEdgeRad), 2)
            + Math.pow(Math.sin(leftScreenEdgeRad) - Math.sin(rightScreenEdgeRad), 2));
        double lx = F3Information.getPosition().getX() - 20/w * Math.sin(leftScreenEdgeRad);
        double lz = F3Information.getPosition().getZ() + 20/w * Math.cos(leftScreenEdgeRad);
        System.out.printf("%f %f\n", lx, lz);
        double rx = F3Information.getPosition().getX() - 20/w * Math.sin(rightScreenEdgeRad);
        double rz = F3Information.getPosition().getZ() + 20/w * Math.cos(rightScreenEdgeRad);
        System.out.printf("%f %f\n", rx, rz);
        double cx = Math.floor((lx + rx) / 32) * 16;
        double cz = Math.floor((lz + rz) / 32) * 16;
        System.out.printf("%f %f\n", cx, cz);
        double btx = cx + 9.5;
        double btz = cx + 9.5;
        AutoSpeedrunApi.chatMessage("Found BT at " + btx + "," + btz);
        AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
        Navigation.setGoalPosition(btx, btz);
        subsection = Subsection.MOVING_TO_9_9;
    }

    /* overall/misc */

    public Subsection subsection = Subsection.SCANNING;

    public void perform() {
        if (subsection == Subsection.SCANNING) {
            performScan();
        }
    }
}

