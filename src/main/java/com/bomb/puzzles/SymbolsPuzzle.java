package com.bomb.puzzles;

import com.bomb.game.Bomb;
import com.bomb.game.GameFiles;
import com.bomb.ui.Theme;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

// 5 symbols that hide a 5-letter word. The analyst decodes it with the symbol table.
public class SymbolsPuzzle extends Puzzle {

    private final String answer;
    private final JTextField input = new JTextField(6);

    public SymbolsPuzzle(Bomb bomb) {
        super("SYMBOLS", bomb);
        List<String> words = GameFiles.words();
        String word = words.get(random.nextInt(words.size()));
        // more than 2 batteries: the word is typed backwards
        answer = bomb.getBatteries() > 2 ? new StringBuilder(word).reverse().toString() : word;

        Map<Character, String> symbols = GameFiles.symbols();
        JPanel tiles = new JPanel(new GridLayout(1, 5, 8, 0));
        tiles.setOpaque(false);
        for (char letter : word.toCharArray()) {
            JLabel tile = Theme.label(symbols.get(letter), Theme.plain(30), Theme.TEXT);
            tile.setOpaque(true);
            tile.setBackground(Theme.PANEL_DARK);
            tile.setBorder(new LineBorder(Theme.BORDER, 2));
            tile.setPreferredSize(new Dimension(52, 56));
            tiles.add(tile);
        }

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

        addRow(tiles, 0);
        addRow(typeRow, 1);
    }

    private void check() {
        if (locked()) return;
        if (input.getText().trim().equalsIgnoreCase(answer)) {
            solve();
        } else {
            strike();
            input.setText("");
        }
    }
}
