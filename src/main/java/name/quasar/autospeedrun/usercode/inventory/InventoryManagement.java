package name.quasar.autospeedrun.usercode.inventory;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.usercode.inventory.containers.Chest3Container;
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
            AutoSpeedrunApi.tapRightClick();
        } else if (2 <= testTime) {
            if (testTime <= 5) {
                container = ContainerRecognition.getInstance().pullFromScreen();
                if (container == null) {
                    AutoSpeedrunApi.chatMessage("no valid container recognized");
                    if (testTime == 3) {
                        AutoSpeedrunApi.emergencyStopUserCode();
                        testTime = -1;
                        container = null;
                    }
                    return true;
                }
                testTime = 5;
            }
            if (!lootChest() || testTime > 40) {
                testTime = -1;
                container = null;
                return false;
            }
        }
        return true;
    }

    private boolean isValuedItem(ContainerItem item) {
        return !ContainerItem.isEmpty(item) && !item.getName().equals("air");  // todo replace
    }

    private boolean lootChest() {
        AutoSpeedrunApi.chatMessage("loot chest");
        for (int slot = 0; slot < container.getNumContainerSlots(); slot++) {
            ContainerItem item = container.getItem(slot);
            if (isValuedItem(item)) {
                AutoSpeedrunApi.pressKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
                int x = container.getContainerSlotScreenX(slot);
                int y = container.getContainerSlotScreenY(slot);
                AutoSpeedrunApi.screenClick(x, y, 0);
                AutoSpeedrunApi.releaseKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
                container.setItem(null, slot);
                return true;
            }
        }
        return false;
    }
}
