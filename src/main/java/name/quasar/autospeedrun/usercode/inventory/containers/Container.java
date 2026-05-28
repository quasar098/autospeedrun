package name.quasar.autospeedrun.usercode.inventory.containers;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.inventory.ContainerItem;

public abstract class Container {

    public final static int SLOT_PX = 18;

    // AbstractContainerScreen useful

    public abstract int getImageWidth();
    public abstract int getImageHeight();

    public int getImageScreenX() {
        return (AutoSpeedrunApi.getScreenshotWidth() - getImageWidth() * Util.OPTIONS_TXT_GUI_SCALE) / 2;
    }

    public int getImageScreenY() {
        return (AutoSpeedrunApi.getScreenshotHeight() - getImageHeight() * Util.OPTIONS_TXT_GUI_SCALE) / 2;
    }

    // identification purposes
    public abstract int getIdTextX();
    public abstract int getIdTextY();
    public abstract String getIdTextStr();

    public int getIdTextScreenX() {
        return getImageScreenX() + getIdTextX() * Util.OPTIONS_TXT_GUI_SCALE;
    }

    public int getIdTextScreenY() {
        return getImageScreenY() + getIdTextY() * Util.OPTIONS_TXT_GUI_SCALE;
    }

    public abstract int getNumContainerSlots();
    public abstract int getContainerSlotX(int slot);
    public abstract int getContainerSlotY(int slot);

    public int getContainerSlotScreenX(int slot) {
        return getImageScreenX() + getContainerSlotX(slot) * Util.OPTIONS_TXT_GUI_SCALE;
    }

    public int getContainerSlotScreenY(int slot) {
        return getImageScreenY() + getContainerSlotY(slot) * Util.OPTIONS_TXT_GUI_SCALE;
    }

    protected ContainerItem[] containerSlotItems;

    public void setItem(ContainerItem item, int slot) {
        containerSlotItems[slot] = item;
    }

    public ContainerItem getItem(int slot) {
        return containerSlotItems[slot];
    }

    public abstract int getInventorySlotX(int slot);
    public abstract int getInventorySlotY(int slot);

    public int getInventorySlotScreenX(int slot) {
        return getImageScreenX() + getInventorySlotX(slot) * Util.OPTIONS_TXT_GUI_SCALE;
    }

    public int getInventorySlotScreenY(int slot) {
        return getImageScreenY() + getInventorySlotY(slot) * Util.OPTIONS_TXT_GUI_SCALE;
    }

    public boolean isRecognized() {
        String recognized = Util.readScreenStringForward(
            getIdTextScreenX(), getIdTextScreenY(), c -> c == 0x3f3f3f, Util.OPTIONS_TXT_GUI_SCALE
        );
//        AutoSpeedrunApi.chatMessage("recognized:" + recognized + "," + getIdTextScreenX() + "," + getIdTextScreenY());
        return recognized.equals(getIdTextStr());
    }
}
