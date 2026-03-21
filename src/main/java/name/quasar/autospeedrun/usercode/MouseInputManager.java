package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;

import static name.quasar.autospeedrun.usercode.Util.*;

public class MouseInputManager {
    private static int calibrationStage = 0;
    private static double calibrationOffsetX = 0;
    private static double calibrationOffsetY = 0;
    private static double degPerPix = 0;

    public static void lookAtPoint(Vector3 point) {
        Vector3 cur = F3Information.getPosition();
        double goalYaw = Math.atan2(point.getX() - cur.getX(), point.getZ() - cur.getZ()) * 180 / Math.PI;
        double distanceXZ = Math.sqrt(Math.pow(cur.getX() - point.getX(), 2) + Math.pow(cur.getZ() - point.getZ(), 2));
        double eyeHeight = Util.PLAYER_STANDING_EYE_HEIGHT;  // todo: crouching stuff
        double goalPitch = Math.atan2((point.getY() - eyeHeight) - cur.getY(), distanceXZ) * 180 / Math.PI;
        setPlayerAngle(-goalYaw, -goalPitch);
    }

    public static Double lastPlayerYaw = null;
    public static Double lastPlayerPitch = null;

    private static double psbsYawLeftAccept = 0.0;
    private static double psbsYawRightAccept = 1.0;
    private static Double psbsOriginalRealYaw = 0.0;
    private static int psbsYawPixelOffset = 0;

    private static final double psbsAcceptableYawRange = 0.00005;
    private static final double psbsAcceptablePitchRange = 0.001;

    private static boolean psbsAwaitingF3Change = false;

    public static void setPlayerAngle(double yaw, double pitch) {
        AutoSpeedrunApi.mouseMove(
                (int) (calibrationOffsetX + 1.0/degPerPix * yaw),
                (int) (calibrationOffsetY + 1.0/degPerPix * pitch)
        );
        lastPlayerYaw = yaw;
        lastPlayerPitch = pitch;
    }

