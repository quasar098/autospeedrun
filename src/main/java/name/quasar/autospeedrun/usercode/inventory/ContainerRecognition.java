package name.quasar.autospeedrun.usercode.inventory;

public class ContainerRecognition {
    private static ContainerRecognition instance;

    public static ContainerRecognition getInstance() {
        if (instance == null) {
            instance = new ContainerRecognition();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public int testTime = -1;

    public ContainerRecognition() {

    }
}
