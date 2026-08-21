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

        keyLeft = mc.player.input.left;
        keyRight = mc.player.input.right;
        keyUp = mc.player.input.up;
        keyDown = mc.player.input.down;
        keyJump = mc.player.input.jumping;
        keyShift = mc.player.input.shiftKeyDown;
        keySprint = mc.options.keySprint.isDown();
    }

    public String toString() {
        return String.format(
            "RT<pos=(%+.5f, %+.5f, %+.5f), yaw=%+.5f, pitch=%+.5f, w=%s, a=%s, s=%s, d=%s, space=%s, shift=%s, sprint=%s>",
            playerX, playerY, playerZ, playerYaw, playerPitch,
            keyUp ? 'T' : 'F', keyLeft ? 'T' : 'F', keyDown ? 'T' : 'F', keyRight ? 'T' : 'F',
            keyJump ? 'T' : 'F', keyShift ? 'T' : 'F', keySprint ? 'T' : 'F'
        );
    }
}
