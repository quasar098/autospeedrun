package name.quasar.autospeedrun;

import name.quasar.autospeedrun.usercode.AutoSpeedrunUserCode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AutoSpeedrunStartTick implements ClientTickEvents.StartTick {
    public AutoSpeedrunUserCode userCode;

    public AutoSpeedrunStartTick(AutoSpeedrunUserCode userCode) {
        this.userCode = userCode;
    }

    @Override
    public void onStartTick(Minecraft client) {
        if (Util.isDebuggingMousePosition) {
            long handle = Minecraft.getInstance().getWindow().getWindow();
            double[] xBuffer = new double[1];
            double[] yBuffer = new double[1];
            GLFW.glfwGetCursorPos(handle, xBuffer, yBuffer);
            double x = xBuffer[0];
            double y = yBuffer[0];
            AutoSpeedrunAPI.chatMessage(x + "," + y);
        }
        if (Minecraft.getInstance().player != null && !Util.togglePaused) {
            if (Minecraft.getInstance().screen == null || !Minecraft.getInstance().screen.getTitle().getString().equals("Game paused")) {
                userCode.tick();
            }
        }
        AutoSpeedrunAPI.screenClickedThisTick = false;
    }
}
