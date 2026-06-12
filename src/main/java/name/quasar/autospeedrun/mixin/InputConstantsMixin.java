package name.quasar.autospeedrun.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static name.quasar.autospeedrun.AutoSpeedrunAPI.keyOverridden;

@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
    @Overwrite
    public static boolean isKeyDown(long l, int i) {
        return GLFW.glfwGetKey(l, i) == 1 || keyOverridden(i);
    }
}
