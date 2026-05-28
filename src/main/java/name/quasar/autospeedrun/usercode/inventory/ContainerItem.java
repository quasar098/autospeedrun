package name.quasar.autospeedrun.usercode.inventory;

public class ContainerItem {
    private final String name;
    private final Integer count;

    public ContainerItem(String name) {
        this.name = name;
        this.count = null;
    }

    public ContainerItem(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public static boolean isEmpty(ContainerItem item) { return item == null || item.getName().equals("air"); }

    public ContainerItem withCount(int count) {
        return new ContainerItem(getName(), count);
    }

    public String getName() {
        return name;
    }

    public Integer getCount() {
        return count;
    }
}
