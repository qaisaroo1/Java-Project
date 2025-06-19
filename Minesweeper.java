package Minesweeper;
import java.util.*;
import java.io.*;

public class Minesweeper {
    // File paths for saving game state and scores
    static final String SAVE_FILE = "minesweeper_save.dat";
    static final String SCORES_FILE = "minesweeper_scores.txt";
    static Scanner input = new Scanner(System.in);

    // Game state variables
    static int[][] mineBoard;
    static char[][] displayBoard;
    static int rows, cols, mines, revealedCells, flagsPlaced;
    static boolean gameOver, won;
    static long startTime;
    static String playerName;

    public static void main(String[] args) {
        // Prompt for player name
        System.out.print("Enter your name: ");
        playerName = input.nextLine();

        // Main menu loop
        while (true) {
            System.out.println("\n--- Minesweeper Menu ---");
            System.out.println("1. New Game");
            System.out.println("2. Load Game");
            System.out.println("3. View High Scores");
            System.out.println("4. Exit");
            System.out.print("Choose: ");
            String choice = input.nextLine();

            if (choice.equals("1")) {
                initializeNewGame();
                playGame();
            } else if (choice.equals("2")) {
                loadGame();
                playGame();
            } else if (choice.equals("3")) {
                showHighScores();
            } else if (choice.equals("4")) {
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    // Initializes new game with user-defined dimensions and mines
    public static void initializeNewGame() {
        System.out.print("Enter rows: ");
        rows = Integer.parseInt(input.nextLine());
        System.out.print("Enter columns: ");
        cols = Integer.parseInt(input.nextLine());
        System.out.print("Enter number of mines: ");
        mines = Integer.parseInt(input.nextLine());

        mineBoard = new int[rows][cols];
        displayBoard = new char[rows][cols];
        for (char[] row : displayBoard)
            Arrays.fill(row, '#'); // initialize all cells as hidden

        revealedCells = 0;
        flagsPlaced = 0;
        gameOver = false;
        won = false;
        startTime = System.currentTimeMillis();
        placeMines();
    }

    // Randomly place mines and update adjacent cell counts
    public static void placeMines() {
        Random rand = new Random();
        int placed = 0;
        while (placed < mines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (mineBoard[r][c] != -1) {
                mineBoard[r][c] = -1;
                placed++;
                for (int i = r - 1; i <= r + 1; i++) {
                    for (int j = c - 1; j <= c + 1; j++) {
                        if (i >= 0 && j >= 0 && i < rows && j < cols && mineBoard[i][j] != -1)
                            mineBoard[i][j]++;
                    }
                }
            }
        }
    }

    // Game loop: handles moves and checks game state
    public static void playGame() {
        while (!gameOver && !won) {
            printBoard();
            System.out.print("Enter move (r c action[f/r/s]): ");
            String[] tokens = input.nextLine().split(" ");
            if (tokens.length < 3) continue;

            int r = Integer.parseInt(tokens[0]);
            int c = Integer.parseInt(tokens[1]);
            char action = tokens[2].charAt(0);

            if (r < 0 || r >= rows || c < 0 || c >= cols) {
                System.out.println("Invalid move.");
                continue;
            }

            if (action == 'f') {
                // Toggle flag on cell
                if (displayBoard[r][c] == '#') {
                    displayBoard[r][c] = 'F';
                    flagsPlaced++;
                } else if (displayBoard[r][c] == 'F') {
                    displayBoard[r][c] = '#';
                    flagsPlaced--;
                }
            } else if (action == 'r') {
                reveal(r, c);
            } else if (action == 's') {
                saveGame();
                System.out.println("Game saved.");
            }

            checkWin();
        }

        if (won) {
            printBoard();
            System.out.println("You won!");
            int timeTaken = (int) ((System.currentTimeMillis() - startTime) / 1000);
            saveHighScore(timeTaken);
        } else if (gameOver) {
            printBoard(true);
            System.out.println("Game Over! You hit a mine.");
        }
    }

    // Reveals a cell and recursively reveals surrounding cells if value is 0
    public static void reveal(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols || displayBoard[r][c] != '#') return;

        if (mineBoard[r][c] == -1) {
            gameOver = true;
            return;
        }

        displayBoard[r][c] = (mineBoard[r][c] == 0) ? ' ' : (char) ('0' + mineBoard[r][c]);
        revealedCells++;

        if (mineBoard[r][c] == 0) {
            for (int i = r - 1; i <= r + 1; i++) {
                for (int j = c - 1; j <= c + 1; j++) {
                    if (i != r || j != c) reveal(i, j);
                }
            }
        }
    }

    // Checks if the player has revealed all non-mine cells
    public static void checkWin() {
        if (revealedCells == rows * cols - mines) {
            won = true;
        }
    }

    // Prints the board, optionally revealing all mines
    public static void printBoard() {
        printBoard(false);
    }

    public static void printBoard(boolean revealMines) {
        System.out.print("   ");
        for (int i = 0; i < cols; i++)
            System.out.print(i + " ");
        System.out.println();
        for (int i = 0; i < rows; i++) {
            System.out.print(i + " ");
            if (i < 10) System.out.print(" ");
            for (int j = 0; j < cols; j++) {
                if (revealMines && mineBoard[i][j] == -1)
                    System.out.print("* ");
                else
                    System.out.print(displayBoard[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Saves the current game state to file
    public static void saveGame() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeUTF(playerName);
            out.writeInt(rows);
            out.writeInt(cols);
            out.writeInt(mines);
            out.writeInt(revealedCells);
            out.writeInt(flagsPlaced);
            out.writeLong(startTime);

            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    out.writeInt(mineBoard[i][j]);

            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    out.writeChar(displayBoard[i][j]);
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }

    // Loads a saved game from file
    public static void loadGame() {
        try (DataInputStream in = new DataInputStream(new FileInputStream(SAVE_FILE))) {
            playerName = in.readUTF();
            rows = in.readInt();
            cols = in.readInt();
            mines = in.readInt();
            revealedCells = in.readInt();
            flagsPlaced = in.readInt();
            startTime = in.readLong();

            mineBoard = new int[rows][cols];
            displayBoard = new char[rows][cols];

            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    mineBoard[i][j] = in.readInt();

            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    displayBoard[i][j] = in.readChar();

            gameOver = false;
            won = false;
            System.out.println("Game loaded.");
        } catch (IOException e) {
            System.out.println("Failed to load game. Starting new game instead.");
            initializeNewGame();
        }
    }

    // Saves the player's score to the high score file
    public static void saveHighScore(int time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORES_FILE, true))) {
            writer.write(playerName + ", " + time + "s");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Couldn't save score: " + e.getMessage());
        }
    }

    // Displays the list of saved high scores
    public static void showHighScores() {
        System.out.println("\nHigh Scores:");
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("No high scores found.");
        }
    }
}
