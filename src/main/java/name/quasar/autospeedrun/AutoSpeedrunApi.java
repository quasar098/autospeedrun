package name.quasar.autospeedrun;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static name.quasar.autospeedrun.Util.*;

public class AutoSpeedrunApi {

    private static final ArrayList<DebugRenderLine> lines = new ArrayList<>();

    public static ArrayList<DebugRenderLine> getRenderLines() {
        return lines;
    }

    public static void renderLine(DebugRenderLine line) {
        lines.add(line);
    }

    public static void clearRenderLines() {
        lines.clear();
    }

    static boolean tappingLeftClick = false;
    static boolean holdingLeftClick = false;

    public static void tapLeftClick() {
        tappingLeftClick = true;
    }

    public static void pressLeftClick() {
        tappingLeftClick = true;
        holdingLeftClick = true;
    }

    public static void releaseLeftClick() {
        tappingLeftClick = false;
        holdingLeftClick = false;
    }

    public static void tapRightClick() {
        KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT), true);
        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
        KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT), false);
        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
    }

    public static void pressRightClick() {
        KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT), true);
        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
    }

    public static void releaseRightClick() {
        KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT), false);
        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
    }

    public static boolean screenClickedThisTick = false;

    public static void screenClick(int x, int y, int button) {
        if (Minecraft.getInstance() != null) {
            if (Minecraft.getInstance().screen != null) {
                if (!screenClickedThisTick) {
                    screenClickedThisTick = true;
                    int scale = Minecraft.getInstance().options.guiScale;
                    Minecraft.getInstance().screen.mouseClicked((double) x / scale, (double) y / scale, button);
                    Minecraft.getInstance().screen.mouseReleased((double) x / scale, (double) y / scale, button);
                    // todo fix 0 scale
                    long handle = Minecraft.getInstance().getWindow().getWindow();
                    GLFW.glfwSetCursorPos(handle, x, y);
                }
            }
        }
    }

    public static boolean shifting = false;
    public static boolean leftShifting = false;
    public static boolean rightShifting = false;

    private static Set<Integer> keyOverrides = new HashSet<>();

    private static void addKeyOverride(int key) {
        keyOverrides.add(key);
    }

    private static void removeKeyOverride(int key) {
        keyOverrides.remove(key);
    }

    public static boolean keyOverridden(int key) {
        return keyOverrides.contains(key);
    }

    public static void tapKey(int key) {
        announceAction("Tap key " + keyNameFromConstant(key));
        Minecraft client = Minecraft.getInstance();
        // window, key, scancode, action, mods
        long window = client.getWindow().getWindow();
        int scanCode = GLFW.glfwGetKeyScancode(key);
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_PRESS, 0));
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_RELEASE, 0));
    }

    public static void pressKey(int key) {
        announceAction("Press key " + keyNameFromConstant(key));
        Minecraft client = Minecraft.getInstance();
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) {
            leftShifting = true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            rightShifting = true;
        }
        shifting = leftShifting || rightShifting;
        // window, key, scancode, action, mods
        long window = client.getWindow().getWindow();
        int scanCode = GLFW.glfwGetKeyScancode(key);
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_PRESS, 0));
        addKeyOverride(key);
    }

    public static void releaseKey(int key) {
        announceAction("Release key " + keyNameFromConstant(key));
        Minecraft client = Minecraft.getInstance();
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) {
            leftShifting = false;
        }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            rightShifting = false;
        }
        shifting = leftShifting || rightShifting;
        // window, key, scancode, action, mods
        long window = client.getWindow().getWindow();
        int scanCode = GLFW.glfwGetKeyScancode(key);
        client.execute(() -> client.keyboardHandler.keyPress(window, key, scanCode, GLFW.GLFW_RELEASE, 0));
        removeKeyOverride(key);
    }

    /**
    set the mouse position
     */
    public static void mouseMove(int x, int y) {
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

    // really its 0xAABBGGRR
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

    public static void emergencyStopUserCode() {
        Util.togglePaused = true;
    }
}
