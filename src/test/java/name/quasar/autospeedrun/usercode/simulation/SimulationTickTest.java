package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.Dimension;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import name.quasar.autospeedrun.usercode.world.BlockType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// todo make this more generalized by recording inputs/positions/velocities(?)/blocks and saving them to resource files so it's easy to add test cases

public class SimulationTickTest {
    @Test
    @DisplayName("Standing on air causes falling, standing on solid causes stillness")
    void testVerticalFallingAndStanding() {
        double[] startingXs = {-99.5, 0.0, 100.5};
        double[] startingZs = {-99.5, 0.0, 100.5};
        for (double startingX : startingXs) {
            for (double startingZ : startingZs) {
                SimulationTick tick0 = new SimulationTick(Dimension.OVERWORLD)
                    .setPlayerPos(new Vector3(startingX, 8.0, startingZ))
                    .setPlayerVelo(new Vector3(0, 0, 0))
                    .setPlayerYaw(0.0f)
                    .setPlayerPitch(0.0f);
                TestWorld world = new TestWorld();
                world.setBlock(new BlockLocation(
                    Dimension.OVERWORLD, (long) Math.floor(startingX), 3, (long) Math.floor(startingZ)
                ), new BlockType("minecraft:stone"));

                // data collected in-game
                double[] expectedYValues = new double[] {
                    8.0, 7.921599998474121, 7.766367993957519, 7.535840625044555, 7.231523797587011, 6.854893299348356,
                    6.407395402364938, 5.890447453259965, 5.305438451751212, 4.653729617588595, 4.0
                };

                SimulationTick tickN = tick0;
                for (double expectedY : expectedYValues) {
                    tickN = tickN.getNext(new FakeKBMInputs(), world);
                    assertEquals(startingX, tickN.getPlayerPos().getX());
                    assertEquals(expectedY, tickN.getPlayerPos().getY());
                    assertEquals(startingZ, tickN.getPlayerPos().getZ());
                }
            }
        }
    }
}
