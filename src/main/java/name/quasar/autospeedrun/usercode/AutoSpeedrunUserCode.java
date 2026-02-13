package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class AutoSpeedrunUserCode {
    public void init() {
        // misc useful
        Util.SCREEN_W = 0;
        Util.SCREEN_H = 0;
        Util.tickCount = 0;

        // other systems
        WorldBlocks.reset();
        MouseInputManager.reset();
    }

    public void tick() {
        Util.tickCount++;
        AutoSpeedrunApi.screenshotAsync(1920, 1080);
        // screen resolution not yet resolved, resolve it before doing anything else
        if (Util.SCREEN_W == 0 || Util.SCREEN_H == 0) {
            Util.SCREEN_W = AutoSpeedrunApi.getScreenshotWidth();
            Util.SCREEN_H = AutoSpeedrunApi.getScreenshotHeight();
            if (Util.SCREEN_W == 0 || Util.SCREEN_H == 0) {
                return;
            }
            AutoSpeedrunApi.chatMessage(String.format("Screenshots W/H Resolved: %dx%d", Util.SCREEN_W, Util.SCREEN_H));
        }
        // f3 must open always
        if (!F3Information.isF3Open()) {
            AutoSpeedrunApi.tapKey(GLFW.GLFW_KEY_F3);
            return;
        }
        F3Information.clearCache();
        // live debug information
        BlockLocation targettedBL = F3Information.getTargettedBlockPosition();
        String targettedBlockPositionFormatted = targettedBL == null ? "(not targetting)" : targettedBL.toString();
        AutoSpeedrunApi.subtitleMessage(String.format(
            "%.2fs Y:%.1f,P:%.1f %s %s", Util.tickCount / 20.0,
            F3Information.getYaw(), F3Information.getPitch(),
            targettedBlockPositionFormatted, F3Information.getTargettedBlockName()
        ));
        // do mouse calibration on world join
        if (MouseInputManager.calibrateMouse()) {
            return;
        }
        // collect facing block information
        if (F3Information.getTargettedBlockPosition() != null) {
            WorldBlocks.knownBlocks.put(F3Information.getTargettedBlockPosition(), new Block(
                F3Information.getTargettedBlockName()
            ));
        }
        // do movement
        boolean navigatorResult = Navigation.perform();
        MovementInputManager.handle();
        if (navigatorResult) {
            return;
        }
    }

    public void debug(String debugStr) {
        String[] split = debugStr.split(" ");
        switch (split[0]) {
            case "dimension":
                AutoSpeedrunApi.chatMessage("Dimension: " + F3Information.getDimension());
                break;
            case "clearcache":
                F3Information.clearCache();
                AutoSpeedrunApi.chatMessage("Cache cleared");
                break;
            case "dumpblocks":
                for (BlockLocation bl : WorldBlocks.knownBlocks.keySet()) {
                    name.quasar.autospeedrun.Util.LOGGER.info(bl + " - " + WorldBlocks.knownBlocks.get(bl));
                }
                break;
            case "setnav":
                String[] xyzStr = split[1].split(",");
                Navigation.setGoalPosition(new Vector3(
                    Double.parseDouble(xyzStr[0]), Double.parseDouble(xyzStr[1]), Double.parseDouble(xyzStr[2])
                ));
                Navigation.setAlignment(Navigation.AxisAlignment.PRIORITY_X);
                break;
        }
    }
}
