package Entities;
 
import world.World;
public abstract class Entity {
    protected String name;
    protected World world;
 
    public Entity(String name, World world) {
        this.name = name;
        this.world = world;
    }
 
    public String getName() {
        return name;
    }
 
    public World getWorld() {
        return world;
    }
}
 