package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.HashSet;
import java.util.Set;

import world.World;
import Entities.Hero;
import StructuralElements.Room;
import Entities.Villain;
import Entities.LivingBeing;

public class GameScreen extends JPanel {
    private static final int TILE_SIZE = 64;

    private World world;
    private Hero hero;
    private BufferedImage heroSprite;
    private BufferedImage villainSprite;
    private BufferedImage floorSprite;

    private Set<Integer> pressedKeys = new HashSet<>();
    private double moveTimer = 0;

    public GameScreen(World world, Hero hero) {
        this.world = world;
        this.hero = hero;

        // loading sprites
        try {
            heroSprite    = ImageIO.read(getClass().getResource("/herogoesUporDown"));
            villainSprite = ImageIO.read(getClass().getResource("/villain_goesRight.png"));
            //floorSprite   = ImageIO.read(getClass().getResource("/floor.png")); narisovat pol
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // input from keyboard
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event)  {
                pressedKeys.add(event.getKeyCode());
            }
            public void keyReleased(KeyEvent event) {
                pressedKeys.remove(event.getKeyCode());
            }
        });
    }

    public void startLoop() {
        // 60 fps
        new Timer(16, e -> {
            moveTimer += 0.016;
            if (moveTimer >= 0.15) {
                processInput();
                moveTimer = 0;
            }
            world.tick();
            repaint(); // calls paintComponent
        }).start();
    }

    private void processInput() {
        Room current = hero.getCurrentRoom();
        if (current == null) return;

        int dx = 0, dy = 0;
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP))    dy =  1;
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN))  dy = -1;
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) dx =  1;
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT))  dx = -1;
        if (dx == 0 && dy == 0) return;

        for (Room neighbor : current.getAccessibleRooms()) {
            int nx = neighbor.getCoordinates().getX() - current.getCoordinates().getX();
            int ny = neighbor.getCoordinates().getY() - current.getCoordinates().getY();
            if (nx == dx && ny == dy) {
                hero.move(neighbor);
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // rooms
        for (Room room : world.getRooms()) {
            int x = room.getCoordinates().getX() * TILE_SIZE;
            int y = room.getCoordinates().getY() * TILE_SIZE;
            g.drawImage(floorSprite, x, y, TILE_SIZE, TILE_SIZE, this);
        }

        // hero
        int hx = hero.getPosition().getX() * TILE_SIZE;
        int hy = hero.getPosition().getY() * TILE_SIZE;
        g.drawImage(heroSprite, hx, hy, TILE_SIZE, TILE_SIZE, this);

        // villains
        for (LivingBeing being : world.getLivingBeings()) {
            if (being instanceof Villain && being.isAlive()) {
                int vx = being.getPosition().getX() * TILE_SIZE;
                int vy = being.getPosition().getY() * TILE_SIZE;
                g.drawImage(villainSprite, vx, vy, TILE_SIZE, TILE_SIZE, this);
            }
        }
    }
}