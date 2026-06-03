package GameObjects;

public abstract class GameObject {
    protected String name;
    protected boolean isPortable;

    public GameObject(String name, boolean isPortable) {
        this.name = name;
        this.isPortable = isPortable;
    }

    public String getName() { return name; }
    public boolean isPortable() { return isPortable; }
}