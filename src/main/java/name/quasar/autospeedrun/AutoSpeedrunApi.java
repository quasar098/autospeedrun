package name.quasar.autospeedrun;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

import static name.quasar.autospeedrun.Util.*;

public class AutoSpeedrunApi {
    public static void showF3PieTickBlockEntities() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.containerMenu != null) {
            LOGGER.info(mc.player.containerMenu);
            return;  // cannot access f3 when in inventory
        }
        announceAction("Showing F3 Pie Tick BlockEntities");
        mc.options.renderDebug = true;
        mc.options.renderDebugCharts = true;
        mc.options.renderFpsChart = false;
        // lowk kind of sketchy but whatever
        try {
            Field debugPathField = Minecraft.class.getDeclaredField("debugPath");
            debugPathField.setAccessible(true);
            debugPathField.set(mc, debugPathJoin("root", "tick", "level", "entities", "blockEntities"));
            debugPathField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException ignored) { }
    }

    public static void showF3PieGameRendererEntities() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.containerMenu != null) {
            return;  // cannot access f3 when in inventory
        }
        announceAction("Showing F3 Pie GameRenderer Entities");
        mc.options.renderDebug = true;
        mc.options.renderDebugCharts = true;
        mc.options.renderFpsChart = false;
        // lowk kind of sketchy but whatever
        try {
            Field debugPathField = Minecraft.class.getDeclaredField("debugPath");
            debugPathField.setAccessible(true);
            debugPathField.set(mc, debugPathJoin("root", "gameRenderer", "level", "entities"));
            debugPathField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException ignored) { }
    }

    public static void hideF3() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.containerMenu != null) {
            return;  // cannot access f3 when in inventory
        }
        announceAction("Hiding F3");
        mc.options.renderDebug = false;
        mc.options.renderDebugCharts = false;
        mc.options.renderFpsChart = false;
    }

    public static void showF3NoPie() {
        announceAction("Showing F3 No Pie");
        Minecraft mc = Minecraft.getInstance();
        mc.options.renderDebug = true;
        mc.options.renderDebugCharts = false;
        mc.options.renderFpsChart = false;
    }

    public static void selectHotbarSlot(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.containerMenu != null) {
            return;  // cannot select slot when in inventory
        }
    }

    public static void tapKey(int keyCode) {
        announceAction("Tap key " + keyCode);
        Minecraft client = Minecraft.getInstance();
        // window, key, scancode, action, mods
//        client.execute(() -> client.keyboardHandler.keyPress(client.getWindow().getWindow(), jkey));
    }
}
