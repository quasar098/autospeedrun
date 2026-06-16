package name.quasar.autospeedrun.usercode.inventory;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.usercode.inventory.containers.Container;
import org.lwjgl.glfw.GLFW;

public class InventoryManagement {
    private static InventoryManagement instance;

    public static InventoryManagement getInstance() {
        if (instance == null) {
            instance = new InventoryManagement();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public InventoryManagement() {

    }

    public int testTime = -1;
    private Container container = null;

    public boolean perform() {
        if (testTime == -1) {
            return false;
        }
        testTime++;
        if (testTime == 1) {
            AutoSpeedrunAPI.tapRightClick();
        } else if (2 <= testTime) {
            if (testTime <= 5) {
                container = ContainerRecognition.getInstance().pullFromScreen();
                if (container == null) {
                    AutoSpeedrunAPI.chatMessage("no valid container recognized");
                    if (testTime == 3) {
                        AutoSpeedrunAPI.emergencyStopUserCode();
                        testTime = -1;
                        container = null;
                    }
                    return true;
                }
                testTime = 5;
            }
            if (!lootChest() || testTime > 40) {
                AutoSpeedrunAPI.tapKey(GLFW.GLFW_KEY_ESCAPE);
                testTime = -1;
                container = null;
                return false;
            }
        }
        return true;
    }

    private boolean isValuedItem(ContainerItem item) {
        if (ContainerItem.isEmpty(item)) {
            return false;
        }
        // todo do blocks
        switch (item.getName()) {
            case "diamond":
            case "iron_ingot":
            case "gold_ingot":
            case "cooked_cod":
            case "salmon":
            case "carrot":
            case "golden_carrot":
                return true;
            default:
                return false;
        }
    }

    private boolean lootChest() {
        AutoSpeedrunAPI.chatMessage("loot chest");
        for (int slot = 0; slot < container.getNumContainerSlots(); slot++) {
            ContainerItem item = container.getItem(slot);
            if (isValuedItem(item)) {
                AutoSpeedrunAPI.pressKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
                int x = container.getContainerSlotScreenX(slot);
                int y = container.getContainerSlotScreenY(slot);
                AutoSpeedrunAPI.screenClick(x, y, 0);
                AutoSpeedrunAPI.releaseKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
                container.setItem(null, slot);
                return true;
            }
        }
        return false;
    }
}
