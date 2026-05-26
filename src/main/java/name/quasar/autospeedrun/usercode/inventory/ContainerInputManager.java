package name.quasar.autospeedrun.usercode.inventory;

public class ContainerInputManager {
    private static ContainerInputManager instance = null;

    public static ContainerInputManager getInstance() {
        if (instance == null) {
            instance = new ContainerInputManager();
        }
        return instance;
    }

    public ContainerInputManager() {

    }

}
