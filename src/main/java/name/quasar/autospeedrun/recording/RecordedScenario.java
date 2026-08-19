package name.quasar.autospeedrun.recording;

import name.quasar.autospeedrun.usercode.Dimension;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.world.BlockType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class RecordedScenario implements Serializable {
    private static final long serialVersionUID = 1L;

    public ArrayList<RecordedTick> ticks;
    public HashMap<BlockLocation, BlockType> blocks = null;

    private transient boolean currentlyDoingLocalPlayerTick = false;

    public RecordedScenario() {
        this.ticks = new ArrayList<>();
        this.blocks = new HashMap<>();
    }

    public void recordTick() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        // todo we should also save WorldBlocks which contains small region around the player
        // (what the user chose to include in the scenario basically)
        ticks.add(new RecordedTick(Minecraft.getInstance()));
    }

    public void saveBlock(BlockPos blockPos, BlockState result) {
        // todo use Minecraft.getInstance().level.dimension().location().getPath() switch case on dimension maybe
        blocks.put(
            new BlockLocation(Dimension.OVERWORLD, blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            new BlockType("minecraft:" + Registry.BLOCK.getKey(result.getBlock()).getPath())
        );
    }

    public void store() {

        if (Minecraft.getInstance().player == null) {
            System.out.println("there is no player ???");
            return;
        }

        File scenariosFolder = FabricLoader.getInstance().getGameDir().resolve("scenarios").toFile();
        if (!scenariosFolder.isDirectory()) {
            if (!scenariosFolder.mkdirs()) {
                Minecraft.getInstance().player.displayClientMessage(new TextComponent("mkdirs() failed"), false);
                return;
            }
        }

        String scenarioFileName = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss").format(LocalDateTime.now());
        File scenarioFile = FabricLoader.getInstance().getGameDir().resolve("scenarios").resolve(scenarioFileName).toFile();
        try (FileOutputStream fos = new FileOutputStream(scenarioFile); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);
        } catch (FileNotFoundException e) {
            Minecraft.getInstance().player.displayClientMessage(new TextComponent("FileNotFoundException"), false);
            throw new RuntimeException(e);
        } catch (IOException e) {
            Minecraft.getInstance().player.displayClientMessage(new TextComponent("IOException"), false);
            throw new RuntimeException(e);
        }
    }

    public static RecordedScenario load(String name) {
        return new RecordedScenario();  // todo
    }

    public boolean isCurrentlyDoingLocalPlayerTick() {
        return currentlyDoingLocalPlayerTick;
    }

    public void setCurrentlyDoingLocalPlayerTick(boolean currentlyDoingLocalPlayerTick) {
        this.currentlyDoingLocalPlayerTick = currentlyDoingLocalPlayerTick;
    }
}
