package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.ui.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Random;

// Every puzzle on the bomb extends this class.
// It draws the metal panel, the title and the status light,
// and gives each puzzle solve() and strike().
public abstract class Puzzle extends JPanel {

    protected static final Random random = new Random();

    protected final Bomb bomb;
    // puzzles put their parts in here; GridBagLayout keeps them centred
    protected final JPanel body = new JPanel(new GridBagLayout());

    private boolean solved;
    private boolean flashRed;

    protected Puzzle(String title, Bomb bomb) {
        this.bomb = bomb;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(10, 16, 12, 16));
        JLabel name = new JLabel(title);
        name.setFont(Theme.stencil(20));
        name.setForeground(Theme.KHAKI);
        add(name, BorderLayout.NORTH);
        body.setOpaque(false);
        add(body, BorderLayout.CENTER);
    }

    // Adds a part to the body on the given row, centred
    protected void addRow(Component part, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 0, 4, 0);
        body.add(part, c);
    }

    public boolean isSolved() {
        return solved;
    }

    protected void solve() {
        if (solved) return;
        solved = true;
        disableAll(body);
        bomb.solved();
        stop();
        repaint();
    }

    protected void strike() {
        if (solved) return;
        bomb.strike();
        Toolkit.getDefaultToolkit().beep();
        flashRed = true;
        repaint();
        Timer t = new Timer(500, e -> {
            flashRed = false;
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    // Puzzles with animations override this to stop their timers
    public void stop() {
    }

    private static void disableAll(Container box) {
        for (Component part : box.getComponents()) {
            part.setEnabled(false);
            if (part instanceof Container inner) disableAll(inner);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(Theme.PANEL);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g2.setColor(flashRed ? Theme.RED : Theme.BORDER);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);

        // rivets in the corners
        g2.setColor(Theme.BORDER);
        int[][] rivets = {{8, 8}, {w - 9, 8}, {8, h - 9}, {w - 9, h - 9}};
        for (int[] p : rivets) g2.fillOval(p[0] - 3, p[1] - 3, 6, 6);

        // status light: green when solved
        g2.setColor(solved ? Theme.GREEN : new Color(0x3A3A3A));
        g2.fillOval(w - 36, 14, 18, 18);
        g2.setColor(Theme.PANEL_DARK);
        g2.drawOval(w - 36, 14, 18, 18);
        g2.dispose();
    }
}
