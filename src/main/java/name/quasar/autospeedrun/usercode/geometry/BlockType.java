package name.quasar.autospeedrun.usercode.geometry;

public class BlockType {

    public static final BlockType AIR = new BlockType("minecraft:air");
    public static final BlockType BEDROCK = new BlockType("minecraft:bedrock");

    private String value = null;

    public BlockType(String blockType) {
        assert blockType.startsWith("minecraft:");
        this.value = blockType.replaceFirst("minecraft:", "");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "BT<" + getValue() + ">";
    }
}
