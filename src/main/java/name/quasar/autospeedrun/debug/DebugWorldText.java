package name.quasar.autospeedrun.debug;

import com.mojang.math.Vector3f;

public class DebugWorldText {
    public Vector3f getPos() {
        return pos;
    }

    public String getText() {
        return text;
    }

    public int getColor() {
        return color;
    }

    private final Vector3f pos;
    private final String text;
    private final int color;

    /**
     r,g,b are [0.0f, 1.0f]
     */
    public DebugWorldText(Vector3f pos, String text, int color) {
        this.pos = pos;
        this.text = text;
        this.color = color;
    }
}
