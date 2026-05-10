package GameObjects;

import utilities.Activatable;
import java.util.ArrayList;
import java.util.List;

public class Chest extends GameObject implements Activatable {
    private boolean isLocked;
    private boolean isOpen;
    private List<GameObject> contents;
    public Chest(String name, boolean isLocked) {
        super(name, false);
        this.isLocked = isLocked;
        this.isOpen = false;
        this.contents = new ArrayList<>();
    }

    @Override
    public boolean activate(GameObject item) {
        if (isOpen) {
            System.out.println("Chest is already open.");
            return true;
        }
        
        if (item instanceof Crowbar) {
            return forceOpen();
        }

        System.out.println("Chest is locked.");
        return false;
    }

    public boolean forceOpen() {
        this.isLocked = false;
        this.isOpen = true;
        System.out.println("You've broke the chest using crowbar!");
        return true;
    }

    public List<GameObject> takeAll() {
        if (!isOpen) {
            System.out.println("The chest is closed.");
            return new ArrayList<>();
        }
        if (contents.isEmpty()) {
            System.out.println("The chest is empty.");
            return new ArrayList<>();
        }
        List<GameObject> items = new ArrayList<>(contents);
        contents.clear();
        return items;
    
}

    public boolean isOpen()   {
        return isOpen;
    }
    public boolean isLocked() {
        return isLocked;
    }
}
