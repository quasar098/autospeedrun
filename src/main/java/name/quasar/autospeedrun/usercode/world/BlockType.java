package name.quasar.autospeedrun.usercode.world;

import name.quasar.autospeedrun.usercode.geometry.AABB;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.Vector3;

import java.util.List;

public class BlockType {

    public static final BlockType AIR = new BlockType("minecraft:air");
    public static final BlockType UNKNOWN_SOLID = new BlockType("minecraft:unknown_solid");

    private String value = null;
    private final List<String> properties;

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

    public BlockType(String blockType, List<String> properties) {
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
        return !getValue().equals("air") && !isFluid() && solidBlockYOffset() == null;
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
    public float getFluidHeight() {
        assert isFluid();
        return getReportedFluidHeight() / 9.0F;
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

    public float getBlockSpeedFactor() {
        switch (getValue()) {
            case "honey_block":
            case "soul_sand":
                return 0.4F;
            default:
                return 1.0F;
        }
    }

    public boolean isClimbable() {
        switch (getValue()) {
            case "ladder":
            case "vine":
            case "scaffolding":
            case "weeping_vines":
            case "weeping_vines_plant":
            case "twisting_vines":
            case "twisting_vines_plant":
                return true;
            default:
                return false;
        }
    }

    public float getFriction() {
        switch (getValue()) {
            case "slime_block":
                return 0.8F;
            case "ice":
            case "packed_ice":
            case "frosted_ice":
                return 0.98F;
            case "blue_ice":
                return 0.989F;
            default:
                return 0.6F;
        }
    }

    public float getJumpFactor() {
        switch (getValue()) {
            case "honey_block":
                return 0.5F;
            default:
                return 1.0F;
        }
    }

    // it has to be a list because some hitboxes (e.g. stair, fence, cauldron) are weird and made of multiple AABB
    // todo actually add all the blocks
    // maybe it is advantageous to move these to separate data files because this is not ideal probably
    public AABB[] getCompositeCollisionBoxes(BlockLocation bl) {
        long x = bl.getX();
        long y = bl.getY();
        long z = bl.getZ();
        switch (getValue()) {
            case "air":
                return new AABB[]{};
            case "prismarine_slab":
            case "prismarine_brick_slab":
            case "dark_prismarine_slab":
            case "oak_slab":
            case "spruce_slab":
            case "birch_slab":
            case "jungle_slab":
            case "acacia_slab":
            case "dark_oak_slab":
            case "stone_slab":
            case "smooth_stone_slab":
            case "sandstone_slab":
            case "cut_sandstone_slab":
            case "petrified_oak_slab":
            case "cobblestone_slab":
            case "brick_slab":
            case "stone_brick_slab":
            case "nether_brick_slab":
            case "quartz_slab":
            case "red_sandstone_slab":
            case "cut_red_sandstone_slab":
            case "purpur_slab":
            case "polished_granite_slab":
            case "smooth_red_sandstone_slab":
            case "mossy_stone_brick_slab":
            case "polished_diorite_slab":
            case "mossy_cobblestone_slab":
            case "end_stone_brick_slab":
            case "smooth_sandstone_slab":
            case "smooth_quartz_slab":
            case "granite_slab":
            case "andesite_slab":
            case "red_nether_brick_slab":
            case "polished_andesite_slab":
            case "diorite_slab":
            case "crimson_slab":
            case "warped_slab":
            case "blackstone_slab":
            case "polished_blackstone_brick_slab":
            case "polished_blackstone_slab":
                if (properties.contains("type: top")) {
                    return new AABB[] { new AABB(x, y+0.5, z, x+1, y+1, z+1) };
                } else {
                    return new AABB[] { new AABB(x, y, z, x+1, y+0.5, z+1) };
                }
            default:
                return new AABB[]{ new AABB(x, y, z, x+1, y+1, z+1) };
        }
    }

    public boolean isNoCollider() {
        // Material.java
        switch (getValue()) {
            // noCollider false
            // todo blocks with material CLOTH_DECORATION,PLANT,WATER_PLANT,REPLACEABLE_PLANT,REPLACEABLE_WATER_PLANT,
            //  WATER,BUBBLE_COLUMN,LAVA,TOP_SNOW,FIRE,WEB,DECORATION,BAMBOO_SAPLING would also return false
            case "air":
            case "structure_void":
            case "nether_portal":
            case "end_portal":
            case "end_gateway":
                return false;
            default:
                return true;
        }
    }

    public boolean isCollisionShapeFullBlock() {
        // apparently this is usually implemented by checking the actual shape is the same but that seems insanely
        // inefficient so i'm just going to precompute a list of all blocks that follow this
        // todo do the aforementioned precompution

        // temporary fix
        switch (getValue()) {
            case "stone_slab":
            case "stone":
            case "air":
                return false;
            default:
                return true;
        }
    }

    /* also determines if players get pushed out of the block */
    public boolean isSuffocating() {
        // blocks with noCollider result in false, nonfull blocks result in false
        // (isSuffocating -> false) specially set predicate blocks may result in false
        switch (getValue()) {
            // todo all glasses
            // todo all stained glasses
            case "moving_piston":
            case "oak_leaves":
            case "spruce_leaves":
            case "birch_leaves":
            case "jungle_leaves":
            case "acacia_leaves":
            case "dark_oak_leaves":
            case "shulker_box":  // todo apparently shulker box is only suffocating if it's closed
            case "piston":  // same with piston
                return false;
            default:
                return !isNoCollider() && isCollisionShapeFullBlock();
        }
    }
}
