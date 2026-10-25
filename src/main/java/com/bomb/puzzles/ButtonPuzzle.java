package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.ui.Theme;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// One big button. Depending on its colour, its word and the bomb details
// it must be PRESSED once, TAPPED a few times or HELD and released at the right moment.
public class ButtonPuzzle extends Puzzle {

    private static final int RED = 0, BLUE = 1, YELLOW = 2, WHITE = 3;
    private static final Color[] COLOURS = {
            new Color(0xB83A2E), new Color(0x2A6FC4), new Color(0xE0B81A), new Color(0xE8E6E0)};
    private static final String[] WORDS = {"DETONATE", "HOLD", "ABORT", "PRESS"};
    private static final int HOLD_TIME = 800; // ms the button must be down to count as a hold

    private enum Action { PRESS, TAP, HOLD }

    private final int colour = random.nextInt(COLOURS.length);
    private final String word = WORDS[random.nextInt(WORDS.length)];
    private Action action;
    private int tapsNeeded;

    private long pressedAt;
    private boolean holding;
    private int taps;
    private final Timer tapTimer;

    public ButtonPuzzle(Bomb bomb) {
        super("BUTTON", bomb);
        decide();
        // after the last quick click, wait a moment and then count the taps
        tapTimer = new Timer(900, e -> checkTaps());
        tapTimer.setRepeats(false);
        addRow(new BigButton(), 0);
    }

    // The rules from the manual, checked in order
    private void decide() {
        if (colour == BLUE && word.equals("ABORT")) {
            action = Action.HOLD;
        } else if (bomb.getBatteries() > 1 && word.equals("DETONATE")) {
            action = Action.PRESS;
        } else if (colour == WHITE && bomb.hasVowel()) {
            action = Action.TAP;
            tapsNeeded = 3;
        } else if (colour == YELLOW) {
            action = Action.HOLD;
        } else if (bomb.getBatteries() > 2) {
            action = Action.TAP;
            tapsNeeded = 2;
        } else {
            action = Action.HOLD;
        }
    }

    private void released(long heldFor) {
        if (heldFor >= HOLD_TIME) {
            tapTimer.stop();
            taps = 0;
            // a hold is right only if the timer's last digit is 4 when you let go
            if (action == Action.HOLD && bomb.secondsLeft() % 10 == 4) solve();
            else strike();
        } else {
            taps++;
            tapTimer.restart();
        }
    }

    private void checkTaps() {
        int n = taps;
        taps = 0;
        boolean right = (action == Action.PRESS && n == 1) || (action == Action.TAP && n == tapsNeeded);
        if (right) solve();
        else strike();
    }

    @Override
    public void stop() {
        tapTimer.stop();
    }

    // The round button, drawn by hand so it can look like a real one
    private class BigButton extends JComponent {

        BigButton() {
            setPreferredSize(new Dimension(150, 150));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isSolved()) return;
                    pressedAt = System.currentTimeMillis();
                    holding = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isSolved() || !holding) return;
                    holding = false;
                    repaint();
                    released(System.currentTimeMillis() - pressedAt);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;

            // metal ring (turns khaki while you hold the button)
            g2.setColor(Theme.PANEL_DARK);
            g2.fillOval(x, y, size, size);
            g2.setColor(holding ? Theme.KHAKI : Theme.BORDER);
            g2.setStroke(new BasicStroke(4));
            g2.drawOval(x + 2, y + 2, size - 4, size - 4);

            // the button itself
            int gap = 16;
            Color c = COLOURS[colour];
            g2.setColor(holding ? c.darker() : c);
            g2.fillOval(x + gap, y + gap, size - 2 * gap, size - 2 * gap);

            // the word, centred
            g2.setFont(Theme.stencil(18));
            g2.setColor(colour == YELLOW || colour == WHITE ? new Color(0x222222) : Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(word, (getWidth() - fm.stringWidth(word)) / 2, getHeight() / 2 + fm.getAscent() / 2 - 2);
            g2.dispose();
        }
    }
}
