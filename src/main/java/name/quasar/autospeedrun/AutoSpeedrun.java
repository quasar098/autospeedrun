package name.quasar.autospeedrun;

import com.mojang.blaze3d.platform.InputConstants;
import name.quasar.autospeedrun.commands.AutoSpeedrunDebug;
import name.quasar.autospeedrun.recording.RecordedScenario;
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

		ClientTickEvents.START_CLIENT_TICK.register(new AutoSpeedrunStartTick(userCode));
		ClientTickEvents.END_CLIENT_TICK.register(new AutoSpeedrunEndTick());

		ServerWorldEvents.LOAD.register((minecraftServer, serverWorld) -> {
			userCode.init();
		});

		KeyMapping toggleBot = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.autospeedrun.togglebot",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_EQUAL,
			"category.autospeedrun"
		));

		KeyMapping toggleRecord = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.autospeedrun.togglerecord",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F12,
			"category.autospeedrun"
		));

		// can these be moved into ...EndTick and ...StartTick classes? todo try that
		AtomicBoolean prevWasToggleBotKeyDown = new AtomicBoolean(false);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (toggleBot.isDown() && !prevWasToggleBotKeyDown.get()) {
				if (client.player != null) {
					Util.togglePaused = !Util.togglePaused;
					if (Util.togglePaused) {  // stupid hack
						AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_W);
						AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_A);
						AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_S);
						AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_D);
					}
					client.player.displayClientMessage(new TextComponent("Toggle bot paused/unpaused"), false);
				}
			}
			prevWasToggleBotKeyDown.set(toggleBot.isDown());
		});

		AtomicBoolean prevWasToggleRecordKeyDown = new AtomicBoolean(false);
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (toggleRecord.isDown() && !prevWasToggleRecordKeyDown.get()) {
				if (client.player != null) {
					if (recordedScenario == null) {
						recordedScenario = new RecordedScenario();
						client.player.displayClientMessage(new TextComponent("Started recording"), false);
					} else {
						recordedScenario.store();
						recordedScenario = null;
						client.player.displayClientMessage(new TextComponent("Stopped recording and saved scenario"), false);
					}
				}
			}
			prevWasToggleRecordKeyDown.set(toggleRecord.isDown());
		});

	}
}