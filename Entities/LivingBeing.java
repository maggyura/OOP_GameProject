package Entities;

import world.World;
import StructuralElements.Room;


public abstract class LivingBeing extends Entity {
    protected int health;
    protected int maxHealth;
    protected int strength;
    protected Room currentRoom;
    protected boolean isAlive;

    public LivingBeing(String name, World world, int health, int strength) {
        super(name, world);
        this.health = health;
        this.maxHealth = health;
        this.strength = strength;
        this.isAlive = true;
    }

   //changing rooms
    public void move(Room destination) {
        if (currentRoom != null) {
            currentRoom.removeOccupant(this);
        }
        currentRoom = destination;
        destination.addOccupant(this);
    }

   
    //attacks. hero attacks using weapon, zombies attack with barehands
    public void attack(LivingBeing target) {
        int damage = calculateDamage();
        target.takeDamage(damage);
    }

    //hero - depending on weapon, zombie - const
    public int calculateDamage() {
        return strength;
    }

    //if hp = 0 -> death
    public final void takeDamage(int amount) {
        if (!isAlive) return;
        health -= amount;
        if (health <= 0) {
            health = 0;
            die();
        }
    }

    //hp recovering
    public void heal(int amount) {
        if (!isAlive) return;
        health = Math.min(health + amount, maxHealth);
    
    protected void die() {
        isAlive = false;
        if (currentRoom != null) {
            currentRoom.removeOccupant(this);
        }
        world.removeLivingBeing(this);
        System.out.println(name + " погиб!");
    }

    
    public int getHealth()       { return health; }
    public int getMaxHealth()    { return maxHealth; }
    public int getStrength()     { return strength; }
    public Room getCurrentRoom() { return currentRoom; }
    public boolean isAlive()     { return isAlive; }

    //hp line
    public String getHealthBar() {
        int filled = (int) ((double) health / maxHealth * 10);
        String bar = "█".repeat(filled) + " ".repeat(10 - filled);
        return "[" + bar + "] " + health + "/" + maxHealth;
    }
}