package name.quasar.autospeedrun.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.DebugRenderLine;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class DebugRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"))
    public void renderLevel(
            PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci
    ) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        poseStack.pushPose();

        Vec3 cameraPos = camera.getPosition();

        poseStack.translate(
                -cameraPos.x,
                -cameraPos.y,
                -cameraPos.z
        );

        RenderSystem.disableTexture();
        RenderSystem.disableLighting();
        RenderSystem.lineWidth(1.5f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(1, DefaultVertexFormat.POSITION_COLOR);
        for (DebugRenderLine drl : AutoSpeedrunAPI.getRenderLines()) {
            buffer.vertex(poseStack.last().pose(), drl.getPa().x(), drl.getPa().y(), drl.getPa().z())
                    .color(drl.getR(), drl.getG(), drl.getB(), 1f).endVertex();
            buffer.vertex(poseStack.last().pose(), drl.getPb().x(), drl.getPb().y(), drl.getPb().z())
                    .color(drl.getR(), drl.getG(), drl.getB(), 1f).endVertex();
        }

        tesselator.end();

        RenderSystem.enableTexture();
        RenderSystem.enableLighting();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}
