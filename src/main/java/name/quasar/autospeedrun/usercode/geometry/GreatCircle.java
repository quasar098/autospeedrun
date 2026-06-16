package name.quasar.autospeedrun.usercode.geometry;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.DebugRenderLine;

/**
 * likely not being used but remains just in case
 */
public class GreatCircle {
    private final Vector3 orthog1;
    private final Vector3 orthog2;
    private final Vector3 normal;

    /**
     * normalVec is the normal vector to the great circle orthogonal vectors. must be normalized.
     */
    public GreatCircle(Vector3 normalVec) {
        this.orthog1 = normalVec.anyOrthogonalVector();
        Vector3 orthog2 = normalVec.cross(orthog1);
        this.normal = orthog1.cross(orthog2);
        if (normal.dot(normalVec) < 0) {
            this.orthog2 = orthog2.mult(-1);
        } else {
            this.orthog2 = orthog2;
        }
    }

    public Vector3 getOrthog1() {
        return orthog1;
    }

    public Vector3 getOrthog2() {
        return orthog2;
    }

    public void debugDraw(Vector3 center) {
        double resolution = 16;
        double increment = Math.PI * 2 / resolution;
        // maybe change to reuse end as next start instead of recomputing for 2x speedup
        for (double t = 0; t < Math.PI * 2; t += increment) {
            Vector3 start = orthog1.mult(Math.sin(t)).add(orthog2.mult(Math.cos(t)));
            Vector3 end = orthog1.mult(Math.sin(t+increment)).add(orthog2.mult(Math.cos(t+increment)));
            AutoSpeedrunAPI.renderLine(new DebugRenderLine(
                start.add(center).toVector3f(), end.add(center).toVector3f(), 0.2f, 0.9f, 1.0f
            ));
        }
    }
}
