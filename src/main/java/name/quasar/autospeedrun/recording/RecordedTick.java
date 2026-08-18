package name.quasar.autospeedrun.recording;

import net.minecraft.client.Minecraft;

import java.io.Serializable;

public class RecordedTick implements Serializable {
    private static final long serialVersionUID = 1L;

    public double playerX;
    public double playerY;
    public double playerZ;

    public float playerYaw;
    public float playerPitch;

    public boolean keyLeft;
    public boolean keyRight;
    public boolean keyUp;
    public boolean keyDown;
    public boolean keyJump;
    public boolean keyShift;
    public boolean keySprint;

    public RecordedTick(Minecraft mc) {
        if (mc.player == null) {
            throw new UnsupportedOperationException("no player bad");
        }

        playerX = mc.player.getX();
        playerY = mc.player.getY();
        playerZ = mc.player.getZ();

        playerPitch = mc.player.xRot;
        playerYaw = mc.player.yRot;

        keyLeft = mc.player.input.right;
        keyRight = mc.player.input.right;
        keyUp = mc.player.input.right;
        keyDown = mc.player.input.right;
        keyJump = mc.player.input.right;
        keyShift = mc.player.input.shiftKeyDown;
        keySprint = mc.options.keySprint.isDown();
    }
}
