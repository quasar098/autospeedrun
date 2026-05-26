package name.quasar.autospeedrun.usercode.inventory;

import name.quasar.autospeedrun.AutoSpeedrunApi;

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
        }
        if (testTime == 2) {
            AutoSpeedrunApi.screenClick(400+1+18*5, 188+1+18*2, 0);
        }
        if (testTime == 3) {
            AutoSpeedrunApi.screenClick(400+1+18*5, 188+1+18*2, 0);
        }
        if (testTime >= 10) {
            testTime = -1;
        }
        return true;
    }
}
