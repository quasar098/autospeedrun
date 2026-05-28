package name.quasar.autospeedrun.usercode.inventory.containers;

import name.quasar.autospeedrun.usercode.inventory.ContainerItem;

public class CraftingContainer extends Container {
    // CraftingScreen,CraftingMenu useful
    // inaccurate naming because crafting is not a container but idc

    public CraftingContainer() {
        containerSlotItems = new ContainerItem[9];
    }

    public int getImageWidth() {
        return 176;
    }

    public int getImageHeight() {
        return 166;
    }

    public int getIdTextX() {
        return 29;
    }

    public int getIdTextY() {
        return 6;
    }

    public String getIdTextStr() {
        return "Crafting";
    }

    public int getNumContainerSlots() {
        return 9;
    }

    public int getContainerSlotX(int slot) {
        return 30 + (slot % 3) * Container.SLOT_PX;
    }

    public int getContainerSlotY(int slot) {
        return 17 + (slot / 3) * Container.SLOT_PX;
    }

    public int getInventorySlotX(int slot) {
        return 8 + (slot % 9) * Container.SLOT_PX;
    }

    public int getInventorySlotY(int slot) {
        return slot < 9 ? 142 : 66 + (slot / 9) * Container.SLOT_PX;
    }
}
