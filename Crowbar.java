package GameObjects;

public class Crowbar extends GameObject {
    public Crowbar(String name) {
        super(name, true);
    }

    public void use(Chest target) {
        if (target != null) {
            target.forceOpen();
        }
    }
}