package com.bomb.ui;

import com.bomb.game.Maze;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

// Draws every maze from mazes.txt for the analyst's manual (walls included)
public class MazeView extends JComponent {

    private static final int CELL = 30, COLS = 3, GAP = 50;
    private static final int MAZE_PX = CELL * Maze.SIZE;

    private final List<Maze> mazes = Maze.loadAll();

    public MazeView() {
        int rows = (mazes.size() + COLS - 1) / COLS;
        setPreferredSize(new Dimension(COLS * (MAZE_PX + GAP) + GAP, rows * (MAZE_PX + GAP + 30) + 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int startX = Math.max(GAP, (getWidth() - COLS * (MAZE_PX + GAP) + GAP) / 2);
        for (int i = 0; i < mazes.size(); i++) {
            int x = startX + (i % COLS) * (MAZE_PX + GAP);
            int y = 40 + (i / COLS) * (MAZE_PX + GAP + 30);
            draw(g2, mazes.get(i), x, y);
        }
        g2.dispose();
    }

    private void draw(Graphics2D g2, Maze maze, int x, int y) {
        g2.setFont(Theme.stencil(18));
        g2.setColor(Theme.KHAKI);
        g2.drawString("MAZE " + maze.getNumber(), x, y - 22);

        g2.setColor(Theme.PANEL_DARK);
        g2.fillRect(x, y, MAZE_PX, MAZE_PX);

        // column letters and row numbers, same as on the bomb
        g2.setFont(Theme.mono(12));
        g2.setColor(Theme.MUTED);
        for (int i = 0; i < Maze.SIZE; i++) {
            g2.drawString(String.valueOf((char) ('A' + i)), x + i * CELL + CELL / 2 - 3, y - 5);
            g2.drawString(String.valueOf(i + 1), x - 14, y + i * CELL + CELL / 2 + 4);
        }

        for (int r = 0; r < Maze.SIZE; r++) {
            for (int c = 0; c < Maze.SIZE; c++) {
                int cx = x + c * CELL, cy = y + r * CELL;
                g2.setColor(Theme.BORDER);
                g2.fillOval(cx + CELL / 2 - 2, cy + CELL / 2 - 2, 4, 4);
                if (maze.isCircle(r, c)) {
                    g2.setColor(Theme.KHAKI);
                    g2.fillOval(cx + 7, cy + 7, CELL - 14, CELL - 14);
                }
            }
        }

        // walls: a '#' between two cells in the text file is a wall on screen
        g2.setColor(Theme.TEXT);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int text = Maze.SIZE * 2 + 1;
        for (int tr = 0; tr < text; tr++) {
            for (int tc = 0; tc < text; tc++) {
                if (maze.charAt(tr, tc) != '#') continue;
                if (tr % 2 == 0 && tc % 2 == 1) { // wall above/below a cell
                    int wy = y + (tr / 2) * CELL, wx = x + (tc / 2) * CELL;
                    g2.drawLine(wx, wy, wx + CELL, wy);
                } else if (tr % 2 == 1 && tc % 2 == 0) { // wall left/right of a cell
                    int wx = x + (tc / 2) * CELL, wy = y + (tr / 2) * CELL;
                    g2.drawLine(wx, wy, wx, wy + CELL);
                }
            }
        }
    }
}
