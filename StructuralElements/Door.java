package StructuralElements;

import utilities.Activatable;
import GameObjects.GameObject;
import GameObjects.Key;
import GameObjects.Lock;


public class Door extends StructuralElements implements Activatable {
    private int IDdoor;
    private boolean isOpen;
    private Lock lock;
    private Room roomA;
    private Room roomB;

    public Door(int id, String description, Room roomA, Room roomB, Lock lock) {
        super(description);
        this.IDdoor = id;
        this.lock = lock;
        this.isOpen = (lock == null || !lock.isLocked()); //no lock - alread opened
        this.roomA = roomA;
        this.roomB = roomB;

        roomA.addDoor(this);
        roomB.addDoor(this);
    }

    public Room getOtherRoom(Room from) {
        if (from == roomA) return roomB;
        if (from == roomB) return roomA;
        throw new IllegalArgumentException(
            from.getIDroom() + " не соединена с этой дверью"
        );
    }

   
    @Override
    public boolean activate(GameObject item) {
        if (isOpen) return true;

        if (item instanceof Key) {
            if (lock != null) {
                lock.setLocked(false);
            }
            this.isOpen = true;
        }

        System.out.println("You need ky to open this door.");
        return false;
    }

    public int getIDdoor()  { return IDdoor; }
    public boolean isOpen() { return isOpen && (lock == null || !lock.isLocked()); }
    public boolean isLocked() { return lock != null && lock.isLocked(); }
    public Room getRoomA()  { return roomA; }
    public Room getRoomB()  { return roomB; }
}