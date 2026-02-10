package name.quasar.autospeedrun.usercode;

public class Block {

    private String blockType = null;

    public Block(String blockType) {
        assert blockType.startsWith("minecraft:");
        this.blockType = blockType.replaceFirst("minecraft:", "");
    }

    public String getBlockType() {
        return blockType;
    }

    @Override
    public String toString() {
        return "B<" + getBlockType() + ">";
    }
}
