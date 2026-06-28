package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import org.lwjgl.glfw.GLFW;

import static name.quasar.autospeedrun.usercode.Util.*;

public class MouseInputManager {
    public static int calibrationStage = 0;
    private static double calibrationOffsetX = 0;
    private static double calibrationOffsetY = 0;
    private static double degPerPix = 0;

    public static void lookAtPoint(Vector3 point) {
        Vector3 pos = F3Information.getPosition();
        double goalYaw = Math.atan2(point.getX() - pos.getX(), point.getZ() - pos.getZ()) * 180 / Math.PI;
        double distanceXZ = Math.sqrt(Math.pow(pos.getX() - point.getX(), 2) + Math.pow(pos.getZ() - point.getZ(), 2));
        double goalPitch = Math.atan2(point.getY() - Util.getEyePosition().getY(), distanceXZ) * 180 / Math.PI;
        setPlayerAngle(-goalYaw, -goalPitch);
    }

    public static Double lastPlayerYaw = null;
    public static Double lastPlayerPitch = null;

    private static double psbsYawLeftAccept = 0.0;
    private static double psbsYawRightAccept = 1.0;
    private static Double psbsOriginalRealYaw = 0.0;
    private static int psbsYawPixelOffset = 0;

    private static double psbsPitchLeftAccept = 0.0;
    private static double psbsPitchRightAccept = 1.0;
    private static Double psbsOriginalRealPitch = 0.0;
    private static int psbsPitchPixelOffset = 0;

    private static final double PSBS_ACCEPTABLE_YAW_RANGE = 0.0005;
    private static final double PSBS_ACCEPTABLE_PITCH_RANGE = 0.001;

    private static boolean psbsAwaitingF3Change = false;

