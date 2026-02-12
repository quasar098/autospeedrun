package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

public class MouseInputManager {
    private static int calibrationStage = 0;
    private static double calibrationOffsetX = 0;
    private static double calibrationOffsetY = 0;
    private static double mouseMultiplier = 0;

    public static void setPlayerAngle(double yaw, double pitch) {
        AutoSpeedrunApi.moveMouse(
            calibrationOffsetX + mouseMultiplier * yaw,
            calibrationOffsetY + mouseMultiplier * pitch
        );
    }

    /**
     * Does mouse calibration for precise mouse angling
     * @return true if unfinished, false if finished (can move onto next steps)
     */
    public static boolean calibrateMouse() {
        switch (calibrationStage) {
            case 0:
                calibrationStage++;
                break;
            case 1:
                // mc ignores the first move so we do it first thing
                AutoSpeedrunApi.moveMouse(0, 0);
//                AutoSpeedrunApi.chatMessage("Stage 1:" + F3Information.getYaw() + "," + F3Information.getPitch());
                calibrationStage++;
                break;
            case 2:
                calibrationOffsetX = mouseMultiplier * F3Information.getYaw();
                calibrationOffsetY = mouseMultiplier * F3Information.getPitch();
//                AutoSpeedrunApi.chatMessage("Stage 2:" + F3Information.getYaw() + "," + F3Information.getPitch());
//                AutoSpeedrunApi.chatMessage("Stage 2C:" + calibrationOffsetX + "," + calibrationOffsetY);
                setPlayerAngle(0.0, 0.0);
                calibrationStage++;
                break;
            case 3:
//                AutoSpeedrunApi.chatMessage("Stage 3:" + F3Information.getYaw() + "," + F3Information.getPitch());
                if (F3Information.getPitch() != 0.0 || F3Information.getYaw() != 0.0) {
                    break;
                }
                calibrationStage++;
                return false;
            default:
                return false;
        }
        return true;
    }

    public static void reset() {
        calibrationStage = 0;
        mouseMultiplier = -1.0 / (Math.pow(Util.SENS * 0.6 + 0.2, 3) * 8 * 0.15);
    }
}
