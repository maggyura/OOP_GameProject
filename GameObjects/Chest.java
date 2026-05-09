package GameObjects;

import utilities.Activatable;
import java.util.ArrayList;
import java.util.List;

public class Chest extends GameObject implements Activatable {
    private boolean isLocked;
    private boolean isOpen;
    private List<GameObject> inventory; // Можно заменить на массив Item[], если он у тебя есть

    public Chest(String name, boolean isLocked) {
        super(name, false);
        this.isLocked = isLocked;
        this.isOpen = false;
        this.inventory = new ArrayList<>();
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

    public void open(Hero hero) {
    if (this.isOpen) {
        if (inventory.isEmpty()) {
            System.out.println("The chest is empty.");
            return;
        }

        System.out.println("Inside the chest you found items!");
        for (GameObject item : inventory) {
            
            if (item instanceof FirstAidKit) { //if firstaidkit - heals
                int hp = ((FirstAidKit) item).getHealAmount();
                hero.heal(hp); 
                
                hero.getInventory().add(item); //adding other items to the inventory
            }
        }
        inventory.clear(); //clearing chest
    }
}
}
}