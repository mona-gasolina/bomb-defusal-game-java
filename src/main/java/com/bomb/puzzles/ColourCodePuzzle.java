package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.game.GameFiles;
import com.bomb.ui.Theme;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;

// 4 coloured lights. Each colour stands for a digit (colours.txt); type the 4-digit code.
public class ColourCodePuzzle extends Puzzle {

    private static final String[] NAMES = {"RED", "BLUE", "GREEN", "YELLOW"};
    private static final Color[] COLOURS = {
            new Color(0xC8392B), new Color(0x2A6FC4), new Color(0x3A9A52), new Color(0xE0B81A)};

    private final int[] lights = new int[4];
    private final String answer;
    private final JTextField input = new JTextField(5);

    public ColourCodePuzzle(Bomb bomb) {
        super("COLOUR CODE", bomb);
        Map<String, String> digits = GameFiles.colourDigits();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < lights.length; i++) {
            lights[i] = random.nextInt(NAMES.length);
            code.append(digits.get(NAMES[lights[i]]));
        }
        // a vowel in the serial number: the code is typed backwards
        answer = bomb.hasVowel() ? code.reverse().toString() : code.toString();

        input.setFont(Theme.mono(22));
        input.setHorizontalAlignment(JTextField.CENTER);
        input.setBackground(Theme.SCREEN);
        input.setForeground(Theme.LED);
        input.setCaretColor(Theme.LED);
        input.setBorder(new LineBorder(Theme.BORDER, 2));
        JButton enter = Theme.button("ENTER");
        enter.addActionListener(e -> check());
        input.addActionListener(e -> check()); // the Enter key works too

        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        typeRow.setOpaque(false);
        typeRow.add(input);
        typeRow.add(enter);

        addRow(new Lights(), 0);
        addRow(typeRow, 1);
    }

    private void check() {
        if (locked()) return;
        if (input.getText().trim().equals(answer)) {
            solve();
        } else {
            strike();
            input.setText("");
        }
    }

    // The 4 lights, numbered 1 to 4 from the left
    private class Lights extends JComponent {

        Lights() {
            Dimension size = new Dimension(260, 70);
            setPreferredSize(size);
            setMinimumSize(size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(Theme.mono(12));
            int x0 = getWidth() / 2 - 2 * 60 + 8;
            for (int i = 0; i < lights.length; i++) {
                int x = x0 + i * 60;
                g2.setColor(Theme.PANEL_DARK);
                g2.fillOval(x - 4, 0, 52, 52);
                g2.setColor(COLOURS[lights[i]]);
                g2.fillOval(x + 2, 6, 40, 40);
                g2.setColor(Theme.BORDER);
                g2.drawOval(x - 4, 0, 52, 52);
                g2.setColor(Theme.MUTED);
                g2.drawString(String.valueOf(i + 1), x + 19, 66);
            }
            g2.dispose();
        }
    }
}
