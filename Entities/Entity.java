package Entities;
import world.World;
import utilities.Coordinates; // so it sees class coord

public abstract class Entity {
    protected String name; // name of the Entity
    protected World world;
    protected Coordinates coordinates;

    public Entity(String name, World world, Coordinates coordinates) {
        this.name = name;
        this.world = world;
        this.coordinates = coordinates;
    }

    //method to get position
    public Coordinates getPosition(){
        return coordinates;
    }
 
    public String getName() {
        return name;
    }
 
    public World getWorld() {
        return world;
    }
}
 