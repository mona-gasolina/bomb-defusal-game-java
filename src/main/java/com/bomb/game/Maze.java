package com.bomb.game;

import java.util.ArrayList;
import java.util.List;

// One maze from mazes.txt.
// The maze is stored as 13 x 13 characters: '#' is a wall, 'O' is a circle.
// Cell (row, col) of the 6 x 6 grid is the character at (2*row+1, 2*col+1).
public class Maze {

    public static final int SIZE = 6;
    private static final int TEXT_SIZE = SIZE * 2 + 1;

    private final int number;
    private final List<String> rows;
    private int startRow = -1, startCol, endRow, endCol;

    private Maze(int number, List<String> rows) {
        this.number = number;
        this.rows = rows;
        // Find the two circles. Reading top to bottom, the first circle is the start.
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (charAt(2 * r + 1, 2 * c + 1) == 'O') {
                    if (startRow < 0) {
                        startRow = r;
                        startCol = c;
                    } else {
                        endRow = r;
                        endCol = c;
                    }
                }
            }
        }
    }

    public static List<Maze> loadAll() {
        List<String> lines = GameFiles.lines("mazes.txt");
        List<Maze> mazes = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("MAZE")) {
                int number = Integer.parseInt(lines.get(i).substring(5).trim());
                mazes.add(new Maze(number, lines.subList(i + 1, i + 1 + TEXT_SIZE)));
            }
        }
        return mazes;
    }

    // Is there a wall between two cells that are next to each other?
    public boolean blocked(int r1, int c1, int r2, int c2) {
        return charAt(r1 + r2 + 1, c1 + c2 + 1) == '#';
    }

    public char charAt(int textRow, int textCol) {
        return rows.get(textRow).charAt(textCol);
    }

    public boolean isCircle(int row, int col) {
        return charAt(2 * row + 1, 2 * col + 1) == 'O';
    }

    public int getNumber() { return number; }
    public int getStartRow() { return startRow; }
    public int getStartCol() { return startCol; }
    public int getEndRow() { return endRow; }
    public int getEndCol() { return endCol; }
}
