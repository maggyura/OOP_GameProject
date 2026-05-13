package StructuralElements;

import GameObjects.GameObject;
import Entities.LivingBeing;
import utilities.Coordinates;

import java.util.ArrayList;
import java.util.List;


public class Room extends StructuralElements {
    private int IDroom;
    private Coordinates coordinates;
    private List<Door> doors;
    private List<GameObject> items;
    private List<LivingBeing> occupants;

    public Room(int id, String description, Coordinates coordinates) {
        super(description);
        this.IDroom = id;
        this.coordinates = coordinates;
        this.doors = new ArrayList<>();
        this.items = new ArrayList<>();
        this.occupants = new ArrayList<>();
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public List<Door> getDoors() {
        return doors;
    }

    //list of all rooms that can be visited through open doors, used for zombies moving
    public List<Room> getAccessibleRooms() {
        List<Room> accessible = new ArrayList<>();
        for (Door door : doors) {
            if (door.isOpen()) {
                accessible.add(door.getOtherRoom(this));
            }
        }
        return accessible;
    }

   
    public void addItem(GameObject item) {
        items.add(item);
    }

    public void removeItem(GameObject item) {
        items.remove(item);
    }

    public List<GameObject> getItems() {
        return items;
    }

    public void addOccupant(LivingBeing being) {
        if (!occupants.contains(being)) {
            occupants.add(being);
        }
    }

    public void removeOccupant(LivingBeing being) {
        occupants.remove(being);
    }

    public List<LivingBeing> getOccupants() {
        return occupants;
    }

  
    public int getIDroom() {
        return IDroom;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
  


    //зачем нужна эта фнкция посомтреть
    public void describe() {
        System.out.println("\n=== Комната #" + IDroom + " " + coordinates + " ===");
        System.out.println(description);

        System.out.println("Выходы (" + doors.size() + "):");
        for (Door door : doors) {
            Room other = door.getOtherRoom(this);
            String status = door.isOpen() ? "открыта" : "закрыта";
            System.out.println("  → Комната #" + other.getIDroom() + " через дверь #" + door.getIDdoor() + " [" + status + "]");
        }

        if (!items.isEmpty()) {
            System.out.println("Предметы:");
            for (GameObject item : items) {
                System.out.println("  • " + item.getName());
            }
        }

        if (!occupants.isEmpty()) {
            System.out.println("Существа:");
            for (LivingBeing being : occupants) {
                System.out.println("  ⚔ " + being.getName() +
                    " [HP: " + being.getHealth() + "/" + being.getMaxHealth() + "]");
            }
        }
    }
}