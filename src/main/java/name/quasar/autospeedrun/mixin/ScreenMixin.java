package name.quasar.autospeedrun.mixin;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Screen.class)
public class ScreenMixin {

	@Overwrite
	public static boolean hasShiftDown() {
		// not entirely accurate because of right shift but do ppl use right shift at all??
		return AutoSpeedrunApi.shifting;
	}
}