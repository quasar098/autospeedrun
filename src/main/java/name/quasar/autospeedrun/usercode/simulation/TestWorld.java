package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.geometry.AABB;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.world.BlockType;
import name.quasar.autospeedrun.usercode.world.World;

// todo implement everything
public class TestWorld implements World {
    public TestWorld() {

    }

    @Override
    public BlockType getBlockState(BlockLocation bl) {
        return BlockType.AIR;
    }

    @Override
    public boolean noCollision(AABB aabb) {
        return true;
    }

    @Override
    public boolean containsAnyLiquid(AABB aabb) {
        return false;
    }

}
