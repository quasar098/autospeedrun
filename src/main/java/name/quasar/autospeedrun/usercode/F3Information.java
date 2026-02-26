package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;

import java.util.Arrays;

public class F3Information {
    public static int f3TextColor = 0xffdddddd;
    public static boolean f3HasBackgroundDim = false;

    public static boolean isF3Open() {
        if (Util.readScreenStringForward(4, 4, 0xffdddddd, 2).startsWith("Minecraft 1.16.1")) {
            f3TextColor = 0xffdddddd;
            f3HasBackgroundDim = false;
            return true;
        } else if (Util.readScreenStringForward(4, 4, 0xff3c3c3c, 2).startsWith("Minecraft 1.16.1")) {
            f3TextColor = 0xff3c3c3c;
            f3HasBackgroundDim = true;
            return true;
        }
        return false;
    }

    public static void clearCache() {
        cachedPosition = null;
        cachedYaw = null;
        cachedPitch = null;
        cachedDimension = null;
        cachedTargettedBlockPosition = null;
        cachedTargettedBlockName = null;
        cachedPiePathText = null;
        cachedPieLabels = null;
        cachedPieRelativeResults = null;
        cachedPieGlobalResults = null;
    }

    /* xyz position */

    public static Vector3 cachedPosition = null;

    public static Vector3 getPosition() {
        if (cachedPosition == null) {
            String positionLine = Util.readScreenStringForward(4, 4 + 18*10, f3TextColor, 2);
            assert !positionLine.isEmpty();
            String[] splitPositionText = positionLine.replaceFirst("XYZ: ", "").split(" / ");
            cachedPosition = new Vector3(
                    Double.parseDouble(splitPositionText[0]),
                    Double.parseDouble(splitPositionText[1]),
                    Double.parseDouble(splitPositionText[2])
            );
        }
        return cachedPosition;
    }

    /* pitch + yaw angles */

    public static Double cachedPitch = null;

    public static Double getPitch() {
        if (cachedPitch == null) {
            String anglesLine = Util.readScreenStringForward(4, 4 + 18*13, f3TextColor, 2);
            assert !anglesLine.isEmpty();
            String[] splitPositionText = anglesLine
                    .replaceFirst("Facing: [^(]+\\([^)]+\\) \\(", "")
                    .replaceFirst("\\)", "")
                    .split(" / ");
            cachedPitch = Double.parseDouble(splitPositionText[1]);
            cachedYaw = Double.parseDouble(splitPositionText[0]);
        }
        return cachedPitch;
    }

    public static Double cachedYaw = null;

    public static Double getYaw() {
        if (cachedYaw == null) {
            String anglesLine = Util.readScreenStringForward(4, 4 + 18*13, f3TextColor, 2);
            assert !anglesLine.isEmpty();
            String[] splitPositionText = anglesLine
                    .replaceFirst("Facing: [^(]+\\([^)]+\\) \\(", "")
                    .replaceFirst("\\)", "")
                    .split(" / ");
            cachedPitch = Double.parseDouble(splitPositionText[1]);
            cachedYaw = Double.parseDouble(splitPositionText[0]);
        }
        return cachedYaw;
    }

    /* current dimension */

    public static Dimension cachedDimension = null;

    public static Dimension getDimension() {
        if (cachedDimension == null) {
            String f3ReadDimension = Util.readScreenStringForward(4, 4 + 18*8, f3TextColor, 2);
            if (f3ReadDimension.contains("minecraft:overworld")) {
                cachedDimension = Dimension.OVERWORLD;
            } else if (f3ReadDimension.contains("minecraft:the_nether")) {
                cachedDimension = Dimension.NETHER;
            } else if (f3ReadDimension.contains("minecraft:the_end")) {
                cachedDimension = Dimension.END;
            } else {
                AutoSpeedrunApi.chatMessage("Unknown dimension (assuming overworld): " + f3ReadDimension);
                cachedDimension = Dimension.OVERWORLD;
            }
        }
        return cachedDimension;
    }

    /* targetted block position + name */

    public static BlockLocation cachedTargettedBlockPosition = null;

