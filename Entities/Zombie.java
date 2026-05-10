package Entities;

import world.World;
import StructuralElements.Room;
import utilities.Coordinates;
import utilities.Executable;

public class Zombie extends LivingBeing implements Executable {
    private Room spawnRoom;
    private int respawnTimer;
    private boolean isRespawning;
    private Hero targetHero; 

    //const respawn time
    private final int RESPAWN_TIME = 10;

    public Zombie(String name, World world, Coordinates coordinates,
                   int health, int strength, Room spawnRoom) {
        super(name, world, health, strength, coordinates); // ← было 50, теперь параметр
        this.spawnRoom = spawnRoom;
        this.respawnTimer = 0;
        this.isRespawning = false;
        this.targetHero = targetHero;
    }
    //respawn method for zombie
    public void respawn() {
        respawnTimer++;
        if (respawnTimer >= RESPAWN_TIME) {
            this.health = maxHealth;
            this.isAlive = true;
            this.isRespawning = false;
            this.respawnTimer = 0;

            // zombie returns to the initial room
            move(spawnRoom);
            System.out.println(name + " respawned in room " + spawnRoom.getIDroom() + "!");
        }
        else {
            System.out.println(name + " is respawning (Left: " + (RESPAWN_TIME - respawnTimer) + ")");
        }
    }

    public boolean isRespawning() {
        return isRespawning;
    }

    //ai of the villain to chase after the hero
    public void chase(Hero hero) {
        if (!isAlive || hero == null || !hero.isAlive()) return;

        Room heroRoom = hero.getCurrentRoom();
        if (heroRoom == null || currentRoom == null) return;

        // если уже в одной комнате — атакуем
        if (currentRoom.equals(heroRoom)) {
            System.out.println(name + " атакует!");
            attack(hero);
            return;
        }

        // ищем соседнюю комнату ближе к герою
        Room best = null;
        double bestDist = distance(currentRoom, heroRoom);

        for (Room neighbor : currentRoom.getAccessibleRooms()) {
            double d = distance(neighbor, heroRoom);
            if (d < bestDist) {
                bestDist = d;
                best = neighbor;
            }
        }

        if (best != null) {
            move(best);
            System.out.println(name + " движется в комнату #" + best.getIDroom());
        } else {
            System.out.println(name + " не может добраться до героя!");
        }
    }

    private double distance(Room a, Room b) {
        int dx = a.getCoordinates().getX() - b.getCoordinates().getX();
        int dy = a.getCoordinates().getY() - b.getCoordinates().getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    

    // cycle of life of zombie
    @Override
    public void execute() {
        if (isAlive) {
            chase(targetHero); // ← теперь аргумент есть
        } else if (isRespawning) {
            respawn();
        }
    }

    // death and respawn
    @Override
    protected void die() {
        super.die(); // Стандартное удаление из комнаты
        this.isRespawning = true;
    }
}