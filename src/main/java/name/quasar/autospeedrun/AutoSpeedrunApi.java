package name.quasar.autospeedrun;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
            AutoSpeedrunApi.img = Screenshot.takeScreenshot(w, h, mc.getMainRenderTarget());
//            try {
//                img.writeToFile(new File("bruh.png"));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        });
    }

    public static int getScreenshotPixelRGBA(int x, int y) {
        if (img == null) {
            return 0;
        }
        return img.getPixelRGBA(x, y);
    }

    public static int getScreenshotHeight() {
        if (img == null) {
            return 0;
        }
        return img.getHeight();
    }

    public static int getScreenshotWidth() {
        if (img == null) {
            return 0;
        }
        return img.getWidth();
    }

    public static String getClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            System.err.println("Error reading clipboard: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Clipboard unavailable: " + e.getMessage());
        }
        return null;
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
