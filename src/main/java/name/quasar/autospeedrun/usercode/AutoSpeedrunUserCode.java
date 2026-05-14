package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import name.quasar.autospeedrun.usercode.geometry.GreatCircle;
import name.quasar.autospeedrun.usercode.pathing.Navigation;
import name.quasar.autospeedrun.usercode.pathing.PathPlanning;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class AutoSpeedrunUserCode {

    int testStartTick = -1;

    public void init() {
        // misc useful
        Util.SCREEN_W = 0;
        Util.SCREEN_H = 0;
        Util.tickCount = 0;
        Util.runStage = RunStage.OVERWORLD;
        testStartTick = -1;

        // other systems
        WorldBlocks.reset();
        PathPlanning.reset();
        Navigation.reset();
        MouseInputManager.reset();
        BuriedTreasureOverworld.reset();
    }

    public void tick() {
        Util.tickCount++;
        AutoSpeedrunApi.clearRenderLines();
        AutoSpeedrunApi.screenshotAsync(1920, 1080);
        // screen resolution not yet resolved, resolve it before doing anything else
        if (Util.SCREEN_W == 0 || Util.SCREEN_H == 0) {
            Util.SCREEN_W = AutoSpeedrunApi.getScreenshotWidth();
            Util.SCREEN_H = AutoSpeedrunApi.getScreenshotHeight();
            if (Util.SCREEN_W == 0 || Util.SCREEN_H == 0) {
                return;
            }
            AutoSpeedrunApi.chatMessage(String.format("Screenshots W/H Resolved: %dx%d", Util.SCREEN_W, Util.SCREEN_H));
        }
        // f3 must open always
        if (!F3Information.isF3Open()) {
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
            return;
        }
        F3Information.clearCache();
        // movement prediction debug info
        Navigation.getInstance().logCurrentPosition(F3Information.getPosition());
        Navigation.getInstance().drawPredictedPlayerBox();
        // live debug information
        BlockLocation targettedBL = F3Information.getTargettedBlockLocation();
        String targettedBlockPositionFormatted = targettedBL == null ? "(not targetting)" : targettedBL.toString();
        AutoSpeedrunApi.subtitleMessage(String.format(
            "%.2fs Y:%.1f,P:%.1f %s %s", Util.tickCount / 20.0,
            F3Information.getYaw(), F3Information.getPitch(),
            targettedBlockPositionFormatted, F3Information.getTargettedBlockName()
        ));
        if (gc != null) {
            for (GreatCircle gc2 : gc) {
                gc2.debugDraw(Util.getEyePosition());
            }
        }
        // do mouse calibration on world join
        if (MouseInputManager.calibrateMouse()) {
            return;
        }
        // collect facing block information
        WorldBlocks.getInstance().collectFacingBlockInformation();
        // debug draw world blocks information
        WorldBlocks.getInstance().debugDraw();

        // do stuff based on stage of run
//        if (Util.runStage == RunStage.OVERWORLD) {
//            BuriedTreasureOverworld.getInstance().perform();
//            if (BuriedTreasureOverworld.getInstance().subsection == BuriedTreasureOverworld.Subsection.DONE) {
//                Util.runStage = RunStage.ENTERED_NETHER;
//            }
//        }

        // path planning, navigation, exploration
//        PathPlanningResult pathPlanningResult = PathPlanning.getInstance().perform();
//        MovementInputManager.handle();
//        if (pathPlanningResult != PathPlanningResult.SUCCESS) { return; }
//        Exploration.getInstance().perform();
//        Navigation.getInstance().perform();
    }

    private ArrayList<GreatCircle> gc = null;

    public void debug(String debugStr) {
        String[] split = debugStr.split(" ");
        switch (split[0]) {
            case "dimension":
                AutoSpeedrunApi.chatMessage("Dimension: " + F3Information.getDimension());
                break;
            case "clearf3cache":
                F3Information.clearCache();
                AutoSpeedrunApi.chatMessage("F3 Cache cleared");
                break;
            case "clearworldblocks":
                WorldBlocks.reset();
                AutoSpeedrunApi.chatMessage("World blocks cleared");
                break;
            case "mousemove":
                String[] xyStr = split[1].split(",");
                AutoSpeedrunApi.mouseMove(Integer.parseInt(xyStr[0]), Integer.parseInt(xyStr[1]));
                break;
            case "setnav":
                String[] xyzStr = split[1].split(",");
                PathPlanning.getInstance().setGoalPosition(new Vector3(
                    Double.parseDouble(xyzStr[0]), Double.parseDouble(xyzStr[1]), Double.parseDouble(xyzStr[2])
                ));
//                Navigation.setAlignment(Navigation.AxisAlignment.PRIORITY_X);
                break;
            case "clearnav":
                PathPlanning.getInstance().setGoalPosition(null);
                break;
            case "lclicktap":
                AutoSpeedrunApi.tapLeftClick();
                break;
            case "lclickpress":
                AutoSpeedrunApi.pressLeftClick();
                break;
            case "lclickrelease":
                AutoSpeedrunApi.releaseLeftClick();
                break;
            case "rclick":
                AutoSpeedrunApi.mouseActivate(GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_PRESS, 0);
                AutoSpeedrunApi.mouseActivate(GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_RELEASE, 0);
                break;
            case "toggleair":
                Util.toggleDebugAir = !Util.toggleDebugAir;
                break;
            case "greatcircles":
                AutoSpeedrunApi.chatMessage("great circles");
                String[] xyzStr2 = new String[]{};
                if (split.length >= 2) {
                    xyzStr2 = split[1].split(",");
                }
                gc = new ArrayList<>();
                double gcYaw = Math.toRadians(F3Information.getYaw());
                double gcPitch = Math.toRadians(F3Information.getPitch());

                if (xyzStr2.length == 3) {
                    Vector3 gcPosition = new Vector3(
                        Double.parseDouble(xyzStr2[0]), Double.parseDouble(xyzStr2[1]), Double.parseDouble(xyzStr2[2])
                    );
                    Vector3 vecToGCPos = gcPosition.sub(Util.getEyePosition());
                    double[] ypRadians = vecToGCPos.toYawAndPitchRadians();
                    gcYaw = ypRadians[0];
                    gcPitch = ypRadians[1];
                }
                Vector3 lookingVector = Vector3.fromRadians(gcYaw, gcPitch).normalized();
                gc.add(new GreatCircle(Vector3.fromRadians(
                    gcYaw + Math.PI / 2,
                    0
                )));
                if (Math.abs(lookingVector.dot(Vector3.POS_X)) > 0.000001) {
                    gc.add(new GreatCircle(lookingVector.cross(Vector3.POS_X).normalized()));
                }
                if (Math.abs(lookingVector.dot(Vector3.POS_Z)) > 0.000001) {
                    gc.add(new GreatCircle(lookingVector.cross(Vector3.POS_Z).normalized()));
                }
                break;
            default:
                AutoSpeedrunApi.chatMessage("there is no such thing");
                break;
        }
    }
}