    public static void setPlayerAngle(double yaw, double pitch) {
        /* todo change calibration offsets to be where the 0,0 mouse position is and then use LLL to get optimal integer
            mouse coordinates */
        AutoSpeedrunAPI.mouseMove(
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
            double lastPlayerYawCorrected = (((lastPlayerYaw % 360) + 540) % 360) - 180;
            // after LLL is added change > 0.2 to >= 0.1 for both yaw and pitch
            if (Math.abs(lastPlayerYawCorrected - F3Information.getYaw()) > 0.2) {
                calibrationStage = 0;
                AutoSpeedrunAPI.chatMessage(String.format(
                    "Restarting mouse calibration (lastYaw=%f vs currYaw=%f)",
                    lastPlayerYawCorrected, F3Information.getYaw()
                ));
                return true;
            }
            if (Math.abs(lastPlayerPitch - F3Information.getPitch()) > 0.2) {
                calibrationStage = 0;
                AutoSpeedrunAPI.chatMessage(String.format(
                    "Restarting mouse calibration (lastPitch=%f vs currPitch=%f)",
                    lastPlayerPitch, F3Information.getPitch()
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
                // is this even necessary it seems to work alright even with degPerPix commensurable with 0.1
                if (String.valueOf(mod(degPerPix, 0.1)).length() < 8) {
                    // todo alternative mouse handling if degPerPix*10 is approx a nonzero integer
                    // probably that only leaves the case that gcd(round(degPerPix*10), 360) != 1 which is never manageable probably
                    AutoSpeedrunAPI.chatMessage("your sensitivity leads to degPerPix that is highly commensurable with 0.1 (bad)");
                    AutoSpeedrunAPI.emergencyStopUserCode();
                    return true;
                }
                // mc ignores the first move or something so we make sure it happens
                AutoSpeedrunAPI.mouseMove(0, 0);
                psbsYawLeftAccept = 0.0;
                psbsYawRightAccept = 1.0;
                psbsOriginalRealYaw = null;
                psbsPitchLeftAccept = 0.0;
                psbsPitchRightAccept = 1.0;
                psbsOriginalRealPitch = null;
                psbsAwaitingF3Change = false;
                calibrationStage++;
                return true;
            case 2:
                // pixel stepping binary searching, see scripts/mouse-precision/second.py
                if (!psbsAwaitingF3Change) {
                    if (psbsOriginalRealYaw == null || psbsOriginalRealPitch == null) {
                        psbsOriginalRealYaw = F3Information.getYaw();
                        psbsOriginalRealPitch = F3Information.getPitch();
                        AutoSpeedrunAPI.chatMessage("dpp:" + degPerPix);
                        AutoSpeedrunAPI.chatMessage("psbsOriginalRealYaw:" + psbsOriginalRealYaw);
                        AutoSpeedrunAPI.chatMessage("psbsOriginalRealPitch:" + psbsOriginalRealPitch);
                    }
                    // yaw
                    Integer bestYawIndex = null;
                    double midYawAccept = (psbsYawRightAccept + psbsYawLeftAccept) / 2;
                    for (int i = -1000; i < 1000; i++) {
                        if (i == 0) {
                            continue;
                        }
                        if (bestYawIndex == null) {
                            bestYawIndex = i;
                            continue;
                        }
                        double angleOffset = mod(i * degPerPix, 0.1) - (1.0 - midYawAccept) / 10.0;
                        double bestAngleOffset = mod(bestYawIndex * degPerPix, 0.1) - (1.0 - midYawAccept) / 10.0;
                        if (Math.abs(angleOffset) <= Math.abs(bestAngleOffset)) {
                            bestYawIndex = i;
                        }
                    }
                    // pitch
                    Integer bestPitchIndex = null;
                    double midPitchAccept = (psbsPitchRightAccept + psbsPitchLeftAccept) / 2;
                    // -85 < psbsOriginalRealPitch + i * degPerPix < 85
                    // (-85 - psbsOriginalRealPitch)/degPerPix < i < (85 - psbsOriginalRealPitch)/degPerPix
                    int minI = (int) ((-85 - psbsOriginalRealPitch)/degPerPix);
                    int maxI = (int) ((85 - psbsOriginalRealPitch)/degPerPix);
                    if (minI >= maxI) {
                        AutoSpeedrunAPI.chatMessage("no valid pitch wtf");
                        AutoSpeedrunAPI.emergencyStopUserCode();
                        return false;
                    }
                    for (int i = minI; i < maxI; i++) {
                        if (i == 0) {
                            continue;
                        }
                        if (bestPitchIndex == null) {
                            bestPitchIndex = i;
                            continue;
                        }
                        double angleOffset = mod(i * degPerPix, 0.1) - (1.0 - midPitchAccept) / 10.0;
                        double bestAngleOffset = mod(bestPitchIndex * degPerPix, 0.1) - (1.0 - midPitchAccept) / 10.0;
                        if (Math.abs(angleOffset) <= Math.abs(bestAngleOffset)) {
                            bestPitchIndex = i;
                        }
                    }
                    if (bestPitchIndex == null) {  // theoretically never going to happen but intellij is complaining
                        bestPitchIndex = 0;
                    }
                    System.out.println("PSBS moving mouse, pix to move from orig: " + bestYawIndex);
                    psbsYawPixelOffset = bestYawIndex;
                    psbsPitchPixelOffset = bestPitchIndex;
                    AutoSpeedrunAPI.mouseMove(psbsYawPixelOffset, psbsPitchPixelOffset);
                    psbsAwaitingF3Change = true;
                } else {
                    // yaw
                    boolean yawUnchanged = false;
                    if (psbsYawRightAccept - psbsYawLeftAccept >= PSBS_ACCEPTABLE_YAW_RANGE) {
                        double f3Yaw = F3Information.getYaw();
                        double lowerYaw = round(round(psbsOriginalRealYaw, 1) - 0.05, 2);
                        double expectedYaw = round(lowerYaw + degPerPix * psbsYawPixelOffset, 1);
                        AutoSpeedrunAPI.chatMessage("lowerYaw:" + lowerYaw);
                        if (Math.abs(mod(f3Yaw - expectedYaw + 180, 360.0) - 180.0) > 5.0) {
                            AutoSpeedrunAPI.chatMessage("Restarting calibration");
                            calibrationStage = 0;
                            break;
                        }
                        if (mod(f3Yaw - expectedYaw + 180, 360.0) > 180.0) {
                            if (psbsYawLeftAccept == (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1)) * 10) {
                                yawUnchanged = true;
                            }
                            psbsYawLeftAccept = (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1)) * 10;
                            AutoSpeedrunAPI.chatMessage("Modifying leftAccept: " + psbsYawLeftAccept);
                        } else {
                            if (psbsYawRightAccept == (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1)) * 10) {
                                yawUnchanged = true;
                            }
                            psbsYawRightAccept = (0.1 - mod(degPerPix * psbsYawPixelOffset, 0.1)) * 10;
                            AutoSpeedrunAPI.chatMessage("Modifying rightAccept: " + psbsYawRightAccept);
                        }
                        if (psbsYawLeftAccept > psbsYawRightAccept) {
                            AutoSpeedrunAPI.chatMessage("psbsYawLeftAccept > psbsYawRightAccept");
                            AutoSpeedrunAPI.chatMessage(psbsYawLeftAccept + "," + psbsYawRightAccept);
                            AutoSpeedrunAPI.emergencyStopUserCode();
                            return true;
                        }
                        if (psbsYawRightAccept - psbsYawLeftAccept > 1.0) {
                            AutoSpeedrunAPI.chatMessage("psbsYawRightAccept - psbsYawLeftAccept > 1.0");
                            AutoSpeedrunAPI.chatMessage(psbsYawLeftAccept + "," + psbsYawRightAccept);
                            AutoSpeedrunAPI.emergencyStopUserCode();
                            return true;
                        }
                    }
                    // pitch
                    boolean pitchUnchanged = false;
                    if (psbsPitchRightAccept - psbsPitchLeftAccept >= PSBS_ACCEPTABLE_PITCH_RANGE) {
                        double f3Pitch = F3Information.getPitch();
                        double lowerPitch = round(round(psbsOriginalRealPitch, 1) - 0.05, 2);
                        double expectedPitch = round(lowerPitch + degPerPix * psbsPitchPixelOffset, 1);
                        AutoSpeedrunAPI.chatMessage("lowerPitch:" + lowerPitch);
                        if (Math.abs(mod(f3Pitch - expectedPitch + 180, 360.0) - 180.0) > 5.0) {
                            AutoSpeedrunAPI.chatMessage("Restarting calibration");
                            calibrationStage = 0;
                            break;
                        }
                        if (mod(f3Pitch - expectedPitch + 180, 360.0) > 180.0) {
                            if (psbsPitchLeftAccept == (0.1 - mod(degPerPix * psbsPitchPixelOffset, 0.1)) * 10) {
                                pitchUnchanged = true;
                            }
                            psbsPitchLeftAccept = (0.1 - mod(degPerPix * psbsPitchPixelOffset, 0.1)) * 10;
                            AutoSpeedrunAPI.chatMessage("Modifying leftAccept: " + psbsPitchLeftAccept);
                        } else {
                            if (psbsPitchRightAccept == (0.1 - mod(degPerPix * psbsPitchPixelOffset, 0.1)) * 10) {
                                pitchUnchanged = true;
                            }
                            psbsPitchRightAccept = (0.1 - mod(degPerPix * psbsPitchPixelOffset, 0.1)) * 10;
                            AutoSpeedrunAPI.chatMessage("Modifying rightAccept: " + psbsPitchRightAccept);
                        }
                        if (psbsPitchLeftAccept > psbsPitchRightAccept) {
                            AutoSpeedrunAPI.chatMessage("psbsPitchLeftAccept > psbsPitchRightAccept");
                            AutoSpeedrunAPI.chatMessage(psbsPitchLeftAccept + "," + psbsPitchRightAccept);
                            AutoSpeedrunAPI.emergencyStopUserCode();
                            return true;
                        }
                        if (psbsPitchRightAccept - psbsPitchLeftAccept > 1.0) {
                            AutoSpeedrunAPI.chatMessage("psbsPitchRightAccept - psbsPitchLeftAccept > 1.0");
                            AutoSpeedrunAPI.chatMessage(psbsPitchLeftAccept + "," + psbsPitchRightAccept);
                            AutoSpeedrunAPI.emergencyStopUserCode();
                            return true;
                        }
                    }
                    // stop under certain conditions
                    if ((psbsYawRightAccept - psbsYawLeftAccept < PSBS_ACCEPTABLE_YAW_RANGE || yawUnchanged)
                            && (psbsPitchRightAccept - psbsPitchLeftAccept < PSBS_ACCEPTABLE_PITCH_RANGE || pitchUnchanged)) {
                        double minimumYaw = psbsYawLeftAccept/10+round(round(psbsOriginalRealYaw, 1)-0.05, 2);
                        double maximumYaw = psbsYawRightAccept/10+round(round(psbsOriginalRealYaw, 1)-0.05, 2);
                        double minimumPitch = psbsPitchLeftAccept/10+round(round(psbsOriginalRealPitch, 1)-0.05, 2);
                        double maximumPitch = psbsPitchRightAccept/10+round(round(psbsOriginalRealPitch, 1)-0.05, 2);
                        AutoSpeedrunAPI.chatMessage("min/max yaw: " + minimumYaw + "," + maximumYaw);
                        AutoSpeedrunAPI.chatMessage("min/max pitch: " + minimumPitch + "," + maximumPitch);
                        calibrationStage++;
                    } else {
                        psbsAwaitingF3Change = false;
                    }
                }
                break;
            case 3:
                double minimumYaw = psbsYawLeftAccept/10+round(round(psbsOriginalRealYaw, 1)-0.05, 2);
                double maximumYaw = psbsYawRightAccept/10+round(round(psbsOriginalRealYaw, 1)-0.05, 2);
                double minimumPitch = psbsPitchLeftAccept/10+round(round(psbsOriginalRealPitch, 1)-0.05, 2);
                double maximumPitch = psbsPitchRightAccept/10+round(round(psbsOriginalRealPitch, 1)-0.05, 2);
                // todo do better with LLL or something :troll:
                calibrationOffsetX = -(minimumYaw + maximumYaw) / (2 * degPerPix);
                calibrationOffsetY = -(minimumPitch + maximumPitch) / (2 * degPerPix);
                setPlayerAngle(0, 0);
                calibrationStage++;
                break;
            case 4:
                // reenable this after adding LLL stuff
//                if (F3Information.getPitch() != 0.0 || F3Information.getYaw() != 0.0) {
//                    calibrationStage = 0;
//                    AutoSpeedrunApi.chatMessage("Retrying mouse calibration");
//                    break;
//                }
                AutoSpeedrunAPI.chatMessage("Mouse calibration successful");
                calibrationStage = -1;
                return false;
        }
        return true;
    }

    private static boolean leftButtonCurr = false;
    private static boolean rightButtonCurr = false;
    private static boolean leftButtonPrev = false;
    private static boolean rightButtonPrev = false;

    public static void planPressLeftButton() {
        leftButtonCurr = true;
    }

    public static void planPressRightButton() {
        rightButtonCurr = true;
    }

    public static void handle() {
        if (leftButtonCurr != leftButtonPrev) {
            if (leftButtonCurr) {
                AutoSpeedrunAPI.pressLeftClick();
            } else {
                AutoSpeedrunAPI.releaseLeftClick();
            }
        }
        if (rightButtonCurr != rightButtonPrev) {
            if (rightButtonCurr) {
                AutoSpeedrunAPI.pressRightClick();
            } else {
                AutoSpeedrunAPI.releaseRightClick();
            }
        }
        leftButtonPrev = leftButtonCurr;
        rightButtonPrev = rightButtonCurr;
        leftButtonCurr = false;
        rightButtonCurr = false;
    }

    public static void reset() {
        calibrationStage = 0;
        degPerPix = Math.pow(Util.OPTIONS_TXT_SENS * 0.6 + 0.2, 3) * 8 * 0.15;
    }
}
