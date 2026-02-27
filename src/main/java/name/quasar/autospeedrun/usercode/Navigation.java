package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import org.lwjgl.glfw.GLFW;

public class Navigation {

    // player hitbox 0.6 wide, so ~0.2 remaining on each side
    private static final double AXIS_ALIGNMENT_BOUND = 0.15;

    // player hitbox 0.6 wide, so ~0.2 remaining on each side
    private static final double ARRIVED_AT_DEST_POSITION_BOUND = 0.16;
    private static final double ARRIVED_AT_DEST_MAX_VELO = 0.16;

    /* the idea with "axis alignment" is if we are on bridge bastion and going up that one side railing with lava on
       either side we should not be touching the lava by going outside of a bound. we set an axis alignment to
       prioritize staying on a specific axis */

    public enum AxisAlignment {
        PRIORITY_X,
        PRIORITY_Z,
        INDIFFERENT
    }

    private static AxisAlignment alignment = AxisAlignment.INDIFFERENT;

    public static AxisAlignment getAlignment() {
        return alignment;
    }

    public static void setAlignment(AxisAlignment alignment) {
        Navigation.alignment = alignment;
    }

    private static Vector3 goalPosition = null;

    public static Vector3 getGoalPosition() {
        return goalPosition;
    }

    public static void setGoalPosition(Vector3 goalPosition) {
        Navigation.goalPosition = goalPosition;
    }

    public static void setGoalPosition(double x, double y, double z) {
        Navigation.goalPosition = new Vector3(x, y, z);
    }

    public static void setGoalPosition(double x, double z) {
        Navigation.goalPosition = new Vector3(x, -1, z);
    }

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
        if (Navigation.goalPosition == null) {
            return false;
        }
        Vector3 desired = Navigation.goalPosition;
//        if (getAlignment() == AxisAlignment.PRIORITY_X) {
//            if (Math.abs(F3Information.getPosition().getX()-desired.getX()) < AXIS_ALIGNMENT_BOUND) {
//
//            }
//        }
//        if (getAlignment() == AxisAlignment.PRIORITY_Z) {
//
//        }
        double yaw = F3Information.getYaw();
        Double goalYaw = Math.atan2(
            F3Information.getPosition().getX() - desired.getX(),
            desired.getZ() - F3Information.getPosition().getZ()
        ) * 180 / Math.PI;
        double distance = Navigation.goalPosition.distanceTo(F3Information.getPosition());
        if (Navigation.goalPosition.getY() <= 0) {
            distance = Navigation.goalPosition.distanceTo2d(F3Information.getPosition());
        }
        if (distance < ARRIVED_AT_DEST_POSITION_BOUND) {
            // this means we have arrived
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
            MovementInputManager.planPressKeyA();
        } else if (encX == -1) {
            MovementInputManager.planPressKeyD();
        }
        if (encZ == 1) {
            MovementInputManager.planPressKeyW();
        } else if (encZ == -1) {
            MovementInputManager.planPressKeyS();
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
