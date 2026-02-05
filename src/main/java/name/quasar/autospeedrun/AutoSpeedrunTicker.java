package name.quasar.autospeedrun;

import name.quasar.autospeedrun.usercode.SpeedrunUserCode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class AutoSpeedrunTicker implements ClientTickEvents.EndTick {
    SpeedrunUserCode userCode;

    public AutoSpeedrunTicker() {
        userCode = new SpeedrunUserCode();
        userCode.init();
    }

    @Override
    public void onEndTick(Minecraft client) {
        if (Minecraft.getInstance().player != null) {
            userCode.tick();
        }
    }
}
