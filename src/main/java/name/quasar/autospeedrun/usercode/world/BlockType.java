package name.quasar.autospeedrun.usercode.world;

import name.quasar.autospeedrun.usercode.geometry.Vector3;

import java.util.ArrayList;

public class BlockType {

    public static final BlockType AIR = new BlockType("minecraft:air");
    public static final BlockType UNKNOWN_SOLID = new BlockType("minecraft:unknown_solid");

    private String value = null;
    private final ArrayList<String> properties;

    public BlockType(String blockType) {
        assert blockType.startsWith("minecraft:");
        this.value = blockType.replaceFirst("minecraft:", "");
        this.properties = null;
        if (blockType.equals("flowing_water")) {
            this.value = "water";
        }
        if (blockType.equals("flowing_lava")) {
            this.value = "lava";
        }
    }

    public BlockType(String blockType, ArrayList<String> properties) {
        assert blockType.startsWith("minecraft:");
        this.value = blockType.replaceFirst("minecraft:", "");
        this.properties = properties;
    }

    public boolean equals(BlockType obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return obj.getValue().equals(getValue());
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "BT<" + getValue() + ">";
    }

    public boolean isSolid() {
        return !getValue().equals("air") && solidBlockYOffset() == null;
    }

    public boolean isFluid() {
        return getValue().equals("water") || getValue().equals("lava");
    }

    /** "reported" as in "seen on F3 menu" */
    public int getReportedFluidHeight() {
        for (String prop : properties) {
            if (prop.startsWith("level: ")) {
                return Integer.parseInt(prop.substring(7));
            }
        }
        return 8;
    }

    /** block height */
    public double getFluidHeight() {
        assert isFluid();
        return getReportedFluidHeight() / 9.0;
    }

    public Vector3 getFluidFlow() {
        // todo ummm what do we do about this it doesnt show up on f3
        // maybe ml?
        return Vector3.ZERO;  // temp solution
    }

    public Integer solidBlockYOffset() {
        switch (getValue()) {
            case "tall_grass":
            case "large_fern":
            case "rose_bush":
            case "peony":
            case "sunflower":
            case "lilac":
            case "pink_tulip":
                return properties.contains("half: upper") ? -2 : -1;
            case "cornflower":
            case "allium":
            case "red_tulip":
            case "white_tulip":
            case "dandelion":
            case "poppy":
            case "azure_bluet":
            case "orange_tulip":
            case "lily_of_the_valley":
            case "blue_orchid":
            case "oxeye_daisy":
            case "dead_bush":
            case "sign":
            case "torch":
            case "grass":
            case "fern":
                return -1;
            default:
                return null;
        }
    }
}