    /**
     * Does mouse calibration for precise mouse angling, with weird "pixel stepping binary search" algorithm
     * @return true if unfinished, false if finished (can move onto next steps)
     */
    public static boolean calibrateMouse() {
        if (lastPlayerYaw != null && lastPlayerPitch != null && calibrationStage == -1) {
            double lastPlayerYawCorrected = ((lastPlayerYaw % 360) + 540) % 360 - 180;
            if (Math.abs(lastPlayerPitch - F3Information.getPitch()) >= 0.1 ||
                Math.abs(lastPlayerYawCorrected - F3Information.getYaw()) >= 0.1) {
                calibrationStage = 0;
                AutoSpeedrunApi.chatMessage(String.format(
                    "Restarting mouse calibration (%f vs %f)", lastPlayerYawCorrected, F3Information.getYaw()
                ));
                return true;
            }
            lastPlayerPitch = null;
            lastPlayerYaw = null;
        }
        switch (calibrationStage) {
            case -1:
                return false;
            case 0:
            case 1:
                if (String.valueOf(mod(degPerPix, 0.1)).length() < 8) {
                    AutoSpeedrunApi.chatMessage("your sensitivity leads to degPerPix that is highly commensurable with 0.1 (bad)");
                    AutoSpeedrunApi.emergencyStopUserCode();
                    return true;
                }
                // mc ignores the first move so we make sure it happens
                AutoSpeedrunApi.mouseMove(0, 0);
                psbsYawLeftAccept = 0.0;
                psbsYawRightAccept = 1.0;
                psbsAwaitingF3Change = false;
                psbsOriginalRealYaw = null;
//                AutoSpeedrunApi.chatMessage("Stage 1:" + F3Information.getYaw() + "," + F3Information.getPitch());
                calibrationStage++;
                break;
            case 2:
                // pixel stepping binary searching, see scripts/mouse-precision/second.py
                /*
                    best_i = None
                    mid = (right_accept + left_accept) / 2
                    for i in range(-2000, 2000):
                        if i == 0:
                            continue
                        if best_i is None:
                            best_i = i
                            continue
                        angle_offset = (i * deg_per_pix) % 0.1 - (1-mid)/10
                        best_angle_offset = (best_i * deg_per_pix) % 0.1 - (1-mid)/10
                        if abs(angle_offset) <= abs(best_angle_offset):
                            best_i = i
                    # print('pix to move from orig:', best_i)
                    view_pix_offset = best_i
                    real_angle = view_pix_offset * deg_per_pix + original_real_angle
                    lower = round(round(original_real_angle, 1)-0.05, 2)
                    upper = round(round(original_real_angle, 1)-0.05, 2)+0.1
                    if round(real_angle, 1) > round(lower + deg_per_pix * view_pix_offset, 1):
                        left_accept = (upper - lower - (deg_per_pix * view_pix_offset) % 0.1)*10
                    else:
                        right_accept = (upper - lower - (deg_per_pix * view_pix_offset) % 0.1)*10
                 */
                if (!psbsAwaitingF3Change) {
                    if (psbsOriginalRealYaw == null) {
                        psbsOriginalRealYaw = F3Information.getYaw();
                        AutoSpeedrunApi.chatMessage("dpp:" + degPerPix);
                    }
                    Integer bestYawIndex = null;
                    double mid = (psbsYawRightAccept + psbsYawLeftAccept) / 2;
                    for (int i = -3000; i < 3000; i++) {
                        if (i == 0) {
                            continue;
                        }
                        if (bestYawIndex == null) {
                            bestYawIndex = i;
                            continue;
                        }
                        double angleOffset = mod(i * degPerPix, 0.1) - (1.0 - mid) / 10.0;
                        double bestAngleOffset = mod(bestYawIndex * degPerPix, 0.1) - (1.0 - mid) / 10.0;
                        if (Math.abs(angleOffset) <= Math.abs(bestAngleOffset)) {
                            bestYawIndex = i;
                        }
                    }
                    System.out.println("PSBS moving mouse, pix to move from orig: " + bestYawIndex);
                    psbsYawPixelOffset = bestYawIndex;
                    AutoSpeedrunApi.mouseMove(psbsYawPixelOffset, 0);
                    psbsAwaitingF3Change = true;
                } else {
                    double f3Yaw = F3Information.getYaw();
                    double lower = round(round(psbsOriginalRealYaw, 1)-0.05, 2);
                    double expected = round(lower + degPerPix * psbsYawPixelOffset, 1);
                    AutoSpeedrunApi.chatMessage("lower:" + lower);
                    AutoSpeedrunApi.chatMessage("a vs b:" + f3Yaw + "," + expected);
                    AutoSpeedrunApi.chatMessage("stuff:" + mod(f3Yaw-expected+180, 360.0));
                    if (Math.abs(mod(f3Yaw-expected+180, 360.0) - 180.0) > 5.0) {
                        AutoSpeedrunApi.chatMessage("Restarting calibration");
                        calibrationStage = 0;
                        break;
                    }
                    boolean changedNothing = false;
                    if (mod(f3Yaw-expected+180, 360.0) > 180.0) {
                        if (psbsYawLeftAccept == (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1))*10) {
                            changedNothing = true;
                        }
                        psbsYawLeftAccept = (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1))*10;
                        AutoSpeedrunApi.chatMessage("Modifying leftAccept: " + psbsYawLeftAccept);
                    } else {
                        if (psbsYawRightAccept == (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1))*10) {
                            changedNothing = true;
                        }
                        psbsYawRightAccept = (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1))*10;
                        AutoSpeedrunApi.chatMessage("Modifying rightAccept: " + psbsYawRightAccept);
                    }
                    if (psbsYawLeftAccept > psbsYawRightAccept) {
                        AutoSpeedrunApi.chatMessage("psbsYawLeftAccept > psbsYawRightAccept");
                        AutoSpeedrunApi.chatMessage(psbsYawLeftAccept + "," + psbsYawRightAccept);
                        AutoSpeedrunApi.emergencyStopUserCode();
                        return true;
                    }
                    if (psbsYawRightAccept - psbsYawLeftAccept > 1.0) {
                        AutoSpeedrunApi.chatMessage("psbsYawRightAccept - psbsYawLeftAccept > 1.0");
                        AutoSpeedrunApi.chatMessage(psbsYawLeftAccept + "," + psbsYawRightAccept);
                        AutoSpeedrunApi.emergencyStopUserCode();
                        return true;
                    }
                    if (psbsYawRightAccept - psbsYawLeftAccept < psbsAcceptableYawRange || changedNothing) {
                        AutoSpeedrunApi.chatMessage("real,left,right: " + psbsOriginalRealYaw +
                                ',' + psbsYawLeftAccept + ',' + psbsYawRightAccept);
                        double minimum = psbsYawLeftAccept/10+round(round(psbsOriginalRealYaw, 1)-0.05, 2);
                        double maximum = psbsYawRightAccept/10+round(round(psbsOriginalRealYaw, 1)-0.05, 2);
                        AutoSpeedrunApi.chatMessage("min/max: " + minimum + "," + maximum);
                        calibrationStage++;
                    } else {
                        psbsAwaitingF3Change = false;
                    }
                }
                break;
            case 3:
                AutoSpeedrunApi.mouseMove(0, 0);
                calibrationStage++;
                break;
            case 4:
//                if (F3Information.getPitch() != 0.0 || F3Information.getYaw() != 0.0) {
//                    calibrationStage = 0;
//                    AutoSpeedrunApi.chatMessage("Retrying mouse calibration");
//                    break;
//                }
                AutoSpeedrunApi.chatMessage("Mouse calibration successful");
                calibrationStage = -1;
                return false;
        }
        return true;
    }

    public static void reset() {
        calibrationStage = 0;
        degPerPix = Math.pow(Util.OPTIONS_TXT_SENS * 0.6 + 0.2, 3) * 8 * 0.15;
    }
}
