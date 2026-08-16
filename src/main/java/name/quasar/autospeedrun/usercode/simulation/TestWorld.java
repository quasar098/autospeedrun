package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.geometry.AABB;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.world.BlockType;
import name.quasar.autospeedrun.usercode.world.World;

import java.util.HashMap;

// todo implement everything
public class TestWorld implements World {
    private final HashMap<BlockLocation, BlockType> blocks = new HashMap<>();

    public TestWorld() {

    }

    @Override
    public BlockType getBlockState(BlockLocation bl) {
        return blocks.getOrDefault(bl, BlockType.AIR);
    }

    @Override
    public boolean noCollision(AABB aabb) {
        return true;
    }

    @Override
    public boolean containsAnyLiquid(AABB aabb) {
        return false;
    }

    public void setBlock(BlockLocation blockLoc, BlockType blockType) {
        blocks.put(blockLoc, blockType);
    }

}
