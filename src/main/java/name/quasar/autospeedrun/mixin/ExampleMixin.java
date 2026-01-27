package name.quasar.autospeedrun.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static name.quasar.autospeedrun.Util.*;

@Mixin(MinecraftServer.class)
public class ExampleMixin {

	@Inject(at = @At("HEAD"), method = "loadLevel")
	private void init(CallbackInfo info) {
		LOGGER.info("Loaded level");
	}
}