package name.quasar.autospeedrun.usercode.world;

import java.util.HashMap;

/**
 * contains some probability informations about what a block could be from game observances
 */
public class WorldBlock {
    private BlockType blockType;
    private double prob;
    private HashMap<String, Object> attributes;

    public WorldBlock(BlockType blockType, double prob) {
        this(blockType, prob, new HashMap<>());
    }

    public WorldBlock(BlockType blockType, double prob, HashMap<String, Object> attributes) {
        this.blockType = blockType;
        this.prob = Math.max(Math.min(prob, 1.0), 0.0);
        this.attributes = attributes;
    }

    public double getProb() {
        return prob;
    }

    public void setProb(double prob) {
        this.prob = Math.max(Math.min(prob, 1.0), 0.0);
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(String key) {
        return attributes.getOrDefault(key, null);
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String key, Object val) {
        attributes.put(key, val);
    }
}
