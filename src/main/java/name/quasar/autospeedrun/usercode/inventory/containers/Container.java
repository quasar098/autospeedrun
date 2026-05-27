package name.quasar.autospeedrun.usercode.inventory.containers;

public abstract class Container {
    // AbstractContainerScreen useful

    public abstract int getImageWidth();
    public abstract int getImageHeight();

    public abstract int getIdTextX();
    public abstract int getIdTextY();
    public abstract String getIdTextStr();

    public abstract int getNumContainerSlots();

    public static int q() {
        return 0;
    }
}
