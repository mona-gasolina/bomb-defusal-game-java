package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.ui.Theme;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.RenderingHints;

// 4 coloured shapes flash one by one. Start at 0 and do the maths from the manual.
public class ShapesPuzzle extends Puzzle {

    private static final int STAR = 0, CIRCLE = 1, TRIANGLE = 2;
    private static final Color[] COLOURS = {
            new Color(0xC8392B), new Color(0x2A6FC4), new Color(0xE0B81A), new Color(0x3A9A52)};
    private static final int[] VALUES = {2, 3, 5, 4}; // red, blue, yellow, green
    private static final String[] KEYS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "C", "OK"};

    private final int[] shape = new int[4];
    private final int[] colour = new int[4];
    private final int answer;
    private final JLabel display = Theme.label("0", Theme.mono(22), Theme.LED);
    private final ShapeScreen screen = new ShapeScreen();
    private final Timer flasher;
    private String typed = "";
    private int showing = -1; // which shape is on screen, -1 means none
    private int tick;

    public ShapesPuzzle(Bomb bomb) {
        super("SHAPES", bomb);
        for (int i = 0; i < 4; i++) {
            shape[i] = random.nextInt(3);
            colour[i] = random.nextInt(4);
        }
        answer = work();

        display.setOpaque(true);
        display.setBackground(Theme.SCREEN);
        display.setBorder(new LineBorder(Theme.BORDER, 2));
        display.setPreferredSize(new Dimension(150, 34));

        JPanel keypad = new JPanel(new GridLayout(3, 5, 4, 4));
        keypad.setOpaque(false);
        for (String key : KEYS) {
            JButton b = Theme.button(key);
            b.setFont(Theme.mono(14));
            b.setBorder(new LineBorder(Theme.KHAKI, 1));
            b.setPreferredSize(new Dimension(36, 28));
            b.addActionListener(e -> key(key));
            keypad.add(b);
        }

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        right.add(display, BorderLayout.NORTH);
        right.add(keypad, BorderLayout.CENTER);

        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.add(screen, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        addRow(row, 0);

        // each shape shows for 0.7 s, then a short gap, then a longer pause before repeating
        flasher = new Timer(700, e -> nextFlash());
        flasher.start();
    }

    // star: add, circle: subtract, triangle: multiply
    private int work() {
        int n = 0;
        for (int i = 0; i < 4; i++) {
            int v = VALUES[colour[i]];
            if (shape[i] == STAR) n += v;
            else if (shape[i] == CIRCLE) n -= v;
            else n *= v;
        }
        return n;
    }

    private void nextFlash() {
        tick = (tick + 1) % 10;
        showing = (tick < 8 && tick % 2 == 0) ? tick / 2 : -1;
        screen.repaint();
    }

    private void key(String key) {
        if (isSolved()) return;
        switch (key) {
            case "C" -> typed = "";
            case "-" -> {
                if (typed.isEmpty()) typed = "-";
            }
            case "OK" -> {
                if (typed.equals(String.valueOf(answer))) {
                    solve();
                } else {
                    strike();
                }
                typed = "";
            }
            default -> {
                if (typed.length() < 5) typed += key;
            }
        }
        display.setText(typed.isEmpty() ? "0" : typed);
    }

    @Override
    public void stop() {
        flasher.stop();
        showing = -1;
        screen.repaint();
    }

    // Small black screen where the shapes appear
    private class ShapeScreen extends JComponent {

        ShapeScreen() {
            setPreferredSize(new Dimension(104, 110));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(Theme.SCREEN);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

            if (showing >= 0) {
                g2.setColor(COLOURS[colour[showing]]);
                int cx = w / 2, cy = h / 2 - 8, r = 32;
                if (shape[showing] == STAR) g2.fillPolygon(star(cx, cy, r));
                else if (shape[showing] == CIRCLE) g2.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                else g2.fillPolygon(new int[]{cx, cx + r, cx - r}, new int[]{cy - r, cy + r - 4, cy + r - 4}, 3);
            }
            // 4 dots under the shape show which one of the 4 is on screen
            for (int i = 0; i < 4; i++) {
                g2.setColor(i == showing ? Theme.KHAKI : Theme.BORDER);
                g2.fillOval(w / 2 - 30 + i * 16, h - 16, 9, 9);
            }
            g2.dispose();
        }

        private Polygon star(int cx, int cy, int r) {
            Polygon p = new Polygon();
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double len = i % 2 == 0 ? r : r * 0.42;
                p.addPoint(cx + (int) (len * Math.cos(angle)), cy + (int) (len * Math.sin(angle)));
            }
            return p;
        }
    }
}
