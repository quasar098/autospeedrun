package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class AutoSpeedrunUserCode {

    int testStartTick = -1;

    public void init() {
        // misc useful
        Util.SCREEN_W = 0;
        Util.SCREEN_H = 0;
        Util.tickCount = 0;
        Util.runStage = RunStage.OVERWORLD;
        testStartTick = -1;

        // other systems
        WorldBlocks.reset();
        MouseInputManager.reset();
        BuriedTreasureOverworld.reset();
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

        // i actually dk what tf is going on with left click
//        if (click) {
//            click = false;
//            com.mojang.blaze3d.platform.InputConstants.Key key = com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(0);
//            KeyMapping.set(key, true);
//            KeyMapping.click(key);
//        }
//        try {
//            Field f1 = Minecraft.getInstance().options.keyAttack.getClass().getDeclaredField("clickCount");
//            Field f2 = Minecraft.getInstance().options.keyAttack.getClass().getDeclaredField("isDown");
//            f1.setAccessible(true);
//            f2.setAccessible(true);
//            AutoSpeedrunApi.chatMessage(f1.get(Minecraft.getInstance().options.keyAttack) + ", "
//                + f2.get(Minecraft.getInstance().options.keyAttack) + Minecraft.getInstance().player.isUsingItem());
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }

        // do stuff based on stage of run
//        if (Util.runStage == RunStage.OVERWORLD) {
//            BuriedTreasureOverworld.getInstance().perform();
//            if (BuriedTreasureOverworld.getInstance().subsection == BuriedTreasureOverworld.Subsection.DONE) {
//                Util.runStage = RunStage.ENTERED_NETHER;
//            }
//        }

        // movement test
        if (testStartTick == -1) {
            testStartTick = Util.tickCount;
        }
        Vector3 pos = F3Information.getPosition();
        Vec3 real = Minecraft.getInstance().player.position();
        System.out.printf("%d %d %f %f %f\n", Util.tickCount, testStartTick, real.x, real.y, real.z);
        if (Util.tickCount < testStartTick + 40) {
            if (Util.tickCount > 10 && Util.tickCount < 40) {
                System.out.println("Crouching");
                MovementInputManager.planPressKeyCrouch();
            }
            MovementInputManager.planPressKeyW();
        }

        // do movement and mouse
        boolean navigatorResult = Navigation.perform();
        MovementInputManager.handle();
        if (navigatorResult) {
            return;
        }
    }

    private boolean click = false;

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
                    System.out.printf("%s\n", bl + " - " + WorldBlocks.knownBlocks.get(bl));
                }
                break;
            case "setnav":
                String[] xyzStr = split[1].split(",");
                Navigation.setGoalPosition(new Vector3(
                    Double.parseDouble(xyzStr[0]), Double.parseDouble(xyzStr[1]), Double.parseDouble(xyzStr[2])
                ));
                Navigation.setAlignment(Navigation.AxisAlignment.PRIORITY_X);
                break;
            case "lclick":
                click = true;
//                Minecraft.getInstance().options.keyAttack.setDown(true);
                break;
            case "rclick":
                AutoSpeedrunApi.mouseActivate(GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_PRESS, 0);
                AutoSpeedrunApi.mouseActivate(GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_RELEASE, 0);
                break;
        }
    }
}
