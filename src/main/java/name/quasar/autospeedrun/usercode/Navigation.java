package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.DebugRenderLine;
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

    public static boolean perform() {
        if (goalPosition == null) {
            return false;
        }
        Vector3 goal = goalPosition;
        // debug render line
        AutoSpeedrunApi.renderLine(new DebugRenderLine(
                goal.withY(0.0).toVector3f(),
                goal.withY(256.0).toVector3f(),
                0.0f, 0.0f, 0.0f
        ));
//        if (getAlignment() == AxisAlignment.PRIORITY_X) {
//            if (Math.abs(F3Information.getPosition().getX()-goal.getX()) < AXIS_ALIGNMENT_BOUND) {
//
//            }
//        }
//        if (getAlignment() == AxisAlignment.PRIORITY_Z) {
//
//        }
        double yaw = F3Information.getYaw();
        Double goalYaw = Math.atan2(
            F3Information.getPosition().getX() - goal.getX(),
            goal.getZ() - F3Information.getPosition().getZ()
        ) * 180 / Math.PI;
        double distance = goal.distanceTo(F3Information.getPosition());
        if (goal.getY() <= 0) {
            distance = goal.distanceTo2d(F3Information.getPosition());
        }
        if (distance < ARRIVED_AT_DEST_POSITION_BOUND) {
            // this means we have arrived
            AutoSpeedrunApi.chatMessage("Arrived at your destination");
            goalPosition = null;
            return false;
        }
        MouseInputManager.setPlayerAngle(goalYaw, F3Information.getPitch());  // todo better
        return false;
    }
}
