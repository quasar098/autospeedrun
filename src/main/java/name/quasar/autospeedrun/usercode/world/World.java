package name.quasar.autospeedrun.usercode.world;

import name.quasar.autospeedrun.usercode.geometry.AABB;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;

public interface World {
    BlockType getBlockState(BlockLocation bl);

    boolean noCollision(AABB aabb);

    boolean containsAnyLiquid(AABB aabb);
}
