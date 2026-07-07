package name.quasar.autospeedrun.usercode.simulation;

public class FakeKBMInputs {
    public boolean isKeyUp() {
        return keyUp;
    }

    public FakeKBMInputs setKeyUp(boolean keyUp) {
        this.keyUp = keyUp;
        return this;
    }

    public boolean isKeyDown() {
        return keyDown;
    }

    public FakeKBMInputs setKeyDown(boolean keyDown) {
        this.keyDown = keyDown;
        return this;
    }

    public boolean isKeyLeft() {
        return keyLeft;
    }

    public FakeKBMInputs setKeyLeft(boolean keyLeft) {
        this.keyLeft = keyLeft;
        return this;
    }

    public boolean isKeyRight() {
        return keyRight;
    }

    public FakeKBMInputs setKeyRight(boolean keyRight) {
        this.keyRight = keyRight;
        return this;
    }

    public boolean isKeyJump() {
        return keyJump;
    }

    public FakeKBMInputs setKeyJump(boolean keyJump) {
        this.keyJump = keyJump;
        return this;
    }

    public boolean isKeyShift() {
        return keyShift;
    }

    public FakeKBMInputs setKeyShift(boolean keyShift) {
        this.keyShift = keyShift;
        return this;
    }

    public boolean isKeySprint() {
        return keySprint;
    }

    public FakeKBMInputs setKeySprint(boolean keySprint) {
        this.keySprint = keySprint;
        return this;
    }

    protected boolean keyUp = false;
    protected boolean keyDown = false;
    protected boolean keyLeft = false;
    protected boolean keyRight = false;
    protected boolean keyJump = false;
    protected boolean keyShift = false;
    protected boolean keySprint = false;
}
