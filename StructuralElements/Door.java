package StructuralElements;

import utilities.Activatable;
import GameObjects.GameObject;
import GameObjects.Key;
import GameObjects.Lock;

public class Door extends StructuralElements implements Activatable {
    private int IDdoor;
    private boolean isOpen;
    private Lock lock;

    public Door(int id, String description, Lock lock) {
        super(description);
        this.IDdoor = id;
        this.lock = lock;
        this.isOpen = false;
    }

    @Override
    public boolean activate(GameObject item) {
    if (isOpen) return true;

    if (item instanceof Key) {
        // Любой экземпляр класса Key теперь подходит
        if (lock != null) {
            lock.setLocked(false);
        }
        this.isOpen = true;
        System.out.println("Вы использовали ключ и открыли дверь!");
        return true;
    } else {
        System.out.println("Для этой двери нужен ключ.");
        return false;
    }
}

    public int getIDdoor() { return IDdoor; }
    public boolean isOpen() { return isOpen; }
}
