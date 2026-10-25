package com.bomb.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

// Colours and fonts for the military look, in one place
public final class Theme {

    public static final Color BG = new Color(0x171B13);
    public static final Color CASE = new Color(0x2C3524);
    public static final Color PANEL = new Color(0x364229);
    public static final Color PANEL_DARK = new Color(0x2A3320);
    public static final Color BORDER = new Color(0x5D7444);
    public static final Color METAL = new Color(0x6F6F5E);
    public static final Color KHAKI = new Color(0xD4C067);
    public static final Color TEXT = new Color(0xEBE6CC);
    public static final Color MUTED = new Color(0x9AA285);
    public static final Color RED = new Color(0xB83A2E);
    public static final Color GREEN = new Color(0x5FB04A);
    public static final Color LED = new Color(0xE0402F);
    public static final Color SCREEN = new Color(0x0B0B0B);

    private static final List<String> FONTS =
            Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

    private Theme() {
    }

    // Colours for the standard Swing parts (dialogs, toggles, pressed buttons)
    public static void init() {
        UIManager.put("Panel.background", CASE);
        UIManager.put("OptionPane.background", CASE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.select", KHAKI);
        UIManager.put("ToggleButton.select", KHAKI);
    }

    // Stencil font if the laptop has it, otherwise the closest thing
    public static Font stencil(int size) {
        return pick(size, "Stencil", "Impact");
    }

    public static Font mono(int size) {
        return pick(size, "Consolas", "Monospaced");
    }

    // Dialog is Java's built-in font. It can show the Greek and Cyrillic symbols.
    public static Font plain(int size) {
        return new Font(Font.DIALOG, Font.BOLD, size);
    }

    private static Font pick(int size, String first, String second) {
        if (FONTS.contains(first)) return new Font(first, Font.PLAIN, size);
        if (FONTS.contains(second)) return new Font(second, Font.PLAIN, size);
        return plain(size);
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(stencil(16));
        b.setForeground(TEXT);
        b.setBackground(PANEL_DARK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(new LineBorder(KHAKI, 2), new EmptyBorder(4, 12, 4, 12)));
        return b;
    }

    public static JLabel label(String text, Font font, Color colour) {
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(font);
        l.setForeground(colour);
        return l;
    }
}
