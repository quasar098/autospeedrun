package name.quasar.autospeedrun.usercode;

import java.util.HashMap;

/* collect information about all the blocks seen in the world */
public class WorldBlocks {
    public static HashMap<BlockLocation, Block> knownBlocks;

    public static void reset() {
        knownBlocks = new HashMap<>();
    }
}
