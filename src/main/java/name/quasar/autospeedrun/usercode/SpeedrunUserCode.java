package name.quasar.autospeedrun.usercode;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

public class SpeedrunUserCode {
    int tickCount;

    public void init() {
        tickCount = 0;
    }

    public void tick() {
        tickCount++;
        Minecraft.getInstance().player.displayClientMessage(
                new TextComponent("" + tickCount).withStyle(ChatFormatting.AQUA), true);
//        AutoSpeedrunApi.moveMouse((tickCount * 100) % 1000, 0);
    }
}
