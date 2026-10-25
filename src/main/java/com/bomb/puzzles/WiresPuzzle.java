package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.ui.Theme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 3 to 6 coloured wires. The manual says which ONE to cut.
public class WiresPuzzle extends Puzzle {

    private static final int RED = 0, BLUE = 1, YELLOW = 2, WHITE = 3, BLACK = 4;
    private static final Color[] COLOURS = {
            new Color(0xC8392B), new Color(0x2A6FC4), new Color(0xE0B81A), new Color(0xE8E6E0), new Color(0x1A1A1A)};
    private static final int ROW = 22;

    private final int[] wires;
    private final boolean[] cut;
    private final int answer;

    public WiresPuzzle(Bomb bomb) {
        super("WIRES", bomb);
        wires = new int[3 + random.nextInt(4)];
        for (int i = 0; i < wires.length; i++) {
            wires[i] = random.nextInt(COLOURS.length);
        }
        cut = new boolean[wires.length];
        answer = wireToCut();
        addRow(new WireBoard(), 0);
    }

    // The rules from the manual, checked in order
    private int wireToCut() {
        int last = wires.length - 1;
        if (count(RED) == 0) return 1;
        if (bomb.lastDigit() % 2 == 1 && count(RED) > 1) return lastOf(RED);
        if (wires[last] == WHITE) return last;
        if (count(BLUE) > 1) return lastOf(BLUE);
        return 0;
    }

    private int count(int colour) {
        int n = 0;
        for (int w : wires) if (w == colour) n++;
        return n;
    }

    private int lastOf(int colour) {
        for (int i = wires.length - 1; i >= 0; i--) if (wires[i] == colour) return i;
        return -1;
    }

    private void cutWire(int i) {
        if (locked() || cut[i]) return;
        cut[i] = true;
        if (i == answer) solve();
        else strike();
        repaint();
    }

    // Draws the wires and works out which one was clicked
    private class WireBoard extends JComponent {

        WireBoard() {
            Dimension size = new Dimension(320, ROW * wires.length + 8);
            setPreferredSize(size);
            setMinimumSize(size); // never squash the wires away on small screens
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int i = (e.getY() - 4) / ROW;
                    if (i >= 0 && i < wires.length) cutWire(i);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            g2.setFont(Theme.mono(15));
            for (int i = 0; i < wires.length; i++) {
                int y = 4 + i * ROW + ROW / 2;
                g2.setColor(Theme.MUTED);
                g2.drawString(String.valueOf(i + 1), 2, y + 5);
                // terminals
                g2.setColor(Theme.BORDER);
                g2.fillOval(20, y - 7, 14, 14);
                g2.fillOval(w - 16, y - 7, 14, 14);
                // the wire (a cut wire has a gap in the middle)
                g2.setColor(wires[i] == BLACK ? Theme.SCREEN : COLOURS[wires[i]]);
                g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int mid = w / 2 + 10;
                if (cut[i]) {
                    g2.drawLine(30, y, mid - 16, y + 3);
                    g2.drawLine(mid + 16, y + 3, w - 10, y);
                } else {
                    g2.drawLine(30, y, w - 10, y);
                }
            }
            g2.dispose();
        }
    }
}
