package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import org.lwjgl.glfw.GLFW;

public class Navigation {
    public static Vector3 goalPosition = null;

    public static int[][] movementPWMTable = {
        {-18000, 10},
        {-16252, 20, 10, 10},
        {-15453, 20, 10},
        {-14351, 20, 10, 20, 20},
        {-13500, 20},
        {-12420, 20, 20, 20, 21},
        {-11672, 20, 20, 20, 22},
        {-11235, 20, 21},
        {-10079, 20, 21, 21, 21},
        {-9000, 21},
        {-7920, 22, 21, 21, 21},
        {-6764, 22, 21},
        {-6316, 22, 22, 20, 22},
        {-5579, 22, 21, 22, 22},
        {-4499, 22},
        {-3648, 22, 12, 22, 22},
        {-2546, 22, 12},
        {-1747, 22, 12, 12},
        {0, 12},
        {1060, 2, 12, 12, 12},
        {2261, 2, 12},
        {3414, 2, 2, 2, 12},
        {4500, 2},
        {5574, 2, 1, 2, 2},
        {6738, 2, 1},
        {7939, 2, 1, 1, 1},
        {9000, 1},
        {10060, 0, 1, 1, 1},
        {11261, 0, 1},
        {12414, 0, 0, 0, 1},
        {13500, 0},
        {14585, 0, 0, 0, 10},
        {15738, 0, 10},
        {16939, 0, 10, 10, 10},
    };

    public static int lastPWMIndex = -1;
    public static int pwmTickCount = 0;

    // saving for later: /userdebug setnav -100.5,64.00,250.5
    public static boolean perform() {
        AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_W);
        AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_A);
        AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_S);
        AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_D);
        if (Navigation.goalPosition == null) {
            return false;
        }
        double yaw = F3Information.getYaw();
        Double goalYaw = Math.atan2(
            F3Information.getPosition().getX() - Navigation.goalPosition.getX(),
            Navigation.goalPosition.getZ() - F3Information.getPosition().getZ()
        ) * 180 / Math.PI;
        if (Navigation.goalPosition.distanceTo(F3Information.getPosition()) < 0.16) {
            // player hitbox 0.8 wide, this means we have arrived
            AutoSpeedrunApi.chatMessage("Arrived at your destination");
            goalPosition = null;
            return false;
        }
        int bestPWMIndex = getBestPWMIndex(goalYaw, yaw);
        if (bestPWMIndex != lastPWMIndex) {
            pwmTickCount = 0;
        }
        int encoded = movementPWMTable[bestPWMIndex][1 + (pwmTickCount % (movementPWMTable[bestPWMIndex].length - 1))];
        int encX = (encoded / 10) - 1;
        int encZ = (encoded % 10) - 1;
        AutoSpeedrunApi.chatMessage(encX + "," + encZ);
        if (encX == 1) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_A);
        } else if (encX == -1) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_D);
        }
        if (encZ == 1) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_W);
        } else if (encZ == -1) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_S);
        }
        pwmTickCount += 1;
        lastPWMIndex = bestPWMIndex;
        return false;
    }

    private static int getBestPWMIndex(Double goalYaw, double yaw) {
        int lookupTravelYaw = (int) Math.round(100 * (goalYaw - yaw));
        lookupTravelYaw = (lookupTravelYaw + 54000) % 36000 - 18000;
        int bestPWMIndex = 0;
        int bestPWMDiff = 999999;
        for (int i = 0; i < movementPWMTable.length; i++) {
            int currentPWMYaw = movementPWMTable[i][0];
            int currentDiff = Math.min(
                Math.abs(lookupTravelYaw - currentPWMYaw),
                36000 - Math.abs(lookupTravelYaw - currentPWMYaw)
            );
            if (currentDiff < bestPWMDiff) {
                bestPWMIndex = i;
                bestPWMDiff = currentDiff;
            }
        }
        return bestPWMIndex;
    }
}
