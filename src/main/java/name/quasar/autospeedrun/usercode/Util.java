package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.usercode.geometry.Vector3;

import static name.quasar.autospeedrun.usercode.F3Information.getPosition;

public class Util {

    public static final double OPTIONS_TXT_SENS = 0.394650399684906;  // from options.txt
    public static final double OPTIONS_TXT_FOV = 1.0;  // from options.txt
    public static final int OPTIONS_TXT_GUI_SCALE = 2;  // from options.txt

    // idk how these values are calculated if 0.85 * 1.8 is not 1.62 ???? wtf
    public static final double PLAYER_STANDING_EYE_HEIGHT = 1.62;
    public static final double PLAYER_CROUCHING_EYE_HEIGHT = 1.27;

    public static int tickCount = 0;

    public static int SCREEN_W = 0;
    public static int SCREEN_H = 0;

    public static boolean toggleDebugAir = false;

    public static RunStage runStage = RunStage.OVERWORLD;

    public static double mod(double a, double b) { return ((a % b) + b) % b; }

    public static double round(double value, int ndigits) {
        double factor = Math.pow(10, ndigits);
        return Math.round(value * factor) / factor;
    }

    public static double getEyeOffset() {
        return Util.PLAYER_STANDING_EYE_HEIGHT;  // todo support crouching
    }

    public static Vector3 getEyePosition() {
        return getPosition().offsetY(getEyeOffset());
    }

    // Mth replacement stuff

    public static int floor_Mth(float val) {
        int val_int = (int) val;
        return val < val_int ? val_int - 1 : val_int;
    }

    public static int floor_Mth(double val) {
        int val_int = (int) val;
        return val < val_int ? val_int - 1 : val_int;
    }

    public static int ceil_Mth(float val) {
        int val_int = (int) val;
        return val > val_int ? val_int + 1 : val_int;
    }

    public static int ceil_Mth(double val) {
        int val_int = (int) val;
        return val > val_int ? val_int + 1 : val_int;
    }

    public static int clamp_Mth(int val, int min, int max) {
        if (val < min) {
            return min;
        } else {
            return val > max ? max : val;
        }
    }

    public static double clamp_Mth(double v, double a, double b) {
        if (v < a) {
            return a;
        } else {
            return v > b ? b : v;
        }
    }

    private static final float[] SIN = new float[65536];

    static {
        for (int i = 0; i < SIN.length; i++) {
            SIN[i] = (float)Math.sin(i * Math.PI * 2.0 / 65536.0);
        }
    }

    public static float sin_Mth(float f) {
        return SIN[(int)(f * 10430.378F) & 65535];
    }

    public static float cos_Mth(float f) {
        return SIN[(int)(f * 10430.378F + 16384.0F) & 65535];
    }

    public static boolean equal_Mth(float a, float b) {
        return Math.abs(b - a) < 1.0E-5F;
    }

    public static boolean equal_Mth(double a, double b) {
        return Math.abs(b - a) < 1.0E-5F;
    }
}
