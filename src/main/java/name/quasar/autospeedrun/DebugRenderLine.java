package name.quasar.autospeedrun;

import com.mojang.math.Vector3f;

public class DebugRenderLine {
    public Vector3f getPa() {
        return pa;
    }

    public Vector3f getPb() {
        return pb;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    private Vector3f pa;
    private Vector3f pb;
    private float r;
    private float g;
    private float b;

    /**
    r,g,b are [0.0f, 1.0f]
    */
    public DebugRenderLine(Vector3f pa, Vector3f pb, float r, float g, float b) {
        this.pa = pa;
        this.pb = pb;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
