import Entities.Hero;
import Entities.Zombie;
import GameObjects.Chest;
import GameObjects.Crowbar;
import GameObjects.FirstAidKit;
import GameObjects.Key;
import GameObjects.Knife;
import GameObjects.Lock;
import StructuralElements.Door;
import StructuralElements.Room;
import gui.GameScreen;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import utilities.Coordinates;
import world.World;

public class GameStarter {
    static int COLS = 20;
    static int ROWS = 15;
    static Room[][] grid;
    static int doorId = 1;
    static boolean[][] isPartOfBigRoom;
    // array for the big rooms
    static List<int[]> bigRoomDoors = new ArrayList<>(); //row, col, locked

    public static void main(String[] args) {
        World world = new World("ZombieWorld");
        Hero hero = new Hero("Hero", world, new Coordinates(0, 0));

        grid = new Room[ROWS][COLS];
        isPartOfBigRoom = new boolean[ROWS][COLS];
        int id = 1;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Room room = new Room(id++, "Room", new Coordinates(col, row));
                grid[row][col] = room;
                world.addRoom(room);
            }
        }

        // we first place the big rooms
        placeBigRooms();

        //Then we generate the labyrinth itself, skipping over the big rooms
        boolean[][] visited = new boolean[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (isPartOfBigRoom[row][col]) visited[row][col] = true;
            }
        }
        generateMaze(0, 0, visited);

        //ExitRoom that leads to win screen
        Room exitRoom = new Room(id++, "Exit", new Coordinates(COLS, ROWS - 1));
        world.addRoom(exitRoom);
        new Door(doorId++, "Exit Door", grid[ROWS-1][COLS-1], exitRoom, null);

        generateChests();

        hero.move(grid[0][1]);

        //zombie1 behind hero
        Zombie z = new Zombie("Zombie", world, new Coordinates(0, 0), 50, 10, grid[0][0], hero);
        z.move(grid[0][0]);
        world.addLivingBeing(z);

        //zombie 2 in the right top conrer, middle brother
        Zombie z2 = new Zombie("Zombie 2", world, new Coordinates(COLS-1, 0), 50, 10, grid[0][COLS-1], hero);
        z2.move(grid[0][COLS-1]);
        world.addLivingBeing(z2);

        //zombie 3 in the center, stroger and hekthier than his brothers
        Zombie z3 = new Zombie("Zombie 3", world, new Coordinates(COLS/2, ROWS/2), 70, 12, grid[ROWS/2][COLS/2], hero);
        z3.move(grid[ROWS/2][COLS/2]);
        world.addLivingBeing(z3);

        JFrame frame = new JFrame("ZombieGame");
        GameScreen screen = new GameScreen(world, hero, isPartOfBigRoom, bigRoomDoors);
        frame.add(screen);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        screen.startLoop();
    }

    static void placeBigRooms() {
        Random rand = new Random();
        int placed = 0;
        int attempts = 0;

        while (placed < 5 && attempts < 200) {
            attempts++;
            //3x3 or 4x4
            int size = rand.nextBoolean() ? 3 : 4;
            //random position(not the start of the game )
            int startRow = 3 + rand.nextInt(ROWS - size - 4);
            int startCol = 3 + rand.nextInt(COLS - size - 4);

            //checking if the space is free
            boolean free = true;
            for (int r = startRow - 1; r <= startRow + size; r++) {
                for (int c = startCol - 1; c <= startCol + size; c++) {
                    if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                        if (isPartOfBigRoom[r][c]) { free = false; break; }
                    }
                }
                if (!free) break;
            }
            if (!free) continue;

            //marking cells as a part of a big room
            for (int r = startRow; r < startRow + size; r++) {
                for (int c = startCol; c < startCol + size; c++) {
                    isPartOfBigRoom[r][c] = true;
                }
            }

            //connecting the inner corridors to open doors
            for (int r = startRow; r < startRow + size; r++) {
                for (int c = startCol; c < startCol + size; c++) {
                    if (c + 1 < startCol + size) {
                        new Door(doorId++, "Door", grid[r][c], grid[r][c+1], null);
                    }
                    if (r + 1 < startRow + size) {
                        new Door(doorId++, "Door", grid[r][c], grid[r+1][c], null);
                    }
                }
            }

            // exit and entrance doors creation
            int entryRow = startRow;
            int entryCol = startCol - 1;
            if (entryCol >= 0) {
                new Door(doorId++, "Door", grid[entryRow][entryCol], grid[entryRow][startCol], null);
                bigRoomDoors.add(new int[]{entryRow, startCol, 0}); // 0 = open
            }


            int exitRow = startRow + size - 1;
            int exitCol = startCol + size;
            if (exitCol < COLS) {
                new Door(doorId++, "Door", grid[exitRow][startCol + size - 1], grid[exitRow][exitCol], new Lock(true));
                bigRoomDoors.add(new int[]{exitRow, exitCol, 1}); // 1 = closed; it didnt work we couldnt figure out why
            }

            placed++;
        }
    }

    static void generateMaze(int row, int col, boolean[][] visited) {
        visited[row][col] = true;
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
        List<int[]> dirs = Arrays.asList(directions);
        Collections.shuffle(dirs);

        for (int[] dir : dirs) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS && !visited[newRow][newCol]) {
                new Door(doorId++, "Door", grid[row][col], grid[newRow][newCol], null);
                generateMaze(newRow, newCol, visited);
            }
        }
    }

    static void generateChests() {
        Random rand = new Random();

        List<Room> allRooms = new ArrayList<>();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (row == 0 && (col == 0 || col == 1)) continue;
                allRooms.add(grid[row][col]);
            }
        }
        Collections.shuffle(allRooms);

        //the first open chest with a knife and a crowbar
        Chest openChest = new Chest("Chest", false, true);
        openChest.addItem(new Knife("Knife"));
        openChest.addItem(new Crowbar("Crowbar"));
        allRooms.get(0).addItem(openChest);

        //from 5 to 7 random closed chests
        int closedCount = 8 + rand.nextInt(5);
        for (int i = 1; i <= closedCount; i++) {
            Chest chest = new Chest("Chest", true);
            if (rand.nextBoolean()) {
                chest.addItem(new FirstAidKit("First Aid Kit", 30));
            } else {
                chest.addItem(new Key("Key"));
            }
            allRooms.get(i).addItem(chest);
        }
    }
}