package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.recording.RecordedScenario;
import name.quasar.autospeedrun.recording.RecordedTick;
import name.quasar.autospeedrun.usercode.Dimension;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import name.quasar.autospeedrun.usercode.world.BlockType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// todo make this more generalized by recording inputs/positions/velocities(?)/blocks and saving them to resource files so it's easy to add test cases

public class SimulationTickTest {
    @Test
    @DisplayName("Standing on air causes falling, standing on solid causes stillness")
    void testVerticalFallingAndStanding() {
        SimulationTick tick0 = new SimulationTick(Dimension.OVERWORLD)
            .setPlayerPos(new Vector3(0.0, 8.0, 0.0))
            .setPlayerVelo(new Vector3(0, 0, 0))
            .setPlayerYaw(0.0f)
            .setPlayerPitch(0.0f);
        TestWorld world = new TestWorld();
        world.setBlock(new BlockLocation(
            Dimension.OVERWORLD, 0, 3, 0
        ), new BlockType("minecraft:stone"));

        // data collected in-game
        double[] expectedYValues = new double[] {
            8.0, 7.921599998474121, 7.766367993957519, 7.535840625044555, 7.231523797587011, 6.854893299348356,
            6.407395402364938, 5.890447453259965, 5.305438451751212, 4.653729617588595, 4.0, 4.0, 4.0, 4.0, 4.0
        };

        SimulationTick tickN = tick0;
        for (double expectedY : expectedYValues) {
            tickN = tickN.getNext(new FakeKBMInputs(), world);
            assertEquals(0.0, tickN.getPlayerPos().getX());
            assertEquals(expectedY, tickN.getPlayerPos().getY());
            assertEquals(0.0, tickN.getPlayerPos().getZ());
        }
    }

    @Test
    @DisplayName("Jumping in place in various slightly different locations")
    void testJumpingInPlace() {
        double[] startingXs = {-0.1, 0.0, 0.1};
        double[] startingZs = {-0.1, 0.0, 0.1};
        for (double startingX : startingXs) {
            for (double startingZ : startingZs) {
                SimulationTick tick0 = new SimulationTick(Dimension.OVERWORLD)
                    .setPlayerPos(new Vector3(startingX, 4.0, startingZ))
                    .setPlayerVelo(new Vector3(0, 0, 0))
                    .setPlayerYaw(0.0f)
                    .setPlayerPitch(0.0f);
                TestWorld world = new TestWorld();
                world.setBlock(new BlockLocation(
                    Dimension.OVERWORLD, 0, 3, 0
                ), new BlockType("minecraft:stone"));

                // data collected in-game
                double[] expectedYValues = new double[] {
                    4.0, 4.0, 4.419999986886978, 4.7531999805212015, 5.001335979112147, 5.166109260938214,
                    5.249187078744681, 5.252203340253724, 5.176759275064237, 5.02442408821368, 4.796735600668692,
                    4.495200877005911, 4.121296840539189, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0
                };

                SimulationTick tickN = tick0;
                for (int i = 0; i < expectedYValues.length; i++) {
                    double expectedY = expectedYValues[i];
                    tickN = tickN.getNext(new FakeKBMInputs().setKeyJump(i == 2), world);
                    assertEquals(startingX, tickN.getPlayerPos().getX());
                    assertEquals(expectedY, tickN.getPlayerPos().getY());
                    assertEquals(startingZ, tickN.getPlayerPos().getZ());
                }
            }
        }
    }

