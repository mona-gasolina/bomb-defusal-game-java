package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.game.GameFiles;
import com.bomb.ui.Theme;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

// Colours flash in a sequence. For each one, press the colour the manual's table says.
// The sequence grows by one colour every round. 3 rounds to solve.
public class ColoursPuzzle extends Puzzle {

    private static final String[] NAMES = {"RED", "BLUE", "GREEN", "YELLOW"};
    private static final Color[] COLOURS = {
            new Color(0xC8392B), new Color(0x2A6FC4), new Color(0x3A9A52), new Color(0xE0B81A)};
    private static final int ROUNDS = 3;

    private final Map<String, Map<String, String>> table = GameFiles.colourTable();
    private final int[] sequence = new int[ROUNDS];
    private final Pad[] pads = new Pad[4];
    private final JLabel roundLabel = Theme.label("ROUND 1/3", Theme.mono(14), Theme.MUTED);
    private final Timer flasher;
    private int round = 1, step;
    private int lit = -1, tick;

    public ColoursPuzzle(Bomb bomb) {
        super("COLOURS", bomb);
        for (int i = 0; i < ROUNDS; i++) sequence[i] = random.nextInt(4);

        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8));
        grid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            pads[i] = new Pad(i);
            grid.add(pads[i]);
        }
        addRow(grid, 0);
        addRow(roundLabel, 1);

        flasher = new Timer(500, e -> flash());
        flasher.start();
    }

    // Shows the first `round` colours of the sequence, then pauses and repeats
    private void flash() {
        int cycle = round * 2 + 4;
        tick = (tick + 1) % cycle;
        lit = (tick < round * 2 && tick % 2 == 0) ? sequence[tick / 2] : -1;
        for (Pad p : pads) p.repaint();
    }

    private void press(int pressed) {
        if (isSolved()) return;
        String key = (bomb.hasVowel() ? "YES " : "NO ") + bomb.getStrikes();
        String expected = table.get(key).get(NAMES[sequence[step]]);
        if (NAMES[pressed].equals(expected)) {
            step++;
            if (step == round) {
                round++;
                step = 0;
                tick = -1;
                if (round > ROUNDS) solve();
                else roundLabel.setText("ROUND " + round + "/3");
            }
        } else {
            strike();
            step = 0;
            tick = -1;
        }
    }

    @Override
    public void stop() {
        flasher.stop();
        lit = -1;
        for (Pad p : pads) p.repaint();
    }

    // One coloured square. Bright when it flashes, dark otherwise.
    private class Pad extends JComponent {
        private final int index;
        private boolean down;

        Pad(int index) {
            this.index = index;
            setPreferredSize(new Dimension(70, 52));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    down = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    down = false;
                    repaint();
                    press(index);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = COLOURS[index];
            boolean bright = lit == index || down;
            g2.setColor(bright ? c.brighter() : dim(c));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(bright ? Color.WHITE : Theme.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }

        private Color dim(Color c) {
            return new Color(c.getRed() * 2 / 5, c.getGreen() * 2 / 5, c.getBlue() * 2 / 5);
        }
    }
}
