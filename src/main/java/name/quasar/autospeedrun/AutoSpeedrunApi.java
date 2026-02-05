package name.quasar.autospeedrun;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static name.quasar.autospeedrun.Util.*;

public class AutoSpeedrunApi {
    public static void tapKey(int key) {
        announceAction("Tap key " + key);
        Minecraft client = Minecraft.getInstance();
        // window, key, scancode, action, mods
        long window = client.getWindow().getWindow();
        int scanCode = GLFW.glfwGetKeyScancode(key);
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_PRESS, 0));
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_RELEASE, 0));
    }

    public static void pressKey(int key) {
        announceAction("Press key " + key);
        Minecraft client = Minecraft.getInstance();
        // window, key, scancode, action, mods
        long window = client.getWindow().getWindow();
        int scanCode = GLFW.glfwGetKeyScancode(key);
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_PRESS, 0));
    }

    public static void releaseKey(int key) {
        announceAction("Release key " + key);
        Minecraft client = Minecraft.getInstance();
        // window, key, scancode, action, mods
        long window = client.getWindow().getWindow();
        int scanCode = GLFW.glfwGetKeyScancode(key);
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_RELEASE, 0));
    }

    public static void moveMouse(double x, double y) {
        announceAction("Mouse move " + x + "," + y);
        Minecraft client = Minecraft.getInstance();
        long window = client.getWindow().getWindow();
        // lowk kind of sketchy but whatever
        try {
            Method method = client.mouseHandler.getClass().getDeclaredMethod("onMove", long.class, double.class, double.class);
            method.setAccessible(true);
            method.invoke(client.mouseHandler, window, x, y);
            method.setAccessible(false);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private volatile static NativeImage img = null;

    public static void screenshotAsync(int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (AutoSpeedrunApi.img != null) {
                img.close();
            }
            NativeImage img = Screenshot.takeScreenshot(w, h, mc.getMainRenderTarget());
            AutoSpeedrunApi.img = img;
            announceAction("Screenshot stored in memory");
        });
    }

    public static int getScreenshotPixelRGBA(int x, int y) {
        return img.getPixelRGBA(x, y);
    }

    public static void chatMessage(String str) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                new TextComponent(str).withStyle(ChatFormatting.GREEN), false);
        }
    }

    public static void subtitleMessage(String str) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    new TextComponent(str).withStyle(ChatFormatting.GREEN), true);
        }
    }
}
