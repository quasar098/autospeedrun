package name.quasar.autospeedrun.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.debug.DebugWorldLine;
import name.quasar.autospeedrun.debug.DebugWorldText;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
        for (DebugWorldLine drl : AutoSpeedrunAPI.getWorldLines()) {
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

        poseStack.pushPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableLighting();

        Font font = Minecraft.getInstance().font;

        poseStack.translate(
            -cameraPos.x,
            -cameraPos.y,
            -cameraPos.z
        );
        for (DebugWorldText dwt : AutoSpeedrunAPI.getWorldTexts()) {
            poseStack.pushPose();
            poseStack.translate(dwt.getPos().x(), dwt.getPos().y(), dwt.getPos().z());
            poseStack.scale(0.02f, 0.02f, 0.02f);
            poseStack.mulPose(camera.rotation());
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
            poseStack.translate(-font.width(dwt.getText())/2.0, 0, 0);
            font.draw(poseStack, dwt.getText(), 0, 0, dwt.getColor());
            poseStack.popPose();
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableLighting();

        poseStack.popPose();
    }
}
