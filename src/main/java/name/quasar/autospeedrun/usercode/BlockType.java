package name.quasar.autospeedrun.usercode;

public class BlockType {

    public static final BlockType AIR = new BlockType("minecraft:air");

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
