package name.quasar.autospeedrun.usercode.inventory.containers;

import name.quasar.autospeedrun.usercode.inventory.ContainerItem;

public class Chest6Container extends Container {
    // ContainerScreen,ChestMenu useful

    public Chest6Container() {
        containerSlotItems = new ContainerItem[54];
    }

    public int getImageWidth() {
        return 176;
    }

    public int getImageHeight() {
        return 114 + 6 * Container.SLOT_PX;
    }

    public int getIdTextX() {
        return 8;
    }

    public int getIdTextY() {
        return 6;
    }

    public String getIdTextStr() {
        return "Large Chest";
    }

    public int getNumContainerSlots() {
        return 6 * 9;
    }

    public int getContainerSlotX(int slot) {
        return 8 + (slot % 9) * Container.SLOT_PX;
    }

    public int getContainerSlotY(int slot) {
        return 18 + (slot / 9) * Container.SLOT_PX;
    }

    public int getInventorySlotX(int slot) {
        return 8 + (slot % 9) * Container.SLOT_PX;
    }

    public int getInventorySlotY(int slot) {
        int k = (6 - 4) * Container.SLOT_PX;
        return (slot < 9 ? 161 : 85 + (slot / 9) * Container.SLOT_PX) + k;
    }
}
