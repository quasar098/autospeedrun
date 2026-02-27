package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import org.lwjgl.glfw.GLFW;

public class MovementInputManager {
    private static boolean prevW = false;
    private static boolean prevA = false;
    private static boolean prevS = false;
    private static boolean prevD = false;
    private static boolean prevCrouch = false;
    private static boolean prevSprint = false;

    private static boolean currW = false;
    private static boolean currA = false;
    private static boolean currS = false;
    private static boolean currD = false;
    private static boolean currCrouch = false;
    private static boolean currSprint = false;

    public static boolean isSprinting() {
        return currSprint;
    }

    public static void setSprinting(boolean sprint) {
        currSprint = sprint;
    }

    public static void handle() {
        if (prevW != currW) {
            if (currW) {
                AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_W);
            } else {
                AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_W);
            }
        }
        if (prevA != currA) {
            if (currA) {
                AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_A);
            } else {
                AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_A);
            }
        }
        if (prevS != currS) {
            if (currS) {
                AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_S);
            } else {
                AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_S);
            }
        }
        if (prevD != currD) {
            if (currD) {
                AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_D);
            } else {
                AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_D);
            }
        }
        if (prevCrouch != currCrouch) {
            if (currCrouch) {
                AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            } else {
                AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            }
        }
        if (prevSprint != currSprint) {
            AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_P);  // todo fix potential sync issues
        }
        prevW = currW;
        prevA = currA;
        prevS = currS;
        prevD = currD;
        prevCrouch = currCrouch;
        prevSprint = currSprint;
        currW = false;
        currA = false;
        currS = false;
        currD = false;
        currCrouch = false;
    }

    public static void planPressKeyW() { currW = true; }
    public static void planPressKeyA() { currA = true; }
    public static void planPressKeyS() { currS = true; }
    public static void planPressKeyD() { currD = true; }
    public static void planPressKeyCrouch() { currCrouch = true; }
}
