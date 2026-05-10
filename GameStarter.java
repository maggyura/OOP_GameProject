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
    //список входных клеток больших комнат
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

        //ссначала размещаем большие комнаты
        placeBigRooms();

        //потом генерируем лабиринт (пропускаем клетки больших комнат)
        boolean[][] visited = new boolean[ROWS][COLS];
        //помечаем все клетки больших комнат как посещённые кроме входа
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (isPartOfBigRoom[row][col]) visited[row][col] = true;
            }
        }
        generateMaze(0, 0, visited);

        //выходная комната после которой побюеда
        Room exitRoom = new Room(id++, "Exit", new Coordinates(COLS, ROWS - 1));
        world.addRoom(exitRoom);
        new Door(doorId++, "Exit Door", grid[ROWS-1][COLS-1], exitRoom, null);

        generateChests();

        hero.move(grid[0][1]);

        Zombie z = new Zombie("Zombie", world, new Coordinates(0, 0), 50, 10, grid[0][0], hero);
        z.move(grid[0][0]);
        world.addLivingBeing(z);

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

        while (placed < 3 && attempts < 100) {
            attempts++;
            //размер 2x2 или 3x3
            int size = rand.nextBoolean() ? 4 : 5;
            //рандомная позиция (не у края и не у старта героя) хотя можно подумать
            int startRow = 4 + rand.nextInt(ROWS - size - 5);
            int startCol = 4 + rand.nextInt(COLS - size - 5);

            //проверяем что место свободно
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

            //помечаем клетки как часть большой комнаты
            for (int r = startRow; r < startRow + size; r++) {
                for (int c = startCol; c < startCol + size; c++) {
                    isPartOfBigRoom[r][c] = true;
                }
            }

            //соединяем все внутренние клетки открытыми дверями
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

            //добавляем вход (открытый) и выход (закрытый на ключ)
            int entryRow = startRow;
            int entryCol = startCol - 1;
            if (entryCol >= 0) {
                new Door(doorId++, "Door", grid[entryRow][entryCol], grid[entryRow][startCol], null);
                bigRoomDoors.add(new int[]{entryRow, startCol, 0}); // 0 = открытая
            }

            // выход - правая нижняя клетка, закрытая на ключ
            int exitRow = startRow + size - 1;
            int exitCol = startCol + size;
            if (exitCol < COLS) {
                new Door(doorId++, "Door", grid[exitRow][startCol + size - 1], grid[exitRow][exitCol], new Lock(true));                bigRoomDoors.add(new int[]{exitRow, exitCol, 1}); // 1 = закрытая
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

        //первый сундук открытый - нож и crowbar
        Chest openChest = new Chest("Chest", false, true);
        openChest.addItem(new Knife("Knife"));
        openChest.addItem(new Crowbar("Crowbar"));
        allRooms.get(0).addItem(openChest);

        //от 5 до 7 закрытых сундуков
        int closedCount = 5 + rand.nextInt(3);
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