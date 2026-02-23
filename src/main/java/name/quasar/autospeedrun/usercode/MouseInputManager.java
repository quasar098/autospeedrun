package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import net.minecraft.client.Minecraft;

public class MouseInputManager {
    private static int calibrationStage = 0;
    private static double calibrationOffsetX = 0;
    private static double calibrationOffsetY = 0;
    private static double mouseMultiplier = 0;

    public static void lookAtPoint(Vector3 point) {
        Vector3 cur = F3Information.getPosition();
        double goalYaw = Math.atan2(point.getX() - cur.getX(), point.getZ() - cur.getZ()) * 180 / Math.PI;
        double distanceXZ = Math.sqrt(Math.pow(cur.getX() - point.getX(), 2) + Math.pow(cur.getZ() - point.getZ(), 2));
        double eyeHeight = Util.PLAYER_STANDING_EYE_HEIGHT;  // todo: crouching stuff
        double goalPitch = Math.atan2((point.getY() - eyeHeight) - cur.getY(), distanceXZ) * 180 / Math.PI;
        AutoSpeedrunApi.chatMessage("y,p:" + goalYaw + "," + goalPitch);
        setPlayerAngle(goalYaw, goalPitch);
    }

    public static void setPlayerAngle(double yaw, double pitch) {
        AutoSpeedrunApi.mouseMove(
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
                AutoSpeedrunApi.mouseMove(0, 0);
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
                    calibrationStage = 0;
                    AutoSpeedrunApi.chatMessage("Restarting mouse calibration");
                    break;
                }
                calibrationStage++;
                return false;
            default:
//                lookAtPoint(new Vector3(-100.5, 65.00, 239.5));
                return false;
        }
        return true;
    }

    public static int testLLL = 100;
    public static int testLLLStage = 0;

    public static boolean testLLL() {
        if (testLLL >= 100) {
            return false;
        }
        if (testLLLStage == 0) {
            AutoSpeedrunApi.mouseMove(testLLL, 0.0);
        } else if (testLLLStage == 3) {
            if (Minecraft.getInstance().player != null) {
                System.out.printf("%d %f %f\n", testLLL, F3Information.getYaw(), Minecraft.getInstance().player.yRot);
            }
            testLLL++;
        }
        testLLLStage += 1;
        testLLLStage %= 4;
        return true;
    }

    public static void reset() {
        calibrationStage = 0;
        mouseMultiplier = -1.0 / (Math.pow(Util.SENS * 0.6 + 0.2, 3) * 8 * 0.15);
    }
}
