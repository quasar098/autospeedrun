package name.quasar.autospeedrun.usercode.world;

public class BlockType {

    public static final BlockType AIR = new BlockType("minecraft:air");
    public static final BlockType BEDROCK = new BlockType("minecraft:bedrock");
    public static final BlockType UNKNOWN_SOLID = new BlockType("minecraft:unknown_solid");

    private String value = null;

    public BlockType(String blockType) {
        assert blockType.startsWith("minecraft:");
        this.value = blockType.replaceFirst("minecraft:", "");
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
        switch (getValue()) {
            case "dead_bush":
            case "sign":
            case "torch":
            case "grass":
            case "fern":
            case "tall_grass":
            case "large_fern":
            case "rose_bush":
            case "peony":
            case "pink_tulip":
            case "lilac":
            case "sunflower":
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
                return false;
            default:
                return true;
        }
    }
}
