package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.geometry.AABB;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.world.BlockType;

// todo implement everything
public class FakeWorld {
    public FakeWorld() {

    }

    public BlockType getBlockState(BlockLocation bl) {
        return BlockType.AIR;
    }

    public boolean noCollision(AABB aabb) {
        return true;
    }

    public boolean containsAnyLiquid(AABB aabb) {
        return false;
    }

}
