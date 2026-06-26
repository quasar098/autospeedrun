package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import org.lwjgl.glfw.GLFW;

public class MovementInputManager {
    private static boolean prevW = false;
    private static boolean prevA = false;
    private static boolean prevS = false;
    private static boolean prevD = false;
    private static boolean prevSpace = false;
    private static boolean prevCrouch = false;

    private static boolean currW = false;
    private static boolean currA = false;
    private static boolean currS = false;
    private static boolean currD = false;
    private static boolean currSpace = false;
    private static boolean currCrouch = false;
    private static boolean sprinting = false;

    public static boolean isSprinting() {
        return sprinting;
    }

    public static void setSprinting(boolean sprint) {
        sprinting = sprint;
    }

    public static void handle() {
        if (prevW != currW) {
            if (currW) {
                if (sprinting) {
                    AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_P);
                }
                AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_W);
            } else {
                AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_W);
            }
        }
        if (prevA != currA) {
            if (currA) {
                AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_A);
            } else {
                AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_A);
            }
        }
        if (prevS != currS) {
            if (currS) {
                AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_S);
            } else {
                AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_S);
            }
        }
        if (prevD != currD) {
            if (currD) {
                AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_D);
            } else {
                AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_D);
            }
        }
        if (prevSpace != currSpace) {
            if (currSpace) {
                AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_SPACE);
            } else {
                AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_SPACE);
            }
        }
        if (prevCrouch != currCrouch) {
            if (currCrouch) {
                AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            } else {
                AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
            }
        }
        prevW = currW;
        prevA = currA;
        prevS = currS;
        prevD = currD;
        prevSpace = currSpace;
        prevCrouch = currCrouch;
        currW = false;
        currA = false;
        currS = false;
        currD = false;
        currSpace = false;
        currCrouch = false;
    }

    public static void cancelWASD() {
        currW = false;
        currA = false;
        currS = false;
        currD = false;
    }

    public static void planPressKeyW() { currW = true; }
    public static void planPressKeyA() { currA = true; }
    public static void planPressKeyS() { currS = true; }
    public static void planPressKeyD() { currD = true; }
    public static void planPressKeySpace() { currSpace = true; }
    public static void planPressKeyCrouch() { currCrouch = true; }
}
