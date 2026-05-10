package world;

import java.util.List;
import java.util.ArrayList;
import StructuralElements.Room;
import Entities.LivingBeing;
import utilities.Executable;

public class World {
    private String name;
    private List<Room> rooms;
    private List<LivingBeing> livingBeings;
 
    public World(String name) {
        this.name = name;
        this.rooms = new ArrayList<>();
        this.livingBeings = new ArrayList<>();
    }
    // game cycle
    public void tick() {
        // copy of the array to dodge ConcurrentModificationException
        List<LivingBeing> snapshot = new ArrayList<>(livingBeings);
        for (LivingBeing being : snapshot) {
            if (being instanceof Executable) {
                ((Executable) being).execute();
            }
        }
    }
 
    public void addRoom(Room room) {

        rooms.add(room);
    }
 
    public void addLivingBeing(LivingBeing being) {
        livingBeings.add(being);
    }
 
    public void removeLivingBeing(LivingBeing being) {
        livingBeings.remove(being);
    }
 
    public List<Room> getRooms() {
        return rooms;
    }
 
    public List<LivingBeing> getLivingBeings() {
        return livingBeings;
    }
 
    public String getName() {
        return name;
    }
}
 