    @Test
    @DisplayName("Jumping in place in a vertically limited space (2.5 blocks tall due to top half slab)")
    void testJumpingInPlaceHittingHead() {
        SimulationTick tick0 = new SimulationTick(Dimension.OVERWORLD)
            .setPlayerPos(new Vector3(0.0, 4.0, 0.0))
            .setPlayerVelo(new Vector3(0, 0, 0))
            .setPlayerYaw(0.0f)
            .setPlayerPitch(0.0f);
        TestWorld world = new TestWorld();
        world.setBlock(new BlockLocation(
            Dimension.OVERWORLD, 0, 3, 0
        ), new BlockType("minecraft:stone"));
        world.setBlock(new BlockLocation(
                Dimension.OVERWORLD, 0, 6, 0
            ),
            new BlockType("minecraft:stone_slab", Collections.singletonList("type: top"))
        );

        // data collected in-game
        double[] expectedYValues = new double[] { 4.0, 4.0, 4.419999986886978, 4.700000047683716,
            4.621600046157837, 4.466368041641235, 4.235840672728271, 4.0, 4.0, 4.0 };

        SimulationTick tickN = tick0;
        for (int i = 0; i < expectedYValues.length; i++) {
            double expectedY = expectedYValues[i];
            tickN = tickN.getNext(new FakeKBMInputs().setKeyJump(i == 2), world);
            assertEquals(0, tickN.getPlayerPos().getX());
            assertEquals(expectedY, tickN.getPlayerPos().getY());
            assertEquals(0, tickN.getPlayerPos().getZ());
        }
    }

    @Test
    @DisplayName("Test all scenarios-testcases/*")
    void testAllScenariosTestcases() {
        URL scenariosTestcases = getClass().getClassLoader().getResource("scenarios-testcases");
        assertNotNull(scenariosTestcases);
        try (Stream<Path> paths = Files.list(Paths.get(scenariosTestcases.toURI()))) {
            // should i be using a spliterator for parallel testing capabilities? maybe eventually...
            Iterator<Path> nextFile = paths.iterator();
            while (nextFile.hasNext()) {
                Path p = nextFile.next();
                System.out.printf("Testing %s\n", p);
                try (FileInputStream fis = new FileInputStream(p.toString()); ObjectInputStream ois = new ObjectInputStream(fis)) {
                    RecordedScenario scenario = (RecordedScenario) ois.readObject();

                    TestWorld world = new TestWorld();
                    world.setBlocks(scenario.blocks);

                    RecordedTick recordedTick0 = scenario.ticks.get(0);

                    SimulationTick tickN = new SimulationTick(Dimension.OVERWORLD)
                        .setPlayerPos(new Vector3(recordedTick0.playerX, recordedTick0.playerY, recordedTick0.playerZ))
                        .setPlayerVelo(new Vector3(0, 0, 0))
                        .setPlayerYaw(recordedTick0.playerYaw)
                        .setPlayerPitch(recordedTick0.playerPitch);
                    System.out.println("===");
                    for (int i = 0; i < scenario.ticks.size(); i++) {
                        RecordedTick recordedTick = scenario.ticks.get(i);
                        System.out.println(recordedTick);
                        FakeKBMInputs inputs = new FakeKBMInputs()
                            .setKeyDown(recordedTick.keyDown)
                            .setKeyUp(recordedTick.keyUp)
                            .setKeyLeft(recordedTick.keyLeft)
                            .setKeyRight(recordedTick.keyRight)
                            .setKeyJump(recordedTick.keyJump)
                            .setKeyShift(recordedTick.keyShift)
                            .setKeySprint(recordedTick.keySprint);

                        tickN.setPlayerPitch(recordedTick.playerPitch);
                        tickN.setPlayerYaw(scenario.ticks.get(Math.max(0, i-1)).playerYaw);
                        tickN = tickN.getNext(inputs, world);

                        RecordedTick expected = scenario.ticks.get(i);
                        String failMsg = String.format("failure on tick %d on test \"%s\"", i, p);
                        assertEquals(expected.playerX, tickN.getPlayerPos().getX(), failMsg);
                        assertEquals(expected.playerY, tickN.getPlayerPos().getY(), failMsg);
                        assertEquals(expected.playerZ, tickN.getPlayerPos().getZ(), failMsg);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
