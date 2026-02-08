package name.quasar.autospeedrun.usercode;

public class F3Information {
    public static boolean isF3Open() {
        return Util.readScreenStringForward(4, 4, Util.F3_DEBUG_TEXT_COLOR).startsWith("Minecraft 1.16.1");
    }

    public static Vector3 cachedPosition = null;

    public static Vector3 getPosition() {
        if (cachedPosition == null) {
            String positionLine = Util.readScreenStringForward(4, 4 + 18*10, Util.F3_DEBUG_TEXT_COLOR);
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

    public static Double cachedPitch = null;
    public static Double cachedYaw = null;

    public static Double getPitch() {
        if (cachedPitch == null) {
            String anglesLine = Util.readScreenStringForward(4, 4 + 18*13, Util.F3_DEBUG_TEXT_COLOR);
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


    public static Double getYaw() {
        if (cachedYaw == null) {
            String anglesLine = Util.readScreenStringForward(4, 4 + 18*13, Util.F3_DEBUG_TEXT_COLOR);
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

    public static void clearCache() {
        cachedPosition = null;
        cachedYaw = null;
        cachedPitch = null;
    }
}
