package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.ui.Theme;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 8 arrow buttons and a screen showing 4 "lying" arrows.
// Directions are numbered clockwise: 0 = UP, 1 = UP-RIGHT, 2 = RIGHT ... 7 = UP-LEFT.
public class KeypadPuzzle extends Puzzle {

    private final int[] shown = new int[4];
    private final JLabel progress = Theme.label("0/4", Theme.mono(15), Theme.KHAKI);
    private int step;

    public KeypadPuzzle(Bomb bomb) {
        super("KEYPAD", bomb);
        JPanel screen = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        screen.setBackground(Theme.SCREEN);
        screen.setBorder(new LineBorder(Theme.BORDER, 2));
        for (int i = 0; i < shown.length; i++) {
            shown[i] = random.nextInt(8);
            screen.add(new JLabel(new ArrowIcon(shown[i], 20, Theme.LED)));
        }

        // buttons laid out like a compass, with the progress in the middle
        int[] layout = {7, 0, 1, 6, -1, 2, 5, 4, 3};
        JPanel pad = new JPanel(new GridLayout(3, 3, 6, 6));
        pad.setOpaque(false);
        for (int dir : layout) {
            if (dir < 0) {
                pad.add(progress);
                continue;
            }
            JButton b = Theme.button("");
            b.setIcon(new ArrowIcon(dir, 18, Theme.TEXT));
            b.setPreferredSize(new Dimension(50, 34));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    press(dir);
                }
            });
            pad.add(b);
        }
        addRow(screen, 0);
        addRow(pad, 1);
    }

    // Even last digit: turn the arrow 90 degrees clockwise. Odd: the opposite way.
    private int realArrow(int dir) {
        int turn = bomb.lastDigit() % 2 == 0 ? 2 : 4;
        return (dir + turn) % 8;
    }

    private void press(int dir) {
        if (isSolved()) return;
        if (dir == realArrow(shown[step])) {
            step++;
            if (step == shown.length) solve();
        } else {
            strike();
            step = 0;
        }
        progress.setText(step + "/4");
    }

    // Draws an arrow pointing in one of the 8 directions
    static class ArrowIcon implements Icon {
        private final int dir, size;
        private final Color colour;

        ArrowIcon(int dir, int size, Color colour) {
            this.dir = dir;
            this.size = size;
            this.colour = colour;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x + size / 2.0, y + size / 2.0);
            g2.rotate(Math.toRadians(dir * 45));
            g2.setColor(c.isEnabled() ? colour : Theme.MUTED);
            int s = size / 2, t = size / 6;
            // arrow pointing up: head on top, shaft below
            g2.fillPolygon(new int[]{0, s, t, t, -t, -t, -s}, new int[]{-s, 0, 0, s, s, 0, 0}, 7);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
