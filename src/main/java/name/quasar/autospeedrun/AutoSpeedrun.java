package name.quasar.autospeedrun;

import name.quasar.autospeedrun.commands.AutoSpeedrunDebug;
import name.quasar.autospeedrun.usercode.AutoSpeedrunUserCode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import static name.quasar.autospeedrun.Util.*;

public class AutoSpeedrun implements ModInitializer {
	public static AutoSpeedrunUserCode userCode = new AutoSpeedrunUserCode();

	@Override
	public void onInitialize() {
		LOGGER.info("initialized");
		userCode.init();

		CommandRegistrationCallback.EVENT.register(AutoSpeedrunDebug::register);

		ClientTickEvents.END_CLIENT_TICK.register(new AutoSpeedrunTicker(userCode));
	}
}