package name.quasar.autospeedrun.usercode.inventory.containers;

public class Chest3Container extends Container {
    // ContainerScreen useful

    public int getImageWidth() {
        return 176;
    }

    public int getImageHeight() {
        return 114 + 3 * 18;
    }

    public int getIdTextX() {
        return 8;
    }

    public int getIdTextY() {
        return 6;
    }

    public String getIdTextStr() {
        return "Chest";
    }

    public int getNumContainerSlots() {
        return 3 * 9;
    }
}
