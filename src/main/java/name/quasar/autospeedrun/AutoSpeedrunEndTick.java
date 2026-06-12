package name.quasar.autospeedrun;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AutoSpeedrunEndTick implements ClientTickEvents.EndTick {
    private boolean prevHoldingLeftClick = false;

    @Override
    public void onEndTick(Minecraft client) {
        if (Minecraft.getInstance().player != null && !Util.togglePaused) {
            if (Minecraft.getInstance().screen == null || !Minecraft.getInstance().screen.getTitle().getString().equals("Game paused")) {
                if (AutoSpeedrunAPI.tappingLeftClick) {
                    KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_1));
                    AutoSpeedrunAPI.tappingLeftClick = false;
                }
                if (AutoSpeedrunAPI.holdingLeftClick && !prevHoldingLeftClick) {
                    Minecraft.getInstance().options.keyAttack.setDown(true);
                }
                if (!AutoSpeedrunAPI.holdingLeftClick && prevHoldingLeftClick) {
                    Minecraft.getInstance().options.keyAttack.setDown(false);
                }
            }
        }
        prevHoldingLeftClick = AutoSpeedrunAPI.holdingLeftClick;
    }
}
