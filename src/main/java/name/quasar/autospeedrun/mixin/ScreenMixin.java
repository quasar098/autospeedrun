package name.quasar.autospeedrun.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Screen.class)
public class ScreenMixin {

	@Overwrite
	public static boolean hasShiftDown() {
		// not entirely accurate because of right shift but do ppl use right shift at all??
		if (!Util.togglePaused) { return AutoSpeedrunAPI.shifting; }
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
				|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
	}
}