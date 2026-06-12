package name.quasar.autospeedrun.usercode.inventory;

import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.inventory.containers.*;

public class ContainerRecognition {
    private static ContainerRecognition instance;

    public static ContainerRecognition getInstance() {
        if (instance == null) {
            instance = new ContainerRecognition();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public ContainerRecognition() {

    }

    private Container newContainerFromScreen() {
        // this is a super bad design choice probably (?) but we roll with it for now i guess
        Container c;
        c = new Chest3Container();
        if (c.isRecognized()) {
            return c;
        }
        c = new Chest6Container();
        if (c.isRecognized()) {
            return c;
        }
        c = new CraftingContainer();
        if (c.isRecognized()) {
            return c;
        }
        c = new InventoryContainer();
        if (c.isRecognized()) {
            return c;
        }
        return null;
    }

    // todo: read https://en.wikipedia.org/wiki/Cross-correlation and revise to be actually good
    // or maybe https://en.wikipedia.org/wiki/Color_difference
    public double itemDataCompare(int[] observedItemData, int[] expectedItemData) {
        double sumOfSquares = 0;
        for (int v = 0; v < Math.min(observedItemData.length, expectedItemData.length); v++) {
            int aO = (observedItemData[v] & 0xff000000) >> 24;
            int bO = (observedItemData[v] & 0x00ff0000) >> 16;
            int gO = (observedItemData[v] & 0x0000ff00) >> 8;
            int rO = (observedItemData[v] & 0x000000ff);
            int aE = (expectedItemData[v] & 0xff000000) >> 24;
            int bE = (expectedItemData[v] & 0x00ff0000) >> 16;
            int gE = (expectedItemData[v] & 0x0000ff00) >> 8;
            int rE = (expectedItemData[v] & 0x000000ff);
            sumOfSquares += (rO-rE)*(rO-rE) + (gO-gE)*(gO-gE) + (bO-bE)*(bO-bE) + (aO-aE)*(aO-aE);
        }
        return Math.sqrt(sumOfSquares);
    }

    public void populateContainer(Container c) {
        for (int slot = 0; slot < c.getNumContainerSlots(); slot++) {
            int[] observedItemData = new int[256];
            for (int v = 0; v < 256; v++) {
                int x = ((v % 16)) * Util.OPTIONS_TXT_GUI_SCALE + c.getContainerSlotScreenX(slot);
                int y = ((v / 16)) * Util.OPTIONS_TXT_GUI_SCALE + c.getContainerSlotScreenY(slot);
                observedItemData[v] = AutoSpeedrunAPI.getScreenshotPixelRGBA(x, y);
                if ((observedItemData[v] & 0x00ffffff) == 0x8b8b8b) { observedItemData[v] = 0; }
                if ((observedItemData[v] & 0x00ffffff) == 0xc5c5c5) { observedItemData[v] = 0; }
            }
            String bestItem = null;
            double bestItemComparePenalty = 0;
            for (String item : ItemData.images.keySet()) {
                int[] expectedItemData = ItemData.images.get(item);
                double penalty = itemDataCompare(observedItemData, expectedItemData);
                if (bestItem == null || penalty < bestItemComparePenalty) {
                    bestItem = item;
                    bestItemComparePenalty = penalty;
                }
                // perfect match optimization
                if (bestItemComparePenalty == 0) {
                    break;
                }
            }
            c.setItem(new ContainerItem(bestItem), slot);
        }
    }

    public Container pullFromScreen() {
        Container c = newContainerFromScreen();
        if (c == null) {
            return null;
        }
        populateContainer(c);
        return c;
    }

}
