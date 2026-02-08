package name.quasar.autospeedrun;

import name.quasar.autospeedrun.usercode.AutoSpeedrunUserCode;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class AutoSpeedrunTicker implements ClientTickEvents.EndTick {
    public AutoSpeedrunUserCode userCode;

    public AutoSpeedrunTicker(AutoSpeedrunUserCode userCode) {
        this.userCode = userCode;
    }

    @Override
    public void onEndTick(Minecraft client) {
        if (Minecraft.getInstance().player != null && !Util.togglePaused) {
            userCode.tick();
        }
    }
}
