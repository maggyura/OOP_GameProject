// MyGame.java
import javax.swing.*;
import world.World;
import Entities.Hero;
import utilities.Coordinates;
import gui.GameScreen;

public class GameStarter {
    public static void main(String[] args) {
        World world = new World("ZombieWorld");
        Hero hero = new Hero("Hero", world, new Coordinates(0, 0));
        //rooms, doors

        JFrame frame = new JFrame("ZombieGame");
        GameScreen screen = new GameScreen(world, hero);

        frame.add(screen);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        screen.startLoop();
    }
}