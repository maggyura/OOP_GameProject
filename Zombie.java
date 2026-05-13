package Entities;

import StructuralElements.Room;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import utilities.Coordinates;
import world.World;

public class Zombie extends LivingBeing {
    private Room spawnRoom;
    private int respawnTimer;
    private boolean isRespawning;
    private Hero targetHero; 

    //const respawn time
    private final int RESPAWN_TIME = 10;

    public Zombie(String name, World world, Coordinates coordinates,
                   int health, int strength, Room spawnRoom, Hero targetHero) {
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
        // BFS чтобы найти следующий шаг к герою через открытые двери
        Room nextStep = bfs(currentRoom, heroRoom);
        if (nextStep != null) {
            move(nextStep);
            System.out.println(name + " движется в комнату #" + nextStep.getIDroom());
        } else {
            System.out.println(name + " не может добраться до героя!");
        }
    }
        // BFS to find the next step to the hero
        private Room bfs(Room start, Room target) {
            if (start.equals(target)) return null;
            Map<Room, Room> cameFrom = new HashMap<>();
            Queue<Room> queue = new LinkedList<>();
            queue.add(start);
            cameFrom.put(start, null);
            while (!queue.isEmpty()) {
                Room current = queue.poll();
                if (current.equals(target)) {
                    // идём назад чтобы найти первый шаг
                    Room step = target; 
                    while (!cameFrom.get(step).equals(start)) {
                        step = cameFrom.get(step);
                }
                return step;
            }
            for (Room neighbor : current.getAccessibleRooms()) {
                if (!cameFrom.containsKey(neighbor)) {
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return null; // путь не найден
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