package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.Dimension;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimulationTickTest {
    @Test
    @DisplayName("Standing on air causes falling")
    void testStandingOnAir() {
        SimulationTick tick0 = new SimulationTick(Dimension.OVERWORLD)
            .setPlayerPos(new Vector3(0, 0, 0))
            .setPlayerVelo(new Vector3(0, 0, 0))
            .setPlayerYaw(0.0f)
            .setPlayerPitch(0.0f);
        TestWorld world = new TestWorld();

        SimulationTick tickN = tick0;
        for (int i = 0; i < 10; i++) {
            System.out.println("start: i" + i);
            tickN = tickN.getNext(new FakeKBMInputs(), world);
            System.out.println("done: i" + i);
            System.out.println(tickN.getPlayerPos());
            System.out.println(tickN.getPlayerVelo());
        }
        /*
[22:35:50] [Render thread/INFO]: [CHAT] -118.5d, Y: 8.0d, Z: -97.5d
[22:35:52] [Render thread/INFO]: [CHAT] -118.5d, Y: 7.921599998474121d, Z: -97.5d
[22:35:54] [Render thread/INFO]: [CHAT] -118.5d, Y: 7.766367993957519d, Z: -97.5d
[22:35:56] [Render thread/INFO]: [CHAT] -118.5d, Y: 7.535840625044555d, Z: -97.5d
[22:35:58] [Render thread/INFO]: [CHAT] -118.5d, Y: 7.231523797587011d, Z: -97.5d
[22:36:00] [Render thread/INFO]: [CHAT] -118.5d, Y: 6.854893299348356d, Z: -97.5d
[22:36:02] [Render thread/INFO]: [CHAT] -118.5d, Y: 6.407395402364938d, Z: -97.5d
[22:36:04] [Render thread/INFO]: [CHAT] -118.5d, Y: 5.890447453259965d, Z: -97.5d
[22:36:06] [Render thread/INFO]: [CHAT] -118.5d, Y: 5.305438451751212d, Z: -97.5d
[22:36:08] [Render thread/INFO]: [CHAT] -118.5d, Y: 4.653729617588595d, Z: -97.5d
[22:36:10] [Render thread/INFO]: [CHAT] -118.5d, Y: 4.0d, Z: -97.5d
         */
    }

//    @Test
//    @DisplayName("Standing on a solid block causes nothing to happen")
//    void testStandingOnSolidBlock() {
//        assertEquals(5, result);
//    }
}
