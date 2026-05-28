package name.quasar.autospeedrun.usercode.inventory.containers;

import name.quasar.autospeedrun.usercode.inventory.ContainerItem;

public class InventoryContainer extends Container {
    // InventoryScreen,InventoryMenu useful
    // inaccurate naming because inventory is not a container but idc

    public InventoryContainer() {
        containerSlotItems = new ContainerItem[9];
    }

    public int getImageWidth() {
        return 176;
    }

    public int getImageHeight() {
        return 166;
    }

    public int getIdTextX() {
        return 97;
    }

    public int getIdTextY() {
        return 6;
    }

    public String getIdTextStr() {
        return "Chest";
    }

    // there are no container slots in the inventory
    public int getNumContainerSlots() {
        return 0;
    }
    public int getContainerSlotX(int slot) { return -1; }
    public int getContainerSlotY(int slot) { return -1; }

    public int getInventorySlotX(int slot) {
        return 8 + (slot % 9) * Container.SLOT_PX;
    }

    public int getInventorySlotY(int slot) {
        return slot < 9 ? 142 : 66 + (slot / 9) * Container.SLOT_PX;
    }
}
