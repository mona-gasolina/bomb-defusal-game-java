package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.game.GameFiles;
import com.bomb.ui.Theme;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// 3 lights and 5 switches. switches.txt says how the switches must be set for each light pattern.
public class SwitchesPuzzle extends Puzzle {

    private final boolean[] lights = new boolean[3];
    private final JToggleButton[] switches = new JToggleButton[5];
    private final String target; // for example "UP DOWN UP UP DOWN"

    public SwitchesPuzzle(Bomb bomb) {
        super("SWITCHES", bomb);
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < lights.length; i++) {
            lights[i] = random.nextBoolean();
            pattern.append(lights[i] ? "ON " : "OFF ");
        }
        target = GameFiles.switchTable().get(pattern.toString().trim());

        JPanel row = new JPanel(new GridLayout(1, 5, 8, 0));
        row.setOpaque(false);
        ItemListener flip = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JToggleButton s = (JToggleButton) e.getSource();
                s.setText(s.isSelected() ? "UP" : "DOWN");
                s.setForeground(s.isSelected() ? Theme.SCREEN : Theme.TEXT);
            }
        };
        do {
            for (int i = 0; i < switches.length; i++) {
                if (switches[i] == null) {
                    switches[i] = new JToggleButton("DOWN");
                    switches[i].setFont(Theme.stencil(14));
                    switches[i].setForeground(Theme.TEXT);
                    switches[i].setBackground(Theme.PANEL_DARK);
                    switches[i].setFocusPainted(false);
                    switches[i].setBorder(new LineBorder(Theme.KHAKI, 2));
                    switches[i].setPreferredSize(new Dimension(58, 40));
                    switches[i].addItemListener(flip);
                    row.add(switches[i]);
                }
                switches[i].setSelected(random.nextBoolean());
            }
        } while (current().equals(target)); // never start already solved

        JButton set = Theme.button("SET");
        set.addActionListener(e -> check());

        addRow(new Lights(), 0);
        addRow(row, 1);
        addRow(set, 2);
    }

    private String current() {
        StringBuilder sb = new StringBuilder();
        for (JToggleButton s : switches) sb.append(s.isSelected() ? "UP " : "DOWN ");
        return sb.toString().trim();
    }

    private void check() {
        if (locked()) return;
        if (current().equals(target)) solve();
        else strike();
    }

    // The 3 lamps, numbered 1 to 3
    private class Lights extends JComponent {

        Lights() {
            setPreferredSize(new Dimension(200, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(Theme.mono(13));
            for (int i = 0; i < lights.length; i++) {
                int x = getWidth() / 2 - 80 + i * 60;
                g2.setColor(lights[i] ? Theme.LED : Theme.SCREEN);
                g2.fillOval(x, 4, 26, 26);
                g2.setColor(Theme.BORDER);
                g2.drawOval(x, 4, 26, 26);
                g2.setColor(Theme.MUTED);
                g2.drawString(String.valueOf(i + 1), x + 32, 22);
            }
            g2.dispose();
        }
    }
}
