package name.quasar.autospeedrun.recording;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

public class RecordedScenario implements Serializable {
    private static final long serialVersionUID = 1L;

    public ArrayList<RecordedTick> ticks;

    public RecordedScenario() {
        this.ticks = new ArrayList<>();
    }

    public void recordTick() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        // todo we should also save WorldBlocks which contains small region around the player
        // (what the user chose to include in the scenario basically)
        ticks.add(new RecordedTick(Minecraft.getInstance()));
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
}
