package Entities;

import GameObjects.Crowbar;
import GameObjects.GameObject;
import GameObjects.Key;
import GameObjects.Knife;
import java.util.ArrayList;
import java.util.List;
import utilities.Activatable;
import utilities.Coordinates;
import world.World;


public class Hero extends LivingBeing {
    private List<GameObject> inventory;

    public Hero(String name, World world, Coordinates coordinates) {
        super(name, world,100, 15,  coordinates); // 100 HP, 15 сила
        this.inventory = new ArrayList<>();
        world.addLivingBeing(this);
    }

    @Override
    public void execute(){
        if (!isAlive) 
            return;
    }

    //using GameObjects
    public boolean useItem(GameObject item, Activatable target) {
        if (!inventory.contains(item)) {
            System.out.println("You do not have this item");
            return false;
        }
        boolean result = target.activate(item);
        //key is dissapearing after single use
        if (result && item instanceof Key) {
            inventory.remove(item);
        }
        return result;
    }

    //check if crowbar
    public boolean hasCrowbar() {
        for (GameObject item : inventory) {
            if (item instanceof Crowbar) return true;
        }
        return false;
    }

    //crowbar from inventory
    public Crowbar getCrowbar() {
        for (GameObject item : inventory) {
            if (item instanceof Crowbar) return (Crowbar) item;
        }
        return null;
    }

    //if key
    public boolean hasKey() {
        for (GameObject item : inventory) {
            if (item instanceof Key) return true;
        }
        return false;
    }

    //key from inventory
    public Key getKey() {
        for (GameObject item : inventory) {
            if (item instanceof Key) return (Key) item;
        }
        return null;
    }


    @Override
    protected void die() {
        super.die(); //removes from room and world
        System.out.println("\nGAME IS OVER! You are dead :(");
    }

    @Override
    public int calculateDamage() {
    for (GameObject item : inventory) {
        if (item instanceof Knife) {
            Knife knife = (Knife) item;
            if (!knife.isBroken()) {
                return knife.use();
            }
        }
    }
    return getStrength(); // без ножа — просто кулаками
    }


    public List<GameObject> getInventory() {
        return inventory;
    }

    //show inventory in console
    public void printInventory() {
        System.out.println("=== Инвентарь " + name + " ===");
        System.out.println("HP: " + getHealthBar());
        if (inventory.isEmpty()) {
            System.out.println("(пусто)");
        } else {
            for (GameObject item : inventory) {
                System.out.println("  • " + item.getName());
            }
        }
    }
    @Override
    // hero is controlled by a user
    public void execute() {
    }
}