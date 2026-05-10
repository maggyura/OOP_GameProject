package gui;

import Entities.Hero;
import Entities.LivingBeing;
import Entities.Zombie;
import GameObjects.Chest;
import GameObjects.Crowbar;
import GameObjects.GameObject;
import GameObjects.Key;
import StructuralElements.Room;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
import world.World;

public class GameScreen extends JPanel {
    private static final int TILE_SIZE = 64;

    private World world;
    private Hero hero;
    private BufferedImage heroSprite;
    private BufferedImage heroAttackLeftSprite;
    private BufferedImage heroAttackRightSprite;
    private BufferedImage villainSprite;
    private BufferedImage floorSprite;
    private BufferedImage chestLockedSprite;
    private BufferedImage chestNotLockedSprite;
    private BufferedImage chestOpenSprite;

    private Set<Integer> pressedKeys = new HashSet<>();
    private double moveTimer = 0;
    private double zombieMoveTimer = 0;

    //attack animation
    private boolean isAttacking = false;
    private double attackTimer = 0;
    private final double ATTACK_DURATION = 0.3;
    private boolean attackFacingRight = true;

    public GameScreen(World world, Hero hero) {
        this.world = world;
        this.hero = hero;

        //loading sprites
        try {
            heroSprite           = ImageIO.read(getClass().getResource("/images/herogoesUporDown.png"));
            heroAttackLeftSprite = ImageIO.read(getClass().getResource("/images/heroAttacksLeft.png"));
            heroAttackRightSprite= ImageIO.read(getClass().getResource("/images/heroAttacksRight.png"));
            villainSprite        = ImageIO.read(getClass().getResource("/images/villain_goesRight.png"));
            floorSprite          = ImageIO.read(getClass().getResource("/images/floor_tile.png"));
            chestLockedSprite    = ImageIO.read(getClass().getResource("/images/chest_Locked.png"));
            chestNotLockedSprite = ImageIO.read(getClass().getResource("/images/chest_notLocked.png"));
            chestOpenSprite      = ImageIO.read(getClass().getResource("/images/chest_Open.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //input from keyboard
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                pressedKeys.add(event.getKeyCode());
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                // E - открыть сундук
                if (event.getKeyCode() == KeyEvent.VK_E) {
                    interactWithChest();
                }
                // пробел - атака
                if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                    attackZombie();
                }
            }
            public void keyReleased(KeyEvent event) {
                pressedKeys.remove(event.getKeyCode());
            }
        });
    }

    public void startLoop() {
        new Timer(16, e -> {
            moveTimer += 0.016;
            zombieMoveTimer += 0.016;

            if (isAttacking) {
                attackTimer += 0.016;
                if (attackTimer >= ATTACK_DURATION) {
                    isAttacking = false;
                    attackTimer = 0;
                }
            }

            if (moveTimer >= 0.15) {
                processInput();
                moveTimer = 0;
            }
            if (zombieMoveTimer >= 1.2) {
                world.tick();
                zombieMoveTimer = 0;
            }
            repaint();
        }).start();
    }

    private void processInput() {
        Room current = hero.getCurrentRoom();
        if (current == null) return;

        //запоминаем направление для анимации
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            attackFacingRight = true;
        }
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) {
            attackFacingRight = false;
        }

        int dx = 0, dy = 0;
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP))   dy = -1;
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN))  dy =  1;
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) dx =  1;
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT))  dx = -1;
        if (dx == 0 && dy == 0) return;

        for (Room neighbor : current.getAccessibleRooms()) {
            int nx = neighbor.getCoordinates().getX() - current.getCoordinates().getX();
            int ny = neighbor.getCoordinates().getY() - current.getCoordinates().getY();
            if (nx == dx && ny == dy) {
                hero.move(neighbor);
                // автоматически подбираем вещи из открытых сундуков
                pickUpFromOpenChests(neighbor);
                break;
            }
        }
    }

    // втоматически подбираем вещи из открытых сундуков
    private void pickUpFromOpenChests(Room room) {
        for (GameObject item : room.getItems()) {
            if (item instanceof Chest) {
                Chest chest = (Chest) item;
                if (chest.isOpen()) {
                    List<GameObject> loot = chest.takeAll();
                    for (GameObject lootItem : loot) {
                        hero.getInventory().add(lootItem);
                        System.out.println("Picked up: " + lootItem.getName());
                    }
                }
            }
        }
    }

    //E открыть закрытый сундук crowbar или key
    private void interactWithChest() {
        Room current = hero.getCurrentRoom();
        if (current == null) return;

        for (GameObject item : current.getItems()) {
            if (item instanceof Chest) {
                Chest chest = (Chest) item;
                if (!chest.isOpen()) {
                    // пробуем открыть crowbar
                    if (hero.hasCrowbar()) {
                        chest.activate(hero.getCrowbar());
                        // подбираем вещи
                        List<GameObject> loot = chest.takeAll();
                        for (GameObject lootItem : loot) {
                            hero.getInventory().add(lootItem);
                            System.out.println("Picked up: " + lootItem.getName());
                        }
                    // пробуем открыть ключом
                    } else if (hero.hasKey()) {
                        chest.activate(hero.getKey());
                        List<GameObject> loot = chest.takeAll();
                        for (GameObject lootItem : loot) {
                            hero.getInventory().add(lootItem);
                            System.out.println("Picked up: " + lootItem.getName());
                        }
                    } else {
                        System.out.println("You need a crowbar or key to open this chest!");
                    }
                }
            }
        }
    }

    //пробел атака зомби
    private void attackZombie() {
        Room current = hero.getCurrentRoom();
        if (current == null) return;

        for (LivingBeing being : current.getOccupants()) {
            if (being instanceof Zombie && being.isAlive()) {
                isAttacking = true;
                attackTimer = 0;
                hero.attack(being);
                System.out.println("Hero attacks " + being.getName() + "! HP left: " + being.getHealth());
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //floor
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 25; col++) {
                g.drawImage(floorSprite, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
            }
        }

        //walls
        g.setColor(Color.BLACK);
        int WALL = 4;
        for (Room room : world.getRooms()) {
            int col = room.getCoordinates().getX();
            int row = room.getCoordinates().getY();
            int x = col * TILE_SIZE;
            int y = row * TILE_SIZE;

            boolean openRight = false;
            boolean openDown = false;

            for (Room neighbor : room.getAccessibleRooms()) {
                int nx = neighbor.getCoordinates().getX();
                int ny = neighbor.getCoordinates().getY();
                if (nx == col + 1 && ny == row) openRight = true;
                if (nx == col && ny == row + 1) openDown = true;
            }

            if (!openRight && col + 1 < 25) {
                g.fillRect(x + TILE_SIZE - WALL, y, WALL * 2, TILE_SIZE);
            }
            if (!openDown && row + 1 < 15) {
                g.fillRect(x, y + TILE_SIZE - WALL, TILE_SIZE, WALL * 2);
            }
        }

        //chests
        for (Room room : world.getRooms()) {
            for (GameObject item : room.getItems()) {
                if (item instanceof Chest) {
                    Chest chest = (Chest) item;
                    int cx = room.getCoordinates().getX() * TILE_SIZE;
                    int cy = room.getCoordinates().getY() * TILE_SIZE;

                    BufferedImage sprite;
                    if (chest.isLocked())       sprite = chestLockedSprite;
                    else if (!chest.isOpen())   sprite = chestNotLockedSprite;
                    else                        sprite = chestOpenSprite;

                    g.drawImage(sprite, cx, cy, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }

        //hero показываем спрайт атаки если атакует
        int hx = hero.getPosition().getX() * TILE_SIZE;
        int hy = hero.getPosition().getY() * TILE_SIZE;
        if (isAttacking) {
            BufferedImage attackSprite = attackFacingRight ? heroAttackRightSprite : heroAttackLeftSprite;
            g.drawImage(attackSprite, hx, hy, TILE_SIZE, TILE_SIZE, this);
        } else {
            g.drawImage(heroSprite, hx, hy, TILE_SIZE, TILE_SIZE, this);
        }

        //zombies
        for (LivingBeing being : world.getLivingBeings()) {
            if (being instanceof Zombie && being.isAlive()) {
                int vx = being.getPosition().getX() * TILE_SIZE;
                int vy = being.getPosition().getY() * TILE_SIZE;
                g.drawImage(villainSprite, vx, vy, TILE_SIZE, TILE_SIZE, this);
            }
        }
    }
}