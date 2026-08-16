package name.quasar.autospeedrun.usercode.simulation;

public class SimulationInput {
    private final FakeKBMInputs options;

    public boolean up = false;
    public boolean down = false;
    public boolean left = false;
    public boolean right = false;
    public boolean jumping = false;
    public boolean shiftKeyDown = false;
    public float leftImpulse = 0.0f;
    public float forwardImpulse = 0.0f;

    public SimulationInput(FakeKBMInputs options) {
        this.options = options;
    }

    public void tick(boolean crawlingOrCrouching) {
        assert options != null;
        this.up = this.options.keyUp;
        this.down = this.options.keyDown;
        this.left = this.options.keyLeft;
        this.right = this.options.keyRight;
        this.forwardImpulse = this.up == this.down ? 0.0F : (this.up ? 1.0F : -1.0F);
        this.leftImpulse = this.left == this.right ? 0.0F : (this.left ? 1.0F : -1.0F);
        this.jumping = this.options.keyJump;
        this.shiftKeyDown = this.options.keyShift;
        if (crawlingOrCrouching) {
            this.leftImpulse = (float)(this.leftImpulse * 0.3);
            this.forwardImpulse = (float)(this.forwardImpulse * 0.3);
        }
    }

    public FakeKBMInputs getOptions() {
        return options;
    }

    public boolean hasForwardImpulse() {
        return this.forwardImpulse > 1.0E-5F;
    }
}
