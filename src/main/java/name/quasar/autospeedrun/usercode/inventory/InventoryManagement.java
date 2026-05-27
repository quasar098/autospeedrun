package name.quasar.autospeedrun.usercode.inventory;

import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.inventory.containers.Chest3Container;
import name.quasar.autospeedrun.usercode.inventory.containers.Container;

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

    public int testTime = -1;

    public InventoryManagement() {

    }

    public boolean perform() {
        if (testTime == -1) {
            return false;
        }
        testTime++;
        if (testTime == 1) {
            AutoSpeedrunApi.tapRightClick();
        } else {
            if (!lootBTChest() || testTime > 40) {
                testTime = -1;
                return false;
            }
        }
        return true;
    }

    private boolean lootBTChest() {
        // AbstractContainerScreen
        // this.leftPos = (this.width - this.imageWidth) / 2;
        // this.topPos = (this.height - this.imageHeight) / 2;
        Container chest = new Chest3Container();
        int scale = Util.OPTIONS_TXT_GUI_SCALE;
        int leftPos = (AutoSpeedrunApi.getScreenshotWidth() - chest.getImageWidth() * scale) / 2;
        int topPos = (AutoSpeedrunApi.getScreenshotHeight() - chest.getImageHeight() * scale) / 2;
        AutoSpeedrunApi.screenClick(leftPos, topPos, 0);
        return false;
    }
}
