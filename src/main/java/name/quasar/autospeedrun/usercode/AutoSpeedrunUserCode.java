package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import org.lwjgl.glfw.GLFW;

public class AutoSpeedrunUserCode {
    int SCREEN_W = 0;
    int SCREEN_H = 0;
    int tickCount = 0;

    int mouseCalibrationStage = 0;
    double mouseCalibrationYaw1 = 0;
    double mouseCalibrationYaw2 = 0;
    double mouseCalibrationYaw3 = 0;
    double degreesPerPixel = 0;

    public void init() {
        // misc useful
        SCREEN_W = 0;
        SCREEN_H = 0;
        tickCount = 0;

        // mouse calibration
        mouseCalibrationStage = 0;
        mouseCalibrationYaw1 = 0;
        mouseCalibrationYaw2 = 0;
        mouseCalibrationYaw3 = 0;
        degreesPerPixel = 0;
    }

    public void lookAtAngles(double yaw) {
        double yawMod360 = (yaw + 360.0) % 360.0;
        if (yawMod360 < mouseCalibrationYaw1) {
            yawMod360 += 360.0;
        }
        int yawPixels = (int) Math.round((yawMod360 - mouseCalibrationYaw1) / degreesPerPixel);
        AutoSpeedrunApi.moveMouse(yawPixels, 0);
    }

    public boolean performMouseCalibration() {
        // 1 = will look 0, 0
        // 4 = collect angle of 0, 0, will look 100, 0
        // 5 = collect angle of 100, 0, will look 10000, 0
        // 6 = collect angle of 10000, 0
        if (mouseCalibrationStage > 7) {
            return false;
        }
        switch (mouseCalibrationStage) {
            case 1:
                AutoSpeedrunApi.moveMouse(0, 0);
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

                lookAtAngles(0.0);
                break;
        }
        // todo do pitch calibrations as well
        mouseCalibrationStage++;
        return true;
    }

    public void tick() {
        tickCount++;
        AutoSpeedrunApi.screenshotAsync(1920, 1080);
        // screen resolution not yet resolved, resolve it before doing anything else
        if (SCREEN_W == 0 || SCREEN_H == 0) {
            SCREEN_W = AutoSpeedrunApi.getScreenshotWidth();
            SCREEN_H = AutoSpeedrunApi.getScreenshotHeight();
            if (SCREEN_W == 0 || SCREEN_H == 0) {
                return;
            }
            AutoSpeedrunApi.chatMessage(String.format("Screenshots W/H Resolved: %dx%d", SCREEN_W, SCREEN_H));
        }
        // f3 must open always
        if (!F3Information.isF3Open()) {
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
            return;
        }
        F3Information.clearCache();
        // live debug information
        AutoSpeedrunApi.subtitleMessage(String.format(
                "%.2fs %s Y:%.1f,P:%.1f", tickCount / 20.0, F3Information.getPosition().toString(3),
                F3Information.getYaw(), F3Information.getPitch()
        ));
        // do mouse calibration on world join
        if (performMouseCalibration()) {
            return;
        }

    }

    public void debug(String debugStr) {
        String[] split = debugStr.split(" ");
        if (split[0].equals("text")) {  // text x,y
            String[] pos = split[1].split(",");
            int sx = Integer.parseInt(pos[0]);
            int sy = Integer.parseInt(pos[1]);
            AutoSpeedrunApi.chatMessage("\"" + Util.readScreenStringForward(sx, sy, Util.F3_DEBUG_TEXT_COLOR) + "\"");
        } else if (split[0].equals("f3")) {  // f3
            String previous = "";
            int sy = 4;
            for (int j = 0; j < 30; j++) {
                String current = Util.readScreenStringForward(4, sy, Util.F3_DEBUG_TEXT_COLOR);
                if (j == 0 && current.isEmpty()) {
                    AutoSpeedrunApi.chatMessage("f3 is not open (probably)");
                }
                AutoSpeedrunApi.chatMessage(current);
                if (previous.isEmpty() && current.isEmpty()) {
                    break;
                }
                sy += 18;
                previous = current;
            }
        }
    }
}
