package name.quasar.autospeedrun;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
    public static final String MOD_ID = "autospeedrun";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static String debugPathJoin(String... path) { return String.join("\u001e", path); }

    private static boolean announcerEnabled = true;
    public static void enableAnnouncer() { announcerEnabled = true; }
    public static void disableAnnouncer() { announcerEnabled = false; }

    public static void announceAction(String action) {
        LOGGER.info("announcement: {}", action);
        if (!announcerEnabled) {
            return;
        }
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    new TextComponent(action).withStyle(ChatFormatting.AQUA), false);
        }
    }
}
