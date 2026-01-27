package name.quasar.autospeedrun;

import name.quasar.autospeedrun.commands.AutoSpeedrunDebug;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import static name.quasar.autospeedrun.Util.*;

public class AutoSpeedrun implements ModInitializer {
	@Override
	public void onInitialize() {
		LOGGER.info("initialized");

		CommandRegistrationCallback.EVENT.register(AutoSpeedrunDebug::register);
	}
}