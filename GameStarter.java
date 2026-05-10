import javax.swing.*;

import world.World;
import utilities.Coordinates;

import gui.GameScreen;

import StructuralElements.Door;
import StructuralElements.Room;

import Entities.Hero;
import Entities.Zombie;

import GameObjects.Chest;

public class GameStarter {
    public static void main(String[] args) {
        World world = new World("ZombieWorld");
        Hero hero = new Hero("Hero", world, new Coordinates(0, 0));
        //rooms, doors

        Room r1 = new Room(1, "Start", new Coordinates(0, 0));
        Room r2 = new Room(2, "Corridor", new Coordinates(1, 0));
        Room r3 = new Room(3, "Room",     new Coordinates(2, 0));
        world.addRoom(r1);
        world.addRoom(r2);
        world.addRoom(r3);

        // doors between the rooms
        new Door(1, "Door 1-2", r1, r2, null);
        new Door(2, "Door 2-3", r2, r3, null);

        //chest test
        Chest chest = new Chest("Сундук", true); // true = заперт на ключ
        r2.addItem(chest);

        // hero starts in r1- room1
        hero.move(r1);

        // zombie starts in r3 - room3
        Zombie z = new Zombie("Zombie", world, new Coordinates(2, 0), 50, 10, r3);
        z.move(r3);
        world.addLivingBeing(z);

        // window(to start gui)
        JFrame frame = new JFrame("ZombieGame");
        GameScreen screen = new GameScreen(world, hero);

        frame.add(screen);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // fullscreen
        frame.setVisible(true);

        screen.startLoop();
    }
}