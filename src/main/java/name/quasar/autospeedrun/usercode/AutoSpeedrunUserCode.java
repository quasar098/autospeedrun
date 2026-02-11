package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class AutoSpeedrunUserCode {
    int mouseCalibrationStage = 0;
    double mouseCalibrationYaw1 = 0;
    double mouseCalibrationYaw2 = 0;
    double mouseCalibrationYaw3 = 0;
    double degreesPerPixel = 0;

    public void init() {
        // misc useful
        Util.SCREEN_W = 0;
        Util.SCREEN_H = 0;
        Util.tickCount = 0;

        // mouse calibration
        mouseCalibrationStage = 0;
        mouseCalibrationYaw1 = 0;
        mouseCalibrationYaw2 = 0;
        mouseCalibrationYaw3 = 0;
        degreesPerPixel = 0;

        // world information
        WorldBlocks.knownBlocks = new HashMap<>();
    }

    public void lookAtAngles(double yaw, double pitch) {
        double yawMod360 = (yaw + 360.0) % 360.0;
        if (yawMod360 < mouseCalibrationYaw1) {
            yawMod360 += 360.0;
        }
        int yawPixels = (int) Math.round((yawMod360 - mouseCalibrationYaw1) / degreesPerPixel);
        int pitchPixels = (int) Math.round((90.0 - pitch) / degreesPerPixel);
        AutoSpeedrunApi.moveMouse(yawPixels, pitchPixels);
    }

    public boolean performMouseCalibration() {
        // 1 = will look 0, 3000
        // 4 = collect angle of 0, 0, will look 100, 0
        // 5 = collect angle of 100, 0, will look 10000, 0
        // 6 = collect angle of 10000, 0, will look at 0, 1000
        // 7 = confirm calibration
        if (mouseCalibrationStage > 7) {
            return false;
        }
        switch (mouseCalibrationStage) {
            case 1:
                AutoSpeedrunApi.moveMouse(0, 3000);
                break;
            case 4:
                mouseCalibrationYaw1 = (F3Information.getYaw() + 360.0) % 360.0;
                AutoSpeedrunApi.moveMouse(100, 0);
                break;
            case 5:
                mouseCalibrationYaw2 = (F3Information.getYaw() + 360.0) % 360.0;
                if (mouseCalibrationYaw2 % 360.0 == mouseCalibrationYaw1 % 360.0) {
                    return true;
                }
                AutoSpeedrunApi.moveMouse(10000, 0);
                break;
            case 6:
                if (mouseCalibrationYaw3 % 360.0 == mouseCalibrationYaw2 % 360.0) {
                    return true;
                }
                mouseCalibrationYaw3 = (F3Information.getYaw() + 360.0) % 360.0;
                double delta1 = (mouseCalibrationYaw2 - mouseCalibrationYaw1 + 360.0) % 360.0;
                double delta2 = (mouseCalibrationYaw3 - mouseCalibrationYaw2 + 360.0) % 360.0;
                int safetyBreak = 1;
                while (Math.abs(delta2 - delta1 * 99) > 90.0 && (safetyBreak++ < 100)) {
                    delta2 += 360.0;
                }
                if (safetyBreak >= 100) {
                    mouseCalibrationStage = 0;
                }
                degreesPerPixel = delta2 / 9900.0;
                AutoSpeedrunApi.chatMessage(String.format("Calibrated deg/pix = %.4f", degreesPerPixel));
                lookAtAngles(0.0, 0.0);
                break;
            case 7:
                double resultYaw = F3Information.getYaw();
                double resultPitch = F3Information.getPitch();
                if (Math.abs(resultYaw) > 0.1 || Math.abs(resultPitch) > 0.1) {  // todo: better calibration
                    mouseCalibrationStage = 0;  // restart calibration if not acceptable
                    return true;
                }
                break;
        }
        mouseCalibrationStage++;
        return true;
    }

    public void tick() {
        Util.tickCount++;
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
        // live debug information
        BlockLocation targettedBL = F3Information.getTargettedBlockPosition();
        String targettedBlockPositionFormatted = targettedBL == null ? "(not targetting)" : targettedBL.toString();
        AutoSpeedrunApi.subtitleMessage(String.format(
            "%.2fs Y:%.1f,P:%.1f %s %s", Util.tickCount / 20.0,
            F3Information.getYaw(), F3Information.getPitch(),
            targettedBlockPositionFormatted, F3Information.getTargettedBlockName()
        ));
        // do mouse calibration on world join
        if (performMouseCalibration()) {
            return;
        }
        // collect facing block information
        if (F3Information.getTargettedBlockPosition() != null) {
            WorldBlocks.knownBlocks.put(F3Information.getTargettedBlockPosition(), new Block(
                F3Information.getTargettedBlockName()
            ));
        }
        // do movement
        if (Navigation.perform()) {
            return;
        }
    }

    public void debug(String debugStr) {
        String[] split = debugStr.split(" ");
        if (split[0].equals("dimension")) {
            AutoSpeedrunApi.chatMessage("Dimension: " + F3Information.getDimension());
        } else if (split[0].equals("clearcache")) {
            F3Information.clearCache();
            AutoSpeedrunApi.chatMessage("Cache cleared");
        } else if (split[0].equals("dumpblocks")) {
            for (BlockLocation bl : WorldBlocks.knownBlocks.keySet()) {
                name.quasar.autospeedrun.Util.LOGGER.info(bl + " - " + WorldBlocks.knownBlocks.get(bl));
            }
        } else if (split[0].equals("setnav")) {
            String[] xyzStr = split[1].split(",");
            Navigation.goalPosition = new Vector3(
                Double.parseDouble(xyzStr[0]), Double.parseDouble(xyzStr[1]), Double.parseDouble(xyzStr[2])
            );
        }
    }
}
