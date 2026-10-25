package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.game.Maze;
import com.bomb.ui.Theme;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;

// A 6 x 6 grid with two circles and invisible walls.
// Only the analyst's manual shows the walls.
public class MazePuzzle extends Puzzle {

    private final Maze maze;
    private final JButton[][] cells = new JButton[Maze.SIZE][Maze.SIZE];
    private int row, col; // where the technician is right now

    // a small circle drawn on the two circle cells (dark on the lit cell so it still shows)
    private final Icon circle = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground().equals(Theme.KHAKI) ? Theme.SCREEN : Theme.KHAKI);
            g2.fillOval(x, y, 12, 12);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 12;
        }

        @Override
        public int getIconHeight() {
            return 12;
        }
    };

    public MazePuzzle(Bomb bomb) {
        super("MAZE", bomb);
        List<Maze> mazes = Maze.loadAll();
        maze = mazes.get(random.nextInt(mazes.size()));
        row = maze.getStartRow();
        col = maze.getStartCol();

        // 7 x 7: the first row and column are the A-F and 1-6 labels
        JPanel grid = new JPanel(new GridLayout(Maze.SIZE + 1, Maze.SIZE + 1, 2, 2));
        grid.setOpaque(false);
        grid.add(new JLabel(""));
        for (int c = 0; c < Maze.SIZE; c++) {
            grid.add(Theme.label(String.valueOf((char) ('A' + c)), Theme.mono(13), Theme.MUTED));
        }
        for (int r = 0; r < Maze.SIZE; r++) {
            grid.add(Theme.label(String.valueOf(r + 1), Theme.mono(13), Theme.MUTED));
            for (int c = 0; c < Maze.SIZE; c++) {
                JButton cell = new JButton();
                cell.setPreferredSize(new Dimension(30, 20));
                cell.setFocusPainted(false);
                cell.setBorder(new LineBorder(Theme.BORDER, 1));
                int r2 = r, c2 = c;
                cell.addActionListener(e -> goTo(r2, c2));
                cells[r][c] = cell;
                grid.add(cell);
            }
        }
        addRow(grid, 0);
        refresh();
    }

    private void goTo(int r, int c) {
        if (isSolved()) return;
        boolean nextTo = Math.abs(r - row) + Math.abs(c - col) == 1;
        if (!nextTo) return;
        if (maze.blocked(row, col, r, c)) {
            strike();
            return;
        }
        row = r;
        col = c;
        refresh();
        if (r == maze.getEndRow() && c == maze.getEndCol()) solve();
    }

    // The cell you are on is lit. The two circles always show.
    private void refresh() {
        for (int r = 0; r < Maze.SIZE; r++) {
            for (int c = 0; c < Maze.SIZE; c++) {
                JButton cell = cells[r][c];
                cell.setBackground(r == row && c == col ? Theme.KHAKI : Theme.PANEL_DARK);
                cell.setIcon(maze.isCircle(r, c) ? circle : null);
            }
        }
    }
}
