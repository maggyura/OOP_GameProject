package gui;

import Entities.Hero;
import Entities.LivingBeing;
import Entities.Zombie;
import GameObjects.Chest;
import GameObjects.FirstAidKit;
import GameObjects.GameObject;
import StructuralElements.Door;
import StructuralElements.Room;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
import world.World;

public class GameScreen extends JPanel {
    private static final int TILE_SIZE = 64;
    private static final int ROWS = 15;
    private static final int COLS = 20;

    private World world;
    private Hero hero;
    private boolean[][] isPartOfBigRoom;
    private List<int[]> bigRoomDoors;

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

    //win state
    private boolean gameWon = false;

    public GameScreen(World world, Hero hero, boolean[][] isPartOfBigRoom, List<int[]> bigRoomDoors) {
        this.world = world;
        this.hero = hero;
        this.isPartOfBigRoom = isPartOfBigRoom;
        this.bigRoomDoors = bigRoomDoors;

        try {
            heroSprite            = ImageIO.read(getClass().getResource("/images/herogoesUporDown.png"));
            heroAttackLeftSprite  = ImageIO.read(getClass().getResource("/images/heroAttacksLeft.png"));
            heroAttackRightSprite = ImageIO.read(getClass().getResource("/images/heroAttacksRight.png"));
            villainSprite         = ImageIO.read(getClass().getResource("/images/villain_goesRight.png"));
            floorSprite           = ImageIO.read(getClass().getResource("/images/floor_tile.png"));
            chestLockedSprite     = ImageIO.read(getClass().getResource("/images/chest_Locked.png"));
            chestNotLockedSprite  = ImageIO.read(getClass().getResource("/images/chest_notLocked.png"));
            chestOpenSprite       = ImageIO.read(getClass().getResource("/images/chest_Open.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                pressedKeys.add(event.getKeyCode());
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                if (event.getKeyCode() == KeyEvent.VK_E) {
                    GameScreen.this.interactWithChest();
                    GameScreen.this.interactWithDoor();
                }
                if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                    GameScreen.this.attackZombie();
                }
                if (event.getKeyCode() == KeyEvent.VK_H) {
                    GameScreen.this.useHealer();
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
        if (gameWon) return;

        Room current = hero.getCurrentRoom();
        if (current == null) return;

        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            attackFacingRight = true;
        }
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) {
            attackFacingRight = false;
        }

        int dx = 0, dy = 0;
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP))    dy = -1;
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN))   dy =  1;
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT))  dx =  1;
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT))   dx = -1;
        if (dx == 0 && dy == 0) return;

        for (Room neighbor : current.getAccessibleRooms()) {
            int nx = neighbor.getCoordinates().getX() - current.getCoordinates().getX();
            int ny = neighbor.getCoordinates().getY() - current.getCoordinates().getY();
            if (nx == dx && ny == dy) {
                hero.move(neighbor);
                pickUpFromOpenChests(neighbor);
                // проверяем выход
                if (neighbor.getDescription().equals("Exit")) {
                    gameWon = true;
                }
                break;
            }
        }
    }

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

    private void interactWithChest() {
        Room current = hero.getCurrentRoom();
        if (current == null) return;

        for (GameObject item : current.getItems()) {
            if (item instanceof Chest) {
                Chest chest = (Chest) item;
                if (!chest.isOpen()) {
                    if (hero.hasCrowbar()) {
                        chest.activate(hero.getCrowbar());
                        List<GameObject> loot = chest.takeAll();
                        for (GameObject lootItem : loot) {
                            hero.getInventory().add(lootItem);
                            System.out.println("Picked up: " + lootItem.getName());
                        }
                    } else if (hero.hasKey()) {
                        chest.activate(hero.getKey());
                        List<GameObject> loot = chest.takeAll();
                        for (GameObject lootItem : loot) {
                            hero.getInventory().add(lootItem);
                            System.out.println("Picked up: " + lootItem.getName());
                        }
                    } else {
                        System.out.println("You need a crowbar or key!");
                    }
                }
            }
        }
    }

    private void interactWithDoor() {
        Room current = hero.getCurrentRoom();
        if (current == null) return;

        for (Door door : current.getDoors()) {
            if (door.isLocked() && hero.hasKey()) {
                door.activate(hero.getKey());
                hero.getInventory().remove(hero.getKey());
                System.out.println("Door unlocked!");
                break;
            }
        }
    }

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

    private void useHealer() {
        for (GameObject item : hero.getInventory()) {
            if (item instanceof FirstAidKit) {
                FirstAidKit kit = (FirstAidKit) item;
                hero.heal(kit.getHealAmount());
                hero.getInventory().remove(kit);
                System.out.println("Used healer! HP: " + hero.getHealth());
                return;
            }
        }
        System.out.println("No healer in inventory!");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //floor
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                g.drawImage(floorSprite, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
            }
        }

        //walls
        g.setColor(Color.BLACK);
        int WALL = 4;
        for (Room room : world.getRooms()) {
            int col = room.getCoordinates().getX();
            int row = room.getCoordinates().getY();
            if (col >= COLS || row >= ROWS) continue; // пропускаем выходную комнату
            int x = col * TILE_SIZE;
            int y = row * TILE_SIZE;

            boolean openRight = false;
            boolean openDown  = false;

            for (Room neighbor : room.getAccessibleRooms()) {
                int nx = neighbor.getCoordinates().getX();
                int ny = neighbor.getCoordinates().getY();
                if (nx == col + 1 && ny == row) openRight = true;
                if (nx == col && ny == row + 1) openDown  = true;
            }

            if (!openRight && col + 1 < COLS) {
                g.fillRect(x + TILE_SIZE - WALL, y, WALL * 2, TILE_SIZE);
            }
            if (!openDown && row + 1 < ROWS) {
                g.fillRect(x, y + TILE_SIZE - WALL, TILE_SIZE, WALL * 2);
            }
        }

        //выходная дверь — зелёная полоска справа снизу
        g.setColor(Color.GREEN);
        g.fillRect(COLS * TILE_SIZE - WALL, (ROWS - 1) * TILE_SIZE, WALL * 3, TILE_SIZE);

        //цветные двери больших комнат
        for (int[] door : bigRoomDoors) {
            int dRow   = door[0];
            int dCol   = door[1];
            int locked = door[2];
            int x = dCol * TILE_SIZE;
            int y = dRow * TILE_SIZE;
            g.setColor(locked == 1 ? Color.RED : Color.GREEN);
            g.fillRect(x - WALL, y, WALL * 3, TILE_SIZE);
        }

        //chests
        for (Room room : world.getRooms()) {
            for (GameObject item : room.getItems()) {
                if (item instanceof Chest) {
                    Chest chest = (Chest) item;
                    int cx = room.getCoordinates().getX() * TILE_SIZE;
                    int cy = room.getCoordinates().getY() * TILE_SIZE;
                    if (cx >= COLS * TILE_SIZE || cy >= ROWS * TILE_SIZE) continue;

                    BufferedImage sprite;
                    if (chest.isLocked())      sprite = chestLockedSprite;
                    else if (!chest.isOpen())  sprite = chestNotLockedSprite;
                    else                       sprite = chestOpenSprite;

                    g.drawImage(sprite, cx, cy, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }

        //hero (только если не вышел)
        if (!gameWon) {
            int hx = hero.getPosition().getX() * TILE_SIZE;
            int hy = hero.getPosition().getY() * TILE_SIZE;
            if (isAttacking) {
                BufferedImage attackSprite = attackFacingRight ? heroAttackRightSprite : heroAttackLeftSprite;
                g.drawImage(attackSprite, hx, hy, TILE_SIZE, TILE_SIZE, this);
            } else {
                g.drawImage(heroSprite, hx, hy, TILE_SIZE, TILE_SIZE, this);
            }
        }

        //zombies
        for (LivingBeing being : world.getLivingBeings()) {
            if (being instanceof Zombie && being.isAlive()) {
                int vx = being.getPosition().getX() * TILE_SIZE;
                int vy = being.getPosition().getY() * TILE_SIZE;
                g.drawImage(villainSprite, vx, vy, TILE_SIZE, TILE_SIZE, this);
            }
        }

        //панель справа
        int panelX = COLS * TILE_SIZE + 10;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(COLS * TILE_SIZE, 0, getWidth() - COLS * TILE_SIZE, getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("HP:", panelX, 30);

        //hp bar
        int maxHp = hero.getMaxHealth();
        int curHp = hero.getHealth();
        int barWidth = 150;
        int filled = (int)((double) curHp / maxHp * barWidth);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(panelX, 40, barWidth, 16);
        g.setColor(Color.RED);
        g.fillRect(panelX, 40, filled, 16);
        g.setColor(Color.WHITE);
        g.drawString(curHp + "/" + maxHp, panelX, 72);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Inventory:", panelX, 100);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        int itemY = 120;
        if (hero.getInventory().isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("(empty)", panelX, itemY);
        } else {
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (GameObject item : hero.getInventory()) {
                counts.merge(item.getName(), 1, Integer::sum);
            }
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                g.setColor(Color.WHITE);
                String line = entry.getValue() > 1
                    ? "• " + entry.getKey() + " x" + entry.getValue()
                    : "• " + entry.getKey();
                g.drawString(line, panelX, itemY);
                itemY += 20;
            }
        }

        //экран  после победы
        if (gameWon) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 64));
            String msg = "YOU WIN!";
            FontMetrics fm = g.getFontMetrics();
            int msgX = (COLS * TILE_SIZE - fm.stringWidth(msg)) / 2;
            int msgY = (ROWS * TILE_SIZE) / 2;
            g.drawString(msg, msgX, msgY);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String sub = "Press ESC to exit";
            FontMetrics fm2 = g.getFontMetrics();
            int subX = (COLS * TILE_SIZE - fm2.stringWidth(sub)) / 2;
            g.drawString(sub, subX, msgY + 50);
        }
    }
}