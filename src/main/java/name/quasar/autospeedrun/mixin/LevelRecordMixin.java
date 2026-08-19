package name.quasar.autospeedrun.mixin;

import name.quasar.autospeedrun.Util;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static net.minecraft.world.level.Level.isOutsideBuildHeight;

@Mixin(Level.class)
public abstract class LevelRecordMixin {
    @Overwrite
    public BlockState getBlockState(BlockPos blockPos) {
        if (isOutsideBuildHeight(blockPos)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            LevelChunk levelChunk = ((Level) (Object) this).getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
            BlockState result = levelChunk.getBlockState(blockPos);
            if (Util.recordedScenario != null && Util.recordedScenario.isCurrentlyDoingLocalPlayerTick()) {
                Util.recordedScenario.saveBlock(blockPos, result);
            }
            return result;
        }
    }
}
