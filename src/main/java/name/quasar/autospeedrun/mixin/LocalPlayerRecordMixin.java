package name.quasar.autospeedrun.mixin;

import name.quasar.autospeedrun.Util;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerRecordMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tickHead(CallbackInfo ci) {
        if (Util.recordedScenario != null) {
            Util.recordedScenario.setCurrentlyDoingLocalPlayerTick(true);
            Util.recordedScenario.recordTick();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickTail(CallbackInfo ci) {
        if (Util.recordedScenario != null) {
            Util.recordedScenario.setCurrentlyDoingLocalPlayerTick(false);
        }
    }
}
