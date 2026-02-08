package name.quasar.autospeedrun;

import com.mojang.blaze3d.platform.InputConstants;
import name.quasar.autospeedrun.commands.AutoSpeedrunDebug;
import name.quasar.autospeedrun.usercode.AutoSpeedrunUserCode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;

import static name.quasar.autospeedrun.Util.*;

public class AutoSpeedrun implements ModInitializer {
	public static AutoSpeedrunUserCode userCode = new AutoSpeedrunUserCode();

	@Override
	public void onInitialize() {
		LOGGER.info("initialized");

		CommandRegistrationCallback.EVENT.register(AutoSpeedrunDebug::register);

		ClientTickEvents.END_CLIENT_TICK.register(new AutoSpeedrunTicker(userCode));

		ServerWorldEvents.LOAD.register((minecraftServer, serverWorld) -> {
			userCode.init();
		});

		KeyMapping toggleRun = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.autospeedrun.toggle",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_EQUAL,
		"category.autospeedrun"
		));

		AtomicBoolean prevWasToggleKeyDown = new AtomicBoolean(false);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (toggleRun.isDown() && !prevWasToggleKeyDown.get()) {
				if (client.player != null) {
					Util.togglePaused = !togglePaused;
					client.player.displayClientMessage(new TextComponent("Toggle paused/unpaused"), false);
				}
			}
			prevWasToggleKeyDown.set(toggleRun.isDown());
		});
	}
}