    public static BlockLocation getTargettedBlockPosition() {
        if (cachedTargettedBlockPosition == null) {
            String positionLine = Util.readScreenStringBackward(Util.SCREEN_W - 6, 4 + 18*10, f3TextColor, 2);
            String[] split = positionLine.replaceFirst("Targeted Block: ", "").split(", ");
            if (split.length != 3) {
                return null;
            }
            try {
                cachedTargettedBlockPosition = new BlockLocation(
                    F3Information.getDimension(),
                    Integer.parseInt(split[0]),
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[2])
                );
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return cachedTargettedBlockPosition;
    }

    public static String cachedTargettedBlockName = null;

    public static String getTargettedBlockName() {
        if (cachedTargettedBlockName == null) {
            cachedTargettedBlockName = Util.readScreenStringBackward(
                Util.SCREEN_W - 6, 4 + 18*11, f3TextColor, new int[]{
                    63, 65, 66, 67, 68, 69, 71, 72, 74, 77, 78, 79, 80,
                    81, 82, 83, 85, 86, 87, 88, 89, 90, 70, 75, 84,
                    76, 26, 73
                }, 2);
        }
        return cachedTargettedBlockName;
    }

    /* pie charting */

    public static String cachedPiePathText = null;
    public static String[] cachedPieLabels = null;
    public static String[] cachedPieRelativeResults = null;
    public static String[] cachedPieGlobalResults = null;

    public static final int[] pieTextColors = {
        0xc2ccc2,  // gameRenderer
        0xca4365,  // tick
        0xc2cc63,  // level
        0xc245e1,  // entities
        0x4d6de9,  // blockentities (gameRenderer.level.entities / pie-ray)
        0x6dc4c4,  // blockEntities (tick.level.entities / pie-dar)
        0xcae14d,  // minecraft:mob_spawner
        0xe16dc4,  // minecraft:chest
        0x65cc45,  // unspecified
    };

    /* includes the "[0] " prefix */
    public static String getPiePathRawText() {
        if (cachedPiePathText == null) {
            cachedPiePathText = Util.readScreenStringForward(Util.SCREEN_W - 330, Util.SCREEN_H - 416, 0xfffcfcfc, 1);
        }
        return cachedPiePathText;
    }

    public static boolean isPieChartShown() {
        return getPiePathRawText().startsWith("[0] ");
    }

    public static int getRecommendedNumForPiePathTraversal(String desiredPath) {
        String currPiePath = getPiePathRawText().replaceFirst("\\[0] ", "");
        if (desiredPath.equals(currPiePath)) {
            return -1;
        }
        if (!desiredPath.startsWith(currPiePath) || currPiePath.length() > desiredPath.length()) {
            return 0;
        }
        String clickThisLabelNext = desiredPath.substring(currPiePath.length() + 1).split("\\.")[0];
        if (cachedPieLabels == null) {
            cachedPieLabels = new String[9];
        }
        for (int i = 0; i < 9; i++) {
            if (cachedPieLabels[i] == null) {
                cachedPieLabels[i] = Util.readScreenStringForward(
                    Util.SCREEN_W - 330,
                    Util.SCREEN_H - 220 + 8 * i,
                    c -> Arrays.stream(pieTextColors).anyMatch(x -> x == c),
                    1
                ).replaceFirst("\\[\\d+] ", "");
            }
            if (cachedPieLabels[i].equals(clickThisLabelNext)) {
                return i+1;
            }
        }
        return 0;
    }

    // returns null if the text doesn't exist
    public static Double getPieDirectoryRelativePercentage(String directory) {
        if (cachedPieLabels == null) {
            cachedPieLabels = new String[9];
        }
        if (cachedPieRelativeResults == null) {
            cachedPieRelativeResults = new String[9];
        }
        for (int i = 0; i < 9; i++) {
            if (cachedPieLabels[i] == null) {
                cachedPieLabels[i] = Util.readScreenStringForward(
                    Util.SCREEN_W - 330,
                    Util.SCREEN_H - 220 + 8 * i,
                    c -> Arrays.stream(pieTextColors).anyMatch(x -> x == c),
                    1
                ).replaceFirst("\\[\\d+] ", "");
            }
            if (directory.equals(cachedPieLabels[i])) {
                if (cachedPieRelativeResults[i] == null) {
                    cachedPieRelativeResults[i] = Util.readScreenStringBackward(
                        Util.SCREEN_W - 61,
                        Util.SCREEN_H - 220 + 8 * i,
                        c -> Arrays.stream(pieTextColors).anyMatch(x -> x == c),
                        1
                    );
                }
                try {
                    return Double.parseDouble(cachedPieRelativeResults[i].replaceFirst("%", ""));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    // returns null if the text doesn't exist
    public static Double getPieDirectoryGlobalPercentage(String directory) {
        if (cachedPieLabels == null) {
            cachedPieLabels = new String[9];
        }
        if (cachedPieGlobalResults == null) {
            cachedPieGlobalResults = new String[9];
        }
        for (int i = 0; i < 9; i++) {
            if (cachedPieLabels[i] == null) {
                cachedPieLabels[i] = Util.readScreenStringForward(
                    Util.SCREEN_W - 330,
                    Util.SCREEN_H - 220 + 8 * i,
                    c -> Arrays.stream(pieTextColors).anyMatch(x -> x == c),
                    1
                ).replaceFirst("\\[\\d+] ", "");
            }
            if (directory.equals(cachedPieLabels[i])) {
                if (cachedPieGlobalResults[i] == null) {
                    cachedPieGlobalResults[i] = Util.readScreenStringBackward(
                        Util.SCREEN_W - 11,
                        Util.SCREEN_H - 220 + 8 * i,
                        c -> Arrays.stream(pieTextColors).anyMatch(x -> x == c),
                        1
                    );
                }
                try {
                    return Double.parseDouble(cachedPieGlobalResults[i].replaceFirst("%", ""));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
